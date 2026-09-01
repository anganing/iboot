package com.iboot.pf.module.aps.solver.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code aps_strategy.config_json} 的结构。
 *
 * <p>求解期间只读，作为问题事实进入解。约束权重不写死在 {@code ConstraintProvider} 里，
 * 而是由策略配置驱动：同一套约束换个权重就是另一种排程偏好（保交期还是保产能利用率），
 * 硬编码会让 {@code aps_strategy} 沦为摆设，调权重要改代码重新发版。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApsStrategyConfig {

    /** 排程方向，缺省正排。 */
    private ApsPlanningDirectionEnum direction = ApsPlanningDirectionEnum.FORWARD;

    /** 求解最长耗时（秒），缺省 1 小时。 */
    private Long spentLimitSeconds = DEFAULT_SPENT_LIMIT_SECONDS;

    /** 多久没有更优解就停（秒），缺省 60 秒。 */
    private Long unimprovedSpentLimitSeconds = DEFAULT_UNIMPROVED_LIMIT_SECONDS;

    /**
     * 是否约束物料齐套。
     *
     * <p>本轮只落配置不生效：齐套联动要把库存投影与 BOM 展开一起纳入求解，
     * 属于下一轮范围。缺料仍由 {@code refreshShortage} 单独汇总。
     */
    private Boolean materialConstrained = Boolean.FALSE;

    /**
     * 约束权重覆盖：约束名 -> 权重。
     *
     * <p>约束名取值见 {@code ApsConstraintNames}。未配置的约束使用代码内默认权重；
     * 配了未知约束名时，Timefold 在构建求解器时会直接报错，不静默忽略。
     */
    private Map<String, ApsConstraintWeight> weights = new LinkedHashMap<>();

    /** 默认求解最长耗时：1 小时。 */
    public static final long DEFAULT_SPENT_LIMIT_SECONDS = 3600L;

    /** 默认无改进终止时长：60 秒。到点还没有更优解就收工，避免白烧 CPU。 */
    public static final long DEFAULT_UNIMPROVED_LIMIT_SECONDS = 60L;

    /** 求解最长耗时，非正数按默认值处理。 */
    public long spentLimitSecondsOrDefault() {
        return spentLimitSeconds == null || spentLimitSeconds <= 0
                ? DEFAULT_SPENT_LIMIT_SECONDS : spentLimitSeconds;
    }

    /** 无改进终止时长，非正数按默认值处理。 */
    public long unimprovedSpentLimitSecondsOrDefault() {
        return unimprovedSpentLimitSeconds == null || unimprovedSpentLimitSeconds <= 0
                ? DEFAULT_UNIMPROVED_LIMIT_SECONDS : unimprovedSpentLimitSeconds;
    }

}
