package com.iboot.pf.module.wms.api.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collection;

/** 库存余额查询 DTO。为空的条件不过滤。 */
@Data
public class InventoryQueryReqDTO {

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    /** 工厂编号。 */
    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 库位编号。 */
    @Schema(description = "库位编号", example = "1")
    private Long locId;

    /** 库位编号集合，按库区圈选库位时使用。 */
    @Schema(description = "库位编号集合")
    private Collection<Long> locIds;

    /** 物料编号。 */
    @Schema(description = "物料编号", example = "1")
    private Long matId;

    /** 物料编号集合。 */
    @Schema(description = "物料编号集合")
    private Collection<Long> matIds;

    /** 批次编号。 */
    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    /** 容器编号。 */
    @Schema(description = "容器编号", example = "1")
    private Long containerId;

    /** 项目编号。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 项目任务编号。 */
    @Schema(description = "项目任务编号", example = "0")
    private Long projectTaskId;

    /** 库存范围：COMMON、PROJECT_DEDICATED。 */
    @Schema(description = "库存范围", example = "COMMON")
    private String stockScope;

    /** 质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN。 */
    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    /** 是否只统计合格库存。 */
    @Schema(description = "是否只统计合格库存", example = "true")
    @NotNull(message = "是否只统计合格库存不能为空")
    private Boolean qualifiedOnly;

}
