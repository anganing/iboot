package com.iboot.pf.module.aps.service.task;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskAdjustReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskGanttRespVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.ApsTaskLockDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.dal.mysql.taskdep.ApsTaskDepMapper;
import com.iboot.pf.module.aps.dal.mysql.tasklock.ApsTaskLockMapper;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsTaskDepTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockStatusEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.snapshot.ApsSnapshotService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_CANNOT_UNLOCK;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_RESOURCE_OVERLAP;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_TIME_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 排程任务 Service 测试。
 *
 * <p>覆盖三条不变量：时间区间必须为正、同一资源不允许时间重叠、已开工任务不允许解锁；
 * 另外验证锁定与解锁同时维护任务标记与锁定留痕记录。
 */
@Import({ApsTaskServiceImpl.class, ApsCommandServiceImpl.class})
class ApsTaskServiceImplTest extends BaseDbUnitTest {

    private static final Long VERSION_ID = 100L;
    private static final Long RES_ID = 200L;

    @Resource
    private ApsTaskService taskService;
    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsTaskDepMapper taskDepMapper;
    @Resource
    private ApsTaskLockMapper taskLockMapper;

    @MockitoBean
    private ApsVersionService versionService;
    @MockitoBean
    private ApsSnapshotService snapshotService;
    @MockitoBean
    private ApsRelationFiller relationFiller;

    @BeforeEach
    void mockExternalDependencies() {
        ResourceRespDTO resource = new ResourceRespDTO();
        resource.setId(RES_ID);
        resource.setCode("RES-A");
        resource.setName("加工中心A");
        when(snapshotService.requireResource(anyLong())).thenReturn(resource);
    }

    private Long insertTask(String taskNo, Long resId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        ApsTaskDO task = ApsTaskDO.builder()
                .versionId(VERSION_ID).taskNo(taskNo)
                .prodOrderId(1L).prodOrderNo("MO001").workOrderId(0L).opTaskId(1L).routeOpId(1L)
                .opCode("OP010").opName("粗车").plantId(1L).workshopId(1L).projectId(0L).projectTaskId(0L)
                .resId(resId).resCode("RES-A").startTime(startTime).endTime(endTime)
                .durationMinutes(60).sequenceNo(1).lockedFlag(false).status(status)
                .build();
        taskMapper.insert(task);
        return task.getId();
    }

    private TaskAdjustReqVO buildAdjustReqVO(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        TaskAdjustReqVO reqVO = new TaskAdjustReqVO();
        reqVO.setId(id);
        reqVO.setResId(RES_ID);
        reqVO.setStartTime(startTime);
        reqVO.setEndTime(endTime);
        reqVO.setSequenceNo(1);
        reqVO.setRemark("人工微调");
        return reqVO;
    }

    private LockTaskReqDTO buildLockCommand(String idempotencyKey, Long taskId) {
        LockTaskReqDTO reqDTO = new LockTaskReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(taskId));
        reqDTO.setTaskId(taskId);
        reqDTO.setLockType(ApsTaskLockTypeEnum.MANUAL.name());
        reqDTO.setReason("等待外协件");
        reqDTO.setOperatorId(1L);
        return reqDTO;
    }

    @Test
    void testAdjustTask_recomputeDurationFromTimeRange() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());

        taskService.adjustTask(buildAdjustReqVO(id, LocalDateTime.of(2026, 8, 1, 13, 0),
                LocalDateTime.of(2026, 8, 1, 15, 30)));

        ApsTaskDO entity = taskMapper.selectById(id);
        // 占用分钟数由起止时间推导，不接受前端提交
        assertEquals(150, entity.getDurationMinutes());
        assertEquals("RES-A", entity.getResCode());
    }

    @Test
    void testAdjustTask_invalidTimeRangeRejected() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());

        assertServiceException(() -> taskService.adjustTask(buildAdjustReqVO(id,
                LocalDateTime.of(2026, 8, 1, 10, 0), LocalDateTime.of(2026, 8, 1, 10, 0))), TASK_TIME_INVALID);
    }

    @Test
    void testAdjustTask_overlapOnSameResourceRejected() {
        insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0), ApsTaskStatusEnum.PLANNED.name());
        Long secondId = insertTask("T002", RES_ID, LocalDateTime.of(2026, 8, 1, 13, 0),
                LocalDateTime.of(2026, 8, 1, 14, 0), ApsTaskStatusEnum.PLANNED.name());

        // 挪到 11:00-13:00 会和第一条 8:00-12:00 重叠
        assertServiceException(() -> taskService.adjustTask(buildAdjustReqVO(secondId,
                LocalDateTime.of(2026, 8, 1, 11, 0), LocalDateTime.of(2026, 8, 1, 13, 0))),
                TASK_RESOURCE_OVERLAP);
    }

    @Test
    void testAdjustTask_headToTailNotOverlap() {
        insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0), ApsTaskStatusEnum.PLANNED.name());
        Long secondId = insertTask("T002", RES_ID, LocalDateTime.of(2026, 8, 1, 15, 0),
                LocalDateTime.of(2026, 8, 1, 16, 0), ApsTaskStatusEnum.PLANNED.name());

        // 首尾相接不算冲突：前一道 10:00 结束、后一道 10:00 开始
        taskService.adjustTask(buildAdjustReqVO(secondId, LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 11, 0)));

        assertEquals(60, taskMapper.selectById(secondId).getDurationMinutes());
    }

    @Test
    void testAdjustTask_lockedRejected() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.LOCKED.name());
        taskMapper.updateById(ApsTaskDO.builder().id(id).lockedFlag(true).build());

        assertServiceException(() -> taskService.adjustTask(buildAdjustReqVO(id,
                LocalDateTime.of(2026, 8, 1, 13, 0), LocalDateTime.of(2026, 8, 1, 14, 0))), TASK_CANNOT_UNLOCK);
    }

    @Test
    void testLockTask_writesFlagAndLockRecord() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());

        taskService.lockTask(buildLockCommand("UI:LOCK:1", id));

        ApsTaskDO entity = taskMapper.selectById(id);
        assertTrue(entity.getLockedFlag());
        assertEquals(ApsTaskStatusEnum.LOCKED.name(), entity.getStatus());
        ApsTaskLockDO lock = taskLockMapper.selectLockedByTaskId(id);
        assertNotNull(lock);
        assertEquals(ApsTaskLockTypeEnum.MANUAL.name(), lock.getLockType());
    }

    @Test
    void testUnlockTask_keepsLockHistory() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());
        taskService.lockTask(buildLockCommand("UI:LOCK:1", id));

        taskService.unlockTask(buildLockCommand("UI:UNLOCK:1", id));

        ApsTaskDO entity = taskMapper.selectById(id);
        assertFalse(entity.getLockedFlag());
        assertEquals(ApsTaskStatusEnum.PLANNED.name(), entity.getStatus());
        // 锁定记录不物理删除，改成已解锁保留历史
        assertEquals(1L, taskLockMapper.selectCountByTaskId(id));
        List<ApsTaskLockDO> locks = taskLockMapper.selectList(ApsTaskLockDO::getTaskId, id);
        assertEquals(ApsTaskLockStatusEnum.UNLOCKED.name(), locks.get(0).getStatus());
        assertNotNull(locks.get(0).getUnlockTime());
        // 已解锁后不该再被当成锁定中的记录
        assertNull(taskLockMapper.selectLockedByTaskId(id));
    }

    @Test
    void testUnlockTask_startedRejected() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.STARTED.name());

        // 已开工的任务解锁等于允许重排把车间正在做的工序挪走
        assertServiceException(() -> taskService.unlockTask(buildLockCommand("UI:UNLOCK:1", id)),
                TASK_CANNOT_UNLOCK);
    }

    @Test
    void testValidateTaskExists_notExists() {
        assertServiceException(() -> taskService.validateTaskExists(randomLongId()), TASK_NOT_EXISTS);
    }

    @Test
    void testGetTaskGantt_groupsByResourceAndKeepsInVersionLinks() {
        mockVersion();
        Long first = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0), ApsTaskStatusEnum.PLANNED.name());
        Long second = insertTask("T002", RES_ID, LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 11, 0), ApsTaskStatusEnum.PLANNED.name());
        Long other = insertTask("T003", RES_ID + 1, LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0), ApsTaskStatusEnum.PLANNED.name());
        insertDep(first, second);
        // 悬空边：后置任务不属于本版本的任务集合，必须被丢掉，否则前端会画出错位的连线
        insertDep(first, randomLongId());

        TaskGanttRespVO respVO = taskService.getTaskGantt(VERSION_ID);

        assertEquals(3, respVO.getTaskCount());
        assertEquals(2, respVO.getResources().size());
        assertEquals(LocalDateTime.of(2026, 8, 1, 8, 0), respVO.getMinTime());
        assertEquals(LocalDateTime.of(2026, 8, 1, 12, 0), respVO.getMaxTime());
        assertEquals(1, respVO.getLinks().size());
        assertEquals(first, respVO.getLinks().get(0).getFromTaskId());
        assertEquals(second, respVO.getLinks().get(0).getToTaskId());
        // Mapper 按资源、开始时间排序，第一个资源上应有两条任务且负荷为两段之和
        TaskGanttRespVO.Resource main = respVO.getResources().stream()
                .filter(row -> RES_ID.equals(row.getResId())).findFirst().orElseThrow();
        assertEquals(2, main.getTaskCount());
        assertEquals(120, main.getLoadMinutes());
        assertEquals("T001", main.getTasks().get(0).getTaskNo());
        assertEquals(other, respVO.getResources().stream()
                .filter(row -> !RES_ID.equals(row.getResId())).findFirst().orElseThrow()
                .getTasks().get(0).getId());
    }

    @Test
    void testGetTaskGantt_emptyVersionReturnsNoResource() {
        mockVersion();

        TaskGanttRespVO respVO = taskService.getTaskGantt(VERSION_ID);

        assertEquals(0, respVO.getTaskCount());
        assertTrue(respVO.getResources().isEmpty());
        assertTrue(respVO.getLinks().isEmpty());
        assertNull(respVO.getMinTime());
    }

    private void mockVersion() {
        VersionRespVO version = new VersionRespVO();
        version.setId(VERSION_ID);
        version.setVersionNo(1);
        version.setScenarioCode("SC001");
        version.setScenarioName("松江工厂 8 月排程");
        version.setStatus(ApsVersionStatusEnum.SOLVED.name());
        when(versionService.getVersion(VERSION_ID)).thenReturn(version);
    }

    private void insertDep(Long fromTaskId, Long toTaskId) {
        taskDepMapper.insert(ApsTaskDepDO.builder()
                .versionId(VERSION_ID)
                .fromTaskId(fromTaskId)
                .toTaskId(toTaskId)
                .depType(ApsTaskDepTypeEnum.FINISH_START.name())
                .lagMinutes(0)
                .build());
    }

}
