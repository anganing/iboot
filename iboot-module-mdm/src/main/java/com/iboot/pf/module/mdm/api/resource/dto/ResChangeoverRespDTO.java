package com.iboot.pf.module.mdm.api.resource.dto;

import lombok.Data;

/**
 * 资源换型矩阵 Response DTO
 *
 * @author iboot
 */
@Data
public class ResChangeoverRespDTO {

    /** 换型矩阵编号 */
    private Long id;

    /** 资源编号，0-按资源组生效 */
    private Long resId;

    /** 资源组 */
    private String resGroup;

    /** 换出换型分组 */
    private String fromGroup;

    /** 换入换型分组 */
    private String toGroup;

    /** 换型分钟数 */
    private Integer changeoverMinutes;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
