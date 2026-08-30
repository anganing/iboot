package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-作业工单新增/修改 Request VO")
@Data
public class WorkOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25812")
    private Long id;

    @Schema(description = "作业工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "作业工单号不能为空")
    private String workOrderNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "88")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "生产工单号快照不能为空")
    private String prodOrderNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6008")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23163")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "400")
    @NotNull(message = "工作中心编号不能为空")
    private Long wcId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "2212")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "3019")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划数量不能为空")
    private BigDecimal plannedQty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已完成数量不能为空")
    private BigDecimal completedQty;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "齐套状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "齐套状态不能为空")
    private String kitStatus;

    @Schema(description = "状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}