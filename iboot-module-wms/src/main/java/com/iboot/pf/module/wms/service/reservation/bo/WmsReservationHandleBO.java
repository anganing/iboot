package com.iboot.pf.module.wms.service.reservation.bo;

import lombok.Data;

import java.math.BigDecimal;

/** 释放与核销预留的内部参数。 */
@Data
public class WmsReservationHandleBO {

    /** 命令幂等键，格式：sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;

    /** 预留编号。 */
    private Long reservationId;

    /** 预留单号，与 reservationId 二选一。 */
    private String reservationNo;

    /** 本次释放或核销数量，基准单位；为空表示全部剩余数量。 */
    private BigDecimal qty;

    /** 来源业务类型，写入流水。 */
    private String bizType;

    /** 来源业务单号，写入流水。 */
    private String bizNo;

    /** 来源业务行号，写入流水。 */
    private String bizLineNo;

    /** 备注。 */
    private String remark;

}
