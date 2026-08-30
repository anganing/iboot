package com.iboot.pf.module.wms.service.count.bo;

import lombok.Data;

/** 确认盘点差异的内部参数。 */
@Data
public class WmsConfirmCountDiffBO {

    /** 命令幂等键，格式：sourceModule:sourceBizNo:action。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 盘点单编号。 */
    private Long orderId;

    /** 盘点单号，与 orderId 二选一。 */
    private String countNo;

    /** 备注。 */
    private String remark;

}
