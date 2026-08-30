package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-质检任务新增/修改 Request VO")
@Data
public class QcTaskSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19592")
    private Long id;

    @Schema(description = "质检任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "质检任务号不能为空")
    private String taskNo;

    @Schema(description = "命中的质检规则编号", example = "1712")
    private Long ruleId;

    @Schema(description = "锁定的检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24589")
    @NotNull(message = "锁定的检验方案编号不能为空")
    private Long qcPlanId;

    @Schema(description = "锁定的检验方案版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "锁定的检验方案版本不能为空")
    private String qcPlanVersion;

    @Schema(description = "触发点：IQC、IPQC、FQC、OQC、FAI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "触发点：IQC、IPQC、FQC、OQC、FAI不能为空")
    private String triggerPoint;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "来源业务类型不能为空")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务单号不能为空")
    private String bizNo;

    @Schema(description = "来源业务明细编号", example = "8115")
    private Long bizLineId;

    @Schema(description = "检验物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20917")
    @NotNull(message = "检验物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "检验批次编号", example = "10315")
    private Long batchId;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "送检数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "送检数量不能为空")
    private BigDecimal qty;

    @Schema(description = "抽样数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "抽样数量不能为空")
    private BigDecimal sampleQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28182")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "计量单位编码快照不能为空")
    private String uomCode;

    @Schema(description = "指派检验员编号", example = "1584")
    private Long assignedInspectorId;

    @Schema(description = "检验开始时间")
    private LocalDateTime inspectStartTime;

    @Schema(description = "检验结束时间")
    private LocalDateTime inspectEndTime;

    @Schema(description = "检验结论：QUALIFIED、REJECTED、CONCESSION")
    private String conclusion;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合格数量不能为空")
    private BigDecimal qtyQualified;

    @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不合格数量不能为空")
    private BigDecimal qtyRejected;

    @Schema(description = "状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "随便")
    private String remark;

}