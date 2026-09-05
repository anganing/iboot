package com.iboot.pf.module.wms.api.outorder.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/** 创建出库单命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateOutOrderReqDTO extends WmsCommandReqDTO {

    /** 出库单号，为空由编码规则生成。 */
    @Schema(description = "出库单号")
    private String outNo;

    /** 出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT。 */
    @Schema(description = "出库业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROD_ISSUE_OUT")
    @NotNull(message = "出库业务类型不能为空")
    private String bizType;

    /** 原始来源类型。 */
    @Schema(description = "原始来源类型")
    private String originType;

    /** 原始来源单号。 */
    @Schema(description = "原始来源单号")
    private String originNo;

    /** 客户编号。 */
    @Schema(description = "客户编号")
    private Long customerId;

    /** 供应商编号。 */
    @Schema(description = "供应商编号")
    private Long supplierId;

    /** 生产工单号快照。 */
    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    /** 作业工单号快照。 */
    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    /** 工序任务编号。 */
    @Schema(description = "工序任务编号")
    private Long opTaskId;

    /** 领退补料申请单号。 */
    @Schema(description = "领退补料申请单号")
    private String materialReqNo;

    /** 转移或调拨单号。 */
    @Schema(description = "转移或调拨单号")
    private String transferNo;

    /** 盘点单号。 */
    @Schema(description = "盘点单号")
    private String countOrderNo;

    /** 来源库存组织编号。 */
    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库存组织编号不能为空")
    private Long invOrgId;

    /** 来源工厂编号。 */
    @Schema(description = "来源工厂编号")
    private Long plantId;

    /** 项目编号。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 项目任务编号。 */
    @Schema(description = "项目任务编号", example = "0")
    private Long projectTaskId;

    /** 来源仓库编号。 */
    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源仓库编号不能为空")
    private Long whId;

    /** 拣选策略编号。 */
    @Schema(description = "拣选策略编号")
    private Long pickStrategyId;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    @Schema(description = "外部设备触发方式", example = "NONE")
    private String hookMode;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

    /** 出库明细。 */
    @Schema(description = "出库明细")
    @Valid
    @NotEmpty(message = "出库明细不能为空")
    private List<Line> lines;

    /** 出库明细。 */
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

        /** 指定批次编号，为空表示不限批次。 */
        @Schema(description = "指定批次编号")
        private Long batchId;

        /** 批次号快照。 */
        @Schema(description = "批次号快照")
        private String lotNo;

        /** 计量单位编号。 */
        @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "计量单位编号不能为空")
        private Long uomId;

        /** 计量单位编码快照。 */
        @Schema(description = "计量单位编码快照")
        private String uomCode;

        /** 来源库位编号。 */
        @Schema(description = "来源库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "来源库位编号不能为空")
        private Long fromLocId;

        /** 来源库位编码快照。 */
        @Schema(description = "来源库位编码快照")
        private String fromLocCode;

        /** 要求的库存质量状态，为空按合格处理。 */
        @Schema(description = "要求的库存质量状态", example = "QUALIFIED")
        private String qualityStatus;

        /** 计划出库数量。 */
        @Schema(description = "计划出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.000000")
        @NotNull(message = "计划出库数量不能为空")
        private BigDecimal planQty;

        /** 容器编号。 */
        @Schema(description = "容器编号")
        private Long containerId;

        /**
         * 关联库存预留编号，0 或留空表示无预留。
         *
         * <p>MES 领料这类"先建预留再出库"的场景传入，出库过账后据此核销预留
         * （见 {@code WmsOutOrderServiceImpl.consumeReservation}）。不传的话预留永不核销，
         * 出库已扣现存而预留仍占着可用量，可用量会永久偏低，就是双重占用。
         *
         * <p>与内部 BO 的 {@code reservationId} 同名，由 {@code BeanUtils.toBean} 自动映射。
         */
        @Schema(description = "关联库存预留编号", example = "0")
        private Long reservationId;

        /** 备注。 */
        @Schema(description = "备注")
        private String remark;

    }

}
