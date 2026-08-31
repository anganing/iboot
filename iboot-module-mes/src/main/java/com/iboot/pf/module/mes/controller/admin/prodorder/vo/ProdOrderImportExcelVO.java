package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产工单 Excel 导入 VO。
 *
 * <p>模板只收业务可读编码，编号由 Service 解析；BOM 与工艺路线按物料的生效版本自动锁定，不让用户填编号。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdOrderImportExcelVO {

    @ExcelProperty("工厂编码")
    private String plantCode;

    @ExcelProperty("车间编码")
    private String workshopCode;

    @ExcelProperty("项目编码")
    private String projectCode;

    @ExcelProperty("物料编码")
    private String matCode;

    @ExcelProperty("计划数量")
    private BigDecimal qty;

    @ExcelProperty("计量单位编码")
    private String uomCode;

    @ExcelProperty("工单交期")
    private LocalDateTime dueDate;

    @ExcelProperty("优先级")
    private Integer priority;

    @ExcelProperty("备注")
    private String remark;

}
