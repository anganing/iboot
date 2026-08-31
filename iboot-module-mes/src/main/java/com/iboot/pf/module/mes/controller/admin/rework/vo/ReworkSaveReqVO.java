package com.iboot.pf.module.mes.controller.admin.rework.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesReworkStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 返工任务新增/修改 Request VO。
 *
 * <p>生产工单、作业工单编号与返工路线版本由后端从工序任务与工艺路线带出，不由前端提交。
 */
@Schema(description = "管理后台 - MES-返工任务新增/修改 Request VO")
@Data
public class ReworkSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "返工任务编号，留空由编码规则分配")
    @Size(max = 64, message = "返工任务编号长度不能超过 64")
    private String reworkNo;

    @Schema(description = "来源质检任务编号，0 表示手工发起", example = "1")
    private Long sourceQcTaskId;

    @Schema(description = "返工起始工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "返工起始工序任务不能为空")
    private Long opTaskId;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "返工数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "返工数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "返工工艺路线不能为空")
    private Long routeId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesReworkStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
