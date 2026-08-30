package com.iboot.pf.module.mes.controller.admin.optask.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-工序任务分页 Request VO")
@Data
public class OpTaskPageReqVO extends PageParam {

    @Schema(description = "工序任务号")
    private String taskNo;

    @Schema(description = "作业工单编号", example = "24770")
    private Long workOrderId;

    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    @Schema(description = "锁定的路线工序编号", example = "24505")
    private Long routeOpId;

    @Schema(description = "标准工序编号", example = "6706")
    private Long opId;

    @Schema(description = "工序编码快照")
    private String opCode;

    @Schema(description = "工序名称快照", example = "芋艿")
    private String opName;

    @Schema(description = "工序顺序号")
    private Integer seq;

    @Schema(description = "并行工序组")
    private String parallelGroup;

    @Schema(description = "前序工序任务编号", example = "1542")
    private Long prevTaskId;

    @Schema(description = "后序工序任务编号", example = "26398")
    private Long nextTaskId;

    @Schema(description = "执行模式：SEQUENTIAL、PARALLEL、OPTIONAL")
    private String execMode;

    @Schema(description = "质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE")
    private String qcMode;

    @Schema(description = "跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED")
    private String skipPolicy;

    @Schema(description = "是否关键工序")
    private Boolean keyFlag;

    @Schema(description = "计划数量")
    private BigDecimal plannedQty;

    @Schema(description = "合格完成数量")
    private BigDecimal completedQty;

    @Schema(description = "报废数量")
    private BigDecimal scrapQty;

    @Schema(description = "当前分配资源编号", example = "25539")
    private Long resId;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedStartTime;

    @Schema(description = "计划结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedEndTime;

    @Schema(description = "实际开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualStartTime;

    @Schema(description = "实际结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualEndTime;

    @Schema(description = "状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED", example = "1")
    private String status;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}