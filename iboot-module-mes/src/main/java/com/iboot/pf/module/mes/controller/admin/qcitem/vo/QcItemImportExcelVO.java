package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-检验项目 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QcItemImportExcelVO {

    @ExcelProperty("检验方案编号")
    private Long planId;

    @ExcelProperty("检验项编码")
    private String itemCode;

    @ExcelProperty("检验项名称")
    private String itemName;

    @ExcelProperty("检验项类型：NUMERIC、TEXT、BOOLEAN")
    private String itemType;

    @ExcelProperty("检验值单位")
    private String unit;

    @ExcelProperty("标准值")
    private String stdValue;

    @ExcelProperty("数值上限")
    private BigDecimal upperLimit;

    @ExcelProperty("数值下限")
    private BigDecimal lowerLimit;

    @ExcelProperty("是否必检")
    private Boolean requiredFlag;

    @ExcelProperty("排序号")
    private Integer sortNo;

    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}