package com.iboot.pf.module.mes.service.qctask.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 质检任务生命周期动作参数，指派、开始检验、提交结论、取消共用。
 *
 * <p>**目标状态不由调用方指定**：动作名决定目标状态，能不能迁移过去由
 * {@link com.iboot.pf.module.mes.enums.MesQcTaskStateMachine} 裁决。入参带目标状态就等于把状态机
 * 降级成建议，调用方可以把任务推到任意状态，检验员指派、数量校验全部可以绕过。
 *
 * <p>四个动作都是状态推进型，没有新编号可返回，继承 {@link MesCommandReqDTO} 走命令幂等。
 * 幂等键留空时由服务端按 {@code {来源模块}:{质检任务号}:{命令类型}:0} 派生，与 MES 既有动作的
 * 四段格式一致，例如 {@code UI:QCT0001:ASSIGN_QC_TASK:0}。
 *
 * <p>提交结论有额外的结论与数量载荷，见
 * {@link MesQcTaskConclusionBO}（与工序域 {@code MesOpTaskFinishBO extends MesOpTaskActionBO}
 * 同构：共用动作骨架，专用动作加字段）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesQcTaskActionBO extends MesCommandReqDTO {

    /** 质检任务编号，必填。 */
    private Long taskId;

    /** 指派检验员编号，仅指派动作使用。 */
    private Long inspectorId;

    /** 动作原因或备注，可空。取消时说明来源业务为什么取消，写进任务 remark 追溯。 */
    private String remark;

}
