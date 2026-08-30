package com.iboot.pf.module.aps.api.projection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 物料投影查询条件。 */
@Data
public class MaterialProjectionQueryReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
    /** 来源物料编号。 */ private Long sourceMatId;
    /** 物料类型：RAW、SEMI、FIN、PACK、MRO。 */ private String matType;
}
