package com.iboot.pf.module.wms.controller.admin.zone.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import com.iboot.pf.module.system.enums.DictTypeConstants;

@Schema(description = "管理后台 - WMS-库区 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ZoneRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库区编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库区编码")
    private String code;

    @Schema(description = "库区名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库区名称")
    private String name;

    @Schema(description = "库区类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "库区类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.ZONE_TYPE)
    private String zoneType;

    @Schema(description = "是否允许混放物料", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否允许混放物料")
    private Boolean allowMixedMat;

    @Schema(description = "是否允许混放批次", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否允许混放批次")
    private Boolean allowMixedLot;

    @Schema(description = "温区要求")
    @ExcelProperty("温区要求")
    private String tempRange;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注")
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

}
