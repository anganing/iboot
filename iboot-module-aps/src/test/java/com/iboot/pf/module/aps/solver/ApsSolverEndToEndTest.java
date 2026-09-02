package com.iboot.pf.module.aps.solver;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import com.iboot.pf.module.aps.enums.ApsDurationTypeEnum;
import com.iboot.pf.module.aps.framework.web.config.ApsSolverConfiguration;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import com.iboot.pf.module.aps.solver.model.ApsTimeWindow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 端到端求解：3 台资源 × 9 道工序的小算例。
 *
 * <p>验证的是整套模型能不能真的排出一份可行计划，以及四条不变量是否成立：硬分为零、
 * 同资源不重叠、前后序满足间隔、锁定任务原地不动。单条约束的分值由
 * {@code ApsConstraintProviderTest} 覆盖，这里只看整体结论。
 *
 * <p>不连数据库：求解域是纯内存对象，把 DB 拉进来只会让这条测试变慢变脆。
 */
class ApsSolverEndToEndTest {

    private static final LocalDateTime PLAN_TIME = LocalDateTime.of(2026, 9, 1, 8, 0);

    /** 连续 5 天、每天 8:00-20:00 的可用产能，够小算例排完。 */
    private List<ApsTimeWindow> windows() {
        List<ApsTimeWindow> windows = new ArrayList<>();
        for (int day = 0; day < 5; day++) {
            LocalDateTime start = PLAN_TIME.plusDays(day);
            windows.add(new ApsTimeWindow(start, start.plusHours(12)));
        }
        return windows;
    }

    private ApsSolverResource resource(long id, String code, Map<Long, BigDecimal> capabilities) {
        ApsSolverResource resource = new ApsSolverResource();
        resource.setId(id);
        resource.setCode(code);
        resource.setEfficiency(BigDecimal.ONE);
        resource.setWindows(windows());
        resource.setCapabilityEfficiencies(capabilities);
        return resource;
    }

    private ApsSolverTask task(long id, long prodOrderId, long opId, int seq, int minutes,
                               Set<Long> eligibleResIds) {
        ApsSolverTask task = new ApsSolverTask();
        task.setId(id);
        task.setProdOrderId(prodOrderId);
        task.setProdOrderNo("MO" + prodOrderId);
        task.setOpId(opId);
        task.setOpCode("OP" + opId);
        task.setSeq(seq);
        task.setQty(BigDecimal.ONE);
        task.setStdMinutesPerUnit(BigDecimal.ZERO);
        task.setDurationType(ApsDurationTypeEnum.FIXED.name());
        task.setStdDuration(minutes);
        task.setSetupMinutes(0);
        task.setTeardownMinutes(0);
        task.setQueueMinutes(0);
        task.setMoveMinutes(0);
        task.setPriority(0);
        task.setChangeoverGroup("");
        task.setDueDate(PLAN_TIME.plusDays(3));
        task.setEligibleResIds(eligibleResIds);
        return task;
    }

    /**
     * 三个工单各三道工序：每道工序限定一类资源，形成必须跨资源流转的工艺路线。
     */
    private ApsSolution buildProblem() {
        Map<Long, BigDecimal> op10 = new LinkedHashMap<>();
        op10.put(10L, BigDecimal.ONE);
        Map<Long, BigDecimal> op20 = new LinkedHashMap<>();
        op20.put(20L, BigDecimal.ONE);
        Map<Long, BigDecimal> op30 = new LinkedHashMap<>();
        op30.put(30L, BigDecimal.ONE);

        List<ApsSolverResource> resources = List.of(
                resource(1L, "CUT-01", op10),
                resource(2L, "CNC-01", op20),
                resource(3L, "ASM-01", op30));

        List<ApsSolverTask> tasks = new ArrayList<>();
        for (long order = 1; order <= 3; order++) {
            ApsSolverTask first = task(order * 100 + 1, order, 10L, 10, 60, Set.of(1L));
            ApsSolverTask second = task(order * 100 + 2, order, 20L, 20, 90, Set.of(2L));
            ApsSolverTask third = task(order * 100 + 3, order, 30L, 30, 60, Set.of(3L));
            second.getPredecessors().add(first);
            third.getPredecessors().add(second);
            tasks.add(first);
            tasks.add(second);
            tasks.add(third);
        }

        ApsSolution solution = new ApsSolution();
        solution.setVersionId(1L);
        solution.setScenarioId(1L);
        solution.setPlanTime(PLAN_TIME);
        solution.setStrategyConfig(new ApsStrategyConfig());
        solution.setResources(new ArrayList<>(resources));
        solution.setTasks(tasks);
        solution.setConstraintWeightOverrides(
                ApsConstraintWeightResolver.resolve(solution.getStrategyConfig()));
        return solution;
    }

    private ApsSolution solve(ApsSolution problem) {
        SolverConfig config = new ApsSolverConfiguration().apsSolverConfig()
                .withTerminationConfig(new TerminationConfig()
                        .withSpentLimit(Duration.ofSeconds(10))
                        .withUnimprovedSpentLimit(Duration.ofSeconds(3)));
        return SolverFactory.<ApsSolution>create(config).buildSolver().solve(problem);
    }

    @Test
    void shouldProduceFeasibleSchedule() {
        ApsSolution solved = solve(buildProblem());

        assertNotNull(solved.getScore());
        // 硬分为零才是可行解；为负说明存在无法回避的冲突，这样的计划不允许发布
        assertEquals(0L, solved.getScore().hardScore(), "求解结果应当可行，实际得分：" + solved.getScore());

        // 每道工序都必须落到某台资源上，不允许悬空
        long assigned = solved.getResources().stream().mapToLong(res -> res.getTaskList().size()).sum();
        assertEquals(9L, assigned);
        solved.getTasks().forEach(task -> {
            assertNotNull(task.getResource(), "任务未分派资源：" + task);
            assertNotNull(task.getStartTime(), "任务未算出开始时间：" + task);
            assertNotNull(task.getEndTime(), "任务未算出结束时间：" + task);
            assertTrue(task.isResourceEligible(task.getResource().getId()), "任务排到了不具备能力的资源：" + task);
        });
    }

    @Test
    void shouldNotOverlapOnSameResource() {
        ApsSolution solved = solve(buildProblem());
        for (ApsSolverResource resource : solved.getResources()) {
            List<ApsSolverTask> queue = resource.getTaskList();
            for (int index = 1; index < queue.size(); index++) {
                LocalDateTime previousEnd = queue.get(index - 1).getEndTime();
                LocalDateTime currentStart = queue.get(index).getStartTime();
                // 列表变量模型下重叠是结构上不可能的，这里断言的是时间推导没有把顺序算错
                assertFalse(currentStart.isBefore(previousEnd),
                        "资源 " + resource.getCode() + " 上出现时间重叠");
            }
        }
    }

    @Test
    void shouldRespectOperationPrecedence() {
        ApsSolution solved = solve(buildProblem());
        for (ApsSolverTask task : solved.getTasks()) {
            for (ApsSolverTask predecessor : task.getPredecessors()) {
                LocalDateTime earliest = predecessor.getEndTime()
                        .plusMinutes(task.lagMinutesAfter(predecessor));
                assertFalse(task.getStartTime().isBefore(earliest),
                        "工序 " + task.getOpCode() + " 早于前道 " + predecessor.getOpCode() + " 完工时间开工");
            }
        }
    }

    @Test
    void shouldKeepPinnedTaskOnOriginalResourceAndTime() {
        ApsSolution problem = buildProblem();
        // 把 1 号工单的首道工序设为已开工：资源与时间都必须原样保留
        ApsSolverTask pinned = problem.getTasks().get(0);
        pinned.setPinned(true);
        pinned.setPinnedResId(1L);
        pinned.setPinnedStartTime(PLAN_TIME);
        pinned.setPinnedEndTime(PLAN_TIME.plusHours(1));
        ApsSolverResource firstResource = problem.getResources().get(0);
        firstResource.getTaskList().add(pinned);
        firstResource.setFirstUnpinnedIndex(1);

        ApsSolution solved = solve(problem);

        ApsSolverTask solvedPinned = solved.getTasks().stream()
                .filter(ApsSolverTask::isPinned)
                .findFirst()
                .orElseThrow();
        assertEquals(1L, solvedPinned.getResource().getId(), "锁定任务被挪到了别的资源");
        assertEquals(PLAN_TIME, solvedPinned.getStartTime(), "锁定任务的开始时间被改动");
        assertEquals(0, solved.getResources().get(0).getTaskList().indexOf(solvedPinned),
                "锁定任务不在队首，说明有任务被插到了它前面");
        assertEquals(0L, solved.getScore().hardScore(), "带锁定任务的算例仍应可行");
    }

    /**
     * 在制工序开工于计划起点之前时仍应排出可行解。
     *
     * <p>这是线上踩到的场景：MES 的在制工序（RUNNING / QC_WAITING）没有 actualEndTime，
     * 开工时刻又早于计划窗口起点。若完工时间被回落成"按裁剪后的日历从开工时刻累加"，
     * 它会被推到计划起点之后，后道工序的实际开工反而更早，operationPrecedence 必然违反，
     * 整份计划判为无可行解。ApsSolverProblemBuilder 必须在构建输入时就把完工时间推算好。
     */
    @Test
    void shouldStayFeasibleWhenPinnedTaskStartedBeforePlanTime() {
        ApsSolution problem = buildProblem();
        // 1 号工单：首道 8/28 开工、8/28 完工（都在计划起点 9/01 之前），二道紧随其后
        ApsSolverTask first = problem.getTasks().get(0);
        first.setPinned(true);
        first.setPinnedResId(1L);
        first.setPinnedStartTime(PLAN_TIME.minusDays(4));
        first.setPinnedEndTime(PLAN_TIME.minusDays(4).plusHours(1));
        ApsSolverResource firstResource = problem.getResources().get(0);
        firstResource.getTaskList().add(first);
        firstResource.setFirstUnpinnedIndex(1);

        ApsSolverTask second = problem.getTasks().get(1);
        second.setPinned(true);
        second.setPinnedResId(2L);
        second.setPinnedStartTime(PLAN_TIME.minusDays(3));
        second.setPinnedEndTime(PLAN_TIME.minusDays(3).plusHours(2));
        ApsSolverResource secondResource = problem.getResources().get(1);
        secondResource.getTaskList().add(second);
        secondResource.setFirstUnpinnedIndex(1);

        ApsSolution solved = solve(problem);

        assertEquals(0L, solved.getScore().hardScore(),
                "在制工序开工早于计划起点时仍应可行，实际得分：" + solved.getScore());
        // 后道的完工时间早于前道开工也不该违反：两者都是既成事实
        assertFalse(second.getPinnedStartTime().isBefore(first.getPinnedEndTime()),
                "测试数据本身应满足前后序");
    }

}
