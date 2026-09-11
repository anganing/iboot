package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 质检任务指派请求。 */
@Schema(description = "管理后台 - MES-质检任务指派 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QcTaskAssignReqVO extends QcTaskActionReqVO {

    @Schema(description = "检验员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验员不能为空")
    private Long inspectorId;
}
