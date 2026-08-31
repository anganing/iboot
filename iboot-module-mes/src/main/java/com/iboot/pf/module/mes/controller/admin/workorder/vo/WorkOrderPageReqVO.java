package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 作业工单分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-作业工单分页 Request VO")
@Data
public class WorkOrderPageReqVO extends PageParam {

    @Schema(description = "作业工单号")
    private String workOrderNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    private String prodOrderNo;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "工作中心编号", example = "1")
    private Long wcId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "齐套状态", example = "UNCHECKED")
    private String kitStatus;

    @Schema(description = "状态", example = "PENDING")
    private String status;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedStartTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
