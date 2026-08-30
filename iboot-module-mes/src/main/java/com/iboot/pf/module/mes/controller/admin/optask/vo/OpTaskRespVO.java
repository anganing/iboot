package com.iboot.pf.module.mes.controller.admin.optask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-工序任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OpTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "264")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "工序任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序任务号")
    private String taskNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24770")
    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @Schema(description = "作业工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("作业工单号快照")
    private String workOrderNo;

    @Schema(description = "锁定的路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24505")
    @ExcelProperty("锁定的路线工序编号")
    private Long routeOpId;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6706")
    @ExcelProperty("标准工序编号")
    private Long opId;

    @Schema(description = "工序编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序编码快照")
    private String opCode;

    @Schema(description = "工序名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("工序名称快照")
    private String opName;

    @Schema(description = "工序顺序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序顺序号")
    private Integer seq;

    @Schema(description = "并行工序组")
    @ExcelProperty("并行工序组")
    private String parallelGroup;

    @Schema(description = "前序工序任务编号", example = "1542")
    @ExcelProperty("前序工序任务编号")
    private Long prevTaskId;

    @Schema(description = "后序工序任务编号", example = "26398")
    @ExcelProperty("后序工序任务编号")
    private Long nextTaskId;

    @Schema(description = "执行模式：SEQUENTIAL、PARALLEL、OPTIONAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("执行模式：SEQUENTIAL、PARALLEL、OPTIONAL")
    private String execMode;

    @Schema(description = "质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE")
    private String qcMode;

    @Schema(description = "跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED")
    private String skipPolicy;

    @Schema(description = "是否关键工序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否关键工序")
    private Boolean keyFlag;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划数量")
    private BigDecimal plannedQty;

    @Schema(description = "合格完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("合格完成数量")
    private BigDecimal completedQty;

    @Schema(description = "报废数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报废数量")
    private BigDecimal scrapQty;

    @Schema(description = "当前分配资源编号", example = "25539")
    @ExcelProperty("当前分配资源编号")
    private Long resId;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED")
    private String status;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "作业工单work_order_no")
    @ExcelProperty("作业工单work_order_no")
    private String workOrderWork_order_no;

}