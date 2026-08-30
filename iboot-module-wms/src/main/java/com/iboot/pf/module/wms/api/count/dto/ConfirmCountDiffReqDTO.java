package com.iboot.pf.module.wms.api.count.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 确认盘点差异命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfirmCountDiffReqDTO extends WmsCommandReqDTO {

    /** 盘点单编号。 */
    @Schema(description = "盘点单编号")
    private Long orderId;

    /** 盘点单号，与 orderId 二选一。 */
    @Schema(description = "盘点单号")
    private String countNo;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
