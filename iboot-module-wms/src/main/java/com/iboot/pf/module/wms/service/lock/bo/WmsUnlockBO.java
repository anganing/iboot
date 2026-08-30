package com.iboot.pf.module.wms.service.lock.bo;

import lombok.Data;

import java.math.BigDecimal;

/** 解冻库存的内部参数。 */
@Data
public class WmsUnlockBO {

    /** 解冻幂等键，格式：sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;

    /** 冻结记录编号。 */
    private Long lockId;

    /** 冻结编号，与 lockId 二选一。 */
    private String lockNo;

    /** 本次解冻数量，基准单位；为空表示全部剩余数量。 */
    private BigDecimal qty;

    /** 备注。 */
    private String remark;

}
