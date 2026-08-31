package com.iboot.pf.module.mes.controller.admin.optask.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 工序任务分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-工序任务分页 Request VO")
@Data
public class OpTaskPageReqVO extends PageParam {

    @Schema(description = "工序任务号")
    private String taskNo;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    private String workOrderNo;

    @Schema(description = "标准工序编号", example = "1")
    private Long opId;

    @Schema(description = "工序编码")
    private String opCode;

    @Schema(description = "工序名称")
    private String opName;

    @Schema(description = "生产资源编号", example = "1")
    private Long resId;

    @Schema(description = "是否关键工序", example = "false")
    private Boolean keyFlag;

    @Schema(description = "质检模式", example = "NONE")
    private String qcMode;

    @Schema(description = "状态", example = "PENDING")
    private String status;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedStartTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
