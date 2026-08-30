package com.iboot.pf.module.mdm.api.partner.dto;

import lombok.Data;

/**
 * 供应商 Response DTO
 *
 * @author iboot
 */
@Data
public class SupplierRespDTO {

    /** 供应商编号 */
    private Long id;

    /** 供应商编码 */
    private String code;

    /** 供应商名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
