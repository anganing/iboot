package com.iboot.pf.module.wms.controller.admin.loc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-库位 Response VO")
@Data
@ExcelIgnoreUnannotated
public class LocRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26755")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21277")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1961")
    @ExcelProperty("库区编号")
    private Long zoneId;

    @Schema(description = "库位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库位编码")
    private String code;

    @Schema(description = "库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX")
    private String locType;

    @Schema(description = "巷道号")
    @ExcelProperty("巷道号")
    private String aisleNo;

    @Schema(description = "排号")
    @ExcelProperty("排号")
    private String rowNo;

    @Schema(description = "列号")
    @ExcelProperty("列号")
    private String colNo;

    @Schema(description = "层号")
    @ExcelProperty("层号")
    private String levelNo;

    @Schema(description = "纵深号")
    @ExcelProperty("纵深号")
    private Integer depthNo;

    @Schema(description = "容器最大长度，单位毫米")
    @ExcelProperty("容器最大长度，单位毫米")
    private BigDecimal maxLength;

    @Schema(description = "容器最大宽度，单位毫米")
    @ExcelProperty("容器最大宽度，单位毫米")
    private BigDecimal maxWidth;

    @Schema(description = "容器最大高度，单位毫米")
    @ExcelProperty("容器最大高度，单位毫米")
    private BigDecimal maxHeight;

    @Schema(description = "最大承重")
    @ExcelProperty("最大承重")
    private BigDecimal maxWeight;

    @Schema(description = "最大容积")
    @ExcelProperty("最大容积")
    private BigDecimal maxVolume;

    @Schema(description = "最大容器数量")
    @ExcelProperty("最大容器数量")
    private Integer maxContainerQty;

    @Schema(description = "尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM")
    private String dimensionCheckMode;

    @Schema(description = "状态：EMPTY、OCCUPIED、LOCKED、DISABLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：EMPTY、OCCUPIED、LOCKED、DISABLED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "仓库编码")
    @ExcelProperty("仓库编码")
    private String whCode;

    @Schema(description = "仓库名称")
    @ExcelProperty("仓库名称")
    private String whName;

    @Schema(description = "库区编码")
    @ExcelProperty("库区编码")
    private String zoneCode;

    @Schema(description = "库区名称")
    @ExcelProperty("库区名称")
    private String zoneName;

}