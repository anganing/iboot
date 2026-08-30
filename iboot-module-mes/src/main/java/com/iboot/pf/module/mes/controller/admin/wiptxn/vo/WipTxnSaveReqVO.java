package com.iboot.pf.module.mes.controller.admin.wiptxn.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-在制流转流水新增/修改 Request VO")
@Data
public class WipTxnSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "12769")
    private Long id;

    @Schema(description = "在制事务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "在制事务编号不能为空")
    private String txnNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32266")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12103")
    @NotNull(message = "作业工单编号不能为空")
    private Long workOrderId;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26127")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP不能为空")
    private String txnType;

    @Schema(description = "事务数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "事务数量不能为空")
    private BigDecimal qty;

    @Schema(description = "变更前状态", example = "2")
    private String fromStatus;

    @Schema(description = "变更后状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "变更后状态不能为空")
    private String toStatus;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "来源业务类型不能为空")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务单号不能为空")
    private String bizNo;

    @Schema(description = "来源工序任务编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "14730")
    @NotNull(message = "来源工序任务编号，0表示无不能为空")
    private Long fromOpTaskId;

    @Schema(description = "目标工序任务编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "31090")
    @NotNull(message = "目标工序任务编号，0表示无不能为空")
    private Long toOpTaskId;

    @Schema(description = "事务发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "事务发生时间不能为空")
    private LocalDateTime txnTime;

    @Schema(description = "事务幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "事务幂等键不能为空")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}