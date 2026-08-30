package com.iboot.pf.module.wms.api.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 库存流水响应 DTO。 */
@Data
public class InventoryTxnRespDTO {

    /** 库存流水编号。 */
    @Schema(description = "库存流水编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 库存事务编号。 */
    @Schema(description = "库存事务编号", example = "TXN001")
    private String txnNo;

    /** 事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST。 */
    @Schema(description = "事务类型", example = "RECEIPT")
    private String txnType;

    /** 来源业务类型。 */
    @Schema(description = "来源业务类型", example = "PO_IN")
    private String bizType;

    /** 来源业务单号。 */
    @Schema(description = "来源业务单号", example = "IN20260801001")
    private String bizNo;

    /** 来源业务行号。 */
    @Schema(description = "来源业务行号", example = "10")
    private String bizLineNo;

    /** 收发方向：IN、OUT、MOVE、STATUS。 */
    @Schema(description = "收发方向", example = "IN")
    private String ioType;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 来源库位编号。 */
    @Schema(description = "来源库位编号", example = "0")
    private Long fromLocId;

    /** 来源库位编码快照。 */
    @Schema(description = "来源库位编码快照")
    private String fromLocCode;

    /** 目标库位编号。 */
    @Schema(description = "目标库位编号", example = "0")
    private Long toLocId;

    /** 目标库位编码快照。 */
    @Schema(description = "目标库位编码快照")
    private String toLocCode;

    /** 物料编号。 */
    @Schema(description = "物料编号", example = "1")
    private Long matId;

    /** 物料编码快照。 */
    @Schema(description = "物料编码快照", example = "M001")
    private String matCode;

    /** 物料名称快照。 */
    @Schema(description = "物料名称快照", example = "钢板")
    private String matName;

    /** 批次编号。 */
    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    /** 批次号快照。 */
    @Schema(description = "批次号快照", example = "LOT20260801")
    private String lotNo;

    /** 容器编号，0 表示散件。 */
    @Schema(description = "容器编号", example = "0")
    private Long containerId;

    /** 事务数量，使用正数。 */
    @Schema(description = "事务数量", example = "100.000000")
    private BigDecimal qty;

    /** 过账前现存数量。 */
    @Schema(description = "过账前现存数量")
    private BigDecimal beforeQty;

    /** 过账后现存数量。 */
    @Schema(description = "过账后现存数量")
    private BigDecimal afterQty;

    /** 变更前质量状态。 */
    @Schema(description = "变更前质量状态")
    private String qualityStatusFrom;

    /** 变更后质量状态。 */
    @Schema(description = "变更后质量状态")
    private String qualityStatusTo;

    /** 单位编码快照。 */
    @Schema(description = "单位编码快照", example = "KG")
    private String uomCode;

    /** 过账时间。 */
    @Schema(description = "过账时间")
    private LocalDateTime createTime;

}
