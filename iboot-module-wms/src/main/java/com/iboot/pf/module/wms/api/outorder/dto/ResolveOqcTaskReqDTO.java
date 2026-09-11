package com.iboot.pf.module.wms.api.outorder.dto;

import lombok.Data;

/** OQC 规则解析结果回写请求。 */
@Data
public class ResolveOqcTaskReqDTO {

    private Long outOrderId;
    private Long qcTaskId;
}
