package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-派工任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DispatchTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5865")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "派工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("派工单号")
    private String dispatchNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3251")
    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @Schema(description = "作业人员编号", example = "6062")
    @ExcelProperty("作业人员编号")
    private Long operatorId;

    @Schema(description = "作业班组编号", example = "21197")
    @ExcelProperty("作业班组编号")
    private Long teamId;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6033")
    @ExcelProperty("生产资源编号")
    private Long resId;

    @Schema(description = "班次编号", example = "12378")
    @ExcelProperty("班次编号")
    private Long shiftId;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "工序任务task_no")
    @ExcelProperty("工序任务task_no")
    private String opTaskTask_no;

}