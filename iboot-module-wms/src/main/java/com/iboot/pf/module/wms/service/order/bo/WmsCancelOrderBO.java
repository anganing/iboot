package com.iboot.pf.module.wms.service.order.bo;

import lombok.Data;

/** 取消单据的内部参数，入库、出库、转移单共用。 */
@Data
public class WmsCancelOrderBO {

    /** 命令幂等键，格式：sourceModule:sourceBizNo:action。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 单据编号。 */
    private Long orderId;

    /** 单据号，与 orderId 二选一。 */
    private String orderNo;

    /** 取消原因。 */
    private String reason;

}
