package com.iboot.pf.module.aps.controller.admin.projectionorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-MES需求工单任务投影新增/修改 Request VO")
@Data
public class ProjectionOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17815")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30640")
    @NotNull(message = "排程场景编号不能为空")
    private Long scenarioId;

    @Schema(description = "来源类型：DEMAND、PROD_ORDER、OP_TASK", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "来源类型：DEMAND、PROD_ORDER、OP_TASK不能为空")
    private String sourceType;

    @Schema(description = "MES来源对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21689")
    @NotNull(message = "MES来源对象编号不能为空")
    private Long sourceId;

    @Schema(description = "MES来源对象单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "MES来源对象单号不能为空")
    private String sourceNo;

    @Schema(description = "生产工单编号，0表示尚未生成工单", requiredMode = Schema.RequiredMode.REQUIRED, example = "5497")
    @NotNull(message = "生产工单编号，0表示尚未生成工单不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0表示尚未生成作业工单", requiredMode = Schema.RequiredMode.REQUIRED, example = "6024")
    @NotNull(message = "作业工单编号，0表示尚未生成作业工单不能为空")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序投影", requiredMode = Schema.RequiredMode.REQUIRED, example = "17427")
    @NotNull(message = "工序任务编号，0表示非工序投影不能为空")
    private Long opTaskId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25416")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号，0表示未指定", requiredMode = Schema.RequiredMode.REQUIRED, example = "19093")
    @NotNull(message = "车间编号，0表示未指定不能为空")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "21577")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "4851")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23628")
    @NotNull(message = "生产物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "路线工序编号，0表示需求或整单", requiredMode = Schema.RequiredMode.REQUIRED, example = "9698")
    @NotNull(message = "路线工序编号，0表示需求或整单不能为空")
    private Long routeOpId;

    @Schema(description = "当前资源编号，0表示未分配", requiredMode = Schema.RequiredMode.REQUIRED, example = "15554")
    @NotNull(message = "当前资源编号，0表示未分配不能为空")
    private Long resId;

    @Schema(description = "计划数量或剩余数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划数量或剩余数量不能为空")
    private BigDecimal qty;

    @Schema(description = "已完成数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已完成数量快照不能为空")
    private BigDecimal completedQty;

    @Schema(description = "交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "交期不能为空")
    private LocalDateTime dueDate;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "来源对象状态快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "来源对象状态快照不能为空")
    private String currentStatus;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "是否已开工或业务锁定", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否已开工或业务锁定不能为空")
    private Boolean lockedFlag;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源数据更新时间不能为空")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "投影快照时间不能为空")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}