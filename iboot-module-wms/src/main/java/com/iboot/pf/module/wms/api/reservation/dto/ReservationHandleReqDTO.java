package com.iboot.pf.module.wms.api.reservation.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 释放或核销预留命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReservationHandleReqDTO extends WmsCommandReqDTO {

    /** 预留记录编号。 */
    @Schema(description = "预留记录编号", example = "1")
    private Long reservationId;

    /** 预留单号，与 reservationId 二选一。 */
    @Schema(description = "预留单号", example = "RSV001")
    private String reservationNo;

    /** 来源业务类型，写入流水。 */
    @Schema(description = "来源业务类型", example = "PROD_ISSUE")
    private String bizType;

    /** 本次释放或核销数量，基准单位；为空表示全部剩余数量。 */
    @Schema(description = "释放或核销数量，为空表示全部剩余", example = "50.000000")
    private BigDecimal qty;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
