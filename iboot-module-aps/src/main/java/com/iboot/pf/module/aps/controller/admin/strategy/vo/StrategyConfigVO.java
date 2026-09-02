package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 排程策略配置 VO，对应 {@code aps_strategy.config_json} 的结构。
 *
 * <p>接口上收发结构化对象而不是 JSON 字符串：字符串没法挂 Bean Validation，非法配置只能等到
 * 点"生成排程"时才在求解链路里报错，那时候计划员已经不知道自己上次改了什么。
 *
 * <p>与求解域里的 {@code ApsStrategyConfig} 刻意分开：那个类是求解输入，字段随求解能力演进；
 * 这个是对外契约，多一层转换换来的是"求解域可以改，接口不用跟着动"。
 * {@code direction} 不出现在这里，只支持正排，暴露出去只会让人配错后被拒。
 */
@Schema(description = "管理后台 - APS-排程策略配置")
@Data
public class StrategyConfigVO {

    /** 求解耗时上限的下界：低于 1 分钟基本拿不到可用解。 */
    public static final long MIN_SPENT_LIMIT_SECONDS = 60L;

    /**
     * 求解耗时上限的上界。
     *
     * <p>卡在 1 小时是因为场景锁 {@code aps:solve:lock:{scenarioId}} 的 TTL 是 2 小时：
     * 求解时长必须显著小于它，否则锁过期后同场景能并发求解，两份计划互相覆盖。
     */
    public static final long MAX_SPENT_LIMIT_SECONDS = 3600L;

    /** 无改进终止时长的下界：低于 10 秒会在求解刚起步时就收工。 */
    public static final long MIN_UNIMPROVED_LIMIT_SECONDS = 10L;

    /** 单条约束权重上限：软分按分钟累加，再高就没有实际区分度，只会放大溢出风险。 */
    public static final long MAX_CONSTRAINT_WEIGHT = 1_000_000L;

    @Schema(description = "求解最长耗时（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    @NotNull(message = "求解最长耗时不能为空")
    @Min(value = MIN_SPENT_LIMIT_SECONDS, message = "求解最长耗时不能小于 60 秒")
    @Max(value = MAX_SPENT_LIMIT_SECONDS, message = "求解最长耗时不能大于 3600 秒")
    private Long spentLimitSeconds;

    @Schema(description = "多久没有更优解就停（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "无改进终止时长不能为空")
    @Min(value = MIN_UNIMPROVED_LIMIT_SECONDS, message = "无改进终止时长不能小于 10 秒")
    @Max(value = MAX_SPENT_LIMIT_SECONDS, message = "无改进终止时长不能大于 3600 秒")
    private Long unimprovedSpentLimitSeconds;

    @Schema(description = "是否约束物料齐套", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否约束物料齐套不能为空")
    private Boolean materialConstrained;

    /**
     * 约束权重：约束名 -> 硬/软权重。
     *
     * <p>约束名必须是 {@code ApsConstraintNames} 里登记过的，未知名一律拒绝——静默忽略会让人
     * 以为权重生效了，实际排程结果完全不受影响。
     */
    @Schema(description = "约束权重，键为约束名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "约束权重不能为空")
    private Map<String, @Valid ConstraintWeight> weights = new LinkedHashMap<>();

    /** 单条约束的硬软权重。 */
    @Schema(description = "管理后台 - APS-约束权重")
    @Data
    public static class ConstraintWeight {

        @Schema(description = "硬约束权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "硬约束权重不能为空")
        @Min(value = 0, message = "硬约束权重不能小于 0")
        @Max(value = MAX_CONSTRAINT_WEIGHT, message = "硬约束权重不能大于 1000000")
        private Long hard;

        @Schema(description = "软约束权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "软约束权重不能为空")
        @Min(value = 0, message = "软约束权重不能小于 0")
        @Max(value = MAX_CONSTRAINT_WEIGHT, message = "软约束权重不能大于 1000000")
        private Long soft;

    }

}
