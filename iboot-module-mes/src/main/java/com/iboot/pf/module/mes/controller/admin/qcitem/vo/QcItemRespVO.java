package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 检验项目 Response VO。方案编码与名称来自模块内 JOIN。 */
@Schema(description = "管理后台 - MES-检验项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long planId;

    @Schema(description = "检验方案编码")
    @ExcelProperty("方案编码")
    private String planCode;

    @Schema(description = "检验方案名称")
    @ExcelProperty("方案名称")
    private String planName;

    @Schema(description = "检验项编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验项编码")
    private String itemCode;

    @Schema(description = "检验项名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验项名称")
    private String itemName;

    @Schema(description = "检验项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "NUMERIC")
    @ExcelProperty(value = "检验项类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_ITEM_TYPE)
    private String itemType;

    @Schema(description = "检验值单位")
    @ExcelProperty("单位")
    private String unit;

    @Schema(description = "标准值")
    @ExcelProperty("标准值")
    private String stdValue;

    @Schema(description = "数值上限", example = "1")
    @ExcelProperty("上限")
    private BigDecimal upperLimit;

    @Schema(description = "数值下限", example = "1")
    @ExcelProperty("下限")
    private BigDecimal lowerLimit;

    @Schema(description = "是否必检", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty(value = "是否必检", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BOOLEAN_STRING)
    private Boolean requiredFlag;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("排序号")
    private Integer sortNo;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
