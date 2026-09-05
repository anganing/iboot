package com.iboot.pf.module.mes.service.optask;

import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;

/**
 * 工序任务领域动作。
 *
 * <p>与 codegen 的 {@link OpTaskService} 并存，分工是硬的：codegen Service 负责建任务、改任务、删任务与查询，
 * 本服务负责一切改状态的动作。工序任务状态只能由这里推进，表单不许直填——表单能填状态就意味着开工门禁、
 * 报工累计、完工数量校验、后序串联全部可以被绕过。
 *
 * <p>五个动作共同遵守：状态由 {@link com.iboot.pf.module.mes.enums.MesOpTaskStateMachine} 裁决、
 * 更新带 {@code version} 乐观锁条件、每次变更经
 * {@link com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder} 留痕、
 * 幂等走 {@link com.iboot.pf.module.mes.service.command.MesCommandService}。
 *
 * <p>动作表见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程五：工序动作与开工门禁"。
 */
public interface MesOpTaskDomainService {

    /**
     * 开工：{@code READY} -&gt; {@code RUNNING}。
     *
     * <p>三件事：过开工门禁（{@link com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker}）、
     * 写实际开始时间与在制流水、级联把作业工单与生产工单也推到 {@code RUNNING}。
     *
     * <p>级联是"开工后三级状态一致"的唯一保证：少了它，车间在干活而看板上工单还显示待开工。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{taskNo}:START_OP_TASK:0}
     */
    void startOpTask(MesOpTaskActionBO bo);

    /**
     * 暂停：{@code RUNNING} -&gt; {@code PAUSED}。
     *
     * @param bo 动作参数，{@code reason} 记暂停原因，幂等键格式 {@code UI:{taskNo}:PAUSE_OP_TASK:0}
     */
    void pauseOpTask(MesOpTaskActionBO bo);

    /**
     * 恢复：{@code PAUSED} -&gt; {@code RUNNING}。
     *
     * <p>质量处置挂起（{@code HOLD}）的恢复走同一个动作，状态机把两条边都挂在 {@code resume} 上。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{taskNo}:RESUME_OP_TASK:0}
     */
    void resumeOpTask(MesOpTaskActionBO bo);

    /**
     * 完工：{@code RUNNING} -&gt; {@code DONE}。
     *
     * <p>四件事：按 {@code MesBusinessRules.validateFinishQty} 校验累计合格数 + 累计报废数 = 计划数、
     * 写实际结束时间与在制流水、沿 {@code next_task_id} 把后序工序推到 {@code READY}（后序在并行组里时
     * 整组一起推）、把进度上卷到作业工单与生产工单。
     *
     * <p>完工不写数量：数量只由报工推进，这里只校验它凑齐了。
     *
     * @param bo 完工参数，幂等键格式 {@code UI:{taskNo}:FINISH_OP_TASK:0}
     */
    void finishOpTask(MesOpTaskFinishBO bo);

    /**
     * 跳序：{@code PENDING} / {@code READY} -&gt; {@code SKIPPED}。
     *
     * <p>**本任务只声明接口，实现在 T1-17**：跳序要按 {@code skip_policy} 分三种策略处置
     * （直接拒绝 / 填原因立即生效 / 走 BPM 审批），还要写 {@code mes_op_skip_record}，
     * 与另外四个动作不是一个量级，混在本任务里做会把两件事都做半截。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{taskNo}:SKIP_OP_TASK:0}
     * @throws UnsupportedOperationException 实现落在 T1-17
     */
    void skipOpTask(MesOpTaskActionBO bo);

}
