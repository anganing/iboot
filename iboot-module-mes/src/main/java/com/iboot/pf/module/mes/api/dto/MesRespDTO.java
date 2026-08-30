package com.iboot.pf.module.mes.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** MES API 通用事实响应 DTO。 */
@Data
public class MesRespDTO {
    /** 记录编号。 */
    @Schema(description = "记录编号")
    private Long id;
    /** 业务编码。 */
    @Schema(description = "业务编码")
    private String code;
    /** 业务名称。 */
    @Schema(description = "业务名称")
    private String name;
    /** 业务状态，具体取值由所属 API 定义。 */
    @Schema(description = "业务状态")
    private String status;
    /** 关联工单编号。 */
    private Long prodOrderId;
    /** 物料编号。 */
    private Long matId;
    /** 计划数量或业务数量。 */
    private BigDecimal qty;
    /** 发生时间。 */
    private LocalDateTime occurredTime;
}
