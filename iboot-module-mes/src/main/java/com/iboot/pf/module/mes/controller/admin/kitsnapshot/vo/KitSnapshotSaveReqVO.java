package com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-齐套检查快照新增/修改 Request VO")
@Data
public class KitSnapshotSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2421")
    private Long id;

    @Schema(description = "齐套检查编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "齐套检查编号不能为空")
    private String checkNo;

    @Schema(description = "检查范围：PROD_ORDER、WORK_ORDER、OP_TASK", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检查范围：PROD_ORDER、WORK_ORDER、OP_TASK不能为空")
    private String checkScope;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10712")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0表示整单检查", requiredMode = Schema.RequiredMode.REQUIRED, example = "17517")
    @NotNull(message = "作业工单编号，0表示整单检查不能为空")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序检查", requiredMode = Schema.RequiredMode.REQUIRED, example = "13473")
    @NotNull(message = "工序任务编号，0表示非工序检查不能为空")
    private Long opTaskId;

    @Schema(description = "检查明细行号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检查明细行号不能为空")
    private Integer lineNo;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17036")
    @NotNull(message = "需求物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求数量不能为空")
    private BigDecimal reqQty;

    @Schema(description = "检查时可用数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检查时可用数量不能为空")
    private BigDecimal availQty;

    @Schema(description = "检查时本需求已预留数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检查时本需求已预留数量不能为空")
    private BigDecimal reservedQty;

    @Schema(description = "检查时已领用数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检查时已领用数量不能为空")
    private BigDecimal issuedQty;

    @Schema(description = "短缺数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "短缺数量不能为空")
    private BigDecimal shortQty;

    @Schema(description = "实际采用替代料编号，0表示未采用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1026")
    @NotNull(message = "实际采用替代料编号，0表示未采用不能为空")
    private Long altMatId;

    @Schema(description = "检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE不能为空")
    private String result;

    @Schema(description = "检查时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检查时间不能为空")
    private LocalDateTime checkTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

}