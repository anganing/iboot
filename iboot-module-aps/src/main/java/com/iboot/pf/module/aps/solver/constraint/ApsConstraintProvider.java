package com.iboot.pf.module.aps.solver.constraint;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * 排程约束定义。
 *
 * <p>硬约束是“不许违反”，软约束是“尽量优化”。这里刻意没有写“同一资源上任务不许时间重叠”：
 * 资源用 {@code @PlanningListVariable} 建模，一道工序只能出现在一个资源队列的一个位置上，
 * 时间又是沿队列顺序累加出来的，结构上就不可能重叠。参考项目 jvs-aps 用的是普通规划变量，
 * 才需要额外写一条重叠惩罚；照搬过来只会白算一遍笛卡尔积，拖慢求解。
 *
 * <p>每条约束的权重都能被 {@code aps_strategy.config_json} 覆盖，这里的常量只是默认值。
 * 约束名必须走 {@link ApsConstraintNames}，它是权重配置与命中快照的对齐锚点。
 */
public class ApsConstraintProvider implements ConstraintProvider {

    /** 默认硬约束权重：一次违反罚 1 分硬分，够把不可行解与可行解区分开。 */
    private static final HardSoftLongScore ONE_HARD = HardSoftLongScore.ONE_HARD;

    /** 默认软约束权重。 */
    private static final HardSoftLongScore ONE_SOFT = HardSoftLongScore.ONE_SOFT;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                resourceCapability(factory),
                operationPrecedence(factory),
                pinnedTaskImmutable(factory),
                calendarCapacity(factory),
                shadowInconsistent(factory),
                dueDateDelay(factory),
                totalMakespan(factory),
                priorityInversion(factory),
                precedenceIdleGap(factory),
                changeoverTime(factory),
        };
    }

    /**
     * 硬约束：任务必须排在具备该工序能力的资源上。
     *
     * <p>能力矩阵为空时不限制（见 {@code ApsSolverTask#isResourceEligible}）：现场刚上线时
     * {@code mdm_res_capability} 往往还没维护，硬卡会让所有任务都排不出来。
     */
    Constraint resourceCapability(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getResource() != null
                        && !task.isResourceEligible(task.getResource().getId()))
                .penalizeLong(ONE_HARD, task -> 1L)
                .asConstraint(ApsConstraintNames.RESOURCE_CAPABILITY);
    }

    /**
     * 硬约束：前道工序完工加间隔后才能开工。
     *
     * <p>惩罚量取“提前了多少分钟”而不是简单计数：违反越多罚越重，求解器才知道往哪个方向修。
     * 只按计数罚会让“提前 1 分钟”和“提前 3 天”同分，搜索没有梯度。
     */
    Constraint operationPrecedence(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getStartTime() != null)
                .join(ApsSolverTask.class,
                        Joiners.filtering((task, predecessor) -> task.getPredecessors().contains(predecessor)))
                .filter((task, predecessor) -> predecessor.getEndTime() != null)
                .filter((task, predecessor) -> task.getStartTime()
                        .isBefore(predecessor.getEndTime().plusMinutes(task.lagMinutesAfter(predecessor))))
                .penalizeLong(ONE_HARD, (task, predecessor) -> minutesBetween(task.getStartTime(),
                        predecessor.getEndTime().plusMinutes(task.lagMinutesAfter(predecessor))))
                .asConstraint(ApsConstraintNames.OPERATION_PRECEDENCE);
    }

    /**
     * 硬约束：锁定任务的资源与开始时间不得变更。
     *
     * <p>结构上已由 {@code @PlanningPinToIndex} 保证，这条是兜底告警：万一构建输入时把锁定任务
     * 放到了错误的资源或错误的下标，宁可让版本判为不可行，也不要发布一份把在制工序挪走的计划。
     */
    Constraint pinnedTaskImmutable(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(ApsSolverTask::isPinned)
                .filter(task -> task.getResource() == null
                        || !Objects.equals(task.getResource().getId(), task.getPinnedResId())
                        || !Objects.equals(task.getStartTime(), task.getPinnedStartTime()))
                .penalizeLong(ONE_HARD, task -> 1L)
                .asConstraint(ApsConstraintNames.PINNED_TASK_IMMUTABLE);
    }

    /**
     * 硬约束：任务必须落在资源日历可用产能内。
     *
     * <p>时间推导算不出起止（计划窗口内产能耗尽）时会留空，在这里被罚成不可行。
     * 让它显式违反而不是悄悄跳过，计划员才能从约束命中快照里看到“产能不够”这个结论。
     */
    Constraint calendarCapacity(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getResource() != null
                        && (task.getStartTime() == null || task.getEndTime() == null))
                .penalizeLong(ONE_HARD, task -> 1L)
                .asConstraint(ApsConstraintNames.CALENDAR_CAPACITY);
    }

    /**
     * 硬约束：影子变量陷入循环等待。
     *
     * <p>求解中间态可能出现“前道排在后道之后”，形成时间上的互相等待。求解器检测到环路后会把相关
     * 影子变量置空并标记 {@code inconsistent}，这里罚掉它，求解器就会通过移动任务打破环路。
     * 不罚的话这些任务会以空时间的状态留在解里，最后落库出来一批没有起止时间的计划。
     */
    Constraint shadowInconsistent(ConstraintFactory factory) {
        // 必须用 forEachUnfiltered：forEach 会把影子变量不一致的实体过滤掉，罚不到它们
        return factory.forEachUnfiltered(ApsSolverTask.class)
                .filter(ApsSolverTask::isInconsistent)
                .penalizeLong(ONE_HARD, task -> 1L)
                .asConstraint(ApsConstraintNames.SHADOW_INCONSISTENT);
    }

    /** 软约束：交期延误分钟数，按优先级加权，优先级越高越不能晚。 */
    Constraint dueDateDelay(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getEndTime() != null && task.getDueDate() != null)
                .filter(task -> task.getEndTime().isAfter(task.getDueDate()))
                .penalizeLong(ONE_SOFT, task -> minutesBetween(task.getDueDate(), task.getEndTime())
                        * priorityWeight(task))
                .asConstraint(ApsConstraintNames.DUE_DATE_DELAY);
    }

    /**
     * 软约束：总完工时间。
     *
     * <p>取所有任务里最晚的结束时刻，用相对纪元的分钟数惩罚，推动整体尽早收尾。
     * 用 groupBy max 而不是逐任务累加：后者会变成“让每个任务都尽早”，与最小化总工期不是一回事，
     * 且会和优先级、交期这两条约束互相打架。
     */
    Constraint totalMakespan(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getEndTime() != null)
                .groupBy(ConstraintCollectors.max(ApsSolverTask::getEndTime))
                .penalizeLong(ONE_SOFT, maxEndTime -> maxEndTime.toEpochSecond(ZoneOffset.UTC) / SECONDS_PER_MINUTE)
                .asConstraint(ApsConstraintNames.TOTAL_MAKESPAN);
    }

    /**
     * 软约束：同一资源上高优先级任务被排在低优先级之后。
     *
     * <p>只在同资源内比较：跨资源的先后没有可比性，两台设备本来就并行。
     */
    Constraint priorityInversion(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getStartTime() != null && task.getResource() != null && !task.isPinned())
                .join(ApsSolverTask.class,
                        Joiners.equal(ApsSolverTask::getResource),
                        Joiners.lessThan(ApsSolverTask::getPriority))
                .filter((highPriority, lowPriority) -> lowPriority.getStartTime() != null
                        && !lowPriority.isPinned())
                // highPriority 的优先级数值更小，说明它更不重要；真正的倒挂是“更重要的排在后面”
                .filter((lessImportant, moreImportant) -> lessImportant.getStartTime()
                        .isBefore(moreImportant.getStartTime()))
                .penalizeLong(ONE_SOFT, (lessImportant, moreImportant) ->
                        (long) (moreImportant.getPriority() - lessImportant.getPriority()))
                .asConstraint(ApsConstraintNames.PRIORITY_INVERSION);
    }

    /** 软约束：前后道工序之间的空等时间，压缩在制品停留。 */
    Constraint precedenceIdleGap(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getStartTime() != null)
                .join(ApsSolverTask.class,
                        Joiners.filtering((task, predecessor) -> task.getPredecessors().contains(predecessor)))
                .filter((task, predecessor) -> predecessor.getEndTime() != null)
                .penalizeLong(ONE_SOFT, (task, predecessor) -> {
                    long expectedStartMinutes = task.lagMinutesAfter(predecessor);
                    long gap = minutesBetween(predecessor.getEndTime(), task.getStartTime()) - expectedStartMinutes;
                    return Math.max(gap, 0L);
                })
                .asConstraint(ApsConstraintNames.PRECEDENCE_IDLE_GAP);
    }

    /** 软约束：换型总时长，推动把同换型分组的任务排在一起。 */
    Constraint changeoverTime(ConstraintFactory factory) {
        return factory.forEach(ApsSolverTask.class)
                .filter(task -> task.getChangeoverMinutes() != null && task.getChangeoverMinutes() > 0)
                .penalizeLong(ONE_SOFT, task -> (long) task.getChangeoverMinutes())
                .asConstraint(ApsConstraintNames.CHANGEOVER_TIME);
    }

    /** 优先级权重：优先级越高，延误一分钟的代价越大。基准 1，避免优先级为 0 时权重归零。 */
    private static long priorityWeight(ApsSolverTask task) {
        int priority = task.getPriority() == null ? 0 : task.getPriority();
        return 1L + Math.max(priority, 0);
    }

    private static long minutesBetween(LocalDateTime from, LocalDateTime to) {
        return Math.abs(Duration.between(from, to).toMinutes());
    }

    /** 一分钟的秒数，用于把纪元秒折成分钟，避免软分数量级过大。 */
    private static final long SECONDS_PER_MINUTE = 60L;

}
