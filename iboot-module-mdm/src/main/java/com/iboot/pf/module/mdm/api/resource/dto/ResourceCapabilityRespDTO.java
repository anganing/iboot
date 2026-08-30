package com.iboot.pf.module.mdm.api.resource.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资源能力 Response DTO
 *
 * @author iboot
 */
@Data
public class ResourceCapabilityRespDTO {

    /** 资源能力编号 */
    private Long id;

    /** 生产资源编号 */
    private Long resId;

    /** 标准工序编号 */
    private Long opId;

    /** 是否首选资源 */
    private Boolean preferredFlag;

    /** 该资源做该工序的专属效率系数 */
    private BigDecimal efficiency;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
