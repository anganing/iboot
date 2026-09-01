package com.iboot.pf.module.aps.solver.constraint;

import java.util.List;

/**
 * 约束名常量。
 *
 * <p>这些字符串是三处的对齐锚点：{@code ConstraintProvider} 里 {@code asConstraint} 的入参、
 * {@code aps_strategy.config_json} 里的权重键、约束命中快照里的分类键。任何一处改名都会让
 * 历史策略配置与历史快照对不上，所以集中在这里定义，不允许在别处写字面量。
 *
 * <p>用英文而不是中文：{@code ConstraintWeightOverrides} 的键会进 JSON 配置和快照，
 * 中文键在跨系统传输与日志里容易出编码问题。中文名放在 {@link #displayName(String)} 里做展示映射。
 */
public interface ApsConstraintNames {

    /** 硬约束：任务必须排在具备该工序能力的资源上。 */
    String RESOURCE_CAPABILITY = "resourceCapability";

    /** 硬约束：前道工序完工加间隔后才能开工。 */
    String OPERATION_PRECEDENCE = "operationPrecedence";

    /** 硬约束：锁定任务的资源与时间不得变更。 */
    String PINNED_TASK_IMMUTABLE = "pinnedTaskImmutable";

    /** 硬约束：任务必须落在资源日历可用产能内。 */
    String CALENDAR_CAPACITY = "calendarCapacity";

    /** 硬约束：影子变量陷入循环等待，时间无法推导。 */
    String SHADOW_INCONSISTENT = "shadowInconsistent";

    /**
     * 硬约束：缺料工序不许排产。
     *
     * <p>只在策略开了 {@code materialConstrained} 时生效：关掉时约束方法直接放行，
     * 而不是把约束从数组里摘掉——约束集合必须稳定，否则历史快照的分类键会对不上。
     */
    String MATERIAL_SHORTAGE = "materialShortage";

    /** 软约束：交期延误分钟数。 */
    String DUE_DATE_DELAY = "dueDateDelay";

    /** 软约束：总完工时间（makespan）。 */
    String TOTAL_MAKESPAN = "totalMakespan";

    /** 软约束：同资源上高优先级任务排在低优先级之后。 */
    String PRIORITY_INVERSION = "priorityInversion";

    /** 软约束：前后道工序之间的等待间隔。 */
    String PRECEDENCE_IDLE_GAP = "precedenceIdleGap";

    /** 软约束：换型总时长。 */
    String CHANGEOVER_TIME = "changeoverTime";

    /** 全部约束名，供策略配置校验与快照分类使用。 */
    List<String> ALL = List.of(RESOURCE_CAPABILITY, OPERATION_PRECEDENCE, PINNED_TASK_IMMUTABLE,
            CALENDAR_CAPACITY, SHADOW_INCONSISTENT, MATERIAL_SHORTAGE, DUE_DATE_DELAY, TOTAL_MAKESPAN,
            PRIORITY_INVERSION, PRECEDENCE_IDLE_GAP, CHANGEOVER_TIME);

    /** 约束名对应的中文说明，用于前端展示与快照可读性。 */
    static String displayName(String constraintName) {
        return switch (constraintName) {
            case RESOURCE_CAPABILITY -> "资源能力不匹配";
            case OPERATION_PRECEDENCE -> "违反工序前后序";
            case PINNED_TASK_IMMUTABLE -> "锁定任务被改动";
            case CALENDAR_CAPACITY -> "超出日历可用产能";
            case SHADOW_INCONSISTENT -> "工序前后序存在循环等待";
            case MATERIAL_SHORTAGE -> "物料未齐套";
            case DUE_DATE_DELAY -> "交期延误";
            case TOTAL_MAKESPAN -> "总完工时间";
            case PRIORITY_INVERSION -> "优先级倒挂";
            case PRECEDENCE_IDLE_GAP -> "工序间等待";
            case CHANGEOVER_TIME -> "换型时长";
            default -> constraintName;
        };
    }

}
