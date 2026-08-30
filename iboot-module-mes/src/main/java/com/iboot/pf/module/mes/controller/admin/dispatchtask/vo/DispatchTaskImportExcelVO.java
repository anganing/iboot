package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-派工任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispatchTaskImportExcelVO {

    @ExcelProperty("派工单号")
    private String dispatchNo;

    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @ExcelProperty("作业人员编号")
    private Long operatorId;

    @ExcelProperty("作业班组编号")
    private Long teamId;

    @ExcelProperty("生产资源编号")
    private Long resId;

    @ExcelProperty("班次编号")
    private Long shiftId;

    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @ExcelProperty("状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}