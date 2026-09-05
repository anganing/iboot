package com.iboot.pf.module.mes.service.optask.gate.impl;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
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
 *
 * <p>并行组的两个用例是本类的要害：同组范围是**同一生产工单**而不是同一作业工单。作业工单按工作
 * 中心建，同一并行组的工序会落进不同的作业工单（04 文档的"喷漆 ‖ 标识"），按作业工单取组会让后序
 * 在只完成一道并行工序时就放行。
 */
@Import(PrevDoneChecker.class)
class PrevDoneCheckerTest extends BaseDbUnitTest {

    private static final Long PROD_ORDER_ID = 8001L;
    private static final Long OTHER_PROD_ORDER_ID = 8002L;
    private static final String GROUP_A = "GA";

    @Resource
    private PrevDoneChecker checker;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;

    /** 同一生产工单下的两张作业工单，对应两个工作中心 */
    private Long workOrderId;
    private Long siblingWorkOrderId;
    /** 另一张生产工单的作业工单，用来验证跨生产工单的隔离 */
    private Long otherOrderWorkOrderId;

    @BeforeEach
    void setUp() {
        workOrderId = insertWorkOrder(PROD_ORDER_ID, 501L).getId();
        siblingWorkOrderId = insertWorkOrder(PROD_ORDER_ID, 502L).getId();
        otherOrderWorkOrderId = insertWorkOrder(OTHER_PROD_ORDER_ID, 501L).getId();
    }

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
    void testCheck_parallelGroupSpansWorkOrdersRequiresAllDone() {
        // 准备：并行组跨两张作业工单——喷漆在喷漆房、标识在打标工位，同属 GROUP_A。
        // 这是 04 文档"喷漆 ‖ 标识"的原型，也是下达时按整条路线分组、作业工单按工作中心建的必然结果
        OpTaskDO paintDone = insertTask(workOrderId, 30, "喷漆", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.DONE);
        insertTask(siblingWorkOrderId, 30, "标识", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.RUNNING);
        OpTaskDO assemble = insertTask(workOrderId, 40, "装配", paintDone.getId(), null,
                MesOpTaskStatusEnum.READY);

        // 调用并断言：按作业工单取组会取不到"标识"这道，装配会被误放行。
        // 同组范围必须是同一生产工单，消息里列出的正是另一张作业工单里那道未完工的工序
        assertServiceException(() -> checker.check(assemble, new MesStartGateContext()),
                OP_TASK_PREV_NOT_DONE, "第30道 标识（加工中）");
    }

    @Test
    void testCheck_parallelGroupSpansWorkOrdersAllDonePassed() {
        // 准备：跨作业工单的并行组两道都完工
        OpTaskDO paintDone = insertTask(workOrderId, 30, "喷漆", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.DONE);
        insertTask(siblingWorkOrderId, 30, "标识", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.DONE);
        OpTaskDO assemble = insertTask(workOrderId, 40, "装配", paintDone.getId(), null,
                MesOpTaskStatusEnum.READY);

        checker.check(assemble, new MesStartGateContext());
    }

    @Test
    void testCheck_parallelGroupScopedToSameProdOrder() {
        // 准备：另一张生产工单里有同名并行组且未完工。parallel_group 只是字符串标记，
        // 不限生产工单的话别的单子的同名组会被算进来，把本来能开的工挡住
        OpTaskDO groupDone = insertTask(workOrderId, 10, "粗车", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.DONE);
        insertTask(otherOrderWorkOrderId, 10, "别单粗车", MesConstants.NONE_ID, GROUP_A,
                MesOpTaskStatusEnum.RUNNING);
        OpTaskDO current = insertTask(workOrderId, 20, "精车", groupDone.getId(), null,
                MesOpTaskStatusEnum.READY);

        checker.check(current, new MesStartGateContext());
    }

    private WorkOrderDO insertWorkOrder(Long prodOrderId, Long wcId) {
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .workOrderNo("WO-" + prodOrderId + "-" + wcId)
                .prodOrderId(prodOrderId)
                .prodOrderNo("PO-" + prodOrderId)
                .plantId(1L)
                .workshopId(1L)
                .wcId(wcId)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(MesConstants.ZERO_QTY)
                .status(MesWorkOrderStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    private OpTaskDO insertTask(int seq, String opName, Long prevTaskId, String parallelGroup,
                                MesOpTaskStatusEnum status) {
        return insertTask(workOrderId, seq, opName, prevTaskId, parallelGroup, status);
    }

    private OpTaskDO insertTask(Long ownerWorkOrderId, int seq, String opName, Long prevTaskId,
                                String parallelGroup, MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + seq + "-" + System.nanoTime())
                .workOrderId(ownerWorkOrderId)
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
