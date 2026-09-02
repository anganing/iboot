package com.iboot.pf.module.aps.solver.constraint;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 约束目录：约束名到中文名、类别、业务说明的登记表。
 *
 * <p>{@link ApsConstraintNames} 只负责当"名字锚点"（约束方法入参、策略配置权重键、命中快照分类键），
 * 面向人的那部分信息集中在这里：计划员在页面上调权重时看到的是中文名与"调大调小会怎样"，
 * 而不是 {@code dueDateDelay} 这种英文键。两者分开是为了让加约束时的动作清晰：
 * 先在 {@code ApsConstraintNames} 加常量，再来这里登记说明，漏一步会被 {@code ApsConstraintCatalogTest} 挡住。
 *
 * <p>说明文案刻意写"调大/调小会怎样"而不是"这条约束是什么"：权重配置页面上，
 * 前者才是计划员真正需要的决策依据。
 */
public final class ApsConstraintCatalog {

    /** 按 {@link ApsConstraintNames#ALL} 的顺序登记，顺序即页面展示顺序。 */
    private static final Map<String, ApsConstraintDefinition> DEFINITIONS = buildDefinitions();

    private ApsConstraintCatalog() {
    }

    /** 全部约束定义，顺序与 {@link ApsConstraintNames#ALL} 一致。 */
    public static List<ApsConstraintDefinition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    /** 单条约束定义；未登记时返回 {@code null}。 */
    public static ApsConstraintDefinition definition(String constraintName) {
        return DEFINITIONS.get(constraintName);
    }

    /**
     * 约束名对应的中文名，用于前端展示与快照可读性。
     *
     * <p>未登记的约束名原样返回：历史快照里可能存在已经改过名的约束，
     * 这里返回原始键比返回空字符串更有追溯价值。
     */
    public static String displayName(String constraintName) {
        ApsConstraintDefinition definition = DEFINITIONS.get(constraintName);
        return definition == null ? constraintName : definition.displayName();
    }

    private static Map<String, ApsConstraintDefinition> buildDefinitions() {
        Map<String, ApsConstraintDefinition> definitions = new LinkedHashMap<>();
        put(definitions, ApsConstraintNames.RESOURCE_CAPABILITY, "资源能力不匹配",
                ApsConstraintCategoryEnum.HARD,
                "禁止把工序排到不具备该工序能力的资源上。资源能力矩阵没维护时这条不生效，"
                        + "调成 0 等于允许任意设备干任意活，排出来的计划车间没法执行。");
        put(definitions, ApsConstraintNames.OPERATION_PRECEDENCE, "违反工序前后序",
                ApsConstraintCategoryEnum.HARD,
                "后道工序必须等前道完工加转移、排队时间之后才能开工，按提前的分钟数罚分。"
                        + "调成 0 会排出前后道倒挂的计划，工艺上根本做不出来。");
        put(definitions, ApsConstraintNames.PINNED_TASK_IMMUTABLE, "锁定任务被改动",
                ApsConstraintCategoryEnum.HARD,
                "已开工或人工锁定的工序不许换设备、不许改开工时间。调成 0 会让在制工序被挪走，"
                        + "车间已经领到的派工单和系统对不上。");
        put(definitions, ApsConstraintNames.CALENDAR_CAPACITY, "超出日历可用产能",
                ApsConstraintCategoryEnum.HARD,
                "工序必须落在资源日历的可用班次内。计划窗口内产能不够时算不出起止时间，由这条罚成不可行。"
                        + "调成 0 会落出没有起止时间的任务，甘特图上是空白。");
        put(definitions, ApsConstraintNames.SHADOW_INCONSISTENT, "工序前后序存在循环等待",
                ApsConstraintCategoryEnum.HARD,
                "工序之间形成互相等待的环路时，时间无法推导。调成 0 会让这批任务以空时间状态留在结果里，"
                        + "落库后是一批没有起止时间的计划。");
        put(definitions, ApsConstraintNames.MATERIAL_SHORTAGE, "物料未齐套",
                ApsConstraintCategoryEnum.SOFT,
                "让缺料工序给同一台设备上有料的工序让路，只在开启物料齐套约束时生效。"
                        + "调大越倾向于先干有料的活；升成硬约束会让含缺料工序的版本永远判为求解失败。");
        put(definitions, ApsConstraintNames.DUE_DATE_DELAY, "交期延误",
                ApsConstraintCategoryEnum.SOFT,
                "按延误分钟数乘工单优先级罚分，是默认权重最高的一条（100）。"
                        + "调小会让求解器为了压缩空闲、减少换型而牺牲交期。");
        put(definitions, ApsConstraintNames.TOTAL_MAKESPAN, "总完工时间",
                ApsConstraintCategoryEnum.SOFT,
                "按最晚一道工序的完工时刻罚分，推动整批活尽早收尾。"
                        + "调大越倾向于压缩总工期，可能以牺牲个别工单交期为代价。");
        put(definitions, ApsConstraintNames.PRIORITY_INVERSION, "优先级倒挂",
                ApsConstraintCategoryEnum.SOFT,
                "同一台设备上，高优先级工单排在低优先级之后时按优先级差罚分。"
                        + "调大越严格地按优先级排队，代价是设备空闲和换型可能变多。");
        put(definitions, ApsConstraintNames.PRECEDENCE_IDLE_GAP, "工序间等待",
                ApsConstraintCategoryEnum.SOFT,
                "按前后道之间超出必要间隔的空等分钟罚分，压缩在制品停留。"
                        + "调大越紧凑，但可能挤掉换型合并的空间。");
        put(definitions, ApsConstraintNames.CHANGEOVER_TIME, "换型时长",
                ApsConstraintCategoryEnum.SOFT,
                "按换型总分钟数罚分，推动把同换型分组的工单排在一起。"
                        + "调大越倾向于攒批生产，可能推迟个别工单交期。");
        // 用 unmodifiableMap 包 LinkedHashMap 而不是 Map.copyOf：后者的迭代顺序是不确定的，
        // 而这里的顺序就是页面上约束的展示顺序
        return Collections.unmodifiableMap(definitions);
    }

    private static void put(Map<String, ApsConstraintDefinition> definitions, String constraintName,
                            String displayName, ApsConstraintCategoryEnum category, String description) {
        definitions.put(constraintName,
                new ApsConstraintDefinition(constraintName, displayName, category, description));
    }

}
