package com.iboot.pf.module.aps.solver.model;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 一次排程求解的完整输入与输出。
 *
 * <p>输入是资源、待排工序、策略配置与计划起点，输出是资源上的工序队列与得分。
 * 全部字段都由场景投影快照构建，求解期间不访问数据库。
 *
 * <p>用 {@link HardSoftLongScore} 而不是 int 版本：交期延误、总完工时间这些软约束按分钟累加，
 * 上千个任务很容易越过 int 上限，溢出后得分会变成负数，求解器朝着错误方向优化。
 */
@PlanningSolution
@Getter
@Setter
public class ApsSolution {

    /** 排程版本编号，同时作为 SolverManager 的 problemId。 */
    private Long versionId;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** 计划起点：所有任务都不得早于它开工。 */
    private LocalDateTime planTime;

    /** 策略配置，求解期间不变，作为问题事实参与约束。 */
    @ProblemFactProperty
    private ApsStrategyConfig strategyConfig;

    /** 参与排程的资源，同时是规划实体集合。 */
    @PlanningEntityCollectionProperty
    private List<ApsSolverResource> resources = new ArrayList<>();

    /**
     * 参与排程的工序任务。
     *
     * <p>既是规划实体集合（挂影子变量），又是列表变量的值域。值域包含全部任务，
     * 包括已锁定的：锁定语义由资源上的 {@code firstUnpinnedIndex} 表达，
     * 从值域里剔除会让值域与资源队列内容不一致。
     */
    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = ApsSolverConstants.TASK_RANGE_ID)
    private List<ApsSolverTask> tasks = new ArrayList<>();

    /**
     * 约束权重覆盖，来自 {@code aps_strategy.config_json}。
     *
     * <p>由 Timefold 在构建求解器时校验：出现未定义的约束名会直接报错，不会静默忽略，
     * 正好满足“策略里配错约束名要暴露出来”的要求。
     */
    private ConstraintWeightOverrides<HardSoftLongScore> constraintWeightOverrides =
            ConstraintWeightOverrides.none();

    /** 求解得分。 */
    @PlanningScore
    private HardSoftLongScore score;

}
