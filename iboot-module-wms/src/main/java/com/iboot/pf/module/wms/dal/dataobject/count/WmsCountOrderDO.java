package com.iboot.pf.module.wms.dal.dataobject.count;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/** 盘点单。 */
@TableName("wms_count_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsCountOrderDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 盘点单号。 */
    private String countNo;

    /** 盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC。 */
    private String countType;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 仓库编号。 */
    private Long whId;

    /** 库区编号，0 表示全部库区。 */
    private Long zoneId;

    /** 项目编号，0 表示不限项目。 */
    private Long projectId;

    /** 账面快照时间。 */
    private LocalDateTime snapshotTime;

    /** 是否盲盘。 */
    private Boolean blindCountFlag;

    /** 状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED。 */
    private String status;

    /** 创建幂等键。 */
    private String idempotencyKey;

    /** 备注。 */
    private String remark;

    /** 审批状态，取值见 BpmProcessInstanceStatusEnum。 */
    private Integer approvalStatus;
    /** 流程实例编号。 */
    private String processInstanceId;
    /** 审批完成时间。 */
    private LocalDateTime approvalTime;
    /** 审批意见或驳回原因。 */
    private String approvalReason;

}
