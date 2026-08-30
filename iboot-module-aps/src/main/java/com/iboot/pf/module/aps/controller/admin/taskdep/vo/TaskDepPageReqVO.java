package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-排程任务依赖分页 Request VO")
@Data
public class TaskDepPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "13102")
    private Long versionId;

    @Schema(description = "前置排程任务编号", example = "18147")
    private Long fromTaskId;

    @Schema(description = "后置排程任务编号", example = "18828")
    private Long toTaskId;

    @Schema(description = "依赖类型：FINISH_START、START_START、FINISH_FINISH", example = "2")
    private String depType;

    @Schema(description = "依赖间隔分钟数")
    private Integer lagMinutes;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}