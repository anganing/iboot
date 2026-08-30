package com.iboot.pf.module.mdm.api.org.dto;

import lombok.Data;

/**
 * 车间 Response DTO
 *
 * @author iboot
 */
@Data
public class WorkshopRespDTO {

    /** 车间编号 */
    private Long id;

    /** 工厂编号 */
    private Long plantId;

    /** 车间编码 */
    private String code;

    /** 车间名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
