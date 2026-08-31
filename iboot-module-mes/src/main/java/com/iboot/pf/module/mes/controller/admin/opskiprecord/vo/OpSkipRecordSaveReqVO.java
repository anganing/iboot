package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipReasonTypeEnum;
import com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工序跳过记录新增/修改 Request VO。
 *
 * <p>跳序是否允许、是否需要审批由工序任务的跳序策略决定，Service 会按策略校验并推导审批状态。
 */
@Schema(description = "管理后台 - MES-工序跳过记录新增/修改 Request VO")
@Data
public class OpSkipRecordSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "被跳过工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "被跳过工序任务不能为空")
    private Long opTaskId;

    @Schema(description = "跳过原因类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "NOT_APPLICABLE")
    @NotEmpty(message = "跳过原因类型不能为空")
    @InEnum(MesSkipReasonTypeEnum.class)
    private String skipReasonType;

    @Schema(description = "跳过原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "跳过原因不能为空")
    @Size(max = 500, message = "跳过原因长度不能超过 500")
    private String skipReason;

    @Schema(description = "操作人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "操作人员不能为空")
    private Long operatorId;

    @Schema(description = "审批人员编号，0 表示尚未审批", example = "1")
    private Long approvedBy;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "NOT_REQUIRED")
    @NotEmpty(message = "审批状态不能为空")
    @InEnum(MesSkipApproveStatusEnum.class)
    private String approveStatus;

    @Schema(description = "实际跳过时间，留空取当前时间")
    private LocalDateTime skipTime;

    @Schema(description = "对后续工序、质检及排程的影响说明")
    @Size(max = 500, message = "影响说明长度不能超过 500")
    private String effectRemark;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesSkipRecordStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
