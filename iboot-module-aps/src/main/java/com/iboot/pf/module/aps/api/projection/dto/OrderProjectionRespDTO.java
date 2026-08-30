package com.iboot.pf.module.aps.api.projection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** MES 工单投影响应。 */
@Data
public class OrderProjectionRespDTO {
    /** 投影编号。 */ private Long id;
    /** 场景编号。 */ private Long scenarioId;
    /** 来源类型：DEMAND、PROD_ORDER、OP_TASK。 */ private String sourceType;
    /** 来源对象编号。 */ private Long sourceId;
    /** 来源单号。 */ private String sourceNo;
    /** 生产工单编号。 */ private Long prodOrderId;
    /** 工序任务编号。 */ private Long opTaskId;
    /** 工厂编号。 */ private Long plantId;
    /** 物料编号。 */ private Long matId;
    /** 计划或剩余数量。 */ private BigDecimal qty;
    /** 交期。 */ private LocalDateTime dueDate;
    /** 当前状态。 */ private String currentStatus;
    /** 是否锁定。 */ private Boolean lockedFlag;
    /** 快照时间。 */ private LocalDateTime snapshotTime;
}
