package com.iboot.pf.module.aps.service.load;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.load.ApsLoadMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsResourceProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.LOAD_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 资源负荷 Service 测试。
 *
 * <p>覆盖“按资源 + 日期汇总任务占用、对比日历可用分钟得出负荷率与超负荷”，
 * 以及可用分钟为 0（当天没排班）时负荷率记 0、重复刷新不产生重复行。
 */
@Import({ApsLoadServiceImpl.class, ApsCommandServiceImpl.class})
class ApsLoadServiceImplTest extends BaseDbUnitTest {

    private static final Long VERSION_ID = 100L;
    private static final Long SCENARIO_ID = 10L;
    private static final Long RES_ID = 200L;
    private static final LocalDate WORK_DATE = LocalDate.of(2026, 8, 1);

    @Resource
    private ApsLoadService loadService;
    @Resource
    private ApsLoadMapper loadMapper;
    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsResourceProjectionMapper resourceProjectionMapper;

    @MockitoBean
    private ApsVersionService versionService;

    @BeforeEach
    void mockVersion() {
        when(versionService.getVersionDO(anyLong())).thenReturn(ApsVersionDO.builder()
                .id(VERSION_ID).scenarioId(SCENARIO_ID).versionNo(1)
                .status(ApsVersionStatusEnum.SOLVED.name()).version(0).build());
    }

    private void insertTask(String taskNo, LocalDateTime startTime, int durationMinutes) {
        taskMapper.insert(ApsTaskDO.builder()
                .versionId(VERSION_ID).taskNo(taskNo)
                .prodOrderId(1L).prodOrderNo("MO001").workOrderId(0L).opTaskId(randomLongId()).routeOpId(1L)
                .opCode("OP010").opName("粗车").plantId(1L).workshopId(1L).projectId(0L).projectTaskId(0L)
                .resId(RES_ID).resCode("RES-A")
                .startTime(startTime).endTime(startTime.plusMinutes(durationMinutes))
                .durationMinutes(durationMinutes).sequenceNo(1).lockedFlag(false)
                .status(ApsTaskStatusEnum.PLANNED.name())
                .build());
    }

    private void insertDailyCapacity(int availableMinutes) {
        resourceProjectionMapper.insert(ApsResourceProjectionDO.builder()
                .scenarioId(SCENARIO_ID).sourceResId(RES_ID).resCode("RES-A").resName("加工中心A")
                .resType("EQUIPMENT").plantId(1L).workshopId(1L).wcId(1L)
                .workDate(WORK_DATE).shiftId(0L).availableMinutes(availableMinutes)
                .sourceUpdateTime(LocalDateTime.now()).snapshotTime(LocalDateTime.now())
                .build());
    }

    private RefreshLoadReqDTO buildCommand(String idempotencyKey) {
        RefreshLoadReqDTO reqDTO = new RefreshLoadReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(VERSION_ID));
        reqDTO.setVersionId(VERSION_ID);
        return reqDTO;
    }

    @Test
    void testRefreshLoad_sumTaskMinutesByResourceAndDay() {
        insertTask("T001", LocalDateTime.of(2026, 8, 1, 8, 0), 120);
        insertTask("T002", LocalDateTime.of(2026, 8, 1, 13, 0), 180);
        insertDailyCapacity(480);

        loadService.refreshLoad(buildCommand("UI:REFRESH_LOAD:1"));

        List<ApsLoadDO> rows = loadMapper.selectListByVersionId(VERSION_ID);
        assertEquals(1, rows.size());
        ApsLoadDO row = rows.get(0);
        assertEquals(300, row.getLoadMinutes());
        assertEquals(480, row.getCapacityMinutes());
        assertEquals(0, row.getOverloadMinutes());
        assertEquals(0, new BigDecimal("0.6250").compareTo(row.getLoadRate()));
    }

    @Test
    void testRefreshLoad_overloadWhenExceedCapacity() {
        insertTask("T001", LocalDateTime.of(2026, 8, 1, 8, 0), 600);
        insertDailyCapacity(480);

        loadService.refreshLoad(buildCommand("UI:REFRESH_LOAD:1"));

        ApsLoadDO row = loadMapper.selectListByVersionId(VERSION_ID).get(0);
        assertEquals(120, row.getOverloadMinutes());
        assertEquals(0, new BigDecimal("1.2500").compareTo(row.getLoadRate()));
    }

    @Test
    void testRefreshLoad_zeroCapacityKeepsRateZero() {
        insertTask("T001", LocalDateTime.of(2026, 8, 1, 8, 0), 120);

        // 当天没有日历记录：可用分钟为 0，除零没有业务含义，负荷率记 0
        loadService.refreshLoad(buildCommand("UI:REFRESH_LOAD:1"));

        ApsLoadDO row = loadMapper.selectListByVersionId(VERSION_ID).get(0);
        assertEquals(0, row.getCapacityMinutes());
        assertEquals(120, row.getOverloadMinutes());
        assertEquals(0, BigDecimal.ZERO.compareTo(row.getLoadRate()));
    }

    @Test
    void testRefreshLoad_rebuildWithoutDuplicate() {
        insertTask("T001", LocalDateTime.of(2026, 8, 1, 8, 0), 120);
        insertDailyCapacity(480);

        loadService.refreshLoad(buildCommand("UI:REFRESH_LOAD:1"));
        loadService.refreshLoad(buildCommand("UI:REFRESH_LOAD:2"));

        // 整表重建走物理删除，重复刷新不会撞唯一索引也不会留下重复行
        assertEquals(1, loadMapper.selectListByVersionId(VERSION_ID).size());
    }

    @Test
    void testGetLoad_notExists() {
        assertServiceException(() -> loadService.getLoad(randomLongId()), LOAD_NOT_EXISTS);
    }

}
