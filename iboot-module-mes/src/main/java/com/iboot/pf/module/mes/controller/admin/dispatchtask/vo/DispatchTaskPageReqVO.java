package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 派工任务分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-派工任务分页 Request VO")
@Data
public class DispatchTaskPageReqVO extends PageParam {

    @Schema(description = "派工单号")
    private String dispatchNo;

    @Schema(description = "工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "作业人员编号", example = "1")
    private Long operatorId;

    @Schema(description = "作业班组编号", example = "1")
    private Long teamId;

    @Schema(description = "生产资源编号", example = "1")
    private Long resId;

    @Schema(description = "班次编号", example = "1")
    private Long shiftId;

    @Schema(description = "状态", example = "PENDING")
    private String status;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedStartTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
