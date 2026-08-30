package com.iboot.pf.module.wms.api.transfer.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/** 创建转移调拨单命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateTransferOrderReqDTO extends WmsCommandReqDTO {

    /** 转移单号，为空由编码规则生成。 */
    @Schema(description = "转移单号")
    private String transferNo;

    /** 转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER。 */
    @Schema(description = "转移类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOC_TRANSFER")
    @NotNull(message = "转移类型不能为空")
    private String transferType;

    /** 来源库存组织编号。 */
    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库存组织编号不能为空")
    private Long fromInvOrgId;

    /** 目标库存组织编号。 */
    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long toInvOrgId;

    /** 来源仓库编号。 */
    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源仓库编号不能为空")
    private Long fromWhId;

    /** 目标仓库编号。 */
    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标仓库编号不能为空")
    private Long toWhId;

    /** 来源项目编号，0 表示公共库存。 */
    @Schema(description = "来源项目编号", example = "0")
    private Long fromProjectId;

    /** 目标项目编号，0 表示公共库存。 */
    @Schema(description = "目标项目编号", example = "0")
    private Long toProjectId;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    @Schema(description = "外部设备触发方式", example = "NONE")
    private String hookMode;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

    /** 转移明细。 */
    @Schema(description = "转移明细")
    @Valid
    @NotEmpty(message = "转移明细不能为空")
    private List<Line> lines;

    /** 转移明细。 */
    @Data
    public static class Line {

        /** 行号。 */
        @Schema(description = "行号", example = "10")
        private Integer lineNo;

        /** 物料编号。 */
        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物料编号不能为空")
        private Long matId;

        /** 物料编码快照。 */
        @Schema(description = "物料编码快照")
        private String matCode;

        /** 物料名称快照。 */
        @Schema(description = "物料名称快照")
        private String matName;

        /** 批次编号。 */
        @Schema(description = "批次编号")
        private Long batchId;

        /** 批次号快照。 */
        @Schema(description = "批次号快照")
        private String lotNo;

        /** 容器编号，0 表示散件。 */
        @Schema(description = "容器编号", example = "0")
        private Long containerId;

        /** 来源库位编号。 */
        @Schema(description = "来源库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "来源库位编号不能为空")
        private Long fromLocId;

        /** 目标库位编号。 */
        @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "目标库位编号不能为空")
        private Long toLocId;

        /** 来源库存范围：COMMON、PROJECT_DEDICATED。 */
        @Schema(description = "来源库存范围", example = "COMMON")
        private String fromStockScope;

        /** 目标库存范围：COMMON、PROJECT_DEDICATED。 */
        @Schema(description = "目标库存范围", example = "COMMON")
        private String toStockScope;

        /** 来源项目编号，0 表示公共库存。 */
        @Schema(description = "来源项目编号", example = "0")
        private Long fromProjectId;

        /** 目标项目编号，0 表示公共库存。 */
        @Schema(description = "目标项目编号", example = "0")
        private Long toProjectId;

        /** 来源项目任务编号，0 表示无项目任务。 */
        @Schema(description = "来源项目任务编号", example = "0")
        private Long fromProjectTaskId;

        /** 目标项目任务编号，0 表示无项目任务。 */
        @Schema(description = "目标项目任务编号", example = "0")
        private Long toProjectTaskId;

        /** 来源质量状态。 */
        @Schema(description = "来源质量状态", example = "QUALIFIED")
        private String qualityStatusFrom;

        /** 目标质量状态。 */
        @Schema(description = "目标质量状态", example = "QUALIFIED")
        private String qualityStatusTo;

        /** 基准计量单位编号。 */
        @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "基准计量单位编号不能为空")
        private Long uomId;

        /** 基准单位编码快照。 */
        @Schema(description = "基准单位编码快照")
        private String uomCode;

        /** 计划转移数量。 */
        @Schema(description = "计划转移数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.000000")
        @NotNull(message = "计划转移数量不能为空")
        private BigDecimal planQty;

        /** 备注。 */
        @Schema(description = "备注")
        private String remark;

    }

}
