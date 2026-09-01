package com.iboot.pf.module.aps.solver;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import com.iboot.pf.module.aps.solver.config.ApsConstraintWeight;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把策略配置里的权重翻译成 Timefold 的约束权重覆盖。
 *
 * <p>默认权重定在这里而不是 {@code ConstraintProvider} 里：{@code ConstraintProvider} 里的
 * {@code ONE_HARD}/{@code ONE_SOFT} 只是占位单位，真正的量纲由这里给出。这样调权重只改配置，
 * 不动约束代码。
 *
 * <p>软约束权重之间的数量级是刻意拉开的：交期延误 100、总完工 10、等待与换型 1、优先级 5。
 * 都设成 1 的话，几百个任务的等待分钟数会盖过交期延误，求解器会为了压缩空隙而牺牲交期，
 * 这与制造业“先保交期”的常识相反。
 */
public final class ApsConstraintWeightResolver {

    /** 硬约束默认权重：一次违反罚 1 分硬分。硬分只要非零就能把不可行解排除在外，量纲不重要。 */
    private static final long DEFAULT_HARD_WEIGHT = 1L;

    private static final Map<String, ApsConstraintWeight> DEFAULT_WEIGHTS = buildDefaults();

    private ApsConstraintWeightResolver() {
    }

    /**
     * 生成约束权重覆盖。
     *
     * <p>策略里配了的用策略值，没配的用默认值；两者都会显式写进覆盖表，
     * 让每条约束的实际权重在快照里可见，而不是散落在代码常量里。
     */
    public static ConstraintWeightOverrides<HardSoftLongScore> resolve(ApsStrategyConfig config) {
        Map<String, ApsConstraintWeight> configured = config == null || config.getWeights() == null
                ? Map.of() : config.getWeights();
        Map<String, HardSoftLongScore> overrides = new LinkedHashMap<>();
        for (String constraintName : ApsConstraintNames.ALL) {
            ApsConstraintWeight weight = configured.getOrDefault(constraintName,
                    DEFAULT_WEIGHTS.get(constraintName));
            overrides.put(constraintName,
                    HardSoftLongScore.of(weight.hardOrZero(), weight.softOrZero()));
        }
        return ConstraintWeightOverrides.of(overrides);
    }

    /** 默认权重，供测试与文档引用。 */
    public static Map<String, ApsConstraintWeight> defaultWeights() {
        return DEFAULT_WEIGHTS;
    }

    private static Map<String, ApsConstraintWeight> buildDefaults() {
        Map<String, ApsConstraintWeight> defaults = new LinkedHashMap<>();
        defaults.put(ApsConstraintNames.RESOURCE_CAPABILITY, ApsConstraintWeight.ofHard(DEFAULT_HARD_WEIGHT));
        defaults.put(ApsConstraintNames.OPERATION_PRECEDENCE, ApsConstraintWeight.ofHard(DEFAULT_HARD_WEIGHT));
        defaults.put(ApsConstraintNames.PINNED_TASK_IMMUTABLE, ApsConstraintWeight.ofHard(DEFAULT_HARD_WEIGHT));
        defaults.put(ApsConstraintNames.CALENDAR_CAPACITY, ApsConstraintWeight.ofHard(DEFAULT_HARD_WEIGHT));
        defaults.put(ApsConstraintNames.SHADOW_INCONSISTENT, ApsConstraintWeight.ofHard(DEFAULT_HARD_WEIGHT));
        // 交期最重：制造业的第一目标是准时交付
        defaults.put(ApsConstraintNames.DUE_DATE_DELAY, ApsConstraintWeight.ofSoft(100L));
        defaults.put(ApsConstraintNames.TOTAL_MAKESPAN, ApsConstraintWeight.ofSoft(10L));
        defaults.put(ApsConstraintNames.PRIORITY_INVERSION, ApsConstraintWeight.ofSoft(5L));
        defaults.put(ApsConstraintNames.PRECEDENCE_IDLE_GAP, ApsConstraintWeight.ofSoft(1L));
        defaults.put(ApsConstraintNames.CHANGEOVER_TIME, ApsConstraintWeight.ofSoft(1L));
        return Map.copyOf(defaults);
    }

}
