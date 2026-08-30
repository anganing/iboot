package com.iboot.pf.module.aps.controller.admin.task.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-工序排程任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskImportExcelVO {

    @ExcelProperty("排程版本编号")
    private Long versionId;

    @ExcelProperty("排程任务编号")
    private String taskNo;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @ExcelProperty("MES工序任务编号")
    private Long opTaskId;

    @ExcelProperty("路线工序编号")
    private Long routeOpId;

    @ExcelProperty("工序编码快照")
    private String opCode;

    @ExcelProperty("工序名称快照")
    private String opName;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("车间编号")
    private Long workshopId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("排定生产资源编号")
    private Long resId;

    @ExcelProperty("资源编码快照")
    private String resCode;

    @ExcelProperty("计划开始时间")
    private LocalDateTime startTime;

    @ExcelProperty("计划结束时间")
    private LocalDateTime endTime;

    @ExcelProperty("计划占用分钟数")
    private Integer durationMinutes;

    @ExcelProperty("资源内执行顺序号")
    private Integer sequenceNo;

    @ExcelProperty("是否锁定不可移动")
    private Boolean lockedFlag;

    @ExcelProperty("状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}