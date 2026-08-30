package com.iboot.pf.module.wms.dal.dataobject.outorder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 出库单。 */
@TableName("wms_out_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsOutOrderDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 出库单号。 */
    private String outNo;

    /** 出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT。 */
    private String bizType;

    /** 直接来源类型。 */
    private String sourceType;

    /** 直接来源业务单号。 */
    private String sourceBizNo;

    /** 直接来源明细编号。 */
    private Long sourceLineId;

    /** 原始来源类型。 */
    private String originType;

    /** 原始来源单号。 */
    private String originNo;

    /** 客户编号。 */
    private Long customerId;

    /** 供应商编号。 */
    private Long supplierId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 作业工单号快照。 */
    private String workOrderNo;

    /** 工序任务编号。 */
    private Long opTaskId;

    /** 领退补料申请单号。 */
    private String materialReqNo;

    /** 转移或调拨单号。 */
    private String transferNo;

    /** 盘点单号。 */
    private String countOrderNo;

    /** 来源库存组织编号。 */
    private Long invOrgId;

    /** 来源工厂编号。 */
    private Long plantId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 来源仓库编号。 */
    private Long whId;

    /** 来源仓库编码快照。 */
    private String whCode;

    /** 目标库存组织编号，0 表示非调拨。 */
    private Long toInvOrgId;

    /** 目标仓库编号，0 表示非调拨。 */
    private Long toWhId;

    /** 拣选策略编号。 */
    private Long pickStrategyId;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    private String hookMode;

    /** 状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION。 */
    private String status;

    /** 创建幂等键。 */
    private String idempotencyKey;

    /** 低频扩展属性。 */
    private String bizAttrsJson;

    /** 备注或人工原因。 */
    private String remark;

}
