package com.iboot.pf.module.wms.controller.admin.containertype.vo;


import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-容器类型 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerTypeImportExcelVO {

    @ExcelProperty("容器类型编码")
    private String code;

    @ExcelProperty("容器类型名称")
    private String name;

    @ExcelProperty("标准长度，单位毫米")
    private BigDecimal stdLength;

    @ExcelProperty("标准宽度，单位毫米")
    private BigDecimal stdWidth;

    @ExcelProperty("标准高度，单位毫米")
    private BigDecimal stdHeight;

    @ExcelProperty("最大承重")
    private BigDecimal maxWeight;

    @ExcelProperty("最大容积")
    private BigDecimal maxVolume;

    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}