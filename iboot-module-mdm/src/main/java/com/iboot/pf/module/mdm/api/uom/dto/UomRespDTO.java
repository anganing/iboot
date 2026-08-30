package com.iboot.pf.module.mdm.api.uom.dto;

import lombok.Data;

/**
 * 计量单位 Response DTO
 *
 * @author iboot
 */
@Data
public class UomRespDTO {

    /** 计量单位编号 */
    private Long id;

    /** 计量单位编码 */
    private String code;

    /** 计量单位名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
