package com.iboot.pf.module.mes.service.qctask;

import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;

import java.util.Optional;

/**
 * 质检任务领域动作。
 *
 * <p>与 codegen 的 {@link QcTaskService} 并存，分工是硬的：codegen Service 负责任务数据的
 * 增删改查，本服务负责"任务怎么来、怎么走"——五个检验点的业务动作只调 {@link #createQcTask}，
 * 规则解析、方案版本锁定、抽样计算、检验项展开全部收敛在这里，业务代码里不出现任何一条
 * 质检 if；任务的生命周期推进只经 {@link com.iboot.pf.module.mes.enums.MesQcTaskStateMachine}，
 * 表单不许直填状态。
 *
 * <p>四个生命周期动作共用一套骨架：命令幂等 -&gt; 状态机裁决目标状态 -&gt; 带乐观锁条件落库，
 * 与工序任务域动作同构。
 *
 * @author iboot
 */
public interface MesQcTaskDomainService {

    /**
     * 按规则解析结果创建质检任务。
     *
     * <p>流程见 {@code dev-docs/业务流程/05-MES质量子域流程.md} 的"规则解析"流程图后半段：
     * 解析规则 -&gt; 未命中直接返回空（业务动作放行，不检也是规则说了算）-&gt; 命中则建
     * {@code mes_qc_task}（任务号走编码规则、锁 {@code qc_plan_id + qc_plan_version}、
     * 快照物料与批次）-&gt; 按抽样方式算 {@code sample_qty} -&gt; 按方案展开启用的检验项，
     * 为每个检验项 × 每个样本预生成 {@code mes_qc_result} 空行。
     *
     * <p>幂等键 {@code MES:{bizNo}:QC_CREATE:{triggerPoint}} 落
     * {@code mes_qc_task.idempotency_key}：创建型动作不走
     * {@link com.iboot.pf.module.mes.service.command.MesCommandService}，业务表自带
     * 幂等列判重，同一业务单在同一检验点重复触发只生成一个任务。
     *
     * @param bo 创建参数，触发点与业务上下文（含送检数量）从它取
     * @return 质检任务编号；规则未命中返回 {@link Optional#empty()}，调用方继续原业务动作
     */
    Optional<Long> createQcTask(MesCreateQcTaskBO bo);

    /**
     * 指派检验员：{@code CREATED} -&gt; {@code ASSIGNED}，写 {@code assigned_inspector_id}。
     *
     * <p>指派是任务流转的第一步，检验任务池按已指派/未指派分栏时靠这个动作区分。
     *
     * @param bo 动作参数，{@code inspectorId} 必填
     */
    void assignQcTask(MesQcTaskActionBO bo);

    /**
     * 开始检验：{@code ASSIGNED} -&gt; {@code INSPECTING}，写 {@code inspect_start_time}。
     *
     * <p>开检后任务进入可录入结果的状态（T2-05 的结果录入按 {@code INSPECTING} 校验），
     * 同时取消出边关闭——料已检、数已录的检验不能销毁记录，不合格走处置流程。
     *
     * @param bo 动作参数
     */
    void startInspect(MesQcTaskActionBO bo);

    /**
     * 提交结论：{@code RESULT_POSTING} -&gt; {@code DONE}，写 {@code inspect_end_time}
     * {@code conclusion} {@code qty_qualified} {@code qty_rejected}。
     *
     * <p>校验合格数 + 不合格数 = 抽检数（{@code MesBusinessRules.validateQualityQty}）：
     * 抽检的每一件都必须有去向。结论提交后任务落定，下游门禁（五个检验点各自的 Handler）
     * 按结论放行或拦截——分派钩子在实现末尾预留，T2-07 ~ T2-11 逐点接入。
     *
     * @param bo 结论参数，含结论与合格、不合格数量
     */
    void submitConclusion(MesQcTaskConclusionBO bo);

    /**
     * 取消质检任务：{@code CREATED} / {@code ASSIGNED} -&gt; {@code CANCELLED}。
     *
     * <p>来源业务取消（到货单撤回、工序任务取消）时调用。仅未开检的任务允许取消：
     * 检验已开始则产生过检验事实，取消等于销毁记录，必须走完检验与处置流程。
     *
     * @param bo 动作参数
     */
    void cancelQcTask(MesQcTaskActionBO bo);

}
