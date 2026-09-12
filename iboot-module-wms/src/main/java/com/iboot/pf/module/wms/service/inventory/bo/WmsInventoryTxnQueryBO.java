package com.iboot.pf.module.wms.service.inventory.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;

/** 库存流水查询参数。为空的条件不过滤。 */
@Data
public class WmsInventoryTxnQueryBO {

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 仓库编号。 */
    private Long whId;

    /** 物料编号。 */
    private Long matId;

    /** 批次编号。 */
    private Long batchId;

    /** 批次编号集合，把"已知一批批次"的过滤下推到 SQL，避免按物料拉全量流水回内存再筛。 */
    private Collection<Long> batchIds;

    /** 批次号集合，按 {@code lot_no} 快照过滤，供只持有批次号的调用方使用。 */
    private Collection<String> lotNos;

    /** 事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST。 */
    private String txnType;

    /** 来源业务类型。 */
    private String bizType;

    /** 来源业务单号。 */
    private String bizNo;

    /** 过账时间起。 */
    private LocalDateTime fromTime;

    /** 过账时间止。 */
    private LocalDateTime toTime;

}
