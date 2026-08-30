package com.iboot.pf.module.wms.dal.dataobject.inorder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 入库单。 */
@TableName("wms_in_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInOrderDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 入库单号。 */
    private String inNo;

    /** 入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN。 */
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

    /** 到货登记编号。 */
    private Long arrivalId;

    /** 供应商编号。 */
    private Long supplierId;

    /** 客户编号。 */
    private Long customerId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 作业工单号快照。 */
    private String workOrderNo;

    /** 转移或调拨单号。 */
    private String transferNo;

    /** 盘点单号。 */
    private String countOrderNo;

    /** 目标库存组织编号。 */
    private Long invOrgId;

    /** 目标工厂编号。 */
    private Long plantId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 目标仓库编号。 */
    private Long whId;

    /** 目标仓库编码快照。 */
    private String whCode;

    /** 来源库存组织编号，0 表示非调拨。 */
    private Long fromInvOrgId;

    /** 来源仓库编号，0 表示非调拨。 */
    private Long fromWhId;

    /** 是否需要质检。 */
    private Boolean needQc;

    /** 来料检验模式。 */
    private String iqcMode;

    /** 入库质量状态。 */
    private String qualityStatus;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    private String hookMode;

    /** 状态：DRAFT、SUBMITTED、QC_PENDING、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION。 */
    private String status;

    /** 创建幂等键。 */
    private String idempotencyKey;

    /** 低频扩展属性。 */
    private String bizAttrsJson;

    /** 备注或人工原因。 */
    private String remark;

}
