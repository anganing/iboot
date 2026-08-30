package com.iboot.pf.module.aps.controller.admin.projectionorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 工单投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17815")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30640")
    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @Schema(description = "来源类型：DEMAND、PROD_ORDER、OP_TASK", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("来源类型：DEMAND、PROD_ORDER、OP_TASK")
    private String sourceType;

    @Schema(description = "MES来源对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21689")
    @ExcelProperty("MES来源对象编号")
    private Long sourceId;

    @Schema(description = "MES来源对象单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("MES来源对象单号")
    private String sourceNo;

    @Schema(description = "生产工单编号，0表示尚未生成工单", requiredMode = Schema.RequiredMode.REQUIRED, example = "5497")
    @ExcelProperty("生产工单编号，0表示尚未生成工单")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0表示尚未生成作业工单", requiredMode = Schema.RequiredMode.REQUIRED, example = "6024")
    @ExcelProperty("作业工单编号，0表示尚未生成作业工单")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序投影", requiredMode = Schema.RequiredMode.REQUIRED, example = "17427")
    @ExcelProperty("工序任务编号，0表示非工序投影")
    private Long opTaskId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25416")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "车间编号，0表示未指定", requiredMode = Schema.RequiredMode.REQUIRED, example = "19093")
    @ExcelProperty("车间编号，0表示未指定")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "21577")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "4851")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23628")
    @ExcelProperty("生产物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "路线工序编号，0表示需求或整单", requiredMode = Schema.RequiredMode.REQUIRED, example = "9698")
    @ExcelProperty("路线工序编号，0表示需求或整单")
    private Long routeOpId;

    @Schema(description = "当前资源编号，0表示未分配", requiredMode = Schema.RequiredMode.REQUIRED, example = "15554")
    @ExcelProperty("当前资源编号，0表示未分配")
    private Long resId;

    @Schema(description = "计划数量或剩余数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划数量或剩余数量")
    private BigDecimal qty;

    @Schema(description = "已完成数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已完成数量快照")
    private BigDecimal completedQty;

    @Schema(description = "交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "来源对象状态快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("来源对象状态快照")
    private String currentStatus;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "是否已开工或业务锁定", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否已开工或业务锁定")
    private Boolean lockedFlag;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景名称")
    private String scenarioName;

}