package com.iboot.pf.module.wms.service.inventory.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存过账参数。
 *
 * <p>过账是库存变动的唯一入口：先按 {@link #idempotencyKey} 判重，再写 {@code wms_inv_txn}，
 * 最后按版本号更新 {@code wms_inv_balance}，三步在同一事务内完成。
 */
@Data
public class WmsInvPostBO {

    /** 事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST。 */
    private String txnType;

    /**
     * 收发方向：IN、OUT、MOVE、STATUS。
     * 为空时按 {@link #txnType} 推导，盘点调整这类同一事务类型既有盘盈又有盘亏的场景必须显式指定。
     */
    private String ioType;

    /** 来源业务类型。 */
    private String bizType;

    /** 来源业务单号。 */
    private String bizNo;

    /** 来源业务行号。 */
    private String bizLineNo;

    /** 过账幂等键，重复过账直接返回原流水。 */
    private String idempotencyKey;

    /** 过账数量，正数。 */
    private BigDecimal qty;

    // ========== 来源维度（OUT / MOVE 使用） ==========
    /**
     * 来源库存组织编号，为空沿用 {@link #invOrgId}。
     * 跨库存组织调拨（INV_ORG_TRANSFER）必须显式指定。
     */
    private Long fromInvOrgId;
    /** 来源工厂编号，为空沿用 {@link #plantId}。 */
    private Long fromPlantId;
    /** 来源仓库编号，为空沿用 {@link #whId}。跨仓库调拨（WH_TRANSFER）必须显式指定。 */
    private Long fromWhId;
    /** 来源库位编号。 */
    private Long fromLocId;
    /** 来源库位编码快照。 */
    private String fromLocCode;
    /** 来源质量状态。 */
    private String qualityStatusFrom;
    /** 来源库存范围。 */
    private String fromStockScope;
    /** 来源项目编号。 */
    private Long projectIdFrom;
    /** 来源项目任务编号。 */
    private Long projectTaskIdFrom;

    // ========== 目标维度（IN / MOVE 使用） ==========
    /** 目标库存组织编号，为空沿用 {@link #invOrgId}。 */
    private Long toInvOrgId;
    /** 目标工厂编号，为空沿用 {@link #plantId}。 */
    private Long toPlantId;
    /** 目标仓库编号，为空沿用 {@link #whId}。 */
    private Long toWhId;
    /** 目标库位编号。 */
    private Long toLocId;
    /** 目标库位编码快照。 */
    private String toLocCode;
    /** 目标质量状态。 */
    private String qualityStatusTo;
    /** 目标库存范围。 */
    private String toStockScope;
    /** 目标项目编号。 */
    private Long projectIdTo;
    /** 目标项目任务编号。 */
    private Long projectTaskIdTo;

    // ========== 公共维度 ==========
    /** 库存组织编号。 */
    private Long invOrgId;
    /** 工厂编号。 */
    private Long plantId;
    /** 仓库编号。 */
    private Long whId;
    /** 物料编号。 */
    private Long matId;
    /** 批次编号。 */
    private Long batchId;
    /** 容器编号，0 表示散件。 */
    private Long containerId;
    /** 基准计量单位编号。 */
    private Long uomId;
    /** 物料编码快照。 */
    private String matCode;
    /** 物料名称快照。 */
    private String matName;
    /** 基准单位编码快照。 */
    private String uomCode;
    /** 批次号快照。 */
    private String lotNo;

    /** 备注。 */
    private String remark;

}
