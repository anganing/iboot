package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-生产需求 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProdDemandRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21999")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "生产需求编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产需求编号")
    private String demandNo;

    @Schema(description = "来源类型：MANUAL、EXCEL、ERP、APS", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("来源类型：MANUAL、EXCEL、ERP、APS")
    private String sourceType;

    @Schema(description = "来源单号")
    @ExcelProperty("来源单号")
    private String sourceNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7793")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "19484")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "28313")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10796")
    @ExcelProperty("需求物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("物料规格快照")
    private String matSpec;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求数量")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3052")
    @ExcelProperty("计量单位编号")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @Schema(description = "需求交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED")
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}