package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-工序跳过记录分页 Request VO")
@Data
public class OpSkipRecordPageReqVO extends PageParam {

    @Schema(description = "被跳过工序任务编号", example = "10030")
    private Long opTaskId;

    @Schema(description = "跳过原因类型", example = "2")
    private String skipReasonType;

    @Schema(description = "跳过原因", example = "不香")
    private String skipReason;

    @Schema(description = "操作人员编号", example = "10615")
    private Long operatorId;

    @Schema(description = "审批人员编号")
    private Long approvedBy;

    @Schema(description = "审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED", example = "2")
    private String approveStatus;

    @Schema(description = "实际跳过时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] skipTime;

    @Schema(description = "对后续工序、质检及排程的影响说明", example = "随便")
    private String effectRemark;

    @Schema(description = "状态：PENDING、EFFECTIVE、REVOKED", example = "1")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}