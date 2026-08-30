package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-作业工单分页 Request VO")
@Data
public class WorkOrderPageReqVO extends PageParam {

    @Schema(description = "作业工单号")
    private String workOrderNo;

    @Schema(description = "生产工单编号", example = "88")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "工厂编号", example = "6008")
    private Long plantId;

    @Schema(description = "车间编号", example = "23163")
    private Long workshopId;

    @Schema(description = "工作中心编号", example = "400")
    private Long wcId;

    @Schema(description = "项目编号，0表示无项目", example = "2212")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "3019")
    private Long projectTaskId;

    @Schema(description = "计划数量")
    private BigDecimal plannedQty;

    @Schema(description = "已完成数量")
    private BigDecimal completedQty;

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

    @Schema(description = "齐套状态", example = "1")
    private String kitStatus;

    @Schema(description = "状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED", example = "2")
    private String status;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}