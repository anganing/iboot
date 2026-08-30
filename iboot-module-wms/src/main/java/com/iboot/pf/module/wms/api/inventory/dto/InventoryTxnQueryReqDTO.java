package com.iboot.pf.module.wms.api.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 库存流水查询 DTO。为空的条件不过滤。 */
@Data
public class InventoryTxnQueryReqDTO {

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 物料编号。 */
    @Schema(description = "物料编号", example = "1")
    private Long matId;

    /** 批次编号。 */
    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    /** 事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST。 */
    @Schema(description = "事务类型", example = "RECEIPT")
    private String txnType;

    /** 来源业务类型。 */
    @Schema(description = "来源业务类型", example = "PO_IN")
    private String bizType;

    /** 来源业务单号。 */
    @Schema(description = "来源业务单号", example = "IN20260801001")
    private String bizNo;

    /** 过账时间起。 */
    @Schema(description = "过账时间起")
    private LocalDateTime fromTime;

    /** 过账时间止。 */
    @Schema(description = "过账时间止")
    private LocalDateTime toTime;

}
