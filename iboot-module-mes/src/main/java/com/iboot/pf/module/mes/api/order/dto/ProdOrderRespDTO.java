package com.iboot.pf.module.mes.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 生产工单跨模块只读事实。 */
@Data
public class ProdOrderRespDTO {

    private Long id;
    private String orderNo;
    private Long demandId;
    private Long plantId;
    private Long workshopId;
    private Long projectId;
    private Long projectTaskId;
    private Long matId;
    private String matCode;
    private String matSpec;
    private BigDecimal qty;
    private BigDecimal completedQty;
    private BigDecimal scrapQty;
    private Long uomId;
    private Long bomId;
    private String bomVersion;
    private Long routeId;
    private String routeVersion;
    private LocalDateTime dueDate;
    private Integer priority;
    private String kitStatus;
    private String status;
    private LocalDateTime releaseTime;
}
