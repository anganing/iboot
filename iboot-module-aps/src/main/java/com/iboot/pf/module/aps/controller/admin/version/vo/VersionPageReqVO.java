package com.iboot.pf.module.aps.controller.admin.version.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 排程版本分页 Request VO")
@Data
public class VersionPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "17546")
    private Long scenarioId;

    @Schema(description = "排程版本号")
    private Integer versionNo;

    @Schema(description = "状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED", example = "2")
    private String status;

    @Schema(description = "Timefold综合得分")
    private String score;

    @Schema(description = "硬约束得分")
    private Long hardScore;

    @Schema(description = "软约束得分")
    private Long softScore;

    @Schema(description = "求解开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] solveStartTime;

    @Schema(description = "求解结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] solveEndTime;

    @Schema(description = "发布时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] publishedTime;

    @Schema(description = "发布人员编号")
    private Long publishedBy;

    @Schema(description = "求解结果或失败信息")
    private String solverMessage;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}