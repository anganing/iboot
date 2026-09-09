package com.iboot.pf.module.mes.service.prodreport;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.opskiprecord.MesOpSkipDomainService;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainServiceImpl;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker;
import com.iboot.pf.module.mes.service.prodorder.MesFinishInboundService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesCancelReportBO;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_CANCEL_OP_TASK_DONE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_QTY_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 报工推进链测试。三级数量推进的口径是本任务的全部难点，因此断言集中在口径本身。
 *
 * <p>最重要的一个用例是 {@code testSubmitReport_fiveOpsNotFiveHundred}：5 道工序、每道计划 100，
 * 逐道报满，生产工单的完成数必须始终等于末道工序的值。这道题的错法是把各工序相加得到 500——
 * 流水线上 100 个件依次过 5 道工序，产出还是 100 个。
 *
 * <p>作业工单与生产工单的收尾判定要靠完工动作触发（"本段全部工序 DONE"只可能在完工那一刻成立），
 * 所以这里把 T1-13 的工序动作一并装进上下文，开工门禁 mock 掉。
 */
@Import({MesProdReportDomainServiceImpl.class, MesProgressPropagator.class,
        MesOpTaskDomainServiceImpl.class, MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesProdReportDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal PLAN_QTY = new BigDecimal("100");
    private static final Long WC_MACHINE = 201L;
    private static final Long WC_PAINT = 202L;
    /** 取消原因，反向流水的 remark 断言按它比对。 */
    private static final String CANCEL_REASON = "数量报错了";

    /** 工序任务号计数器，保证同一个用例里落的任务号互不相同（生产上由编码规则分配）。 */
    private final AtomicInteger taskSeq = new AtomicInteger();

    @Resource
    private MesProdReportDomainService domainService;
    @Resource
    private MesOpTaskDomainService opTaskDomainService;
    @Resource
    private MesProgressPropagator progressPropagator;
    @Resource
    private ProdReportMapper prodReportMapper;
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
    /** 完工入库由 T1-15 接在生产工单转 DONE 那一步，本类只验推进口径，把它 mock 掉。 */
    @MockitoBean
    private MesFinishInboundService finishInboundService;
    /** 跳序由 T1-17 实现，本类装 T1-13 的工序动作只为触发上卷，跳序域用不到。 */
    @MockitoBean
    private MesOpSkipDomainService opSkipDomainService;
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
    void testSubmitReport_fiveOpsNotFiveHundred() {
        // 准备（验收硬指标的构造）：一张 100 件的工单，5 道工序每道计划 100。
        // 前三道在加工中心、后两道在喷漆房，因此是两张作业工单
        ProdOrderDO order = insertOrder();
        WorkOrderDO machineWorkOrder = insertWorkOrder(order, WC_MACHINE);
        WorkOrderDO paintWorkOrder = insertWorkOrder(order, WC_PAINT);
        List<OpTaskDO> tasks = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            // 五道同时置加工中只是为了逐道报工：报工动作不关心前后序，那是开工门禁的事
            tasks.add(insertTask(index < 3 ? machineWorkOrder : paintWorkOrder,
                    (index + 1) * 10, "工序" + (index + 1), MesOpTaskStatusEnum.RUNNING));
        }

        // 调用并逐道断言：生产工单的完成数始终等于末道工序（第五道）的完成数
        for (int index = 0; index < 5; index++) {
            OpTaskDO task = tasks.get(index);
            domainService.submitReport(submitBO(task, "100", "0", "0", index + 1));

            BigDecimal lastOpQty = opTaskMapper.selectById(tasks.get(4).getId()).getCompletedQty();
            BigDecimal orderQty = prodOrderMapper.selectById(order.getId()).getCompletedQty();
            assertEquals(0, lastOpQty.compareTo(orderQty),
                    "报完第" + (index + 1) + "道后，生产工单完成数必须等于末道工序的值");
            // 前四道报完时末道还没报，整单产出仍是 0——料还在线上，没有一件走完全程
            assertEquals(0, (index == 4 ? PLAN_QTY : BigDecimal.ZERO).compareTo(orderQty));
        }

        // 断言（验收硬指标）：各工序累计确实是 500，而生产工单是 100 不是 500
        BigDecimal sumOfTasks = tasks.stream().map(task -> opTaskMapper.selectById(task.getId()).getCompletedQty())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("500").compareTo(sumOfTasks));
        assertEquals(0, PLAN_QTY.compareTo(prodOrderMapper.selectById(order.getId()).getCompletedQty()));
        // 两张作业工单各取本段末道：加工段末道是第三道、喷漆段末道是第五道，都是 100
        assertEquals(0, PLAN_QTY.compareTo(workOrderMapper.selectById(machineWorkOrder.getId()).getCompletedQty()));
        assertEquals(0, PLAN_QTY.compareTo(workOrderMapper.selectById(paintWorkOrder.getId()).getCompletedQty()));
    }

    @Test
    void testSubmitReport_batchesAccumulate() {
        // 准备：同一道工序分三批报 30 / 30 / 40
        OpTaskDO task = runningTask();

        domainService.submitReport(submitBO(task, "30", "0", "0", 1));
        domainService.submitReport(submitBO(task, "30", "0", "0", 2));
        domainService.submitReport(submitBO(task, "40", "0", "0", 3));

        // 断言：累加而不是覆盖，覆盖会让最后只剩 40
        OpTaskDO reported = opTaskMapper.selectById(task.getId());
        assertEquals(0, PLAN_QTY.compareTo(reported.getCompletedQty()));
        assertEquals(3L, prodReportMapper.selectCount());
        // 三次报工三条流水，每条记本次合格数而不是累计数
        List<WipTxnDO> reportTxns = reportTxns();
        assertEquals(3, reportTxns.size());
        assertEquals(0, new BigDecimal("100").compareTo(reportTxns.stream()
                .map(WipTxnDO::getQty).reduce(BigDecimal.ZERO, BigDecimal::add)));
        assertTrue(reportTxns.stream().anyMatch(txn -> new BigDecimal("40").compareTo(txn.getQty()) == 0));
        // 报工不改工序状态：够数了也要人点完工，那是另一个动作
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), reported.getStatus());
    }

    @Test
    void testSubmitReport_scrapAccumulatesAndSumsToOrder() {
        // 准备：两道工序，各报 90 合格 + 10 报废
        ProdOrderDO order = insertOrder();
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE);
        OpTaskDO first = insertTask(workOrder, 10, "下料", MesOpTaskStatusEnum.RUNNING);
        OpTaskDO second = insertTask(workOrder, 20, "折弯", MesOpTaskStatusEnum.RUNNING);

        domainService.submitReport(submitBO(first, "90", "10", "0", 1));
        domainService.submitReport(submitBO(second, "90", "10", "0", 2));

        // 断言：报废按各工序之和汇总，因为每道工序报废掉的是不同的件；
        // 完成数仍取末道工序，两个口径在同一次推进里必须分开处理
        ProdOrderDO propagated = prodOrderMapper.selectById(order.getId());
        assertEquals(0, new BigDecimal("20").compareTo(propagated.getScrapQty()));
        assertEquals(0, new BigDecimal("90").compareTo(propagated.getCompletedQty()));
        // 作业工单没有 scrap_qty 列，这一级只推完成数
        assertEquals(0, new BigDecimal("90").compareTo(
                workOrderMapper.selectById(workOrder.getId()).getCompletedQty()));
    }

    @Test
    void testSubmitReport_recordAndTxnFields() {
        // 准备
        OpTaskDO task = runningTask();

        // 调用
        Long reportId = domainService.submitReport(submitBO(task, "60", "0", "0", 1));

        // 断言：报工记录落 SUBMITTED，单据链从工序任务反查而不是让前端传
        ProdReportDO report = prodReportMapper.selectById(reportId);
        assertEquals(MesProdReportStatusEnum.SUBMITTED.name(), report.getStatus());
        assertEquals(task.getId(), report.getOpTaskId());
        assertEquals(task.getWorkOrderId(), report.getWorkOrderId());
        assertNotNull(report.getProdOrderId());
        assertNotNull(report.getReportNo());
        // 三个 NOT NULL 的时间列缺省取报工时间，现场扫码报工不该被逼着填假的起止时间
        assertNotNull(report.getReportTime());
        assertEquals(report.getReportTime(), report.getStartTime());
        assertEquals(report.getReportTime(), report.getEndTime());
        // 可选维度落 0 不落 null
        assertEquals(MesConstants.NONE_ID, report.getResId());
        assertEquals(MesConstants.NONE_ID, report.getShiftId());
        // 流水挂在报工单上而不是工序任务上：一道工序分批报三次，挂工序任务会分不清是哪一次
        WipTxnDO txn = reportTxns().get(0);
        assertEquals(MesBizTypeEnum.PROD_REPORT.name(), txn.getBizType());
        assertEquals(report.getReportNo(), txn.getBizNo());
        assertEquals(MesWipTxnTypeEnum.REPORT.name(), txn.getTxnType());
        assertEquals(0, new BigDecimal("60").compareTo(txn.getQty()));
        assertNull(txn.getFromStatus());
        assertEquals(MesProdReportStatusEnum.SUBMITTED.name(), txn.getToStatus());
        assertEquals(task.getId(), txn.getOpTaskId());
    }

    @Test
    void testSubmitReport_idempotent() {
        // 准备
        OpTaskDO task = runningTask();
        MesSubmitReportBO bo = submitBO(task, "60", "0", "0", 1);

        // 调用：同键两次
        Long first = domainService.submitReport(bo);
        Long second = domainService.submitReport(submitBO(task, "60", "0", "0", 1));

        // 断言：返回同一条记录，数量只算一次，流水也只有一条
        assertEquals(first, second);
        assertEquals(1L, prodReportMapper.selectCount());
        assertEquals(0, new BigDecimal("60").compareTo(
                opTaskMapper.selectById(task.getId()).getCompletedQty()));
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testSubmitReport_reworkNotPropagated() {
        // 准备：只报返工数
        OpTaskDO task = runningTask();

        Long reportId = domainService.submitReport(submitBO(task, "0", "0", "10", 1));

        // 断言：返工数记在报工记录上，但不进三级推进。返工件既没合格也没报废，
        // 现在算进产出、等返工完再算一次就重复计数了
        assertEquals(0, new BigDecimal("10").compareTo(prodReportMapper.selectById(reportId).getQtyRework()));
        OpTaskDO reported = opTaskMapper.selectById(task.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(reported.getCompletedQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reported.getScrapQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reportTxns().get(0).getQty()));
    }

    @Test
    void testSubmitReport_qtyExceededRejected() {
        // 准备：已报 60，再报 50 就超过计划 100
        OpTaskDO task = runningTask();
        domainService.submitReport(submitBO(task, "60", "0", "0", 1));

        // 断言：拦下。完工要求"合格 + 报废 = 计划数"严格相等，超报之后那道工序永远凑不出这个等式
        assertServiceException(() -> domainService.submitReport(submitBO(task, "50", "0", "0", 2)),
                PROD_REPORT_QTY_EXCEEDED, "下料", new BigDecimal("50"), BigDecimal.ZERO,
                new BigDecimal("110.000000"), new BigDecimal("100.000000"));
        assertEquals(1L, prodReportMapper.selectCount());
        assertEquals(0, new BigDecimal("60").compareTo(
                opTaskMapper.selectById(task.getId()).getCompletedQty()));
    }

    @Test
    void testSubmitReport_qtyAllZeroRejected() {
        OpTaskDO task = runningTask();

        assertServiceException(() -> domainService.submitReport(submitBO(task, "0", "0", "0", 1)),
                PROD_REPORT_QTY_EMPTY);
        assertEquals(0L, prodReportMapper.selectCount());
    }

    @Test
    void testSubmitReport_negativeQtyRejected() {
        // 负数会把累计值静默扣小，比报错难查得多
        OpTaskDO task = runningTask();

        assertServiceException(() -> domainService.submitReport(submitBO(task, "-10", "0", "0", 1)),
                QTY_MUST_BE_POSITIVE);
        assertEquals(0L, prodReportMapper.selectCount());
    }

    @Test
    void testSubmitReport_opTaskNotRunningRejected() {
        // 准备：待开工的工序还没开始干，不该有产出
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", MesOpTaskStatusEnum.READY);

        assertServiceException(() -> domainService.submitReport(submitBO(task, "10", "0", "0", 1)),
                OP_TASK_STATUS_INVALID);
        assertEquals(0L, prodReportMapper.selectCount());
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testSubmitReport_idempotencyKeyRequired() {
        // 准备：键必须由调用方给。服务端按已有报工数派生会让每次重试都算出新的 seq，幂等当场失效
        OpTaskDO task = runningTask();
        MesSubmitReportBO bo = submitBO(task, "10", "0", "0", 1);
        bo.setIdempotencyKey(null);

        assertThrows(IllegalArgumentException.class, () -> domainService.submitReport(bo));
        assertEquals(0L, prodReportMapper.selectCount());
    }

    @Test
    void testQtyReached_boundary() {
        // 数量达标判定：合格 + 报废 >= 计划
        OpTaskDO task = runningTask();
        assertFalse(progressPropagator.qtyReached(task));

        domainService.submitReport(submitBO(task, "90", "0", "0", 1));
        assertFalse(progressPropagator.qtyReached(opTaskMapper.selectById(task.getId())),
                "90 合格还差 10 件，不算达标");

        domainService.submitReport(submitBO(task, "0", "10", "0", 2));
        assertTrue(progressPropagator.qtyReached(opTaskMapper.selectById(task.getId())),
                "报废也算这道工序处理掉的件数，90 + 10 达标");
    }

    @Test
    void testPropagate_workOrderDoneWhenSegmentAllDone() {
        // 准备：加工段两道工序 + 喷漆段一道，报满后把加工段两道都完工
        TwoSegmentChain chain = twoSegmentChain();
        reportFull(chain.first, 1);
        reportFull(chain.second, 2);
        reportFull(chain.third, 3);

        opTaskDomainService.finishOpTask(finishBO(chain.first));
        opTaskDomainService.finishOpTask(finishBO(chain.second));

        // 断言：加工段全部完工 -> 作业工单 DONE 并写实际结束时间；喷漆段还没完工，仍在作业中
        WorkOrderDO doneWorkOrder = workOrderMapper.selectById(chain.first.getWorkOrderId());
        assertEquals(MesWorkOrderStatusEnum.DONE.name(), doneWorkOrder.getStatus());
        assertNotNull(doneWorkOrder.getActualEndTime());
        assertEquals(0, PLAN_QTY.compareTo(doneWorkOrder.getCompletedQty()));
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(),
                workOrderMapper.selectById(chain.third.getWorkOrderId()).getStatus());
        // 生产工单不能跟着完工：整单末道工序（第三道）还没完工
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                prodOrderMapper.selectById(chain.orderId).getStatus());
    }

    @Test
    void testPropagate_prodOrderDoneWhenLastOpDone() {
        // 准备：三道工序全部报满并完工
        TwoSegmentChain chain = twoSegmentChain();
        reportFull(chain.first, 1);
        reportFull(chain.second, 2);
        reportFull(chain.third, 3);
        opTaskDomainService.finishOpTask(finishBO(chain.first));
        opTaskDomainService.finishOpTask(finishBO(chain.second));

        // 调用：末道工序完工
        opTaskDomainService.finishOpTask(finishBO(chain.third));

        // 断言：末道工序完工且数量达标 -> 生产工单 DONE，完成数取末道工序的值
        ProdOrderDO order = prodOrderMapper.selectById(chain.orderId);
        assertEquals(MesProdOrderStatusEnum.DONE.name(), order.getStatus());
        assertNotNull(order.getActualEndTime());
        assertEquals(0, PLAN_QTY.compareTo(order.getCompletedQty()));
        // 两级收尾各留一条 COMPLETE 流水，记的是工单自己的状态迁移
        assertTrue(wipTxnMapper.selectList().stream()
                .anyMatch(txn -> MesBizTypeEnum.PROD_ORDER.name().equals(txn.getBizType())
                        && MesProdOrderStatusEnum.DONE.name().equals(txn.getToStatus())
                        && MesWipTxnTypeEnum.COMPLETE.name().equals(txn.getTxnType())));
    }

    @Test
    void testPropagate_prodOrderDoneWhenLastOpSkipped() {
        // 准备（整改点名的构造）：三道工序，末道被跳序（SKIPPED 的工序 completed_qty 恒为 0）。
        // 旧口径要求末道 DONE，被跳过的末道永远凑不出这个条件——工单停在 RUNNING、关不掉、预留永久挂着
        TwoSegmentChain chain = twoSegmentChain();
        reportFull(chain.first, 1);
        reportFull(chain.second, 2);
        // 跳序域在本类是 mock：桩成"立即生效"的真实行为——工序置 SKIPPED、返回 true。
        // doSkip 收到 true 后会触发上卷（跳序场景工单收尾的唯一触发点）
        when(opSkipDomainService.skip(any(), any(), any(), anyString())).thenAnswer(invocation -> {
            OpTaskDO skipping = invocation.getArgument(0);
            opTaskMapper.updateById(new OpTaskDO().setId(skipping.getId())
                    .setStatus(MesOpTaskStatusEnum.SKIPPED.name()));
            return true;
        });
        opTaskDomainService.skipOpTask(skipBO(chain.third));

        // 调用：前两道报满并完工，触发上卷
        opTaskDomainService.finishOpTask(finishBO(chain.first));
        opTaskDomainService.finishOpTask(finishBO(chain.second));

        // 断言（验收点名）：末道 SKIPPED 也算达标 -> 生产工单 DONE；
        // 完成数取最后一道实际执行的工序（第二道）的值，不是被跳过的末道把产出清零
        ProdOrderDO order = prodOrderMapper.selectById(chain.orderId);
        assertEquals(MesProdOrderStatusEnum.DONE.name(), order.getStatus());
        assertNotNull(order.getActualEndTime());
        assertEquals(0, PLAN_QTY.compareTo(order.getCompletedQty()));
        // 跳过的末道不贡献报废，汇总口径不被跳序打乱
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getScrapQty()));
        // 完工流水照写
        assertTrue(wipTxnMapper.selectList().stream()
                .anyMatch(txn -> MesBizTypeEnum.PROD_ORDER.name().equals(txn.getBizType())
                        && MesProdOrderStatusEnum.DONE.name().equals(txn.getToStatus())));
        // 工单一转 DONE 就触发自动入库（T1-15 链路），入库量由 FinishInbound 自己按剩余算
        verify(finishInboundService).autoInboundOnOrderDone(order.getId(), chain.second.getWorkOrderId());
        // 喷漆段唯一的工序被跳过，整段作业工单在跳序触发的上卷里同步收尾
        assertEquals(MesWorkOrderStatusEnum.DONE.name(),
                workOrderMapper.selectById(chain.third.getWorkOrderId()).getStatus());
    }

    @Test
    void testPropagate_lastOpRunningNotDone() {
        // 反例锁边：末道还在加工（RUNNING，数量已报满但没人点完工）时，前序完工不许把工单推走。
        // 数量达标与状态收尾是两个条件，收尾必须等末道 DONE 或 SKIPPED
        TwoSegmentChain chain = twoSegmentChain();
        reportFull(chain.first, 1);
        reportFull(chain.second, 2);
        reportFull(chain.third, 3);
        opTaskDomainService.finishOpTask(finishBO(chain.first));
        opTaskDomainService.finishOpTask(finishBO(chain.second));

        // 断言：末道 RUNNING -> 生产工单与喷漆段作业工单都不收尾，产出照常上卷 100
        ProdOrderDO order = prodOrderMapper.selectById(chain.orderId);
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), order.getStatus());
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(),
                workOrderMapper.selectById(chain.third.getWorkOrderId()).getStatus());
        assertEquals(0, PLAN_QTY.compareTo(order.getCompletedQty()));
    }

    @Test
    void testCancelReport_reverseThreeLevels() {
        // 准备：两道工序各报 90 + 10，整单完成 90、报废 20
        ProdOrderDO order = insertOrder();
        WorkOrderDO workOrder = insertWorkOrder(order, WC_MACHINE);
        OpTaskDO first = insertTask(workOrder, 10, "下料", MesOpTaskStatusEnum.RUNNING);
        OpTaskDO second = insertTask(workOrder, 20, "折弯", MesOpTaskStatusEnum.RUNNING);
        domainService.submitReport(submitBO(first, "90", "10", "0", 1));
        Long secondReportId = domainService.submitReport(submitBO(second, "90", "10", "0", 2));

        // 调用：取消末道工序那一笔
        domainService.cancelReport(cancelBO(secondReportId));

        // 断言：报工记录转已取消，三级数量反向扣减
        assertEquals(MesProdReportStatusEnum.CANCELLED.name(),
                prodReportMapper.selectById(secondReportId).getStatus());
        OpTaskDO reversed = opTaskMapper.selectById(second.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(reversed.getCompletedQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reversed.getScrapQty()));
        // 上卷是重算而不是增量，所以两级工单自动回到"末道工序为 0"的正确值，不需要另写反向公式
        assertEquals(0, BigDecimal.ZERO.compareTo(
                workOrderMapper.selectById(workOrder.getId()).getCompletedQty()));
        ProdOrderDO reversedOrder = prodOrderMapper.selectById(order.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(reversedOrder.getCompletedQty()));
        assertEquals(0, new BigDecimal("10").compareTo(reversedOrder.getScrapQty()));
        // 反向流水：REPORT 类型 + 负数量表达冲销，并记下取消原因
        WipTxnDO reverseTxn = reportTxns().stream()
                .filter(txn -> txn.getQty().signum() < 0).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("-90").compareTo(reverseTxn.getQty()));
        assertEquals(MesProdReportStatusEnum.SUBMITTED.name(), reverseTxn.getFromStatus());
        assertEquals(MesProdReportStatusEnum.CANCELLED.name(), reverseTxn.getToStatus());
        assertEquals(CANCEL_REASON, reverseTxn.getRemark());
    }

    @Test
    void testCancelReport_idempotent() {
        // 准备
        OpTaskDO task = runningTask();
        Long reportId = domainService.submitReport(submitBO(task, "60", "0", "0", 1));

        // 调用：同键两次。第二次若真执行，数量会被扣两遍变成 -60
        domainService.cancelReport(cancelBO(reportId));
        domainService.cancelReport(cancelBO(reportId));

        assertEquals(0, BigDecimal.ZERO.compareTo(opTaskMapper.selectById(task.getId()).getCompletedQty()));
        assertEquals(1L, commandRecordMapper.selectCount());
        assertEquals(2L, wipTxnMapper.selectCount());
    }

    @Test
    void testCancelReport_opTaskDoneRejected() {
        // 准备：报满并完工
        OpTaskDO task = runningTask();
        Long reportId = domainService.submitReport(submitBO(task, "100", "0", "0", 1));
        opTaskDomainService.finishOpTask(finishBO(task));

        // 断言：已完工的工序不许取消报工。完工按"合格 + 报废 = 计划数"校验过，
        // 抽掉一笔会让工序停在"数量不够却已完工"，而 DONE 是终态没有回退边
        assertServiceException(() -> domainService.cancelReport(cancelBO(reportId)),
                PROD_REPORT_CANCEL_OP_TASK_DONE, "下料");
        assertEquals(MesProdReportStatusEnum.SUBMITTED.name(),
                prodReportMapper.selectById(reportId).getStatus());
        assertEquals(0, PLAN_QTY.compareTo(opTaskMapper.selectById(task.getId()).getCompletedQty()));
    }

    @Test
    void testCancelReport_notExists() {
        MesCancelReportBO bo = cancelBO(randomLongId());

        assertServiceException(() -> domainService.cancelReport(bo), PROD_REPORT_NOT_EXISTS);
    }

    /**
     * 落一张在产的生产工单，{@code version} 显式给初始值，带 version 条件的更新才更得到。
     *
     * <p>本任务的用例一律从"在产"起步：报工的前提是工序已开工，那意味着三级都已经在 RUNNING。
     */
    private ProdOrderDO insertOrder() {
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
                .status(MesProdOrderStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    private WorkOrderDO insertWorkOrder(ProdOrderDO order, Long wcId) {
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
                .status(MesWorkOrderStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    private OpTaskDO insertTask(WorkOrderDO workOrder, int seq, String opName, MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + taskSeq.incrementAndGet())
                .workOrderId(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .routeOpId(100L + seq)
                .opId(40L + seq)
                .opCode("OP" + seq)
                .opName(opName)
                .seq(seq)
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

    /** 一道加工中的工序，连带上两级都在产。 */
    private OpTaskDO runningTask() {
        return insertTask(runningWorkOrder(), 10, "下料", MesOpTaskStatusEnum.RUNNING);
    }

    private WorkOrderDO runningWorkOrder() {
        return insertWorkOrder(insertOrder(), WC_MACHINE);
    }

    /**
     * 两段三道工序：加工段（第一、二道）+ 喷漆段（第三道）。
     *
     * <p>分两张作业工单才能把"本段全部完工"与"整单末道完工"两个判定分开看：
     * 加工段完工时生产工单不能跟着完工。
     */
    private TwoSegmentChain twoSegmentChain() {
        ProdOrderDO order = insertOrder();
        WorkOrderDO machineWorkOrder = insertWorkOrder(order, WC_MACHINE);
        WorkOrderDO paintWorkOrder = insertWorkOrder(order, WC_PAINT);
        TwoSegmentChain chain = new TwoSegmentChain();
        chain.orderId = order.getId();
        chain.first = insertTask(machineWorkOrder, 10, "下料", MesOpTaskStatusEnum.RUNNING);
        chain.second = insertTask(machineWorkOrder, 20, "折弯", MesOpTaskStatusEnum.RUNNING);
        chain.third = insertTask(paintWorkOrder, 30, "喷漆", MesOpTaskStatusEnum.RUNNING);
        return chain;
    }

    /** 报满一道工序，让它够条件完工。 */
    private void reportFull(OpTaskDO task, int seq) {
        domainService.submitReport(submitBO(task, PLAN_QTY.toPlainString(), "0", "0", seq));
    }

    /**
     * 报工参数。幂等键按 08 文档的 {@code UI:{taskNo}:REPORT:{seq}} 给全，服务端不派生：
     * 键里的 seq 是"第几次报工"，只有调用方知道。
     */
    private MesSubmitReportBO submitBO(OpTaskDO task, String qtyGood, String qtyScrap,
                                       String qtyRework, int seq) {
        MesSubmitReportBO bo = new MesSubmitReportBO();
        bo.setOpTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":REPORT:" + seq);
        bo.setQtyGood(new BigDecimal(qtyGood));
        bo.setQtyScrap(new BigDecimal(qtyScrap));
        bo.setQtyRework(new BigDecimal(qtyRework));
        bo.setRemark("现场报工");
        return bo;
    }

    /** 取消参数：不带幂等键，走服务端按四段格式派生的那条路。 */
    private MesCancelReportBO cancelBO(Long reportId) {
        MesCancelReportBO bo = new MesCancelReportBO();
        bo.setProdReportId(reportId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("R-" + reportId);
        bo.setReason(CANCEL_REASON);
        return bo;
    }

    private MesOpTaskFinishBO finishBO(OpTaskDO task) {
        MesOpTaskFinishBO bo = new MesOpTaskFinishBO();
        bo.setOpTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        return bo;
    }

    /** 跳序参数：填原因即可生效的一档，构造与跳序测试类保持同构。 */
    private MesOpSkipBO skipBO(OpTaskDO task) {
        MesOpSkipBO bo = new MesOpSkipBO();
        bo.setOpTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setSkipReasonType("NOT_APPLICABLE");
        bo.setReason("该型号不需要这道工序");
        return bo;
    }

    /** 取报工流水，按报工单号定位（一次动作还会写上卷产生的完工流水）。 */
    private List<WipTxnDO> reportTxns() {
        return wipTxnMapper.selectList().stream()
                .filter(txn -> MesWipTxnTypeEnum.REPORT.name().equals(txn.getTxnType())).toList();
    }

    /** 两段三道工序的载体，避免用下标取值。 */
    private static final class TwoSegmentChain {

        private Long orderId;
        private OpTaskDO first;
        private OpTaskDO second;
        private OpTaskDO third;

    }

}
