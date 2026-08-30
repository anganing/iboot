package com.iboot.pf.module.mes.controller.admin.rework.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-返工任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ReworkRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3805")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "返工任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("返工任务编号")
    private String reworkNo;

    @Schema(description = "来源质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31445")
    @ExcelProperty("来源质检任务编号")
    private Long sourceQcTaskId;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19246")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "16920")
    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @Schema(description = "返工起始工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3226")
    @ExcelProperty("返工起始工序任务编号")
    private Long opTaskId;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("返工数量")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号", example = "15518")
    @ExcelProperty("返工工艺路线编号")
    private Long routeId;

    @Schema(description = "返工工艺路线版本")
    @ExcelProperty("返工工艺路线版本")
    private String routeVersion;

    @Schema(description = "状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED")
    private String status;

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

    @Schema(description = "返工起始工序任务task_no")
    @ExcelProperty("返工起始工序任务task_no")
    private String opTaskTask_no;

}