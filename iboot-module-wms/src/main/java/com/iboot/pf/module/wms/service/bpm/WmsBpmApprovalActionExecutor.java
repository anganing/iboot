package com.iboot.pf.module.wms.service.bpm;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 审批终态回调的领域动作执行器。
 *
 * <p>与 MES 侧的 {@code MesBpmApprovalActionExecutor} 同构，两模块不互相依赖所以各存一份；
 * 两份的实现约束必须一起改，否则跨模块的审批回调会出现"一边不回滚、一边回滚"。
 *
 * <p>解决的问题只有一个：<b>审批已通过但领域动作失败时，不能把审批终态一起回滚</b>。
 * {@code BpmProcessInstanceStatusEventListener#onEvent} 跑在 Flowable 的事务里，业务回写方法以
 * {@code REQUIRED} 传播加入其中。如果回调里直接执行领域动作（盘盈入库 / 盘亏出库过账），动作一旦失败
 * 会把整个事务标记为 rollback-only —— {@code onEvent} 虽然 catch 住了异常不向外抛，Flowable 在最外层
 * 提交时仍会抛 {@code UnexpectedRollbackException}，表现为"审批人点了通过，盘点单却还停在审批中"。
 * 这正是 {@code dev-docs/业务流程/07-BPM审批接入流程.md} 要求避免的事：
 * 「领域动作失败时单据置一个审批通过但执行失败的状态，由人工重试执行，不要让流程回滚」。
 *
 * <p>两条实现约束，缺一条这个类就没有意义：
 *
 * <ol>
 *   <li><b>挂 afterCommit 而不是就地 {@code REQUIRES_NEW}。</b>回写方法里的第三层条件 UPDATE
 *   （{@code WHERE approval_status = RUNNING}）已经持有盘点单的行锁，同一个方法内再起
 *   {@code REQUIRES_NEW} 子事务去改同一行，子事务等的是父事务尚未提交的锁——自己等自己，
 *   只会锁等待超时。挂到 afterCommit 之后，审批终态已落库、锁已释放，动作才可能在独立事务里安全重做。</li>
 *   <li><b>afterCommit 里必须显式开新事务。</b>Spring 触发 afterCommit 回调时，当前事务的资源尚未从
 *   {@code TransactionSynchronizationManager} 清理（清理发生在 {@code cleanupAfterCompletion}），
 *   此时以 {@code REQUIRED} 传播会"加入"那个已经提交完的事务，过账的写入永远不会落库。
 *   因此这里固定用 {@link TransactionDefinition#PROPAGATION_REQUIRES_NEW}。</li>
 * </ol>
 *
 * <p>动作抛出的异常在本类内部吞掉并记 error 日志：afterCommit 的异常会直接传播给流程提交方
 * （审批人那次"通过"请求会 500），比回滚流程更难看。盘点差异的重试入口是既有的
 * {@code confirmCountDiff} 端点：它的 {@code approval_status = APPROVE} 前置校验在审批通过后即放行，
 * 未过账的差异行仍然可被逐行补齐。
 */
@Slf4j
@Component
public class WmsBpmApprovalActionExecutor {

    @Resource
    private PlatformTransactionManager transactionManager;

    /** 独立新事务模板，动作不与 Flowable 的事务共享回滚标记。 */
    private TransactionTemplate newTransaction;

    @PostConstruct
    void init() {
        newTransaction = new TransactionTemplate(transactionManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 把领域动作挂到当前事务提交之后执行；调用处没有事务时立即在新事务里执行。
     *
     * @param actionName 动作的业务描述，进日志用，要能让人看懂"哪张单的什么事没做成"
     * @param action     领域动作
     */
    public void executeAfterCommit(String actionName, Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runQuietly(actionName, action);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                runQuietly(actionName, action);
            }

        });
    }

    private void runQuietly(String actionName, Runnable action) {
        try {
            newTransaction.executeWithoutResult(status -> action.run());
        } catch (Exception e) {
            log.error("[executeAfterCommit][审批终态已落库，但领域动作执行失败。动作={}，"
                    + "单据停在【审批已通过但未执行完】的状态，请人工重试执行]", actionName, e);
        }
    }

}
