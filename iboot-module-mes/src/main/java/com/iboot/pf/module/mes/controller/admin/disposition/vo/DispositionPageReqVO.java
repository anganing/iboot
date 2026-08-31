package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 不合格品处置单分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-不合格品处置单分页 Request VO")
@Data
public class DispositionPageReqVO extends PageParam {

    @Schema(description = "处置单号")
    private String dispositionNo;

    @Schema(description = "质检任务编号", example = "1")
    private Long taskId;

    @Schema(description = "处置类型", example = "REWORK")
    private String dispositionType;

    @Schema(description = "联动目标业务类型", example = "REWORK")
    private String targetBizType;

    @Schema(description = "联动目标业务单号")
    private String targetBizNo;

    @Schema(description = "审批人员编号", example = "1")
    private Long approvedBy;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "审批时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] approveTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
