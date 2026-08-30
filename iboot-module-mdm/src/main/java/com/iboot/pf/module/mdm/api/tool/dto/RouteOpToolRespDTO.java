package com.iboot.pf.module.mdm.api.tool.dto;

import lombok.Data;

/**
 * 工序工装 Response DTO
 *
 * @author iboot
 */
@Data
public class RouteOpToolRespDTO {

    /** 记录编号 */
    private Long id;

    /** 路线工序编号 */
    private Long routeOpId;

    /** 工装编号 */
    private Long toolId;

    /** 需求数量 */
    private Integer qty;

    /** 是否必备 */
    private Boolean requiredFlag;

}
