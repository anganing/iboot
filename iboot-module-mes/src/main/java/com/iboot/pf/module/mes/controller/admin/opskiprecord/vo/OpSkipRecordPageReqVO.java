package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 工序跳过记录分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-工序跳过记录分页 Request VO")
@Data
public class OpSkipRecordPageReqVO extends PageParam {

    @Schema(description = "被跳过工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "跳过原因类型", example = "NOT_APPLICABLE")
    private String skipReasonType;

    @Schema(description = "操作人员编号", example = "1")
    private Long operatorId;

    @Schema(description = "审批人员编号", example = "1")
    private Long approvedBy;

    @Schema(description = "审批状态", example = "NOT_REQUIRED")
    private String approveStatus;

    @Schema(description = "状态", example = "PENDING")
    private String status;

    @Schema(description = "实际跳过时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] skipTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
