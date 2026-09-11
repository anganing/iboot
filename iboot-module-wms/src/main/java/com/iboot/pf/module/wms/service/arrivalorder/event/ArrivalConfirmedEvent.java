package com.iboot.pf.module.wms.service.arrivalorder.event;

import lombok.Value;

import java.math.BigDecimal;

/**
 * 到货确认后的单行事件。
 *
 * <p>WMS 不依赖 MES；MES 作为 WMS 的下游模块监听本事件并按质量规则决定是否创建 IQC 任务。
 */
@Value
public class ArrivalConfirmedEvent {

    Long arrivalLineId;
    String arrivalNo;
    Long supplierId;
    Long matId;
    Long uomId;
    Long batchId;
    BigDecimal qty;
    String lotNo;
}
