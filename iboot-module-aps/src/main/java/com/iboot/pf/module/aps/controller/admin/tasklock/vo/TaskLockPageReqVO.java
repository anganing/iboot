package com.iboot.pf.module.aps.controller.admin.tasklock.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 任务锁定分页 Request VO")
@Data
public class TaskLockPageReqVO extends PageParam {

    @Schema(description = "排程任务编号", example = "27913")
    private Long taskId;

    @Schema(description = "锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE", example = "2")
    private String lockType;

    @Schema(description = "锁定原因", example = "不喜欢")
    private String reason;

    @Schema(description = "锁定人员编号")
    private Long lockedBy;

    @Schema(description = "锁定时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lockedTime;

    @Schema(description = "解锁时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] unlockTime;

    @Schema(description = "状态：LOCKED、UNLOCKED", example = "2")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}