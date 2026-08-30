package com.iboot.pf.module.wms.api.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 库位响应 DTO。 */
@Data
public class LocRespDTO {

    /** 库位编号。 */
    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 库区编号。 */
    @Schema(description = "库区编号", example = "1")
    private Long zoneId;

    /** 库位编码。 */
    @Schema(description = "库位编码", example = "A-01-01")
    private String code;

    /** 库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX。 */
    @Schema(description = "库位类型", example = "ASRS_CELL")
    private String locType;

    /** 巷道号。 */
    @Schema(description = "巷道号")
    private String aisleNo;

    /** 排号。 */
    @Schema(description = "排号")
    private String rowNo;

    /** 列号。 */
    @Schema(description = "列号")
    private String colNo;

    /** 层号。 */
    @Schema(description = "层号")
    private String levelNo;

    /** 尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM。 */
    @Schema(description = "尺寸校验模式", example = "HEIGHT_ONLY")
    private String dimensionCheckMode;

    /** 状态：EMPTY、OCCUPIED、LOCKED、DISABLED。 */
    @Schema(description = "状态", example = "EMPTY")
    private String status;

}
