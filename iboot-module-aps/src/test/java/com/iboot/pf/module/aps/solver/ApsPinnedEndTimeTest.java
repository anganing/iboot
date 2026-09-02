package com.iboot.pf.module.aps.solver;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsOrderProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsResourceProjectionMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsDurationTypeEnum;
import com.iboot.pf.module.aps.enums.ApsProjectionSourceTypeEnum;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 在制工序完工时间推算测试。
 *
 * <p>守的是这条链路：MES 只在报完工后才写 {@code actual_end_time}，在制工序（RUNNING / QC_WAITING）
 * 没有它。缺值时若回落到"按裁剪后的日历从开工时刻累加工时"，已开工工序的完工时间会被推到计划起点
 * 之后，后道工序的实际开工反而更早，{@code operationPrecedence} 硬约束必然违反，整份计划判为无可行解。
 */
@Import(ApsSolverProblemBuilder.class)
class ApsPinnedEndTimeTest extends BaseDbUnitTest {

    private static final Long SCENARIO_ID = 700L;
    private static final Long RES_ID = 701L;
    /** 计划起点定在 9/10，两道在制工序的开工时刻刻意落在它之前。 */
    private static final LocalDateTime PLAN_START = LocalDateTime.of(2026, 9, 10, 8, 0);

    @Resource
    private ApsSolverProblemBuilder problemBuilder;
    @Resource
    private ApsResourceProjectionMapper resourceProjectionMapper;
    @Resource
    private ApsOrderProjectionMapper orderProjectionMapper;

    @MockitoBean
    private ResourceApi resourceApi;

    @BeforeEach
    void prepareProjection() {
        ResourceRespDTO master = new ResourceRespDTO();
        master.setId(RES_ID);
        master.setCode("RES-A");
        master.setName("加工中心A");
        master.setEfficiency(BigDecimal.ONE);
        when(resourceApi.getResourceMap(any())).thenReturn(Map.of(RES_ID, master));
        when(resourceApi.getChangeoverList(any(), any())).thenReturn(List.of());
        // 资源日历覆盖 9/01 ~ 9/30，每天 8:00-20:00 一个班次
        for (int day = 1; day <= 30; day++) {
            resourceProjectionMapper.insert(ApsResourceProjectionDO.builder()
                    .scenarioId(SCENARIO_ID).sourceResId(RES_ID).resCode("RES-A").resName("加工中心A")
                    .resType("MACHINE").plantId(1L).workshopId(1L).wcId(ApsConstants.NONE_ID)
                    .workDate(LocalDate.of(2026, 9, day)).shiftId(9L)
                    .shiftStartTime(LocalTime.of(8, 0)).shiftEndTime(LocalTime.of(20, 0))
                    .availableMinutes(720).capabilityJson(null)
                    .sourceUpdateTime(LocalDateTime.now()).snapshotTime(LocalDateTime.now())
                    .build());
        }
    }

    /** 在制工序：已开工未完工，开工时刻早于计划起点。 */
    private void insertRunningTask(Long opTaskId, int seq, LocalDateTime actualStart) {
        orderProjectionMapper.insert(baseOrder(opTaskId, seq)
                .lockedFlag(true).actualStartTime(actualStart).actualEndTime(null)
                .currentStatus("RUNNING").build());
    }

    /** 待排工序。 */
    private void insertPendingTask(Long opTaskId, int seq) {
        orderProjectionMapper.insert(baseOrder(opTaskId, seq)
                .lockedFlag(false).currentStatus("PENDING").build());
    }

    private ApsOrderProjectionDO.ApsOrderProjectionDOBuilder baseOrder(Long opTaskId, int seq) {
        return ApsOrderProjectionDO.builder()
                .scenarioId(SCENARIO_ID)
                .sourceType(ApsProjectionSourceTypeEnum.OP_TASK.name())
                .sourceId(opTaskId).sourceNo("OPT" + opTaskId)
                .prodOrderId(1L).workOrderId(ApsConstants.NONE_ID).opTaskId(opTaskId)
                .plantId(1L).workshopId(1L)
                .projectId(ApsConstants.NONE_ID).projectTaskId(ApsConstants.NONE_ID)
                .matId(9L).matCode("MAT-9")
                .routeOpId(1L).seq(seq).opId(10L).opCode("OP0" + seq).opName("工序" + seq)
                .resId(RES_ID)
                .qty(new BigDecimal("100")).completedQty(ApsConstants.ZERO_QTY)
                .dueDate(LocalDateTime.of(2026, 9, 25, 18, 0)).priority(5)
                .setupMinutes(0).stdMinutesPerUnit(BigDecimal.ZERO).teardownMinutes(0)
                .queueMinutes(0).moveMinutes(0)
                .durationType(ApsDurationTypeEnum.FIXED.name()).stdDuration(120).manQty(1)
                .changeoverGroup("").materialShortageFlag(false).materialShortQty(ApsConstants.ZERO_QTY)
                .sourceUpdateTime(LocalDateTime.now()).snapshotTime(LocalDateTime.now());
    }

    private ApsScenarioDO scenario() {
        return ApsScenarioDO.builder()
                .id(SCENARIO_ID).code("SC-PIN").name("在制完工推算").plantId(1L)
                .workshopId(ApsConstants.NONE_ID).strategyId(1L)
                .planStartTime(PLAN_START).planEndTime(LocalDateTime.of(2026, 9, 30, 20, 0))
                .build();
    }

    @Test
    void shouldDerivePinnedEndTimeFromUnclippedCalendar() {
        // 9/05 08:00 开工、固定工时 120 分钟，按完整日历应在 9/05 10:00 完工
        insertRunningTask(801L, 10, LocalDateTime.of(2026, 9, 5, 8, 0));
        insertPendingTask(802L, 20);

        ApsSolution solution = problemBuilder.build(scenario(), 1L, new ApsStrategyConfig());

        ApsSolverTask running = solution.getTasks().stream()
                .filter(ApsSolverTask::isPinned).findFirst().orElseThrow();
        assertNotNull(running.getPinnedEndTime(), "在制工序的完工时间必须被推算出来，否则会误伤前后序硬约束");
        assertEquals(LocalDateTime.of(2026, 9, 5, 10, 0), running.getPinnedEndTime());
        // 关键不变量：推算出的完工时间不能被推到计划起点之后
        assertTrue(running.getPinnedEndTime().isBefore(PLAN_START),
                "完工时间被推到计划起点之后，说明用了裁剪过的日历");
    }

    @Test
    void shouldKeepActualEndTimeWhenReported() {
        // 已报完工的工序直接用 MES 的实际完工时间，不再推算
        orderProjectionMapper.insert(baseOrder(803L, 10)
                .lockedFlag(true)
                .actualStartTime(LocalDateTime.of(2026, 9, 5, 8, 0))
                .actualEndTime(LocalDateTime.of(2026, 9, 6, 15, 30))
                .currentStatus("DONE").build());
        insertPendingTask(804L, 20);

        ApsSolution solution = problemBuilder.build(scenario(), 1L, new ApsStrategyConfig());

        ApsSolverTask done = solution.getTasks().stream()
                .filter(ApsSolverTask::isPinned).findFirst().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 9, 6, 15, 30), done.getPinnedEndTime());
    }

    @Test
    void clippedWindowsShouldStillExcludePastCapacity() {
        insertPendingTask(805L, 10);

        ApsSolution solution = problemBuilder.build(scenario(), 1L, new ApsStrategyConfig());

        // 排新计划用的窗口仍然不含计划起点之前的产能
        assertTrue(solution.getResources().get(0).getWindows().stream()
                .allMatch(window -> !window.start().isBefore(PLAN_START)),
                "裁剪后的窗口不该包含计划起点之前的产能");
        // 完整窗口保留 9/01 起的历史产能，供在制工序推算使用
        assertFalse(solution.getResources().get(0).getRawWindows().stream()
                .allMatch(window -> !window.start().isBefore(PLAN_START)),
                "完整窗口必须保留计划起点之前的班次");
    }

}
