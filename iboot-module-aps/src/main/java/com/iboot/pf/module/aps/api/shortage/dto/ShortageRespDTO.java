package com.iboot.pf.module.aps.api.shortage.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 排程缺料响应。 */
@Data
public class ShortageRespDTO {
    /** 缺料记录编号。 */ private Long id;
    /** 版本编号。 */ private Long versionId;
    /** 生产工单编号。 */ private Long prodOrderId;
    /** 物料编号。 */ private Long matId;
    /** 物料编码。 */ private String matCode;
    /** 物料名称。 */ private String matName;
    /** 需求数量。 */ private BigDecimal reqQty;
    /** 可用数量。 */ private BigDecimal availQty;
    /** 短缺数量 = max(需求数量 - 可用数量, 0)。 */ private BigDecimal shortQty;
    /** 需求到位时间。 */ private LocalDateTime needDate;
    /** 状态：OPEN、PARTIAL、RESOLVED、IGNORED。 */ private String status;
}
