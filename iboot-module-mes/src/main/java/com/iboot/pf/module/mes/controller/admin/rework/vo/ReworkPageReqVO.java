package com.iboot.pf.module.mes.controller.admin.rework.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 返工任务分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-返工任务分页 Request VO")
@Data
public class ReworkPageReqVO extends PageParam {

    @Schema(description = "返工任务编号")
    private String reworkNo;

    @Schema(description = "来源质检任务编号", example = "1")
    private Long sourceQcTaskId;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "返工起始工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
