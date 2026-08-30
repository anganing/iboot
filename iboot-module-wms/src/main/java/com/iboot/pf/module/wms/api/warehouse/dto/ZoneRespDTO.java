package com.iboot.pf.module.wms.api.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 库区响应 DTO。 */
@Data
public class ZoneRespDTO {

    /** 库区编号。 */
    @Schema(description = "库区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 库区编码。 */
    @Schema(description = "库区编码", example = "Z01")
    private String code;

    /** 库区名称。 */
    @Schema(description = "库区名称", example = "原材料区")
    private String name;

    /** 库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING。 */
    @Schema(description = "库区类型", example = "RAW")
    private String zoneType;

    /** 是否允许混放物料。 */
    @Schema(description = "是否允许混放物料", example = "false")
    private Boolean allowMixedMat;

    /** 是否允许混放批次。 */
    @Schema(description = "是否允许混放批次", example = "false")
    private Boolean allowMixedLot;

    /** 温区要求。 */
    @Schema(description = "温区要求")
    private String tempRange;

    /** 状态：0-启用，1-停用。 */
    @Schema(description = "状态", example = "0")
    private Integer status;

}
