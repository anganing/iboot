package com.iboot.pf.module.mes.service.optask;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.prodreport.MesProgressPropagator;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_FINISH_QTY_MISMATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_KITTED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_REPORT_QTY_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 工序任务五动作测试。
 *
 * <p>开工门禁整条链已由 T1-12 的六个测试类覆盖，这里把它 mock 掉只验证两件事：门禁被调到了、
 * 门禁拦下时一行都没改。重点放在本任务自己的东西：三级状态级联、后序就绪与并行组、乐观锁、幂等。
 *
 * <p>三级级联的断言不看最终状态就完事，还要看在制流水的轨迹：作业工单必须留下
 * {@code PENDING -> READY} 与 {@code READY -> RUNNING} 两条，因为状态机里没有
 * {@code PENDING -> RUNNING} 这条边，一步到位就说明有人绕开状态机硬写了状态。
 */
@Import({MesOpTaskDomainServiceImpl.class, MesProgressPropagator.class,
        MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesOpTaskDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal PLAN_QTY = new BigDecimal("100");
    private static final String GROUP_A = "A";
    private static final Long WC_MACHINE = 201L;
    private static final Long WC_PAINT = 202L;
    private static final Long WC_MARK = 203L;

    /** 工序任务号计数器，保证同一个用例里落的任务号互不相同（生产上由编码规则分配）。 */
    private final AtomicInteger taskSeq = new AtomicInteger();

    @Resource
    private MesOpTaskDomainService domainService;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private MesStartGateChecker startGateChecker;
    @MockitoBean
    private MesCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        Map<String, AtomicInteger> counters = new HashMap<>();
        when(codeService.generate(anyString(), any())).thenAnswer(invocation -> {
            String ruleCode = invocation.getArgument(0);
            return invocation.getArgument(1) != null ? invocation.getArgument(1)
                    : ruleCode + "-" + counters.computeIfAbsent(ruleCode, key -> new AtomicInteger())
                    .incrementAndGet();
        });
    }

    @Test
    void testStartOpTask_threeLevelsRunning() {
        // 准备：齐套已跑过的常态——工序待开工、生产工单已就绪，而作业工单还停在下达时的 PENDING
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.READY);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.PENDING);
        OpTaskDO task = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.READY);

        // 调用
        domainService.startOpTask(startBO(task, null));

        // 断言（验收硬指标）：三级状态全部推到 RUNNING
        OpTaskDO started = opTaskMapper.selectById(task.getId());
        WorkOrderDO startedWorkOrder = workOrderMapper.selectById(workOrder.getId());
        ProdOrderDO startedOrder = prodOrderMapper.selectById(order.getId());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), started.getStatus());
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(), startedWorkOrder.getStatus());
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), startedOrder.getStatus());
        // 三级都写实际开始时间，DO 上的语义是"由首道工序开工写入"
        assertNotNull(started.getActualStartTime());
        assertNotNull(startedWorkOrder.getActualStartTime());
        assertNotNull(startedOrder.getActualStartTime());
        // 乐观锁版本自增，证明走的是带 version 条件的更新；作业工单走了两步所以加 2
        assertEquals(MesConstants.INIT_VERSION + 1, started.getVersion());
        assertEquals(MesConstants.INIT_VERSION + 2, startedWorkOrder.getVersion());
        assertEquals(MesConstants.INIT_VERSION + 1, startedOrder.getVersion());
        // 门禁被调到了
        verify(startGateChecker).check(any(), any());
    }

    @Test
    void testStartOpTask_workOrderGoesThroughReady() {
        // 准备：作业工单 PENDING
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.READY);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.PENDING);
        OpTaskDO task = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.READY);

        // 调用
        domainService.startOpTask(startBO(task, null));

        // 断言：作业工单留下两条流水，PENDING -> READY 与 READY -> RUNNING。
        // 状态机里没有 PENDING -> RUNNING 这条边，只留一条就说明有人绕开状态机硬写了状态；
        // 这两步同时补上了齐套检查漏掉的"作业工单未推进到 READY"（车间派工按 READY 筛）
        List<WipTxnDO> workOrderTxns = txnsOf(MesBizTypeEnum.WORK_ORDER, workOrder.getWorkOrderNo());
        assertEquals(2, workOrderTxns.size());
        assertTrue(hasTransition(workOrderTxns, MesWorkOrderStatusEnum.PENDING, MesWorkOrderStatusEnum.READY));
        assertTrue(hasTransition(workOrderTxns, MesWorkOrderStatusEnum.READY, MesWorkOrderStatusEnum.RUNNING));
        // 级联流水记得出是哪道工序带动的，读流水时这是最需要的线索
        assertEquals(task.getId(), workOrderTxns.get(0).getOpTaskId());
        assertEquals(MesWipTxnTypeEnum.START.name(), workOrderTxns.get(0).getTxnType());
        assertTrue(workOrderTxns.get(0).getRemark().contains("下料"));
    }

    @Test
    void testStartOpTask_prodOrderReleasedGoesThroughReady() {
        // 准备：跳过齐套直接领料开工，生产工单还停在 RELEASED
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.READY);
        OpTaskDO task = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.READY);

        // 调用
        domainService.startOpTask(startBO(task, null));

        // 断言：生产工单也按状态机两步走到 RUNNING，不卡在 RELEASED
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                prodOrderMapper.selectById(order.getId()).getStatus());
        List<WipTxnDO> orderTxns = txnsOf(MesBizTypeEnum.PROD_ORDER, ORDER_NO);
        assertEquals(2, orderTxns.size());
        assertTrue(hasTransition(orderTxns, MesProdOrderStatusEnum.RELEASED, MesProdOrderStatusEnum.READY));
        assertTrue(hasTransition(orderTxns, MesProdOrderStatusEnum.READY, MesProdOrderStatusEnum.RUNNING));
        // 作业工单本来就是 READY，只走一步
        assertEquals(1, txnsOf(MesBizTypeEnum.WORK_ORDER, workOrder.getWorkOrderNo()).size());
    }

    @Test
    void testStartOpTask_secondTaskSkipsCascade() {
        // 准备：本段第一道已开工，作业工单与生产工单都已 RUNNING
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.RUNNING);
        OpTaskDO second = insertTask(workOrder, 20, "折弯", null, MesOpTaskStatusEnum.READY);

        // 调用：第二道工序开工
        domainService.startOpTask(startBO(second, null));

        // 断言：上两级静默跳过，版本不动、不重复留痕。开工是现场动作，不该因为上级状态已经对了而失败
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(second.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION, workOrderMapper.selectById(workOrder.getId()).getVersion());
        assertEquals(MesConstants.INIT_VERSION, prodOrderMapper.selectById(order.getId()).getVersion());
        assertEquals(0, txnsOf(MesBizTypeEnum.WORK_ORDER, workOrder.getWorkOrderNo()).size());
        assertEquals(0, txnsOf(MesBizTypeEnum.PROD_ORDER, ORDER_NO).size());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testStartOpTask_gateRejectedNothingChanged() {
        // 准备：门禁判缺料
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.READY);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.PENDING);
        OpTaskDO task = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.READY);
        doThrow(exception(OP_TASK_NOT_KITTED, "MAT-1001 物料1001 还缺 100"))
                .when(startGateChecker).check(any(), any());

        // 调用并断言：门禁在状态推进之前，拦下时三级一行都没改，也没留痕
        assertServiceException(() -> domainService.startOpTask(startBO(task, null)),
                OP_TASK_NOT_KITTED, "MAT-1001 物料1001 还缺 100");
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertNull(opTaskMapper.selectById(task.getId()).getActualStartTime());
        assertEquals(MesWorkOrderStatusEnum.PENDING.name(),
                workOrderMapper.selectById(workOrder.getId()).getStatus());
        assertEquals(MesProdOrderStatusEnum.READY.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(0L, wipTxnMapper.selectCount());
        assertEquals(0L, commandRecordMapper.selectCount());
    }

    @Test
    void testStartOpTask_operatorPassedToGate() {
        // 准备：现场换人，界面上选的人比派工记录准
        OpTaskDO task = readyTask();
        Long operatorId = 8888L;

        // 调用
        domainService.startOpTask(startBO(task, operatorId));

        // 断言：操作者原样传给门禁，由门禁去比技能资质
        ArgumentCaptor<MesStartGateContext> captor = ArgumentCaptor.forClass(MesStartGateContext.class);
        verify(startGateChecker).check(any(), captor.capture());
        assertEquals(operatorId, captor.getValue().getOperatorId());
    }

    @Test
    void testStartOpTask_invalidStatusRejected() {
        // 准备：待排产的工序没有 start 出边（计划还没下达，资源与时间都没有）
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.READY);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.PENDING);
        OpTaskDO task = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.PENDING);

        assertServiceException(() -> domainService.startOpTask(startBO(task, null)), OP_TASK_STATUS_INVALID);
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesWorkOrderStatusEnum.PENDING.name(),
                workOrderMapper.selectById(workOrder.getId()).getStatus());
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testStartOpTask_idempotent() {
        // 准备
        OpTaskDO task = readyTask();

        // 调用：同键两次。第二次若真执行，RUNNING 没有 start 出边，状态机会直接抛错
        domainService.startOpTask(startBO(task, null));
        domainService.startOpTask(startBO(task, null));

        // 断言：只推进一次、只留一套流水、门禁也只跑一次（开工已经发生，再判一次没有意义）
        OpTaskDO started = opTaskMapper.selectById(task.getId());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), started.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, started.getVersion());
        assertEquals(4L, wipTxnMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
        verify(startGateChecker).check(any(), any());
    }

    @Test
    void testStartOpTask_idempotencyKeyDerivedFromTaskNo() {
        // 准备
        OpTaskDO task = readyTask();

        // 调用：不传幂等键，交给服务端按四段格式派生
        domainService.startOpTask(startBO(task, null));

        // 断言：{来源模块}:{工序任务号}:{命令类型}:0
        String expectedKey = SOURCE_MODULE + ":" + task.getTaskNo() + ":"
                + MesCommandTypeEnum.START_OP_TASK.name() + ":0";
        assertNotNull(commandRecordMapper.selectByIdempotencyKey(expectedKey));
        // 工序任务这条流水用动作键本身，级联流水按后缀派生，避免被 Recorder 判成重复留痕
        List<WipTxnDO> taskTxns = txnsOf(MesBizTypeEnum.OP_TASK, task.getTaskNo());
        assertEquals(1, taskTxns.size());
        assertEquals(expectedKey, taskTxns.get(0).getIdempotencyKey());
        assertEquals(4L, wipTxnMapper.selectCount());
        assertTrue(wipTxnMapper.selectList().stream()
                .allMatch(txn -> txn.getIdempotencyKey().startsWith(expectedKey)));
    }

    @Test
    void testStartOpTask_notExists() {
        MesOpTaskActionBO bo = new MesOpTaskActionBO();
        bo.setOpTaskId(randomLongId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("OT-NONE");

        assertServiceException(() -> domainService.startOpTask(bo), OP_TASK_NOT_EXISTS);
    }

    @Test
    void testStartOpTask_optimisticLockConflict() {
        // 准备：让别人在"读到工序任务"与"写回状态"之间改掉这一行。
        // 门禁调用正好在这两步中间，用它当注入点模拟并发
        OpTaskDO task = readyTask();
        doAnswer(invocation -> {
            opTaskMapper.updateById(new OpTaskDO().setId(task.getId())
                    .setVersion(MesConstants.INIT_VERSION + 5));
            return null;
        }).when(startGateChecker).check(any(), any());

        // 调用并断言：WHERE version = 读到的版本 一行都更不到，直接报冲突而不是覆盖别人的写入
        assertServiceException(() -> domainService.startOpTask(startBO(task, null)),
                OP_TASK_CONCURRENT_CONFLICT);
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testPauseOpTask_success() {
        // 准备：加工中的工序
        WorkOrderDO workOrder = runningWorkOrder();
        OpTaskDO task = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.RUNNING);

        // 调用
        domainService.pauseOpTask(actionBO(task, "等待物料"));

        // 断言：转已暂停，流水记 PAUSE 与暂停原因
        assertEquals(MesOpTaskStatusEnum.PAUSED.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, opTaskMapper.selectById(task.getId()).getVersion());
        WipTxnDO txn = wipTxnMapper.selectList().get(0);
        assertEquals(MesWipTxnTypeEnum.PAUSE.name(), txn.getTxnType());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), txn.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.PAUSED.name(), txn.getToStatus());
        assertEquals("等待物料", txn.getRemark());
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.getQty()));
        // 暂停不动上两级：一道工序停一会儿不等于整个工单停了
        assertEquals(0, txnsOf(MesBizTypeEnum.WORK_ORDER, workOrder.getWorkOrderNo()).size());
    }

    @Test
    void testPauseOpTask_invalidStatusRejected() {
        // 准备：待开工的工序没有 pause 出边
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.READY);

        assertServiceException(() -> domainService.pauseOpTask(
                actionBO(task, "等待物料")), OP_TASK_STATUS_INVALID);
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testPauseOpTask_idempotent() {
        // 准备
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);

        // 调用：同键两次。第二次若真执行，PAUSED 没有 pause 出边会抛错
        domainService.pauseOpTask(actionBO(task, "等待物料"));
        domainService.pauseOpTask(actionBO(task, "等待物料"));

        assertEquals(MesOpTaskStatusEnum.PAUSED.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, opTaskMapper.selectById(task.getId()).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testResumeOpTask_success() {
        // 准备：已暂停的工序
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.PAUSED);

        // 调用
        domainService.resumeOpTask(actionBO(task, "物料已到"));

        // 断言：回到加工中，流水记 RESUME
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(task.getId()).getStatus());
        WipTxnDO txn = wipTxnMapper.selectList().get(0);
        assertEquals(MesWipTxnTypeEnum.RESUME.name(), txn.getTxnType());
        assertEquals(MesOpTaskStatusEnum.PAUSED.name(), txn.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), txn.getToStatus());
    }

    @Test
    void testResumeOpTask_fromQualityHold() {
        // 准备：质量处置挂起的工序。状态机把 PAUSED 与 HOLD 两条边都挂在 resume 上，同一个动作管两种恢复
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.HOLD);

        domainService.resumeOpTask(actionBO(task, "处置完成"));

        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesOpTaskStatusEnum.HOLD.name(), wipTxnMapper.selectList().get(0).getFromStatus());
    }

    @Test
    void testResumeOpTask_invalidStatusRejected() {
        // 准备：加工中的工序没有 resume 出边
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);

        assertServiceException(() -> domainService.resumeOpTask(
                actionBO(task, "物料已到")), OP_TASK_STATUS_INVALID);
    }

    @Test
    void testResumeOpTask_idempotent() {
        // 准备
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.PAUSED);

        domainService.resumeOpTask(actionBO(task, "物料已到"));
        domainService.resumeOpTask(actionBO(task, "物料已到"));

        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, opTaskMapper.selectById(task.getId()).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_success() {
        // 准备：加工中的工序，报工已累计到 90 合格 + 10 报废 = 100 计划
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        setQty(task, "90", "10");

        // 调用
        domainService.finishOpTask(finishBO(task, null, null));

        // 断言：转已完工并写实际结束时间
        OpTaskDO done = opTaskMapper.selectById(task.getId());
        assertEquals(MesOpTaskStatusEnum.DONE.name(), done.getStatus());
        assertNotNull(done.getActualEndTime());
        assertEquals(MesConstants.INIT_VERSION + 1, done.getVersion());
        // 流水记 COMPLETE，qty 按 04 文档记累计合格数而不是 0
        WipTxnDO txn = txnsOf(MesBizTypeEnum.OP_TASK, task.getTaskNo()).get(0);
        assertEquals(MesWipTxnTypeEnum.COMPLETE.name(), txn.getTxnType());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), txn.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.DONE.name(), txn.getToStatus());
        assertEquals(0, new BigDecimal("90").compareTo(txn.getQty()));
        // 完工不写数量，数量只由报工推进
        assertEquals(0, new BigDecimal("90").compareTo(done.getCompletedQty()));
        assertEquals(0, new BigDecimal("10").compareTo(done.getScrapQty()));
    }

    @Test
    void testFinishOpTask_qtyNotReachedRejected() {
        // 准备：只报了 60 + 10，没凑齐计划数 100。分批报工可以少报，点完工时必须凑齐
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        setQty(task, "60", "10");

        assertServiceException(() -> domainService.finishOpTask(finishBO(task, null, null)),
                OP_TASK_REPORT_QTY_INVALID);
        // 校验在状态推进之前：DONE 是终态没有回退边，先改状态再发现数量不对就救不回来了
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertNull(opTaskMapper.selectById(task.getId()).getActualEndTime());
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_declaredQtyMismatchRejected() {
        // 准备：界面上看到的是 100/0，后台已被另一笔报工改成 90/10
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        setQty(task, "90", "10");

        // 断言：消息把两组数量都摆出来，工人才知道该刷新界面重新确认，而不是以为系统坏了
        assertServiceException(() -> domainService.finishOpTask(finishBO(task, "100", "0")),
                OP_TASK_FINISH_QTY_MISMATCH, new BigDecimal("100"), new BigDecimal("0"),
                opTaskMapper.selectById(task.getId()).getCompletedQty(),
                opTaskMapper.selectById(task.getId()).getScrapQty());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testFinishOpTask_declaredQtyMatchedPassed() {
        // 准备：界面数据没过期，声明与累计一致
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        setQty(task, "90", "10");

        domainService.finishOpTask(finishBO(task, "90", "10"));

        assertEquals(MesOpTaskStatusEnum.DONE.name(), opTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testFinishOpTask_invalidStatusRejected() {
        // 准备：待开工的工序没有 finish 出边
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.READY);
        setQty(task, "100", "0");

        assertServiceException(() -> domainService.finishOpTask(finishBO(task, null, null)),
                OP_TASK_STATUS_INVALID);
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_idempotent() {
        // 准备
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        setQty(task, "100", "0");

        // 调用：同键两次。第二次若真执行，DONE 是终态会直接抛错
        domainService.finishOpTask(finishBO(task, null, null));
        domainService.finishOpTask(finishBO(task, null, null));

        assertEquals(MesOpTaskStatusEnum.DONE.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, opTaskMapper.selectById(task.getId()).getVersion());
        // 三条：工序完工 + 上卷带动的作业工单完工与生产工单完工，第二次一条都不加
        assertEquals(3L, wipTxnMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testSkipOpTask_notImplementedInThisTask() {
        // 跳序只留接口：三种 skip_policy 的处置与 mes_op_skip_record 在 T1-17
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.READY);

        assertThrows(UnsupportedOperationException.class,
                () -> domainService.skipOpTask(actionBO(task, null)));
    }

    @Test
    void testFinishOpTask_serialNextTaskReady() {
        // 准备：下料 -> 折弯，折弯还在待排产
        WorkOrderDO workOrder = runningWorkOrder();
        OpTaskDO cut = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        OpTaskDO bend = insertTask(workOrder, 20, "折弯", null, MesOpTaskStatusEnum.PENDING);
        chain(cut, MesConstants.NONE_ID, bend.getId());
        chain(bend, cut.getId(), MesConstants.NONE_ID);
        setQty(cut, "100", "0");

        // 调用
        domainService.finishOpTask(finishBO(cut, null, null));

        // 断言：后序转待开工，版本自增（走的是带 version 条件的更新）
        OpTaskDO readyNext = opTaskMapper.selectById(bend.getId());
        assertEquals(MesOpTaskStatusEnum.READY.name(), readyNext.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, readyNext.getVersion());
        // 流水：完工一条 + 流转一条。MOVE 用两个专用列记流出与流入工序，qty 记流转数量
        WipTxnDO move = moveTxn();
        assertEquals(cut.getId(), move.getFromOpTaskId());
        assertEquals(bend.getId(), move.getToOpTaskId());
        assertEquals(0, new BigDecimal("100").compareTo(move.getQty()));
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), move.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.READY.name(), move.getToStatus());
        assertTrue(move.getRemark().contains("下料"));
        assertEquals(2L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_lastTaskHasNoNext() {
        // 准备：末道工序，next_task_id 为 0
        OpTaskDO last = insertTask(runningWorkOrder(), 40, "装配", null, MesOpTaskStatusEnum.RUNNING);
        setQty(last, "100", "0");

        domainService.finishOpTask(finishBO(last, null, null));

        assertEquals(MesOpTaskStatusEnum.DONE.name(), opTaskMapper.selectById(last.getId()).getStatus());
        // 没有后序可推，只有工序完工那条流水，以及上卷带动的两级工单完工（本段只有这一道工序）
        assertEquals(0, txnsOf(MesBizTypeEnum.OP_TASK, last.getTaskNo()).stream()
                .filter(txn -> MesWipTxnTypeEnum.MOVE.name().equals(txn.getTxnType())).count());
        assertEquals(3L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_nextAlreadyReadyNotPushedAgain() {
        // 准备：后序已被 APS 下达计划推到 READY
        WorkOrderDO workOrder = runningWorkOrder();
        OpTaskDO cut = insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.RUNNING);
        OpTaskDO bend = insertTask(workOrder, 20, "折弯", null, MesOpTaskStatusEnum.READY);
        chain(cut, MesConstants.NONE_ID, bend.getId());
        setQty(cut, "100", "0");

        domainService.finishOpTask(finishBO(cut, null, null));

        // 断言：不重复推，版本不动、不留流转流水
        assertEquals(MesConstants.INIT_VERSION, opTaskMapper.selectById(bend.getId()).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_wholeParallelGroupPushedToReady() {
        // 准备：加工 -> （喷漆 ‖ 标识） -> 装配，两道并行工序都还在待排产
        ParallelChain chain = parallelChain(MesOpTaskStatusEnum.PENDING, MesOpTaskStatusEnum.PENDING);
        setQty(chain.machine, "100", "0");

        // 调用：加工完工
        domainService.finishOpTask(finishBO(chain.machine, null, null));

        // 断言：并行组两道一起转待开工。next_task_id 是单值列，指向的只是组内代表（喷漆），
        // 只推代表的话标识永远停在 PENDING，它的 start 会被状态机拒掉，那道活谁也干不了
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(chain.paint.getId()).getStatus());
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(chain.mark.getId()).getStatus());
        // 每道后序各留一条流转流水，键带后序编号所以不会互相判重
        assertEquals(2, wipTxnMapper.selectList().stream()
                .filter(txn -> MesWipTxnTypeEnum.MOVE.name().equals(txn.getTxnType())).count());
    }

    @Test
    void testFinishOpTask_parallelGroupPartialDoneKeepsNextPending() {
        // 准备：喷漆完工，标识还在加工
        ParallelChain chain = parallelChain(MesOpTaskStatusEnum.RUNNING, MesOpTaskStatusEnum.RUNNING);
        setQty(chain.paint, "100", "0");

        // 调用：只完工喷漆
        domainService.finishOpTask(finishBO(chain.paint, null, null));

        // 断言：装配仍在待排产。后序要的是整组的产出，只完成一道就往下走，装配拿到的是没打标识的半成品
        assertEquals(MesOpTaskStatusEnum.DONE.name(), opTaskMapper.selectById(chain.paint.getId()).getStatus());
        assertEquals(MesOpTaskStatusEnum.PENDING.name(),
                opTaskMapper.selectById(chain.assemble.getId()).getStatus());
        // 没有流转流水；喷漆所在的作业工单本段只有它一道，因此那一级完工了，生产工单还没有（末道是装配）
        assertEquals(0, wipTxnMapper.selectList().stream()
                .filter(txn -> MesWipTxnTypeEnum.MOVE.name().equals(txn.getTxnType())).count());
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                prodOrderMapper.selectById(chain.orderId).getStatus());
        assertEquals(2L, wipTxnMapper.selectCount());
    }

    @Test
    void testFinishOpTask_parallelGroupAllDonePushesNext() {
        // 准备：喷漆已完工，标识在加工中
        ParallelChain chain = parallelChain(MesOpTaskStatusEnum.DONE, MesOpTaskStatusEnum.RUNNING);
        setQty(chain.mark, "100", "0");

        // 调用：标识完工，组内全部完成
        domainService.finishOpTask(finishBO(chain.mark, null, null));

        // 断言：装配转待开工。组内成员分布在两张作业工单上（喷漆与标识工作中心不同），
        // 按作业工单取组会把这个组切成两个单元素组，判定当场失效
        assertEquals(MesOpTaskStatusEnum.READY.name(),
                opTaskMapper.selectById(chain.assemble.getId()).getStatus());
        WipTxnDO move = moveTxn();
        assertEquals(chain.mark.getId(), move.getFromOpTaskId());
        assertEquals(chain.assemble.getId(), move.getToOpTaskId());
    }

    /**
     * 落一张生产工单。
     *
     * <p>{@code version} 必须显式给初始值：带 {@code WHERE version = ?} 条件的更新碰上 NULL 版本
     * 会一行都更不到，那是数据问题不是并发问题。生产上由 codegen 建单统一落 0。
     */
    private ProdOrderDO insertOrder(MesProdOrderStatusEnum status) {
        ProdOrderDO order = ProdOrderDO.builder()
                .orderNo(ORDER_NO)
                .demandId(MesConstants.NONE_ID)
                .plantId(1L)
                .workshopId(2L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(1L)
                .matCode("MAT-001")
                .qty(PLAN_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(1L)
                .priority(0)
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    private WorkOrderDO insertWorkOrder(ProdOrderDO order, Long wcId, MesWorkOrderStatusEnum status) {
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .workOrderNo("WO-" + wcId)
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .plantId(order.getPlantId())
                .workshopId(order.getWorkshopId())
                .wcId(wcId)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(PLAN_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    private OpTaskDO insertTask(WorkOrderDO workOrder, int seq, String opName,
                                String parallelGroup, MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + taskSeq.incrementAndGet())
                .workOrderId(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .routeOpId(100L + seq)
                .opId(40L + seq)
                .opCode("OP" + seq)
                .opName(opName)
                .seq(seq)
                .parallelGroup(parallelGroup)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .plannedQty(PLAN_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

    /** 开工用例的常态起点：生产工单已就绪、作业工单还停在下达时的 PENDING、工序待开工。 */
    private OpTaskDO readyTask() {
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.READY);
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.PENDING);
        return insertTask(workOrder, 10, "下料", null, MesOpTaskStatusEnum.READY);
    }

    /** 上两级都已在产的作业工单，暂停恢复完工这类不关心级联的用例用它。 */
    private WorkOrderDO runningWorkOrder() {
        return insertWorkOrder(insertOrder(MesProdOrderStatusEnum.RUNNING), WC_MACHINE,
                MesWorkOrderStatusEnum.RUNNING);
    }

    /** 串前后序。前后序是单值列，并行组指向组内代表工序，与下达时的串联口径一致。 */
    private void chain(OpTaskDO task, Long prevTaskId, Long nextTaskId) {
        task.setPrevTaskId(prevTaskId).setNextTaskId(nextTaskId);
        opTaskMapper.updateById(new OpTaskDO().setId(task.getId())
                .setPrevTaskId(prevTaskId).setNextTaskId(nextTaskId));
    }

    /** 直接给累计数量：生产上这两个值由报工推进（T1-14），本任务只校验它们。不动 version。 */
    private void setQty(OpTaskDO task, String completedQty, String scrapQty) {
        task.setCompletedQty(new BigDecimal(completedQty)).setScrapQty(new BigDecimal(scrapQty));
        opTaskMapper.updateById(new OpTaskDO().setId(task.getId())
                .setCompletedQty(task.getCompletedQty()).setScrapQty(task.getScrapQty()));
    }

    /**
     * 04 文档"工序串联规则"的原型链：加工 -&gt; （喷漆 ‖ 标识） -&gt; 装配。
     *
     * <p>喷漆与标识的工作中心不同，因此落在两张不同的作业工单上——按工作中心分组建作业工单是下达时的
     * 既定规则，所以"并行组跨作业工单"是常态而不是特例。
     */
    private ParallelChain parallelChain(MesOpTaskStatusEnum paintStatus, MesOpTaskStatusEnum markStatus) {
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);
        WorkOrderDO machineWorkOrder = insertWorkOrder(order, WC_MACHINE, MesWorkOrderStatusEnum.RUNNING);
        WorkOrderDO paintWorkOrder = insertWorkOrder(order, WC_PAINT, MesWorkOrderStatusEnum.RUNNING);
        WorkOrderDO markWorkOrder = insertWorkOrder(order, WC_MARK, MesWorkOrderStatusEnum.RUNNING);
        ParallelChain chain = new ParallelChain();
        chain.orderId = order.getId();
        chain.machine = insertTask(machineWorkOrder, 20, "加工", null, MesOpTaskStatusEnum.RUNNING);
        chain.paint = insertTask(paintWorkOrder, 30, "喷漆", GROUP_A, paintStatus);
        chain.mark = insertTask(markWorkOrder, 30, "标识", GROUP_A, markStatus);
        chain.assemble = insertTask(machineWorkOrder, 40, "装配", null, MesOpTaskStatusEnum.PENDING);
        // 邻居指向并行步骤的代表工序（组内 seq 最小、seq 相同时编号最小的那道），组内两道互不为前后序
        chain(chain.machine, MesConstants.NONE_ID, chain.paint.getId());
        chain(chain.paint, chain.machine.getId(), chain.assemble.getId());
        chain(chain.mark, chain.machine.getId(), chain.assemble.getId());
        chain(chain.assemble, chain.paint.getId(), MesConstants.NONE_ID);
        return chain;
    }

    /** 开工参数，操作者可空。 */
    private MesOpTaskActionBO startBO(OpTaskDO task, Long operatorId) {
        MesOpTaskActionBO bo = actionBO(task, null);
        bo.setOperatorId(operatorId);
        return bo;
    }

    /** 状态动作参数：不带幂等键，走服务端按四段格式派生的那条路，键里的命令类型段天然把四个动作分开。 */
    private MesOpTaskActionBO actionBO(OpTaskDO task, String reason) {
        MesOpTaskActionBO bo = new MesOpTaskActionBO();
        bo.setOpTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setReason(reason);
        return bo;
    }

    /** 完工参数，两个对账声明可空。 */
    private MesOpTaskFinishBO finishBO(OpTaskDO task, String qualifiedQty, String scrapQty) {
        MesOpTaskFinishBO bo = new MesOpTaskFinishBO();
        bo.setOpTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setQualifiedQty(qualifiedQty == null ? null : new BigDecimal(qualifiedQty));
        bo.setScrapQty(scrapQty == null ? null : new BigDecimal(scrapQty));
        return bo;
    }

    /** 按来源业务定位流水：一个开工动作会写三级共四条，必须按主体挑出来看。 */
    private List<WipTxnDO> txnsOf(MesBizTypeEnum bizType, String bizNo) {
        return wipTxnMapper.selectList().stream()
                .filter(txn -> bizType.name().equals(txn.getBizType()) && bizNo.equals(txn.getBizNo()))
                .toList();
    }

    /** 取那条工序流转流水，后序就绪的三个用例都要看它的流出、流入与数量。 */
    private WipTxnDO moveTxn() {
        return wipTxnMapper.selectList().stream()
                .filter(txn -> MesWipTxnTypeEnum.MOVE.name().equals(txn.getTxnType()))
                .findFirst().orElseThrow();
    }

    /** 流水里有没有这条状态迁移，用来证明推进确实是一步一步走的、没有绕开状态机。 */
    private boolean hasTransition(List<WipTxnDO> txns, Enum<?> fromStatus, Enum<?> toStatus) {
        return txns.stream().anyMatch(txn -> fromStatus.name().equals(txn.getFromStatus())
                && toStatus.name().equals(txn.getToStatus()));
    }

    /** 并行链的四道工序，避免用下标或 Map 取值。 */
    private static final class ParallelChain {

        private Long orderId;
        private OpTaskDO machine;
        private OpTaskDO paint;
        private OpTaskDO mark;
        private OpTaskDO assemble;

    }

}
