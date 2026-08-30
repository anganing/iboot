package com.iboot.pf.module.wms.controller.admin.loc.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库位分页 Request VO")
@Data
public class LocPageReqVO extends PageParam {

    @Schema(description = "仓库编号", example = "21277")
    private Long whId;

    @Schema(description = "库区编号", example = "1961")
    private Long zoneId;

    @Schema(description = "库位编码")
    private String code;

    @Schema(description = "库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX", example = "2")
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

    @Schema(description = "尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM")
    private String dimensionCheckMode;

    @Schema(description = "状态：EMPTY、OCCUPIED、LOCKED、DISABLED", example = "2")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}