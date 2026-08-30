package com.iboot.pf.module.mdm.api.route.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工艺路线工序 Response DTO
 *
 * @author iboot
 */
@Data
public class RouteOpRespDTO {

    /** 路线工序编号 */
    private Long id;

    /** 工艺路线编号 */
    private Long routeId;

    /** 工序顺序 */
    private Integer seq;

    /** 标准工序编号 */
    private Long opId;

    /** 工作中心编号 */
    private Long wcId;

    /** 覆盖准备工时（分钟），为空继承标准工序 */
    private Integer overrideSetupMinutes;

    /** 覆盖单件加工工时（分钟），为空继承标准工序 */
    private BigDecimal overrideStdMinutesPerUnit;

    /** 覆盖排队等待时间（分钟），为空继承标准工序 */
    private Integer overrideQueueMinutes;

    /** 覆盖转移时间（分钟），为空继承标准工序 */
    private Integer overrideMoveMinutes;

    /** 并行组 */
    private String parallelGroup;

    /** 是否关键工序 */
    private Boolean keyFlag;

    /** 是否首件检验 */
    private Boolean faiFlag;

}
