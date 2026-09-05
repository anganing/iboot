package com.iboot.pf.module.mes.api.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 生产报工记录跨模块只读事实。 */
@Schema(description = "RPC 服务 - 生产报工记录 Response DTO")
@Data
public class ProdReportRespDTO {

    @Schema(description = "报工记录编号", example = "1")
    private Long id;

    @Schema(description = "报工单号", example = "PR202609040001")
    private String reportNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "报工作业人员编号", example = "1")
    private Long workerId;

    @Schema(description = "生产资源编号，0 表示未指定", example = "1")
    private Long resId;

    @Schema(description = "班次编号，0 表示不区分班次", example = "1")
    private Long shiftId;

    @Schema(description = "合格数量", example = "30")
    private BigDecimal qtyGood;

    @Schema(description = "报废数量", example = "1")
    private BigDecimal qtyScrap;

    @Schema(description = "返工数量", example = "0")
    private BigDecimal qtyRework;

    @Schema(description = "本次作业开始时间")
    private LocalDateTime startTime;

    @Schema(description = "本次作业结束时间")
    private LocalDateTime endTime;

    @Schema(description = "报工时间")
    private LocalDateTime reportTime;

    /** 报工状态，取值为 MesProdReportStatusEnum 的枚举名。 */
    @Schema(description = "报工状态", example = "SUBMITTED")
    private String status;

}
