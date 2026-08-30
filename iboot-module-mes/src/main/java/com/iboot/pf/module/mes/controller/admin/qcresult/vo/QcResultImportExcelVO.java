package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-检验结果 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QcResultImportExcelVO {

    @ExcelProperty("质检任务编号")
    private Long taskId;

    @ExcelProperty("检验项目编号")
    private Long itemId;

    @ExcelProperty("样本序号")
    private Integer sampleNo;

    @ExcelProperty("检验项编码快照")
    private String itemCode;

    @ExcelProperty("检验项名称快照")
    private String itemName;

    @ExcelProperty("实测值")
    private String actualValue;

    @ExcelProperty("是否合格")
    private Boolean passFlag;

    @ExcelProperty("检验员编号")
    private Long inspectorId;

    @ExcelProperty("检验时间")
    private LocalDateTime inspectTime;

    @ExcelProperty("本项判定合格数量")
    private BigDecimal qtyQualified;

    @ExcelProperty("本项判定不合格数量")
    private BigDecimal qtyRejected;

    @ExcelProperty("项目结论：PASS、FAIL")
    private String conclusion;

    @ExcelProperty("备注")
    private String remark;

}