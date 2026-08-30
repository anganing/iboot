package com.iboot.pf.module.aps.api.projection.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** MDM 物料投影响应。 */
@Data
public class MaterialProjectionRespDTO {
    /** 投影编号。 */ private Long id;
    /** 场景编号。 */ private Long scenarioId;
    /** MDM 物料编号。 */ private Long sourceMatId;
    /** 物料编码。 */ private String matCode;
    /** 物料名称。 */ private String matName;
    /** 物料规格。 */ private String matSpec;
    /** 物料类型。 */ private String matType;
    /** 基准单位编号。 */ private Long baseUomId;
    /** 基准单位编码。 */ private String baseUomCode;
    /** 是否批次管控。 */ private Boolean batchFlag;
    /** 有效 BOM 编号。 */ private Long bomId;
    /** 有效工艺路线编号。 */ private Long routeId;
    /** 快照时间。 */ private LocalDateTime snapshotTime;
}
