package com.iboot.pf.module.wms.api.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 库存余额响应 DTO。 */
@Data
public class InventoryBalanceRespDTO {

    /** 库存余额编号。 */
    @Schema(description = "库存余额编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    /** 工厂编号。 */
    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 仓库编码快照。 */
    @Schema(description = "仓库编码快照", example = "WH01")
    private String whCode;

    /** 库位编号。 */
    @Schema(description = "库位编号", example = "1")
    private Long locId;

    /** 库位编码快照。 */
    @Schema(description = "库位编码快照", example = "A-01-01")
    private String locCode;

    /** 物料编号。 */
    @Schema(description = "物料编号", example = "1")
    private Long matId;

    /** 物料编码快照。 */
    @Schema(description = "物料编码快照", example = "M001")
    private String matCode;

    /** 物料名称快照。 */
    @Schema(description = "物料名称快照", example = "钢板")
    private String matName;

    /** 物料规格快照。 */
    @Schema(description = "物料规格快照")
    private String matSpec;

    /** 批次编号。 */
    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    /** 批次号快照。 */
    @Schema(description = "批次号快照", example = "LOT20260801")
    private String lotNo;

    /** 容器编号，0 表示散件。 */
    @Schema(description = "容器编号", example = "0")
    private Long containerId;

    /** 项目编号，0 表示无项目归属。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务归属。 */
    private Long projectTaskId;

    /** 库存范围：COMMON、PROJECT_DEDICATED。 */
    @Schema(description = "库存范围", example = "COMMON")
    private String stockScope;

    /** 质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN。 */
    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    /** 现存数量，基准单位。 */
    @Schema(description = "现存数量", example = "100.000000")
    private BigDecimal qtyOnhand;

    /** 预留数量，基准单位。 */
    @Schema(description = "预留数量", example = "0.000000")
    private BigDecimal qtyReserved;

    /** 冻结数量，基准单位。 */
    @Schema(description = "冻结数量", example = "0.000000")
    private BigDecimal qtyFrozen;

    /** 可用数量 = 现存 - 预留 - 冻结，只统计合格库存。 */
    @Schema(description = "可用数量", example = "100.000000")
    private BigDecimal qtyAvailable;

    /** 基准单位编码快照。 */
    @Schema(description = "基准单位编码快照", example = "KG")
    private String uomCode;

}
