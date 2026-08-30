package com.iboot.pf.module.aps.controller.admin.projectionorder.vo;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-MES需求工单任务投影 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectionOrderImportExcelVO {

    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @ExcelProperty("来源类型：DEMAND、PROD_ORDER、OP_TASK")
    private String sourceType;

    @ExcelProperty("MES来源对象编号")
    private Long sourceId;

    @ExcelProperty("MES来源对象单号")
    private String sourceNo;

    @ExcelProperty("生产工单编号，0表示尚未生成工单")
    private Long prodOrderId;

    @ExcelProperty("作业工单编号，0表示尚未生成作业工单")
    private Long workOrderId;

    @ExcelProperty("工序任务编号，0表示非工序投影")
    private Long opTaskId;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("车间编号，0表示未指定")
    private Long workshopId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("生产物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("路线工序编号，0表示需求或整单")
    private Long routeOpId;

    @ExcelProperty("当前资源编号，0表示未分配")
    private Long resId;

    @ExcelProperty("计划数量或剩余数量")
    private BigDecimal qty;

    @ExcelProperty("已完成数量快照")
    private BigDecimal completedQty;

    @ExcelProperty("交期")
    private LocalDateTime dueDate;

    @ExcelProperty("优先级")
    private Integer priority;

    @ExcelProperty("来源对象状态快照")
    private String currentStatus;

    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ExcelProperty("是否已开工或业务锁定")
    private Boolean lockedFlag;

    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("备注")
    private String remark;

}