package com.iboot.pf.module.wms.api.lock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 冻结记录响应 DTO。 */
@Data
public class InventoryLockRespDTO {

    /** 冻结记录编号。 */
    @Schema(description = "冻结记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 冻结编号。 */
    @Schema(description = "冻结编号", example = "LCK001")
    private String lockNo;

    /** 来源业务类型。 */
    @Schema(description = "来源业务类型", example = "QC_HOLD")
    private String bizType;

    /** 来源业务单号。 */
    @Schema(description = "来源业务单号", example = "MO20260830001")
    private String bizNo;

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

    /** 冻结数量。 */
    @Schema(description = "冻结数量", example = "100.000000")
    private BigDecimal qty;

    /** 已解冻数量。 */
    @Schema(description = "已解冻数量", example = "0.000000")
    private BigDecimal releasedQty;

    /** 冻结原因。 */
    @Schema(description = "冻结原因", example = "质检发现异常")
    private String reason;

    /** 状态：ACTIVE、PARTIAL、RELEASED、CANCELLED。 */
    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
