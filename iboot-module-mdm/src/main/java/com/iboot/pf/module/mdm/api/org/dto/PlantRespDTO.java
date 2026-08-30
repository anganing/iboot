package com.iboot.pf.module.mdm.api.org.dto;

import lombok.Data;

/**
 * 工厂 Response DTO
 *
 * @author iboot
 */
@Data
public class PlantRespDTO {

    /** 工厂编号 */
    private Long id;

    /** 库存组织编号 */
    private Long invOrgId;

    /** 工厂编码 */
    private String code;

    /** 工厂名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
