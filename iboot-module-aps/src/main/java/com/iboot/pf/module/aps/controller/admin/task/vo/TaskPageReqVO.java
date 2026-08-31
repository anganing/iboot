package com.iboot.pf.module.aps.controller.admin.task.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 排程任务分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - APS-排程任务分页 Request VO")
@Data
public class TaskPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "1")
    private Long versionId;

    @Schema(description = "排程任务编号")
    private String taskNo;

    @Schema(description = "生产工单号")
    private String prodOrderNo;

    @Schema(description = "工序编码")
    private String opCode;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "生产资源编号", example = "1")
    private Long resId;

    @Schema(description = "是否锁定不可移动")
    private Boolean lockedFlag;

    @Schema(description = "状态", example = "PLANNED")
    private String status;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "计划结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
