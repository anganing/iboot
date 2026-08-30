package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-领退补料申请 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MaterialReqRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10773")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "领退补料申请单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("领退补料申请单号")
    private String reqNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19224")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单编号，0表示工单级申请", requiredMode = Schema.RequiredMode.REQUIRED, example = "19058")
    @ExcelProperty("作业工单编号，0表示工单级申请")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序级申请", requiredMode = Schema.RequiredMode.REQUIRED, example = "9855")
    @ExcelProperty("工序任务编号，0表示非工序级申请")
    private Long opTaskId;

    @Schema(description = "申请类型：ISSUE、RETURN、SUPPLEMENT", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("申请类型：ISSUE、RETURN、SUPPLEMENT")
    private String reqType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12027")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19726")
    @ExcelProperty("目标仓库或来源仓库编号")
    private Long whId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "11633")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "20757")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "WMS生成的出入库单号")
    @ExcelProperty("WMS生成的出入库单号")
    private String wmsBizNo;

    @Schema(description = "状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED")
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "生产工单order_no")
    @ExcelProperty("生产工单order_no")
    private String prodOrderOrder_no;

    @Schema(description = "作业工单编号，0表示工单级申请work_order_no")
    @ExcelProperty("作业工单编号，0表示工单级申请work_order_no")
    private String workOrderWork_order_no;

    @Schema(description = "工序任务编号，0表示非工序级申请task_no")
    @ExcelProperty("工序任务编号，0表示非工序级申请task_no")
    private String opTaskTask_no;

}