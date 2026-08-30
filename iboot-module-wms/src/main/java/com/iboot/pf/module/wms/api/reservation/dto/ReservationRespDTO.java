package com.iboot.pf.module.wms.api.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 预留记录响应 DTO。 */
@Data
public class ReservationRespDTO {

    /** 预留记录编号。 */
    @Schema(description = "预留记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 预留编号。 */
    @Schema(description = "预留编号", example = "RSV001")
    private String reservationNo;

    /** 来源业务类型。 */
    @Schema(description = "来源业务类型", example = "PROD_ISSUE")
    private String bizType;

    /** 来源业务单号。 */
    @Schema(description = "来源业务单号", example = "MO20260830001")
    private String bizNo;

    /** 来源业务行号。 */
    @Schema(description = "来源业务行号", example = "10")
    private String bizLineNo;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 库位编号。 */
    @Schema(description = "库位编号", example = "1")
    private Long locId;

    /** 物料编号。 */
    @Schema(description = "物料编号", example = "1")
    private Long matId;

    /** 批次编号。 */
    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    /** 容器编号。 */
    @Schema(description = "容器编号", example = "0")
    private Long containerId;

    /** 项目编号。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 质量状态。 */
    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    /** 预留数量。 */
    @Schema(description = "预留数量", example = "100.000000")
    private BigDecimal qty;

    /** 已释放或已核销数量。 */
    @Schema(description = "已释放或已核销数量", example = "0.000000")
    private BigDecimal releasedQty;

    /** 状态：ACTIVE、PARTIAL、RELEASED、CONSUMED、CANCELLED。 */
    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    /** 预留失效时间。 */
    @Schema(description = "预留失效时间")
    private LocalDateTime expireTime;

}
