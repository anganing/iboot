package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-生产需求新增/修改 Request VO")
@Data
public class ProdDemandSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21999")
    private Long id;

    @Schema(description = "生产需求编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "生产需求编号不能为空")
    private String demandNo;

    @Schema(description = "来源类型：MANUAL、EXCEL、ERP、APS", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "来源类型：MANUAL、EXCEL、ERP、APS不能为空")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7793")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "19484")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "28313")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10796")
    @NotNull(message = "需求物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求数量不能为空")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3052")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "计量单位编码快照不能为空")
    private String uomCode;

    @Schema(description = "需求交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求交期不能为空")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级，数值越大越优先不能为空")
    private Integer priority;

    @Schema(description = "状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "随便")
    private String remark;

}