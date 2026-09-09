package com.iboot.pf.module.mes.service.qctask;

import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;

import java.util.Optional;

/**
 * 质检任务领域动作。
 *
 * <p>与 codegen 的 {@link QcTaskService} 并存，分工是硬的：codegen Service 负责任务数据的
 * 增删改查，本服务负责"任务怎么来"——五个检验点的业务动作只调 {@link #createQcTask}，
 * 规则解析、方案版本锁定、抽样计算、检验项展开全部收敛在这里，业务代码里不出现任何一条
 * 质检 if。
 *
 * <p>任务生命周期动作（指派、开始检验、提交结论、取消）由 T2-04 在本接口追加，本任务不实现。
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

}
