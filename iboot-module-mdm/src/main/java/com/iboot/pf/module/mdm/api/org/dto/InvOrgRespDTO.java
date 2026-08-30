package com.iboot.pf.module.mdm.api.org.dto;

import lombok.Data;

/**
 * 库存组织 Response DTO
 *
 * @author iboot
 */
@Data
public class InvOrgRespDTO {

    /** 库存组织编号 */
    private Long id;

    /** 库存组织编码 */
    private String code;

    /** 库存组织名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
