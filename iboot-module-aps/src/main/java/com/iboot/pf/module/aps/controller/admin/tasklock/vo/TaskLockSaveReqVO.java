package com.iboot.pf.module.aps.controller.admin.tasklock.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 任务锁定新增/修改 Request VO")
@Data
public class TaskLockSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14338")
    private Long id;

    @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27913")
    @NotNull(message = "排程任务编号不能为空")
    private Long taskId;

    @Schema(description = "锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE不能为空")
    private String lockType;

    @Schema(description = "锁定原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不喜欢")
    @NotEmpty(message = "锁定原因不能为空")
    private String reason;

    @Schema(description = "锁定人员编号")
    private Long lockedBy;

    @Schema(description = "锁定时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "锁定时间不能为空")
    private LocalDateTime lockedTime;

    @Schema(description = "解锁时间")
    private LocalDateTime unlockTime;

    @Schema(description = "状态：LOCKED、UNLOCKED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：LOCKED、UNLOCKED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}