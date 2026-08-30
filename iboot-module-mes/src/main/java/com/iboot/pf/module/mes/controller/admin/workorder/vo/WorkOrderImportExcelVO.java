package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-作业工单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderImportExcelVO {

    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("车间编号")
    private Long workshopId;

    @ExcelProperty("工作中心编号")
    private Long wcId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("计划数量")
    private BigDecimal plannedQty;

    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ExcelProperty("齐套状态")
    private String kitStatus;

    @ExcelProperty("状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED")
    private String status;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}