package com.iboot.pf.module.mes.service.optask.gate.impl;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_PREV_NOT_DONE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 门禁一前序完成的测试。
 *
 * <p>失败用例断言的不只是抛错，还断言消息里带得出工序号与工序名称：车间拿到"前序未完工"四个字
 * 什么也做不了，必须知道是哪一道没做完。
 */
@Import(PrevDoneChecker.class)
class PrevDoneCheckerTest extends BaseDbUnitTest {

    private static final Long WORK_ORDER_ID = 9001L;
    private static final String GROUP_A = "GA";

    @Resource
    private PrevDoneChecker checker;
    @Resource
    private OpTaskMapper opTaskMapper;

    @Test
    void testEnabled_firstOpTaskSkipped() {
        // 首道工序没有前序可等
        OpTaskDO first = insertTask(10, "下料", MesConstants.NONE_ID, null, MesOpTaskStatusEnum.READY);

        assertFalse(checker.enabled(first, new MesStartGateContext()));
    }

    @Test
    void testCheck_prevRunningRejectedWithSeqAndOpName() {
        // 准备：前序还在加工
        OpTaskDO prev = insertTask(10, "下料", MesConstants.NONE_ID, null, MesOpTaskStatusEnum.RUNNING);
        OpTaskDO current = insertTask(20, "折弯", prev.getId(), null, MesOpTaskStatusEnum.READY);

        // 调用并断言：消息带工序号与工序名称，车间照着就能在现场找到那道工序
        assertTrue(checker.enabled(current, new MesStartGateContext()));
        assertServiceException(() -> checker.check(current, new MesStartGateContext()),
                OP_TASK_PREV_NOT_DONE, "第10道 下料（加工中）");
    }

    @Test
    void testCheck_prevDonePassed() {
        // 准备：前序已完工
        OpTaskDO prev = insertTask(10, "下料", MesConstants.NONE_ID, null, MesOpTaskStatusEnum.DONE);
        OpTaskDO current = insertTask(20, "折弯", prev.getId(), null, MesOpTaskStatusEnum.READY);

        // 调用：不抛异常即通过
        checker.check(current, new MesStartGateContext());
    }

    @Test
    void testCheck_prevSkippedPassed() {
        // 准备：前序被跳序。跳过的工序不做了，后序照样能开工
        OpTaskDO prev = insertTask(10, "下料", MesConstants.NONE_ID, null, MesOpTaskStatusEnum.SKIPPED);
        OpTaskDO current = insertTask(20, "折弯", prev.getId(), null, MesOpTaskStatusEnum.READY);

        checker.check(current, new MesStartGateContext());
    }

    @Test
    void testCheck_prevCancelledRejected() {
        // 准备：前序被取消。取消是异常终止，整条链通常要重排，当成已完成会让后序在废弃的链上开工
        OpTaskDO prev = insertTask(10, "下料", MesConstants.NONE_ID, null, MesOpTaskStatusEnum.CANCELLED);
        OpTaskDO current = insertTask(20, "折弯", prev.getId(), null, MesOpTaskStatusEnum.READY);

        assertServiceException(() -> checker.check(current, new MesStartGateContext()),
                OP_TASK_PREV_NOT_DONE, "第10道 下料（已取消）");
    }

    @Test
    void testCheck_parallelGroupRequiresAllDone() {
        // 准备：前序属于并行组，组内两道工序一道完工一道还在加工
        OpTaskDO groupDone = insertTask(10, "粗车", MesConstants.NONE_ID, GROUP_A, MesOpTaskStatusEnum.DONE);
        insertTask(11, "钻孔", MesConstants.NONE_ID, GROUP_A, MesOpTaskStatusEnum.RUNNING);
        OpTaskDO current = insertTask(20, "精车", groupDone.getId(), null, MesOpTaskStatusEnum.READY);

        // 调用并断言：并行组的语义是"这几道可以同时做"，后序要的是它们合起来的产出，
        // 只完成一道就往下走会拿到半成品。消息只列没完工的那一道
        assertServiceException(() -> checker.check(current, new MesStartGateContext()),
                OP_TASK_PREV_NOT_DONE, "第11道 钻孔（加工中）");
    }

    @Test
    void testCheck_parallelGroupAllDonePassed() {
        // 准备：并行组全部完工或跳过
        OpTaskDO groupDone = insertTask(10, "粗车", MesConstants.NONE_ID, GROUP_A, MesOpTaskStatusEnum.DONE);
        insertTask(11, "钻孔", MesConstants.NONE_ID, GROUP_A, MesOpTaskStatusEnum.SKIPPED);
        OpTaskDO current = insertTask(20, "精车", groupDone.getId(), null, MesOpTaskStatusEnum.READY);

        checker.check(current, new MesStartGateContext());
    }

    @Test
    void testCheck_parallelGroupScopedToSameWorkOrder() {
        // 准备：另一张作业工单里有同名并行组且未完工。parallel_group 只是字符串标记，
        // 不限工单的话别的工单的同名组会被算进来，把本来能开的工挡住
        OpTaskDO groupDone = insertTask(10, "粗车", MesConstants.NONE_ID, GROUP_A, MesOpTaskStatusEnum.DONE);
        OpTaskDO otherWorkOrderTask = insertTask(10, "别单粗车", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.RUNNING);
        opTaskMapper.updateById(new OpTaskDO().setId(otherWorkOrderTask.getId())
                .setWorkOrderId(WORK_ORDER_ID + 1));
        OpTaskDO current = insertTask(20, "精车", groupDone.getId(), null, MesOpTaskStatusEnum.READY);

        checker.check(current, new MesStartGateContext());
    }

    private OpTaskDO insertTask(int seq, String opName, Long prevTaskId, String parallelGroup,
                                MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + seq + "-" + System.nanoTime())
                .workOrderId(WORK_ORDER_ID)
                .workOrderNo("WO-0001")
                .routeOpId(100L + seq)
                .opId(200L + seq)
                .opCode("OP" + seq)
                .opName(opName)
                .seq(seq)
                .parallelGroup(parallelGroup)
                .prevTaskId(prevTaskId)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

}
