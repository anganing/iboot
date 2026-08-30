package com.iboot.pf.module.aps.controller.admin.tasklock.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 任务锁定 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TaskLockRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14338")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27913")
    @ExcelProperty("排程任务编号")
    private Long taskId;

    @Schema(description = "锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE")
    private String lockType;

    @Schema(description = "锁定原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不喜欢")
    @ExcelProperty("锁定原因")
    private String reason;

    @Schema(description = "锁定人员编号")
    @ExcelProperty("锁定人员编号")
    private Long lockedBy;

    @Schema(description = "锁定时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("锁定时间")
    private LocalDateTime lockedTime;

    @Schema(description = "解锁时间")
    @ExcelProperty("解锁时间")
    private LocalDateTime unlockTime;

    @Schema(description = "状态：LOCKED、UNLOCKED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：LOCKED、UNLOCKED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}