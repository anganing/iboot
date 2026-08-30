package com.iboot.pf.module.mes.controller.admin.rework.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - MES-返工任务新增/修改 Request VO")
@Data
public class ReworkSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3805")
    private Long id;

    @Schema(description = "返工任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "返工任务编号不能为空")
    private String reworkNo;

    @Schema(description = "来源质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31445")
    @NotNull(message = "来源质检任务编号不能为空")
    private Long sourceQcTaskId;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19246")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "16920")
    private Long workOrderId;

    @Schema(description = "返工起始工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3226")
    @NotNull(message = "返工起始工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "返工数量不能为空")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号", example = "15518")
    private Long routeId;

    @Schema(description = "返工工艺路线版本")
    private String routeVersion;

    @Schema(description = "状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}