package com.iboot.pf.module.wms.service.order.bo;

import lombok.Data;

/**
 * 单据提交参数。
 *
 * <p>提交是状态推进动作，没有新编号可返回，因此走 {@code WmsCommandService.executeVoid} 判重：
 * 界面连点两次、上游重试都只推进一次状态。
 */
@Data
public class WmsSubmitOrderBO {

    /** 幂等键，格式 sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    private String idempotencyKey;
    /** 来源模块，取值见 WmsSourceModuleEnum。 */
    private String sourceModule;
    /** 来源业务单号。 */
    private String sourceBizNo;
    /** 单据编号，与 orderNo 二选一。 */
    private Long orderId;
    /** 单据号，与 orderId 二选一。 */
    private String orderNo;
    /** 操作人编号。 */
    private Long operatorId;
    /** 备注。 */
    private String remark;

}
