package com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-齐套检查快照 Response VO")
@Data
@ExcelIgnoreUnannotated
public class KitSnapshotRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2421")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "齐套检查编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("齐套检查编号")
    private String checkNo;

    @Schema(description = "检查范围：PROD_ORDER、WORK_ORDER、OP_TASK", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查范围：PROD_ORDER、WORK_ORDER、OP_TASK")
    private String checkScope;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10712")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0表示整单检查", requiredMode = Schema.RequiredMode.REQUIRED, example = "17517")
    @ExcelProperty("作业工单编号，0表示整单检查")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序检查", requiredMode = Schema.RequiredMode.REQUIRED, example = "13473")
    @ExcelProperty("工序任务编号，0表示非工序检查")
    private Long opTaskId;

    @Schema(description = "检查明细行号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查明细行号")
    private Integer lineNo;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17036")
    @ExcelProperty("需求物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求数量")
    private BigDecimal reqQty;

    @Schema(description = "检查时可用数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查时可用数量")
    private BigDecimal availQty;

    @Schema(description = "检查时本需求已预留数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查时本需求已预留数量")
    private BigDecimal reservedQty;

    @Schema(description = "检查时已领用数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查时已领用数量")
    private BigDecimal issuedQty;

    @Schema(description = "短缺数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("短缺数量")
    private BigDecimal shortQty;

    @Schema(description = "实际采用替代料编号，0表示未采用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1026")
    @ExcelProperty("实际采用替代料编号，0表示未采用")
    private Long altMatId;

    @Schema(description = "检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE")
    private String result;

    @Schema(description = "检查时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "生产工单order_no")
    @ExcelProperty("生产工单order_no")
    private String prodOrderOrder_no;

    @Schema(description = "作业工单编号，0表示整单检查work_order_no")
    @ExcelProperty("作业工单编号，0表示整单检查work_order_no")
    private String workOrderWork_order_no;

    @Schema(description = "工序任务编号，0表示非工序检查task_no")
    @ExcelProperty("工序任务编号，0表示非工序检查task_no")
    private String opTaskTask_no;

}