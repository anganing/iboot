package com.iboot.pf.module.wms.dal.dataobject.inventory;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 库存流水。 */
@TableName("wms_inv_txn")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInvTxnDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 库存事务编号。 */
    private String txnNo;

    /** 事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST。 */
    private String txnType;

    /** 来源业务类型。 */
    private String bizType;

    /** 来源业务单号。 */
    private String bizNo;

    /** 来源业务行号。 */
    private String bizLineNo;

    /** 收发方向：IN、OUT、MOVE、STATUS。 */
    private String ioType;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 工厂编号。 */
    private Long plantId;

    /** 仓库编号。 */
    private Long whId;

    /** 来源库位编号，0 表示无来源库位。 */
    private Long fromLocId;

    /** 目标库位编号，0 表示无目标库位。 */
    private Long toLocId;

    /** 物料编号。 */
    private Long matId;

    /** 批次编号。 */
    private Long batchId;

    /** 容器编号，0 表示散件。 */
    private Long containerId;

    /** 基准计量单位编号。 */
    private Long uomId;

    /** 事务数量，使用正数。 */
    private BigDecimal qty;

    /** 过账前现存数量。 */
    private BigDecimal beforeQty;

    /** 过账后现存数量。 */
    private BigDecimal afterQty;

    /** 变更前质量状态。 */
    private String qualityStatusFrom;

    /** 变更后质量状态。 */
    private String qualityStatusTo;

    /** 变更前项目编号，0 表示无项目。 */
    private Long projectIdFrom;

    /** 变更前项目任务编号，0 表示无项目任务。 */
    private Long projectTaskIdFrom;

    /** 变更后项目编号，0 表示无项目。 */
    private Long projectIdTo;

    /** 变更后项目任务编号，0 表示无项目任务。 */
    private Long projectTaskIdTo;

    /** 物料编码快照。 */
    private String matCode;

    /** 物料名称快照。 */
    private String matName;

    /** 单位编码快照。 */
    private String uomCode;

    /** 批次号快照。 */
    private String lotNo;

    /** 来源库位编码快照。 */
    private String fromLocCode;

    /** 目标库位编码快照。 */
    private String toLocCode;

    /** 过账幂等键。 */
    private String idempotencyKey;

    /** 备注。 */
    private String remark;

}
