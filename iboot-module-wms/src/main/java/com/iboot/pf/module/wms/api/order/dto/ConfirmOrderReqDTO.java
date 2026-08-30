package com.iboot.pf.module.wms.api.order.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 过账单据命令 DTO，入库、出库、转移单共用。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfirmOrderReqDTO extends WmsCommandReqDTO {

    /** 单据编号。 */
    @Schema(description = "单据编号", example = "1")
    private Long orderId;

    /** 单据号，与 orderId 二选一。 */
    @Schema(description = "单据号", example = "IN20260801001")
    private String orderNo;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
