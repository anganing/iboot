package com.iboot.pf.module.aps.api.projection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 库存投影查询条件。 */
@Data
public class InventoryProjectionQueryReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
    /** 物料编号。 */ private Long matId;
    /** 工厂编号。 */ private Long plantId;
    /** 项目编号。 */ private Long projectId;
    /** 质量状态。 */ private String qualityStatus;
}
