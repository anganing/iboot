package com.iboot.pf.module.mes.service.optask.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序任务状态动作参数，开工、暂停、恢复、跳序共用。
 *
 * <p>**目标状态不由调用方指定**：动作名决定目标状态，能不能迁移过去由
 * {@link com.iboot.pf.module.mes.enums.MesOpTaskStateMachine} 裁决。入参带目标状态就等于把状态机变成建议，
 * 调用方可以直接把工序推到任意状态，开工门禁、报工数量、完工校验全部可以绕过。
 *
 * <p>四个动作都是状态推进型，没有新编号可返回，继承 {@link MesCommandReqDTO} 走命令幂等。
 * 幂等键留空时由服务端按 {@code {来源模块}:{工序任务号}:{命令类型}:0} 派生，例如
 * {@code UI:OT-0001:START_OP_TASK:0}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesOpTaskActionBO extends MesCommandReqDTO {

    /** 工序任务编号，必填。 */
    private Long opTaskId;

    /**
     * 操作者用户编号，可为空。
     *
     * <p>开工时传给开工门禁校验技能资质：现场换人常有，界面上选的人比派工记录更准。
     * 留空时门禁自己从 {@code mes_dispatch_task} 取，取不到就按"未指定作业人员"拦下。
     */
    private Long operatorId;

    /** 动作原因，写进在制流水的 remark。暂停必填（要记清为什么停），其余动作可空。 */
    private String reason;

}
