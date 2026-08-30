package com.iboot.pf.module.mes.controller.admin.rework.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-返工任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReworkImportExcelVO {

    @ExcelProperty("返工任务编号")
    private String reworkNo;

    @ExcelProperty("来源质检任务编号")
    private Long sourceQcTaskId;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @ExcelProperty("返工起始工序任务编号")
    private Long opTaskId;

    @ExcelProperty("返工数量")
    private BigDecimal qty;

    @ExcelProperty("返工工艺路线编号")
    private Long routeId;

    @ExcelProperty("返工工艺路线版本")
    private String routeVersion;

    @ExcelProperty("状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}