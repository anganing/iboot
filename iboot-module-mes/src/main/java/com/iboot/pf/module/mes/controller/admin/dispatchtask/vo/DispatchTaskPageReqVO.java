package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-派工任务分页 Request VO")
@Data
public class DispatchTaskPageReqVO extends PageParam {

    @Schema(description = "派工单号")
    private String dispatchNo;

    @Schema(description = "工序任务编号", example = "3251")
    private Long opTaskId;

    @Schema(description = "作业人员编号", example = "6062")
    private Long operatorId;

    @Schema(description = "作业班组编号", example = "21197")
    private Long teamId;

    @Schema(description = "生产资源编号", example = "6033")
    private Long resId;

    @Schema(description = "班次编号", example = "12378")
    private Long shiftId;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedStartTime;

    @Schema(description = "计划结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedEndTime;

    @Schema(description = "状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED", example = "2")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}