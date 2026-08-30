package com.iboot.pf.module.mdm.api.route.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工艺路线工序用料 Response DTO
 *
 * @author iboot
 */
@Data
public class RouteOpMatRespDTO {

    /** 工序用料编号 */
    private Long id;

    /** 路线工序编号 */
    private Long routeOpId;

    /** 物料编号 */
    private Long matId;

    /** 数量 */
    private BigDecimal qty;

    /** 损耗率 */
    private BigDecimal scrapRate;

    /** 供料方式：PICK-领料，BACKFLUSH-倒冲，PHANTOM-虚拟件，DIRECT-直送线边 */
    private String issueMethod;

    /** 是否必须开工前发料 */
    private Boolean mustIssueBeforeStart;

    /** 计量单位编号 */
    private Long uomId;

}
