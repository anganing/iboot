package com.iboot.pf.module.wms.dal.dataobject.lock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 库存冻结记录。 */
@TableName("wms_inv_lock")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInvLockDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 冻结编号。 */
    private String lockNo;

    /** 来源业务类型。 */
    private String bizType;

    /** 来源业务单号。 */
    private String bizNo;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 仓库编号。 */
    private Long whId;

    /** 库位编号。 */
    private Long locId;

    /** 物料编号。 */
    private Long matId;

    /** 批次编号。 */
    private Long batchId;

    /** 容器编号，0 表示散件。 */
    private Long containerId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 质量状态。 */
    private String qualityStatus;

    /** 冻结数量，基准单位。 */
    private BigDecimal qty;

    /** 已解冻数量。 */
    private BigDecimal releasedQty;

    /** 冻结原因。 */
    private String reason;

    /** 状态：ACTIVE、PARTIAL、RELEASED、CANCELLED。 */
    private String status;

    /** 冻结幂等键。 */
    private String idempotencyKey;

    /** 备注。 */
    private String remark;

}
