package com.iboot.pf.module.mes.api.optask.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 工序任务跨模块只读事实。 */
@Data
public class OpTaskRespDTO {

    private Long id;
    private String taskNo;
    private Long prodOrderId;
    private Long workOrderId;
    private Long routeOpId;
    private Long opId;
    private String opCode;
    private String opName;
    private Integer seq;
    private String parallelGroup;
    private String execMode;
    private String qcMode;
    private Boolean keyFlag;
    private BigDecimal plannedQty;
    private BigDecimal completedQty;
    private BigDecimal scrapQty;
    private Long resId;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String status;
}
