package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 排程任务依赖分页 Request VO。 */
@Schema(description = "管理后台 - APS-排程任务依赖分页 Request VO")
@Data
public class TaskDepPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "1")
    private Long versionId;

    @Schema(description = "前置排程任务编号", example = "1")
    private Long fromTaskId;

    @Schema(description = "后置排程任务编号", example = "1")
    private Long toTaskId;

    @Schema(description = "依赖类型", example = "FINISH_START")
    private String depType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
