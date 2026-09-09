package com.iboot.pf.module.mes.service.qcplan.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验方案状态动作参数，发布、停用共用。
 *
 * <p>**目标状态不由调用方指定**：动作名决定目标状态，能不能迁移过去由
 * {@link com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum} 的当前状态校验裁决。
 * 入参带目标状态就等于把状态机变成建议，调用方可以把方案推到任意状态。
 *
 * <p>两个动作都是状态推进型，没有新编号可返回，继承 {@link MesCommandReqDTO} 走命令幂等。
 * 幂等键留空时由服务端按 {@code {来源模块}:{方案编码}:{命令类型}:0} 派生，例如
 * {@code UI:QCP0001:PUBLISH_QC_PLAN:0}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesQcPlanActionBO extends MesCommandReqDTO {

    /** 检验方案编号，必填。 */
    private Long planId;

}
