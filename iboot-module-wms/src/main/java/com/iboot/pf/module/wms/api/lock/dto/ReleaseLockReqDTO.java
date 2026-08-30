package com.iboot.pf.module.wms.api.lock.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 解冻库存命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReleaseLockReqDTO extends WmsCommandReqDTO {

    /** 冻结记录编号。 */
    @Schema(description = "冻结记录编号", example = "1")
    private Long lockId;

    /** 冻结编号，与 lockId 二选一。 */
    @Schema(description = "冻结编号", example = "LCK001")
    private String lockNo;

    /** 本次解冻数量，基准单位；为空表示全部剩余数量。 */
    @Schema(description = "解冻数量，为空表示全部剩余", example = "50.000000")
    private BigDecimal qty;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
