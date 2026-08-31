package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 不合格品处置单新增/修改 Request VO。
 *
 * <p>处置数量不能超过质检任务的不合格数量；审批通过或驳回时审批人必填，由 Service 校验。
 */
@Schema(description = "管理后台 - MES-不合格品处置单新增/修改 Request VO")
@Data
public class DispositionSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "不合格处置单号，留空由编码规则分配")
    @Size(max = 64, message = "处置单号长度不能超过 64")
    private String dispositionNo;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "处置类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REWORK")
    @NotEmpty(message = "处置类型不能为空")
    @InEnum(MesDispositionTypeEnum.class)
    private String dispositionType;

    @Schema(description = "处置数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "处置数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "处置数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "联动目标业务类型", example = "REWORK")
    @InEnum(MesBizTypeEnum.class)
    private String targetBizType;

    @Schema(description = "联动目标业务单号")
    @Size(max = 64, message = "联动目标业务单号长度不能超过 64")
    private String targetBizNo;

    @Schema(description = "审批人员编号，0 表示尚未审批", example = "1")
    private Long approvedBy;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesDispositionStatusEnum.class)
    private String status;

    @Schema(description = "处置幂等键")
    @Size(max = 128, message = "处置幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "处置说明")
    @Size(max = 500, message = "处置说明长度不能超过 500")
    private String remark;

}
