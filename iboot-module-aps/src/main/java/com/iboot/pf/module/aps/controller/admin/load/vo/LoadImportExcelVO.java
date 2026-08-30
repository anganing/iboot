package com.iboot.pf.module.aps.controller.admin.load.vo;


import java.math.BigDecimal;
import java.time.LocalDate;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-资源负荷 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoadImportExcelVO {

    @ExcelProperty("排程版本编号")
    private Long versionId;

    @ExcelProperty("生产资源编号")
    private Long resId;

    @ExcelProperty("资源编码快照")
    private String resCode;

    @ExcelProperty("资源名称快照")
    private String resName;

    @ExcelProperty("负荷日期")
    private LocalDate workDate;

    @ExcelProperty("计划负荷分钟数")
    private Integer loadMinutes;

    @ExcelProperty("可用产能分钟数")
    private Integer capacityMinutes;

    @ExcelProperty("负荷率")
    private BigDecimal loadRate;

    @ExcelProperty("超负荷分钟数")
    private Integer overloadMinutes;

    @ExcelProperty("备注")
    private String remark;

}