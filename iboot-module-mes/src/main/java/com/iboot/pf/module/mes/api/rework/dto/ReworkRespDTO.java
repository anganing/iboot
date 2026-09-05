package com.iboot.pf.module.mes.api.rework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 返工任务跨模块只读事实。 */
@Schema(description = "RPC 服务 - 返工任务 Response DTO")
@Data
public class ReworkRespDTO {

    @Schema(description = "返工任务编号", example = "1")
    private Long id;

    @Schema(description = "返工任务号", example = "RW202609040001")
    private String reworkNo;

    @Schema(description = "来源质检任务编号，0 表示手工发起", example = "1")
    private Long sourceQcTaskId;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "返工起始工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "返工数量", example = "5")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号", example = "1")
    private Long routeId;

    @Schema(description = "返工工艺路线版本快照", example = "V1.0")
    private String routeVersion;

    /** 返工任务状态，取值为 MesReworkStatusEnum 的枚举名。 */
    @Schema(description = "返工任务状态", example = "PENDING")
    private String status;

}
