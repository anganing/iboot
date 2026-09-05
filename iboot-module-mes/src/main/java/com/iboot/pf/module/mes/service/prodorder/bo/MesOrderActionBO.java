package com.iboot.pf.module.mes.service.prodorder.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产工单状态动作参数，挂起、恢复、取消共用。
 *
 * <p>**目标状态不由调用方指定**：动作名决定目标状态，能不能迁移过去由
 * {@link com.iboot.pf.module.mes.enums.MesProdOrderStateMachine} 裁决。
 * 入参带目标状态就等于把状态机变成建议，调用方可以直接把工单推到任意状态。
 *
 * <p>三个动作都是状态推进型，没有新编号可返回，继承 {@link MesCommandReqDTO} 走命令幂等。
 * 幂等键留空时由服务端按 {@code {来源模块}:{工单号}:{命令类型}:0} 派生，例如
 * {@code UI:PO-0001:HOLD_PROD_ORDER:0}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesOrderActionBO extends MesCommandReqDTO {

    /** 生产工单编号，必填。 */
    private Long prodOrderId;

    /** 动作原因，写进在制流水的 remark；挂起与取消建议填写。 */
    private String reason;

}
