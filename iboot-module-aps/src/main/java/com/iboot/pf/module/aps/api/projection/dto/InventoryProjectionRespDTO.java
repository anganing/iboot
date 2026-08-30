package com.iboot.pf.module.aps.api.projection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** WMS 库存投影响应。 */
@Data
public class InventoryProjectionRespDTO {
    /** 投影编号。 */ private Long id;
    /** 场景编号。 */ private Long scenarioId;
    /** WMS 余额编号。 */ private Long sourceBalanceId;
    /** 工厂编号。 */ private Long plantId;
    /** 仓库编号。 */ private Long whId;
    /** 库位编号。 */ private Long locId;
    /** 物料编号。 */ private Long matId;
    /** 物料编码。 */ private String matCode;
    /** 批次编号。 */ private Long batchId;
    /** 批次号。 */ private String lotNo;
    /** 项目编号。 */ private Long projectId;
    /** 库存范围。 */ private String stockScope;
    /** 质量状态。 */ private String qualityStatus;
    /** 可用数量。 */ private BigDecimal qtyAvailable;
    /** 快照时间。 */ private LocalDateTime snapshotTime;
}
