package com.iboot.pf.module.mdm.api.partner.dto;

import lombok.Data;

/**
 * 客户 Response DTO
 *
 * @author iboot
 */
@Data
public class CustomerRespDTO {

    /** 客户编号 */
    private Long id;

    /** 客户编码 */
    private String code;

    /** 客户名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
