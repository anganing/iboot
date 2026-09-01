package com.iboot.pf.module.aps.service.release;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 排程结果下达测试。
 *
 * <p>覆盖三件事：只回写计划口径的三个字段、没有 MES 工序对应的任务被剔除、已排定任务推进到已下达。
 */
@Import(ApsScheduleReleaseService.class)
class ApsScheduleReleaseServiceTest extends BaseDbUnitTest {

    private static final Long VERSION_ID = 100L;

    @Resource
    private ApsScheduleReleaseService releaseService;
    @Resource
    private ApsTaskMapper taskMapper;

    @MockitoBean
    private OpTaskApi opTaskApi;

    private Long insertTask(String taskNo, Long opTaskId, Long resId, String status) {
        ApsTaskDO task = ApsTaskDO.builder()
                .versionId(VERSION_ID).taskNo(taskNo)
                .prodOrderId(1L).prodOrderNo("MO001").workOrderId(0L).opTaskId(opTaskId).routeOpId(1L)
                .opCode("OP010").opName("粗车").plantId(1L).workshopId(1L).projectId(0L).projectTaskId(0L)
                .resId(resId).resCode("RES-A")
                .startTime(LocalDateTime.of(2026, 9, 10, 8, 0))
                .endTime(LocalDateTime.of(2026, 9, 10, 16, 0))
                .durationMinutes(480).sequenceNo(1).lockedFlag(false).status(status)
                .build();
        taskMapper.insert(task);
        return task.getId();
    }

    @Test
    void testRelease_pushesPlanAndMarksReleased() {
        Long planned = insertTask("T001", 501L, 901L, ApsTaskStatusEnum.PLANNED.name());
        Long locked = insertTask("T002", 502L, 902L, ApsTaskStatusEnum.LOCKED.name());
        when(opTaskApi.applySchedule(any())).thenReturn(2);

        assertEquals(2, releaseService.release(VERSION_ID));

        ArgumentCaptor<ApplyScheduleReqDTO> captor = ArgumentCaptor.forClass(ApplyScheduleReqDTO.class);
        verify(opTaskApi).applySchedule(captor.capture());
        ApplyScheduleReqDTO command = captor.getValue();
        assertEquals(2, command.getItems().size());
        // 幂等键固定，重复发布是同一次下达
        assertEquals("APS:RELEASE_SCHEDULE:" + VERSION_ID, command.getIdempotencyKey());
        ApplyScheduleReqDTO.Item item = command.getItems().stream()
                .filter(row -> row.getOpTaskId().equals(501L)).findFirst().orElseThrow();
        assertEquals(901L, item.getResId());
        assertEquals(LocalDateTime.of(2026, 9, 10, 8, 0), item.getPlannedStartTime());
        // 已排定推进到已下达，已锁定保持原状态
        assertEquals(ApsTaskStatusEnum.RELEASED.name(), taskMapper.selectById(planned).getStatus());
        assertEquals(ApsTaskStatusEnum.LOCKED.name(), taskMapper.selectById(locked).getStatus());
    }

    @Test
    void testRelease_skipsTaskWithoutOpTask() {
        // opTaskId 为占位 0 的任务在 MES 没有对应工序，回写过去落不到任何一行
        insertTask("T001", ApsConstants.NONE_ID, 901L, ApsTaskStatusEnum.PLANNED.name());

        assertEquals(0, releaseService.release(VERSION_ID));

        verify(opTaskApi, never()).applySchedule(any());
    }

    @Test
    void testRelease_emptyVersionDoesNotCallMes() {
        assertEquals(0, releaseService.release(VERSION_ID));

        verify(opTaskApi, never()).applySchedule(any());
    }

    @Test
    void testRelease_partialAffectedStillMarksReleased() {
        Long planned = insertTask("T001", 501L, 901L, ApsTaskStatusEnum.PLANNED.name());
        // MES 侧只有一道生效，另一道已开工被跳过，属于正常业务结果
        insertTask("T002", 502L, 902L, ApsTaskStatusEnum.PLANNED.name());
        when(opTaskApi.applySchedule(any())).thenReturn(1);

        assertEquals(1, releaseService.release(VERSION_ID));

        List<ApsTaskDO> tasks = taskMapper.selectListByVersionId(VERSION_ID);
        assertTrue(tasks.stream()
                .allMatch(task -> ApsTaskStatusEnum.RELEASED.name().equals(task.getStatus())));
        assertEquals(ApsTaskStatusEnum.RELEASED.name(), taskMapper.selectById(planned).getStatus());
    }

}
