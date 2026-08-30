package com.iboot.pf.module.wms.api.lock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collection;

/** 冻结记录查询 DTO。为空的条件不过滤。 */
@Data
public class InventoryLockQueryReqDTO {

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

    /** 状态：ACTIVE、PARTIAL、RELEASED、CANCELLED。 */
    @Schema(description = "状态", example = "ACTIVE")
    private String status;

}
