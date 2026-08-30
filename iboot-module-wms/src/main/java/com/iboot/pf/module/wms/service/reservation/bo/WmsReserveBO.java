package com.iboot.pf.module.wms.service.reservation.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 预留库存的内部参数。 */
@Data
public class WmsReserveBO {

    /** 预留幂等键，格式：sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;

    /** 来源业务类型。 */
    private String bizType;

    /** 来源业务单号。 */
    private String bizNo;

    /** 来源业务行号。 */
    private String bizLineNo;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 仓库编号。 */
    private Long whId;

    /** 库位编号，为空表示不限库位。 */
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

    /** 预留数量，基准单位。 */
    private BigDecimal qty;

    /** 预留失效时间，为空表示不失效。 */
    private LocalDateTime expireTime;

    /** 备注。 */
    private String remark;

}
