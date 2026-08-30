package com.iboot.pf.module.wms.dal.dataobject.transfer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 库存转移调拨单。 */
@TableName("wms_transfer_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsTransferOrderDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 转移或调拨单号。 */
    private String transferNo;

    /** 转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER。 */
    private String transferType;

    /** 来源业务类型。 */
    private String sourceType;

    /** 来源业务单号。 */
    private String sourceBizNo;

    /** 来源库存组织编号。 */
    private Long fromInvOrgId;

    /** 目标库存组织编号。 */
    private Long toInvOrgId;

    /** 来源仓库编号。 */
    private Long fromWhId;

    /** 目标仓库编号。 */
    private Long toWhId;

    /** 来源项目编号，0 表示公共库存。 */
    private Long fromProjectId;

    /** 目标项目编号，0 表示公共库存。 */
    private Long toProjectId;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    private String hookMode;

    /** 状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION。 */
    private String status;

    /** 创建幂等键。 */
    private String idempotencyKey;

    /** 备注。 */
    private String remark;

}
