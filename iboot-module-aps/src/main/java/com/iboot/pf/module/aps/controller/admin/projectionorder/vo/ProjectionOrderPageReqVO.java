package com.iboot.pf.module.aps.controller.admin.projectionorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 工单投影分页 Request VO")
@Data
public class ProjectionOrderPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "30640")
    private Long scenarioId;

    @Schema(description = "来源类型：DEMAND、PROD_ORDER、OP_TASK", example = "2")
    private String sourceType;

    @Schema(description = "MES来源对象编号", example = "21689")
    private Long sourceId;

    @Schema(description = "MES来源对象单号")
    private String sourceNo;

    @Schema(description = "生产工单编号，0表示尚未生成工单", example = "5497")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0表示尚未生成作业工单", example = "6024")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序投影", example = "17427")
    private Long opTaskId;

    @Schema(description = "工厂编号", example = "25416")
    private Long plantId;

    @Schema(description = "车间编号，0表示未指定", example = "19093")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", example = "21577")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "4851")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", example = "23628")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "路线工序编号，0表示需求或整单", example = "9698")
    private Long routeOpId;

    @Schema(description = "当前资源编号，0表示未分配", example = "15554")
    private Long resId;

    @Schema(description = "计划数量或剩余数量")
    private BigDecimal qty;

    @Schema(description = "已完成数量快照")
    private BigDecimal completedQty;

    @Schema(description = "交期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dueDate;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "来源对象状态快照", example = "1")
    private String currentStatus;

    @Schema(description = "实际开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualStartTime;

    @Schema(description = "实际结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualEndTime;

    @Schema(description = "是否已开工或业务锁定")
    private Boolean lockedFlag;

    @Schema(description = "来源数据更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sourceUpdateTime;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}