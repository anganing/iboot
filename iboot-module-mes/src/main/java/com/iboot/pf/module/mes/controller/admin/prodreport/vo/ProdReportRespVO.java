package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-生产报工记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProdReportRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31729")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "报工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报工单号")
    private String reportNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29062")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18129")
    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28825")
    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @Schema(description = "报工作业人员编号", example = "13784")
    @ExcelProperty("报工作业人员编号")
    private Long workerId;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30495")
    @ExcelProperty("生产资源编号")
    private Long resId;

    @Schema(description = "班次编号", example = "1428")
    @ExcelProperty("班次编号")
    private Long shiftId;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("合格数量")
    private BigDecimal qtyGood;

    @Schema(description = "报废数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报废数量")
    private BigDecimal qtyScrap;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("返工数量")
    private BigDecimal qtyRework;

    @Schema(description = "本次作业开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本次作业开始时间")
    private LocalDateTime startTime;

    @Schema(description = "本次作业结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本次作业结束时间")
    private LocalDateTime endTime;

    @Schema(description = "报工时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报工时间")
    private LocalDateTime reportTime;

    @Schema(description = "状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED")
    private String status;

    @Schema(description = "报工幂等键")
    @ExcelProperty("报工幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "生产工单order_no")
    @ExcelProperty("生产工单order_no")
    private String prodOrderOrder_no;

    @Schema(description = "作业工单work_order_no")
    @ExcelProperty("作业工单work_order_no")
    private String workOrderWork_order_no;

    @Schema(description = "工序任务task_no")
    @ExcelProperty("工序任务task_no")
    private String opTaskTask_no;

}