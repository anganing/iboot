package com.iboot.pf.module.mes.controller.admin.defect.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-缺陷记录 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DefectImportExcelVO {

    @ExcelProperty("质检任务编号")
    private Long taskId;

    @ExcelProperty("关联检验结果编号")
    private Long resultId;

    @ExcelProperty("缺陷编码")
    private String defectCode;

    @ExcelProperty("缺陷名称")
    private String defectName;

    @ExcelProperty("缺陷等级：CRITICAL、MAJOR、MINOR")
    private String defectLevel;

    @ExcelProperty("缺陷数量")
    private BigDecimal qty;

    @ExcelProperty("缺陷描述")
    private String description;

    @ExcelProperty("缺陷位置")
    private String location;

    @ExcelProperty("缺陷附件地址列表")
    private String attachmentUrls;

    @ExcelProperty("备注")
    private String remark;

}