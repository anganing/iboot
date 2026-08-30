package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-生产报工记录 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdReportImportExcelVO {

    @ExcelProperty("报工单号")
    private String reportNo;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @ExcelProperty("报工作业人员编号")
    private Long workerId;

    @ExcelProperty("生产资源编号")
    private Long resId;

    @ExcelProperty("班次编号")
    private Long shiftId;

    @ExcelProperty("合格数量")
    private BigDecimal qtyGood;

    @ExcelProperty("报废数量")
    private BigDecimal qtyScrap;

    @ExcelProperty("返工数量")
    private BigDecimal qtyRework;

    @ExcelProperty("本次作业开始时间")
    private LocalDateTime startTime;

    @ExcelProperty("本次作业结束时间")
    private LocalDateTime endTime;

    @ExcelProperty("报工时间")
    private LocalDateTime reportTime;

    @ExcelProperty("状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED")
    private String status;

    @ExcelProperty("报工幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}