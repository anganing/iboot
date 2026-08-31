package com.iboot.pf.module.aps.controller.admin.tasklock.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 排程任务锁定记录分页 Request VO。 */
@Schema(description = "管理后台 - APS-排程任务锁定记录分页 Request VO")
@Data
public class TaskLockPageReqVO extends PageParam {

    @Schema(description = "排程任务编号", example = "1")
    private Long taskId;

    @Schema(description = "锁定类型", example = "MANUAL")
    private String lockType;

    @Schema(description = "状态", example = "LOCKED")
    private String status;

    @Schema(description = "锁定人员编号", example = "1")
    private Long lockedBy;

    @Schema(description = "锁定时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lockedTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
