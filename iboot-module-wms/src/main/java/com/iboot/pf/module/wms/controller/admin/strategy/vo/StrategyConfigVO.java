package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import com.iboot.pf.module.wms.strategy.config.WmsStrategySortOrderEnum;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 仓储策略配置 VO，对应 {@code wms_strategy.config_json} 的结构。
 *
 * <p>接口上收发结构化对象而不是 JSON 字符串：字符串挂不上 Bean Validation，也没法告诉页面
 * "这个类型能筛哪些字段"。手写 JSON 的代价是配错了要等策略真正执行才发现，那时候货已经上错架了。
 *
 * <p>与领域侧的 {@code WmsStrategyConfig} 刻意分开：那个类是引擎输入，字段随选库位、选批次的
 * 能力演进；这个是对外契约。多一层转换换来的是"领域模型可以改，接口不用跟着动"。
 *
 * <p>三种策略类型共用这一个 VO，专属字段按类型只填自己那几个：类型本来就在
 * {@code strategyType} 上，再按类型拆三个请求体，前端得写三套表单提交逻辑。
 * 哪些字段属于哪个类型由 {@code WmsStrategyConfigParser} 按类型校验。
 */
@Schema(description = "管理后台 - WMS-仓储策略配置")
@Data
public class StrategyConfigVO {

    @Schema(description = "筛选条件，多条之间是且的关系；为空表示不过滤")
    private List<@Valid Condition> conditions = new ArrayList<>();

    @Schema(description = "排序链，顺序即优先级；为空表示不排序")
    private List<@Valid Sort> sorts = new ArrayList<>();

    @Schema(description = "上架专属：尺寸校验模式，留空表示按库位自身设置", example = "FULL_DIM")
    private String dimensionCheck;

    @Schema(description = "上架专属：是否允许混批上架", example = "false")
    private Boolean allowMixedLot;

    @Schema(description = "补货专属：来源仓库编码", example = "WH-FLAT01")
    private String fromWhCode;

    @Schema(description = "补货专属：单次补货量", example = "200")
    private BigDecimal replenishQty;

    /** 一条筛选条件。 */
    @Schema(description = "管理后台 - WMS-策略筛选条件")
    @Data
    public static class Condition {

        @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zoneType")
        @NotEmpty(message = "筛选字段不能为空")
        private String field;

        @Schema(description = "算子", requiredMode = Schema.RequiredMode.REQUIRED, example = "EQ")
        @NotNull(message = "筛选算子不能为空或无法识别")
        private WmsStrategyOperatorEnum op;

        /**
         * 比较值。
         *
         * <p>类型跟着字段走，所以这里只能是 {@code Object}：枚举字段收字符串、数字字段收数字、
         * IN 收数组。值与字段是否匹配由 {@code WmsStrategyConfigParser} 拿字段目录校验，
         * Bean Validation 在这一层判不了。
         */
        @Schema(description = "比较值，算子为 IN/NOT_IN 时是数组", requiredMode = Schema.RequiredMode.REQUIRED)
        private Object value;

    }

    /** 一条排序规则。 */
    @Schema(description = "管理后台 - WMS-策略排序规则")
    @Data
    public static class Sort {

        @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "expiryDate")
        @NotEmpty(message = "排序字段不能为空")
        private String field;

        @Schema(description = "排序方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "ASC")
        @NotNull(message = "排序方向不能为空或无法识别")
        private WmsStrategySortOrderEnum order;

    }

}
