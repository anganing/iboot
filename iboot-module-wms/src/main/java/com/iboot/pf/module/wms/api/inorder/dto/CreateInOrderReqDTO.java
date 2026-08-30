package com.iboot.pf.module.wms.api.inorder.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 创建入库单命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateInOrderReqDTO extends WmsCommandReqDTO {

    /** 入库单号，为空由编码规则生成。 */
    @Schema(description = "入库单号")
    private String inNo;

    /** 入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN。 */
    @Schema(description = "入库业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PO_IN")
    @NotNull(message = "入库业务类型不能为空")
    private String bizType;

    /** 原始来源类型。 */
    @Schema(description = "原始来源类型")
    private String originType;

    /** 原始来源单号。 */
    @Schema(description = "原始来源单号")
    private String originNo;

    /** 到货登记编号。 */
    @Schema(description = "到货登记编号")
    private Long arrivalId;

    /** 供应商编号。 */
    @Schema(description = "供应商编号")
    private Long supplierId;

    /** 客户编号。 */
    @Schema(description = "客户编号")
    private Long customerId;

    /** 生产工单号快照。 */
    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    /** 作业工单号快照。 */
    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    /** 转移或调拨单号。 */
    @Schema(description = "转移或调拨单号")
    private String transferNo;

    /** 盘点单号。 */
    @Schema(description = "盘点单号")
    private String countOrderNo;

    /** 目标库存组织编号。 */
    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long invOrgId;

    /** 目标工厂编号。 */
    @Schema(description = "目标工厂编号")
    private Long plantId;

    /** 项目编号。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 项目任务编号。 */
    @Schema(description = "项目任务编号", example = "0")
    private Long projectTaskId;

    /** 目标仓库编号。 */
    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标仓库编号不能为空")
    private Long whId;

    /** 是否需要质检。 */
    @Schema(description = "是否需要质检", example = "false")
    private Boolean needQc;

    /** 入库质量状态，为空按合格处理。 */
    @Schema(description = "入库质量状态", example = "QUALIFIED")
    private String qualityStatus;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    @Schema(description = "外部设备触发方式", example = "NONE")
    private String hookMode;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

    /** 入库明细。 */
    @Schema(description = "入库明细")
    @Valid
    @NotEmpty(message = "入库明细不能为空")
    private List<Line> lines;

    /** 入库明细。 */
    @Data
    public static class Line {

        /** 行号。 */
        @Schema(description = "行号", example = "10")
        private Integer lineNo;

        /** 来源明细编号。 */
        @Schema(description = "来源明细编号")
        private Long sourceLineId;

        /** 来源明细行号。 */
        @Schema(description = "来源明细行号")
        private String sourceLineNo;

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

        /** 物料规格快照。 */
        @Schema(description = "物料规格快照")
        private String matSpec;

        /** 批次编号，为空时按批次号注册。 */
        @Schema(description = "批次编号")
        private Long batchId;

        /** 批次号快照，无批次物料使用默认批次。 */
        @Schema(description = "批次号快照")
        private String lotNo;

        /** 计量单位编号。 */
        @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "计量单位编号不能为空")
        private Long uomId;

        /** 计量单位编码快照。 */
        @Schema(description = "计量单位编码快照")
        private String uomCode;

        /** 目标库位编号。 */
        @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "目标库位编号不能为空")
        private Long locId;

        /** 目标库位编码快照。 */
        @Schema(description = "目标库位编码快照")
        private String locCode;

        /** 入库质量状态，为空沿用单据质量状态。 */
        @Schema(description = "入库质量状态")
        private String qualityStatus;

        /** 计划入库数量。 */
        @Schema(description = "计划入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.000000")
        @NotNull(message = "计划入库数量不能为空")
        private BigDecimal planQty;

        /** 容器编号。 */
        @Schema(description = "容器编号")
        private Long containerId;

        /** 供应商编号。 */
        @Schema(description = "供应商编号")
        private Long supplierId;

        /** 供应商编码快照。 */
        @Schema(description = "供应商编码快照")
        private String supplierCode;

        /** 供应商批次号。 */
        @Schema(description = "供应商批次号")
        private String supplierLotNo;

        /** 炉批号。 */
        @Schema(description = "炉批号")
        private String furnaceLotNo;

        /** 生产日期。 */
        @Schema(description = "生产日期")
        private LocalDate produceDate;

        /** 失效日期。 */
        @Schema(description = "失效日期")
        private LocalDate expiryDate;

        /** 备注。 */
        @Schema(description = "备注")
        private String remark;

    }

}
