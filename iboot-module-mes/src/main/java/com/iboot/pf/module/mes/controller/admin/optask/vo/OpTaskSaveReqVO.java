package com.iboot.pf.module.mes.controller.admin.optask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-工序任务新增/修改 Request VO")
@Data
public class OpTaskSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "264")
    private Long id;

    @Schema(description = "工序任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工序任务号不能为空")
    private String taskNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24770")
    @NotNull(message = "作业工单编号不能为空")
    private Long workOrderId;

    @Schema(description = "作业工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "作业工单号快照不能为空")
    private String workOrderNo;

    @Schema(description = "锁定的路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24505")
    @NotNull(message = "锁定的路线工序编号不能为空")
    private Long routeOpId;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6706")
    @NotNull(message = "标准工序编号不能为空")
    private Long opId;

    @Schema(description = "工序编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工序编码快照不能为空")
    private String opCode;

    @Schema(description = "工序名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "工序名称快照不能为空")
    private String opName;

    @Schema(description = "工序顺序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工序顺序号不能为空")
    private Integer seq;

    @Schema(description = "并行工序组")
    private String parallelGroup;

    @Schema(description = "前序工序任务编号", example = "1542")
    private Long prevTaskId;

    @Schema(description = "后序工序任务编号", example = "26398")
    private Long nextTaskId;

    @Schema(description = "执行模式：SEQUENTIAL、PARALLEL、OPTIONAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "执行模式：SEQUENTIAL、PARALLEL、OPTIONAL不能为空")
    private String execMode;

    @Schema(description = "质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE不能为空")
    private String qcMode;

    @Schema(description = "跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED不能为空")
    private String skipPolicy;

    @Schema(description = "是否关键工序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否关键工序不能为空")
    private Boolean keyFlag;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划数量不能为空")
    private BigDecimal plannedQty;

    @Schema(description = "合格完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合格完成数量不能为空")
    private BigDecimal completedQty;

    @Schema(description = "报废数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报废数量不能为空")
    private BigDecimal scrapQty;

    @Schema(description = "当前分配资源编号", example = "25539")
    private Long resId;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED不能为空")
    private String status;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}