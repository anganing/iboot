package com.iboot.pf.module.mes.api.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在制流转流水跨模块只读事实。
 *
 * <p>{@code fromStatus} 与 {@code toStatus} 是状态机执行轨迹的唯一凭据，追溯查询靠它们复现工单走过的路。
 */
@Schema(description = "RPC 服务 - 在制流转流水 Response DTO")
@Data
public class WipTxnRespDTO {

    @Schema(description = "在制流水编号", example = "1")
    private Long id;

    @Schema(description = "在制事务编号", example = "WT202609040001")
    private String txnNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示无", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示无", example = "1")
    private Long opTaskId;

    /** 事务类型，取值为 MesWipTxnTypeEnum 的枚举名。 */
    @Schema(description = "事务类型", example = "REPORT")
    private String txnType;

    @Schema(description = "事务数量", example = "30")
    private BigDecimal qty;

    /** 变更前状态，取值为 MesOpTaskStatusEnum 的枚举名。 */
    @Schema(description = "变更前状态", example = "READY")
    private String fromStatus;

    /** 变更后状态，取值为 MesOpTaskStatusEnum 的枚举名。 */
    @Schema(description = "变更后状态", example = "RUNNING")
    private String toStatus;

    /** 来源业务类型，取值为 MesBizTypeEnum 的枚举名。 */
    @Schema(description = "来源业务类型", example = "PROD_ORDER")
    private String bizType;

    @Schema(description = "来源业务单号", example = "PO202609040001")
    private String bizNo;

    @Schema(description = "来源工序任务编号，0 表示无", example = "0")
    private Long fromOpTaskId;

    @Schema(description = "目标工序任务编号，0 表示无", example = "0")
    private Long toOpTaskId;

    @Schema(description = "事务发生时间")
    private LocalDateTime txnTime;

    @Schema(description = "备注")
    private String remark;

}
