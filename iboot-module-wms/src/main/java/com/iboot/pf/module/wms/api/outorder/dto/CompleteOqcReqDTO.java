package com.iboot.pf.module.wms.api.outorder.dto;

import lombok.Data;

/** OQC 合格结论放行请求。 */
@Data
public class CompleteOqcReqDTO {

    private Long outOrderId;
    private Long qcTaskId;
}
