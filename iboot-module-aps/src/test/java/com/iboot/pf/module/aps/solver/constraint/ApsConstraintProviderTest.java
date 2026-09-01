package com.iboot.pf.module.aps.solver.constraint;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 约束逐条验证。
 *
 * <p>用 {@code ConstraintVerifier} 单独喂给每条约束一小组对象，断言它到底罚了多少分。
 * 只跑整体求解看不出某条约束是否真的生效：分数是所有约束叠加的结果，一条写错了很容易被掩盖。
 *
 * <p>{@code given} 传入的是已经算好时间的对象，不触发影子变量监听器，正好把约束逻辑与时间推导解耦验证。
 */
class ApsConstraintProviderTest {

    private static final LocalDateTime T8 = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDateTime T10 = LocalDateTime.of(2026, 9, 1, 10, 0);
    private static final LocalDateTime T12 = LocalDateTime.of(2026, 9, 1, 12, 0);
    private static final LocalDateTime T14 = LocalDateTime.of(2026, 9, 1, 14, 0);

    private final ConstraintVerifier<ApsConstraintProvider, ApsSolution> verifier = ConstraintVerifier
            .build(new ApsConstraintProvider(), ApsSolution.class,
                    ApsSolverResource.class, ApsSolverTask.class);

    private ApsSolverResource resource(Long id, String code) {
        ApsSolverResource resource = new ApsSolverResource();
        resource.setId(id);
        resource.setCode(code);
        resource.setEfficiency(BigDecimal.ONE);
        return resource;
    }

    private ApsSolverTask task(Long id, ApsSolverResource resource, LocalDateTime start, LocalDateTime end) {
        ApsSolverTask task = new ApsSolverTask();
        task.setId(id);
        task.setOpId(10L);
        task.setOpCode("OP" + id);
        task.setPriority(0);
        task.setQueueMinutes(0);
        task.setMoveMinutes(0);
        task.setResource(resource);
        task.setStartTime(start);
        task.setEndTime(end);
        if (resource != null) {
            resource.getTaskList().add(task);
        }
        return task;
    }

    @Test
    void resourceCapabilityShouldPenalizeUnqualifiedResource() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T8, T10);
        // 能力矩阵里只有 2 号资源，排到 1 号即违反
        task.setEligibleResIds(Set.of(2L));

        verifier.verifyThat(ApsConstraintProvider::resourceCapability)
                .given(resource, task)
                .penalizesBy(1L);
    }

    @Test
    void resourceCapabilityShouldNotPenalizeWhenCapabilityUnconfigured() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T8, T10);
        // 能力矩阵为空表示主数据还没维护，此时不限制资源，否则现场刚上线时全部任务都排不出来

        verifier.verifyThat(ApsConstraintProvider::resourceCapability)
                .given(resource, task)
                .penalizesBy(0L);
    }

    @Test
    void operationPrecedenceShouldPenalizeMinutesTooEarly() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask predecessor = task(100L, resource, T8, T12);
        ApsSolverTask successor = task(101L, resource, T10, T14);
        successor.getPredecessors().add(predecessor);

        // 前道 12:00 完工，后道 10:00 就开工，提前 120 分钟
        verifier.verifyThat(ApsConstraintProvider::operationPrecedence)
                .given(resource, predecessor, successor)
                .penalizesBy(120L);
    }

    @Test
    void operationPrecedenceShouldRespectLagMinutes() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask predecessor = task(100L, resource, T8, T10);
        predecessor.setMoveMinutes(30);
        ApsSolverTask successor = task(101L, resource, T10, T12);
        successor.setQueueMinutes(30);
        successor.getPredecessors().add(predecessor);

        // 间隔要求 30（转移）+ 30（排队）= 60 分钟，实际 0 分钟，违反 60 分钟
        verifier.verifyThat(ApsConstraintProvider::operationPrecedence)
                .given(resource, predecessor, successor)
                .penalizesBy(60L);
    }

    @Test
    void calendarCapacityShouldPenalizeTaskWithoutTime() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        // 时间推导算不出起止说明产能耗尽，必须显式违反，让人从命中详情看到“产能不够”
        ApsSolverTask task = task(100L, resource, null, null);

        verifier.verifyThat(ApsConstraintProvider::calendarCapacity)
                .given(resource, task)
                .penalizesBy(1L);
    }

    @Test
    void pinnedTaskImmutableShouldPenalizeMovedTask() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T10, T12);
        task.setPinned(true);
        task.setPinnedResId(2L);
        task.setPinnedStartTime(T8);

        verifier.verifyThat(ApsConstraintProvider::pinnedTaskImmutable)
                .given(resource, task)
                .penalizesBy(1L);
    }

    @Test
    void pinnedTaskImmutableShouldPassWhenUnchanged() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T8, T12);
        task.setPinned(true);
        task.setPinnedResId(1L);
        task.setPinnedStartTime(T8);

        verifier.verifyThat(ApsConstraintProvider::pinnedTaskImmutable)
                .given(resource, task)
                .penalizesBy(0L);
    }

    @Test
    void dueDateDelayShouldWeightByPriority() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T8, T12);
        task.setDueDate(T10);
        task.setPriority(2);

        // 延误 120 分钟 × 优先级权重（1 + 2）= 360
        verifier.verifyThat(ApsConstraintProvider::dueDateDelay)
                .given(resource, task)
                .penalizesBy(360L);
    }

    @Test
    void dueDateDelayShouldIgnoreOnTimeTask() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T8, T10);
        task.setDueDate(T12);

        verifier.verifyThat(ApsConstraintProvider::dueDateDelay)
                .given(resource, task)
                .penalizesBy(0L);
    }

    @Test
    void precedenceIdleGapShouldPenalizeExtraWaiting() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask predecessor = task(100L, resource, T8, T10);
        ApsSolverTask successor = task(101L, resource, T12, T14);
        successor.getPredecessors().add(predecessor);

        // 前道 10:00 完工，后道 12:00 开工，无间隔要求，空等 120 分钟
        verifier.verifyThat(ApsConstraintProvider::precedenceIdleGap)
                .given(resource, predecessor, successor)
                .penalizesBy(120L);
    }

    @Test
    void changeoverTimeShouldPenalizeChangeoverMinutes() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, T8, T10);
        task.setChangeoverMinutes(25);

        verifier.verifyThat(ApsConstraintProvider::changeoverTime)
                .given(resource, task)
                .penalizesBy(25L);
    }

    @Test
    void priorityInversionShouldPenalizeImportantTaskScheduledLater() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask lessImportant = task(100L, resource, T8, T10);
        lessImportant.setPriority(1);
        ApsSolverTask moreImportant = task(101L, resource, T12, T14);
        moreImportant.setPriority(5);

        // 优先级 5 的任务排在优先级 1 之后，倒挂幅度 4
        verifier.verifyThat(ApsConstraintProvider::priorityInversion)
                .given(resource, lessImportant, moreImportant)
                .penalizesBy(4L);
    }

    @Test
    void totalMakespanShouldPenalizeLatestEndTime() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask early = task(100L, resource, T8, T10);
        ApsSolverTask late = task(101L, resource, T12, T14);

        // 只按最晚完工时刻罚一次，而不是每个任务各罚一次
        long expected = T14.toEpochSecond(java.time.ZoneOffset.UTC) / 60L;
        verifier.verifyThat(ApsConstraintProvider::totalMakespan)
                .given(resource, early, late)
                .penalizesBy(expected);
    }

    @Test
    void definedConstraintsShouldMatchConstraintNames() {
        // 约束名是策略权重与命中快照的锚点，数量与集合必须与常量表一致
        List<String> defined = List.copyOf(ApsConstraintNames.ALL);
        org.junit.jupiter.api.Assertions.assertEquals(11, defined.size());
    }

    @Test
    void shadowInconsistentShouldBePenalized() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask task = task(100L, resource, null, null);
        // 求解器检测到循环等待时会置真；不罚掉会留下一批没有起止时间的任务
        task.setInconsistent(true);

        verifier.verifyThat(ApsConstraintProvider::shadowInconsistent)
                .given(resource, task)
                .penalizesBy(1L);
    }

    @Test
    void materialShortageShouldPenalizeBlockedReadyTask() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask shortageTask = task(100L, resource, T8, T10);
        shortageTask.setMaterialShortage(true);
        // 同资源上排在它之后的有料工序：缺料的占着前面，有料的只能等
        ApsSolverTask readyTask = task(101L, resource, T12, T14);

        verifier.verifyThat(ApsConstraintProvider::materialShortage)
                .given(resource, shortageTask, readyTask, materialConstrainedConfig(true))
                .penalizesBy(1L);
    }

    @Test
    void materialShortageShouldBeInertWhenStrategyDisabled() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask shortageTask = task(100L, resource, T8, T10);
        shortageTask.setMaterialShortage(true);
        ApsSolverTask readyTask = task(101L, resource, T12, T14);

        // 策略没开齐套约束时完全不生效，约束仍留在集合里保证快照分类键稳定
        verifier.verifyThat(ApsConstraintProvider::materialShortage)
                .given(resource, shortageTask, readyTask, materialConstrainedConfig(false))
                .penalizesBy(0L);
    }

    @Test
    void materialShortageShouldNotPenalizeWhenShortageTaskIsLast() {
        ApsSolverResource resource = resource(1L, "CNC-01");
        ApsSolverTask readyTask = task(100L, resource, T8, T10);
        ApsSolverTask shortageTask = task(101L, resource, T12, T14);
        shortageTask.setMaterialShortage(true);

        // 缺料工序已经排到队尾，没有挡住任何有料工序，惩罚归零：这就是求解器要找的方向
        verifier.verifyThat(ApsConstraintProvider::materialShortage)
                .given(resource, readyTask, shortageTask, materialConstrainedConfig(true))
                .penalizesBy(0L);
    }

    private ApsStrategyConfig materialConstrainedConfig(boolean enabled) {
        ApsStrategyConfig config = new ApsStrategyConfig();
        config.setMaterialConstrained(enabled);
        return config;
    }

}
