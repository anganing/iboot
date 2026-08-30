package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-工序跳过记录新增/修改 Request VO")
@Data
public class OpSkipRecordSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3690")
    private Long id;

    @Schema(description = "被跳过工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10030")
    @NotNull(message = "被跳过工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "跳过原因类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "跳过原因类型不能为空")
    private String skipReasonType;

    @Schema(description = "跳过原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @NotEmpty(message = "跳过原因不能为空")
    private String skipReason;

    @Schema(description = "操作人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10615")
    @NotNull(message = "操作人员编号不能为空")
    private Long operatorId;

    @Schema(description = "审批人员编号")
    private Long approvedBy;

    @Schema(description = "审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED不能为空")
    private String approveStatus;

    @Schema(description = "实际跳过时间")
    private LocalDateTime skipTime;

    @Schema(description = "对后续工序、质检及排程的影响说明", example = "随便")
    private String effectRemark;

    @Schema(description = "状态：PENDING、EFFECTIVE、REVOKED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：PENDING、EFFECTIVE、REVOKED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}