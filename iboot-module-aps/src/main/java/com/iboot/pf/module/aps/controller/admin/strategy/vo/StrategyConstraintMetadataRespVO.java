package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 约束元数据 Response VO：告诉前端"有哪些约束可调、默认调到多少"。
 *
 * <p>元数据由后端给而不是前端写死：约束清单以 {@code ApsConstraintNames} 为准，默认权重以
 * {@code ApsConstraintWeightResolver} 为准。前端硬编码一份的话，加约束时要改两处，
 * 迟早会出现"页面上少一条约束，配了也不知道"的情况。
 */
@Schema(description = "管理后台 - APS-排程策略约束元数据 Response VO")
@Data
public class StrategyConstraintMetadataRespVO {

    @Schema(description = "约束清单，顺序即页面展示顺序")
    private List<Constraint> constraints = new ArrayList<>();

    @Schema(description = "策略级默认配置，新建策略与“恢复默认”都取自它")
    private StrategyConfigVO defaultConfig;

    /** 单条约束的元数据。 */
    @Schema(description = "管理后台 - APS-约束元数据")
    @Data
    public static class Constraint {

        @Schema(description = "约束名", requiredMode = Schema.RequiredMode.REQUIRED, example = "dueDateDelay")
        private String constraintName;

        @Schema(description = "约束中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "交期延误")
        private String displayName;

        @Schema(description = "约束类别：HARD 硬约束、SOFT 软约束",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "SOFT")
        private String category;

        @Schema(description = "约束类别中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "软约束")
        private String categoryName;

        @Schema(description = "业务说明：罚什么、权重调大调小会怎样")
        private String description;

        @Schema(description = "默认硬约束权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Long defaultHard;

        @Schema(description = "默认软约束权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Long defaultSoft;

    }

}
