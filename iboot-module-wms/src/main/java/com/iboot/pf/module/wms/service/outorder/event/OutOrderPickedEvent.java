package com.iboot.pf.module.wms.service.outorder.event;

import lombok.Value;

import java.math.BigDecimal;

/** 销售出库拣选完成事件，由 MES 监听并按规则决定是否生成 OQC 任务。 */
@Value
public class OutOrderPickedEvent {

    Long outOrderId;
    String outNo;
    Long matId;
    Long uomId;
    BigDecimal qty;
}
