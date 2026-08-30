package com.iboot.pf.module.wms.controller.admin.loc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-库位新增/修改 Request VO")
@Data
public class LocSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26755")
    private Long id;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21277")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "库区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1961")
    @NotNull(message = "库区编号不能为空")
    private Long zoneId;

    @Schema(description = "库位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库位编码不能为空")
    private String code;

    @Schema(description = "库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX不能为空")
    private String locType;

    @Schema(description = "巷道号")
    private String aisleNo;

    @Schema(description = "排号")
    private String rowNo;

    @Schema(description = "列号")
    private String colNo;

    @Schema(description = "层号")
    private String levelNo;

    @Schema(description = "纵深号")
    private Integer depthNo;

    @Schema(description = "容器最大长度，单位毫米")
    private BigDecimal maxLength;

    @Schema(description = "容器最大宽度，单位毫米")
    private BigDecimal maxWidth;

    @Schema(description = "容器最大高度，单位毫米")
    private BigDecimal maxHeight;

    @Schema(description = "最大承重")
    private BigDecimal maxWeight;

    @Schema(description = "最大容积")
    private BigDecimal maxVolume;

    @Schema(description = "最大容器数量")
    private Integer maxContainerQty;

    @Schema(description = "尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM不能为空")
    private String dimensionCheckMode;

    @Schema(description = "状态：EMPTY、OCCUPIED、LOCKED、DISABLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：EMPTY、OCCUPIED、LOCKED、DISABLED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}