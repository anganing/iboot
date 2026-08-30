package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-不合格品处置单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DispositionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9174")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "不合格处置单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("不合格处置单号")
    private String dispositionNo;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27490")
    @ExcelProperty("质检任务编号")
    private Long taskId;

    @Schema(description = "处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS")
    private String dispositionType;

    @Schema(description = "处置数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("处置数量")
    private BigDecimal qty;

    @Schema(description = "联动目标业务类型", example = "1")
    @ExcelProperty("联动目标业务类型")
    private String targetBizType;

    @Schema(description = "联动目标业务单号")
    @ExcelProperty("联动目标业务单号")
    private String targetBizNo;

    @Schema(description = "审批人员编号")
    @ExcelProperty("审批人员编号")
    private Long approvedBy;

    @Schema(description = "审批时间")
    @ExcelProperty("审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED")
    private String status;

    @Schema(description = "处置幂等键")
    @ExcelProperty("处置幂等键")
    private String idempotencyKey;

    @Schema(description = "处置说明", example = "随便")
    @ExcelProperty("处置说明")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "质检任务task_no")
    @ExcelProperty("质检任务task_no")
    private String taskTask_no;

}