package com.iboot.pf.module.aps.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-工序排程任务新增/修改 Request VO")
@Data
public class TaskSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15753")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    @NotNull(message = "排程版本编号不能为空")
    private Long versionId;

    @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排程任务编号不能为空")
    private String taskNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29717")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "生产工单号快照不能为空")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27462")
    @NotNull(message = "作业工单编号不能为空")
    private Long workOrderId;

    @Schema(description = "MES工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21603")
    @NotNull(message = "MES工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1889")
    @NotNull(message = "路线工序编号不能为空")
    private Long routeOpId;

    @Schema(description = "工序编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工序编码快照不能为空")
    private String opCode;

    @Schema(description = "工序名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "工序名称快照不能为空")
    private String opName;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24208")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25008")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "31175")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "5280")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "排定生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20629")
    @NotNull(message = "排定生产资源编号不能为空")
    private Long resId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "资源编码快照不能为空")
    private String resCode;

    @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "计划占用分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划占用分钟数不能为空")
    private Integer durationMinutes;

    @Schema(description = "资源内执行顺序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源内执行顺序号不能为空")
    private Integer sequenceNo;

    @Schema(description = "是否锁定不可移动", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否锁定不可移动不能为空")
    private Boolean lockedFlag;

    @Schema(description = "状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}