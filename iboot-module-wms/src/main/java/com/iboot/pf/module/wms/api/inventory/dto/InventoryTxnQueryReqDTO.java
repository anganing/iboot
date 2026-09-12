package com.iboot.pf.module.wms.api.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;

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

    /**
     * 批次编号集合。
     *
     * <p>追溯这类"已知一批批次、要取它们全部流水"的场景用这个条件把过滤下推到 SQL。
     * 只给 {@code matId} 时，单物料流水量大会先把整段流水拉回内存再筛，拉取量随时间只增不减。
     * 与 {@code batchId} 同时给出时两个条件相与。
     */
    @Schema(description = "批次编号集合")
    private Collection<Long> batchIds;

    /**
     * 批次号集合（按 {@code lot_no} 快照过滤）。
     *
     * <p>调用方只持有批次号（外部单据、追溯入口输入）时用这个条件。注意批次号只在物料内唯一
     * （{@code wms_batch} 的唯一索引是 {@code mat_id + lot_no}），跨物料撞号时这个条件会把别的
     * 物料一起带回来，必须同时给 {@code matId} / {@code matIds} 或自行按物料甄别；能拿到批次编号
     * 时一律优先用 {@code batchIds}。
     *
     * <p>本 DTO 的条件一律"为空不过滤、有值相与"，与 {@code batchIds} 同时给出时取交集。
     */
    @Schema(description = "批次号集合")
    private Collection<String> lotNos;

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
