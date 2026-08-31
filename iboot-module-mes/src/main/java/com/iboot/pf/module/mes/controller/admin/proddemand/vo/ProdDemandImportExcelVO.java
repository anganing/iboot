package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产需求 Excel 导入 VO。
 *
 * <p>导入模板只收业务可读的编码，不收主键编号：让用户在 Excel 里填 matId、plantId 这种数据库主键既不可用，
 * 也绕过了外键校验。编码由 Service 解析成编号，解析不到直接记为该行失败。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdDemandImportExcelVO {

    @ExcelProperty("来源单号")
    private String sourceNo;

    @ExcelProperty("工厂编码")
    private String plantCode;

    @ExcelProperty("项目编码")
    private String projectCode;

    @ExcelProperty("物料编码")
    private String matCode;

    @ExcelProperty("需求数量")
    private BigDecimal qty;

    @ExcelProperty("计量单位编码")
    private String uomCode;

    @ExcelProperty("需求交期")
    private LocalDateTime dueDate;

    @ExcelProperty("优先级")
    private Integer priority;

    @ExcelProperty("备注")
    private String remark;

}
