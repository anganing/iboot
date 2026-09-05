package com.iboot.pf.module.wms.service.arrivalorder.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 到货确认参数。
 *
 * <p>确认到货是状态推进动作，没有新编号可返回，因此走 {@code WmsCommandService.executeVoid} 判重：
 * 界面连点两次、上游重试都只注册一次批次。幂等键格式 {@code UI:{arrivalNo}:CONFIRM_ARRIVAL:0}。
 */
@Data
public class WmsConfirmArrivalBO {

    /** 命令幂等键，格式 sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 到货登记编号，与 arrivalNo 二选一。 */
    private Long arrivalId;

    /** 到货登记单号，与 arrivalId 二选一。 */
    private String arrivalNo;

    /** 操作人编号。 */
    private Long operatorId;

    /** 实际到货时间，为空取当前时间。 */
    private LocalDateTime arrivalTime;

    /** 备注。 */
    private String remark;

}
