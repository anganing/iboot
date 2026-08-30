package com.iboot.pf.module.mes.controller.admin.rgvtask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-RGV工位流转任务新增/修改 Request VO")
@Data
public class RgvTaskSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "238")
    private Long id;

    @Schema(description = "RGV本地任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "RGV本地任务号不能为空")
    private String taskNo;

    @Schema(description = "RGV外部任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN不能为空")
    private String taskType;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30010")
    @NotNull(message = "作业工单编号不能为空")
    private Long workOrderId;

    @Schema(description = "来源工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31323")
    @NotNull(message = "来源工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "目标工序任务编号", example = "13527")
    private Long targetOpTaskId;

    @Schema(description = "来源工位或资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12326")
    @NotNull(message = "来源工位或资源编号不能为空")
    private Long fromStationId;

    @Schema(description = "目标工位或资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23897")
    @NotNull(message = "目标工位或资源编号不能为空")
    private Long toStationId;

    @Schema(description = "载具编号")
    private String carrierNo;

    @Schema(description = "外部调度系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部调度系统编码不能为空")
    private String externalSystemCode;

    @Schema(description = "下发状态：PENDING、SENT、FAILED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "下发状态：PENDING、SENT、FAILED不能为空")
    private String dispatchStatus;

    @Schema(description = "执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION不能为空")
    private String status;

    @Schema(description = "业务幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "业务幂等键不能为空")
    private String idempotencyKey;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}