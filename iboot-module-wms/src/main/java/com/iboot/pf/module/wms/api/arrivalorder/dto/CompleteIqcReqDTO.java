package com.iboot.pf.module.wms.api.arrivalorder.dto;

import lombok.Data;

import java.math.BigDecimal;

/** IQC 结论回写请求。 */
@Data
public class CompleteIqcReqDTO {

    private Long arrivalLineId;
    private Long qcTaskId;
    private String conclusion;
    private BigDecimal qtyQualified;
    private BigDecimal qtyRejected;
}
