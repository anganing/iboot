package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 质检任务提交结论请求。 */
@Schema(description = "管理后台 - MES-质检任务提交结论 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QcTaskConclusionReqVO extends QcTaskActionReqVO {

    @Schema(description = "检验结论：QUALIFIED / REJECTED / CONCESSION", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "检验结论不能为空")
    private String conclusion;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合格数量不能为空")
    @DecimalMin(value = "0", message = "合格数量不能为负数")
    private BigDecimal qtyQualified;

    @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不合格数量不能为空")
    @DecimalMin(value = "0", message = "不合格数量不能为负数")
    private BigDecimal qtyRejected;
}
