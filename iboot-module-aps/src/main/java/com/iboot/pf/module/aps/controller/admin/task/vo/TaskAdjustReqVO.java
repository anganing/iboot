package com.iboot.pf.module.aps.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程任务人工调整 Request VO。
 *
 * <p>排程任务由求解器产出，人工只允许微调“排在哪台资源上、什么时候做、第几个做”。
 * 工单、工序、快照字段和状态都不在这里改：前者是投影带过来的事实，后者由业务动作推进。
 */
@Schema(description = "管理后台 - APS-排程任务调整 Request VO")
@Data
public class TaskAdjustReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排程任务编号不能为空")
    private Long id;

    @Schema(description = "排定生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产资源编号不能为空")
    private Long resId;

    @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "资源内执行顺序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "执行顺序号不能为空")
    @Min(value = 1, message = "执行顺序号不能小于 1")
    private Integer sequenceNo;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
