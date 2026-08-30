package com.iboot.pf.module.aps.api.projection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 工单投影查询条件。 */
@Data
public class OrderProjectionQueryReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
    /** 来源类型：DEMAND、PROD_ORDER、OP_TASK。 */ private String sourceType;
    /** 生产工单编号。 */ private Long prodOrderId;
    /** 物料编号。 */ private Long matId;
    /** 当前状态。 */ private String currentStatus;
}
