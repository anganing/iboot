package com.iboot.pf.module.wms.service.lock.bo;

import lombok.Data;

import java.math.BigDecimal;

/** 冻结库存的内部参数。 */
@Data
public class WmsLockBO {

    /** 冻结幂等键，格式：sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;

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

    /** 容器编号。 */
    private Long containerId;

    /** 项目编号。 */
    private Long projectId;

    /** 项目任务编号。 */
    private Long projectTaskId;

    /** 质量状态，为空按合格处理。 */
    private String qualityStatus;

    /** 冻结数量，基准单位。 */
    private BigDecimal qty;

    /** 冻结原因。 */
    private String reason;

    /** 备注。 */
    private String remark;

}
