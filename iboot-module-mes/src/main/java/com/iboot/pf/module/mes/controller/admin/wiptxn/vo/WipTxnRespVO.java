package com.iboot.pf.module.mes.controller.admin.wiptxn.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-在制流转流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WipTxnRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "12769")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "在制事务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("在制事务编号")
    private String txnNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32266")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12103")
    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26127")
    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @Schema(description = "事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP")
    private String txnType;

    @Schema(description = "事务数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("事务数量")
    private BigDecimal qty;

    @Schema(description = "变更前状态", example = "2")
    @ExcelProperty("变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("变更后状态")
    private String toStatus;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("来源业务类型")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源业务单号")
    private String bizNo;

    @Schema(description = "来源工序任务编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "14730")
    @ExcelProperty("来源工序任务编号，0表示无")
    private Long fromOpTaskId;

    @Schema(description = "目标工序任务编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "31090")
    @ExcelProperty("目标工序任务编号，0表示无")
    private Long toOpTaskId;

    @Schema(description = "事务发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("事务发生时间")
    private LocalDateTime txnTime;

    @Schema(description = "事务幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("事务幂等键")
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