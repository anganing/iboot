package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-不合格品处置单新增/修改 Request VO")
@Data
public class DispositionSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9174")
    private Long id;

    @Schema(description = "不合格处置单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "不合格处置单号不能为空")
    private String dispositionNo;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27490")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS不能为空")
    private String dispositionType;

    @Schema(description = "处置数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处置数量不能为空")
    private BigDecimal qty;

    @Schema(description = "联动目标业务类型", example = "1")
    private String targetBizType;

    @Schema(description = "联动目标业务单号")
    private String targetBizNo;

    @Schema(description = "审批人员编号")
    private Long approvedBy;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED不能为空")
    private String status;

    @Schema(description = "处置幂等键")
    private String idempotencyKey;

    @Schema(description = "处置说明", example = "随便")
    private String remark;

}