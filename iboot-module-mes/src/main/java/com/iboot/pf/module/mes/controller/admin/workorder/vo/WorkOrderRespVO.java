package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-作业工单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WorkOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25812")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "作业工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "88")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6008")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23163")
    @ExcelProperty("车间编号")
    private Long workshopId;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "400")
    @ExcelProperty("工作中心编号")
    private Long wcId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "2212")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "3019")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划数量")
    private BigDecimal plannedQty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

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

    @Schema(description = "齐套状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("齐套状态")
    private String kitStatus;

    @Schema(description = "状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED")
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


    @Schema(description = "生产工单order_no")
    @ExcelProperty("生产工单order_no")
    private String prodOrderOrder_no;

}