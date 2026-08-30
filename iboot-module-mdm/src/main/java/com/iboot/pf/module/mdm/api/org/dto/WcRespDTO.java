package com.iboot.pf.module.mdm.api.org.dto;

import lombok.Data;

/**
 * 工作中心 Response DTO
 *
 * @author iboot
 */
@Data
public class WcRespDTO {

    /** 工作中心编号 */
    private Long id;

    /** 车间编号 */
    private Long workshopId;

    /** 工作中心编码 */
    private String code;

    /** 工作中心名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
