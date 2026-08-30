package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-检验项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "12885")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25866")
    @ExcelProperty("检验方案编号")
    private Long planId;

    @Schema(description = "检验项编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验项编码")
    private String itemCode;

    @Schema(description = "检验项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("检验项名称")
    private String itemName;

    @Schema(description = "检验项类型：NUMERIC、TEXT、BOOLEAN", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("检验项类型：NUMERIC、TEXT、BOOLEAN")
    private String itemType;

    @Schema(description = "检验值单位")
    @ExcelProperty("检验值单位")
    private String unit;

    @Schema(description = "标准值")
    @ExcelProperty("标准值")
    private String stdValue;

    @Schema(description = "数值上限")
    @ExcelProperty("数值上限")
    private BigDecimal upperLimit;

    @Schema(description = "数值下限")
    @ExcelProperty("数值下限")
    private BigDecimal lowerLimit;

    @Schema(description = "是否必检", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否必检")
    private Boolean requiredFlag;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序号")
    private Integer sortNo;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "检验方案编码")
    @ExcelProperty("检验方案编码")
    private String planCode;

    @Schema(description = "检验方案名称")
    @ExcelProperty("检验方案名称")
    private String planName;

}