package com.iboot.pf.module.mes.service.opskiprecord;

import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesRevokeSkipBO;

/**
 * 工序跳序领域动作。
 *
 * <p>与 codegen 的 {@code OpSkipRecordService} 并存，分工是硬的：codegen Service 负责建记录、改记录、
 * 删记录与查询，本服务负责按 {@code skip_policy} 决定这次跳序能不能生效、以及生效时怎么带动工序任务。
 *
 * <p>三种策略的行为差异（见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"跳序"）：
 * <table>
 *     <tr><th>{@code skip_policy}</th><th>跳过记录</th><th>工序任务状态</th><th>后序</th></tr>
 *     <tr><td>{@code NOT_ALLOWED}</td><td>不写</td><td>不变</td><td>不推</td></tr>
 *     <tr><td>{@code ALLOW_WITH_REASON}</td><td>{@code EFFECTIVE} / {@code NOT_REQUIRED}</td>
 *         <td>-&gt; {@code SKIPPED}</td><td>推到 {@code READY}</td></tr>
 *     <tr><td>{@code APPROVAL_REQUIRED}</td><td>{@code PENDING} / {@code PENDING}</td>
 *         <td><b>不变</b></td><td>不推</td></tr>
 * </table>
 * {@code APPROVAL_REQUIRED} 下工序状态不能变，这是这一档存在的全部意义：审批还没过就把工序置成
 * 已跳过，等于审批人还没看车间就已经跳过去了，驳回也追不回来。
 *
 * <p>入口留在 {@code MesOpTaskDomainService.skipOpTask}，命令幂等与后序推进都在那一侧：
 * 后序推进要用它的并行组逻辑（后序在并行组里要整组一起推、组的范围按生产工单取），在这里另写一份必然漏项。
 */
public interface MesOpSkipDomainService {

    /**
     * 按策略处置一次跳序，写跳过记录，立即生效时把工序任务推到 {@code SKIPPED} 并写 {@code SKIP} 流水。
     *
     * <p>不负责命令幂等，也不负责推后序：调用方（{@code MesOpTaskDomainServiceImpl.skipOpTask}）
     * 已经在命令幂等里，后序推进用它自己的那份逻辑。
     *
     * @param task        工序任务
     * @param prodOrderId 生产工单编号，在制流水的主维度
     * @param bo          跳序参数
     * @param idempotencyKey 跳序动作的幂等键，流水按它留痕
     * @return 是否立即生效；{@code false} 表示只落了待审批记录，工序状态没动
     */
    boolean skip(OpTaskDO task, Long prodOrderId, MesOpSkipBO bo, String idempotencyKey);

    /**
     * 撤销跳序：记录置 {@code REVOKED}，已生效的把工序任务从 {@code SKIPPED} 退回 {@code READY}，并写反向流水。
     *
     * <p>待审批的记录也能撤（相当于撤回申请），此时工序任务从来没变过 {@code SKIPPED}，只改记录。
     *
     * @param bo 撤销参数，幂等键格式 {@code UI:{taskNo}:REVOKE_OP_SKIP:0}
     */
    void revokeSkip(MesRevokeSkipBO bo);

    /**
     * 回写工序跳序的审批终态（三层幂等）。
     *
     * <p>通过：记录置 {@code EFFECTIVE} / {@code APPROVED}，并在审批终态事务提交后执行生效
     * （工序推 {@code SKIPPED}、写 {@code SKIP} 流水、推后序）；驳回与撤销：记录置 {@code REVOKED} /
     * {@code REJECTED}，工序任务一行不动。
     *
     * <p>审批期间工序任务状态不变是这一档存在的全部意义：没批就把工序置成已跳过，等于审批人还没看
     * 车间就已经跳过去了，驳回也追不回来。
     *
     * @param skipRecordId      跳序记录编号（即 businessKey）
     * @param processInstanceId 流程实例编号
     * @param status            流程终态，取值见 {@code BpmProcessInstanceStatusEnum}
     * @param reason            审批意见或驳回原因
     */
    void updateSkipApprovalStatus(Long skipRecordId, String processInstanceId, Integer status, String reason);

    /**
     * 让一条已批准的跳序记录生效：工序任务推 {@code SKIPPED}、写 {@code SKIP} 流水、推后序。
     *
     * <p>这是审批通过后被延迟到事务提交后执行的那一步，也是<b>人工重试入口</b>：动作失败时记录已经
     * 是 {@code EFFECTIVE} 而工序还停在原状态，这个不一致可查（{@code mes_op_skip_record.status} 与
     * {@code mes_op_task.status} 对不上）且可补，重跑本方法即可，幂等由工序任务的乐观锁与
     * {@code mes_wip_txn} 的幂等键唯一索引共同保证。
     *
     * @param skipRecordId 跳序记录编号
     */
    void takeEffectApprovedSkip(Long skipRecordId);

}
