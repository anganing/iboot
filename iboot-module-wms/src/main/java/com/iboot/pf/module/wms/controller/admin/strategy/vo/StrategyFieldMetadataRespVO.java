package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 策略字段元数据 Response VO：告诉前端"每种策略类型能筛什么、能按什么排、默认长什么样"。
 *
 * <p>元数据由后端给而不是前端写死：字段清单以 {@code WmsStrategyFieldCatalog} 为准，保存时的校验
 * 用的也是它。前端硬编码一份的话，加字段要改两处，迟早出现"页面上能选、保存时被拒"这种
 * 谁都说不清的状况。
 */
@Schema(description = "管理后台 - WMS-仓储策略字段元数据 Response VO")
@Data
public class StrategyFieldMetadataRespVO {

    @Schema(description = "按策略类型给出的可配项，顺序与策略类型字典一致")
    private List<StrategyTypeMetadata> types = new ArrayList<>();

    @Schema(description = "排序方向选项，各类型通用")
    private List<Option> sortOrders = new ArrayList<>();

    /** 一种策略类型的可配项。 */
    @Schema(description = "管理后台 - WMS-策略类型元数据")
    @Data
    public static class StrategyTypeMetadata {

        @Schema(description = "策略类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PICK")
        private String strategyType;

        @Schema(description = "策略类型中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "拣选策略")
        private String typeName;

        @Schema(description = "条件作用在哪批数据上", example = "库存余额（含批次与库位）")
        private String candidateTarget;

        @Schema(description = "可筛字段，顺序即展示顺序")
        private List<ConditionField> conditionFields = new ArrayList<>();

        @Schema(description = "可排序字段，顺序即展示顺序；为空表示该类型不支持排序")
        private List<SortField> sortFields = new ArrayList<>();

        @Schema(description = "该类型的默认配置，新建策略与切换类型时取自它")
        private StrategyConfigVO defaultConfig;

    }

    /** 可筛字段的元数据。 */
    @Schema(description = "管理后台 - WMS-策略可筛字段")
    @Data
    public static class ConditionField {

        @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zoneType")
        private String field;

        @Schema(description = "字段中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "库区类型")
        private String displayName;

        @Schema(description = "值类型：ENUM、NUMBER、BOOLEAN、TEXT",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "ENUM")
        private String valueType;

        @Schema(description = "枚举字段的字典编码，供前端渲染下拉；非枚举字段为空", example = "wms_zone_type")
        private String dictType;

        @Schema(description = "该字段支持的算子，顺序即下拉顺序")
        private List<Operator> operators = new ArrayList<>();

        @Schema(description = "业务说明：配了它会筛掉什么")
        private String description;

    }

    /** 可排序字段的元数据。 */
    @Schema(description = "管理后台 - WMS-策略可排序字段")
    @Data
    public static class SortField {

        @Schema(description = "字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "expiryDate")
        private String field;

        @Schema(description = "字段中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "失效日期")
        private String displayName;

        @Schema(description = "业务说明：按它排会得到什么效果")
        private String description;

    }

    /** 算子元数据。 */
    @Schema(description = "管理后台 - WMS-策略算子")
    @Data
    public static class Operator {

        @Schema(description = "算子", requiredMode = Schema.RequiredMode.REQUIRED, example = "IN")
        private String op;

        @Schema(description = "算子中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "属于")
        private String opName;

        @Schema(description = "值是否为多个，决定前端用单选还是多选",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean multiValue;

    }

    /** 通用的键值选项。 */
    @Schema(description = "管理后台 - WMS-策略选项")
    @Data
    public static class Option {

        @Schema(description = "值", requiredMode = Schema.RequiredMode.REQUIRED, example = "ASC")
        private String value;

        @Schema(description = "中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "升序")
        private String label;

    }

}
