package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-派工任务新增/修改 Request VO")
@Data
public class DispatchTaskSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5865")
    private Long id;

    @Schema(description = "派工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "派工单号不能为空")
    private String dispatchNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3251")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "作业人员编号", example = "6062")
    private Long operatorId;

    @Schema(description = "作业班组编号", example = "21197")
    private Long teamId;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6033")
    @NotNull(message = "生产资源编号不能为空")
    private Long resId;

    @Schema(description = "班次编号", example = "12378")
    private Long shiftId;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}