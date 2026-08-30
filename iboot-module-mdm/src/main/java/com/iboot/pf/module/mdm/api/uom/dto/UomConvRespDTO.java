package com.iboot.pf.module.mdm.api.uom.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单位换算 Response DTO
 *
 * @author iboot
 */
@Data
public class UomConvRespDTO {

    /** 换算记录编号 */
    private Long id;

    /** 物料编号 */
    private Long matId;

    /** 来源计量单位编号 */
    private Long fromUomId;

    /** 目标计量单位编号 */
    private Long toUomId;

    /** 换算比例（来源 -> 目标） */
    private BigDecimal ratio;

}
