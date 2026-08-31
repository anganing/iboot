package com.iboot.pf.module.wms.controller.admin.containertype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import com.iboot.pf.module.system.enums.DictTypeConstants;

@Schema(description = "管理后台 - WMS-容器类型 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ContainerTypeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "容器类型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("容器类型编码")
    private String code;

    @Schema(description = "容器类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("容器类型名称")
    private String name;

    @Schema(description = "标准长度，单位毫米")
    @ExcelProperty("标准长度，单位毫米")
    private BigDecimal stdLength;

    @Schema(description = "标准宽度，单位毫米")
    @ExcelProperty("标准宽度，单位毫米")
    private BigDecimal stdWidth;

    @Schema(description = "标准高度，单位毫米")
    @ExcelProperty("标准高度，单位毫米")
    private BigDecimal stdHeight;

    @Schema(description = "最大承重")
    @ExcelProperty("最大承重")
    private BigDecimal maxWeight;

    @Schema(description = "最大容积")
    @ExcelProperty("最大容积")
    private BigDecimal maxVolume;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}