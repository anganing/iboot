package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.command.MesCommandRecordDO;
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
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderActionBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_WORKSHOP_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.ROUTE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 生产工单领域动作测试：下达锁版本与工序展开、挂起、恢复、取消级联，四个动作的状态校验、幂等与在制流水。
 *
 * <p>建单时故意把工单上的 BOM 与工艺路线编号写成两个过期值，只给"生效版本"的路线挂桩：
 * 下达如果沿用了工单原来的 routeId，展开会因取不到工序而失败，锁版本这件事就不会被漏掉。
 */
@Import({MesProdOrderDomainServiceImpl.class, MesOrderExpandHelper.class,
        MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesProdOrderDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal ORDER_QTY = new BigDecimal("100");
    private static final String GROUP_A = "A";

    /** 建单时落在工单上的过期编号，下达后必须被生效版本替换。 */
    private static final Long STALE_BOM_ID = 98L;
    private static final Long STALE_ROUTE_ID = 99L;

    private static final Long ROUTE_OP_CUT = 101L;
    private static final Long ROUTE_OP_MACHINE = 102L;
    private static final Long ROUTE_OP_PAINT = 103L;
    private static final Long ROUTE_OP_MARK = 104L;
    private static final Long ROUTE_OP_ASSEMBLE = 105L;
    private static final Long WC_CUT = 201L;
    private static final Long WC_MACHINE = 202L;
    private static final Long WC_PAINT = 203L;
    private static final Long WC_MARK = 204L;

    @Resource
    private MesProdOrderDomainService domainService;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private OpApi opApi;
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
        // 生效版本：BOM 11/B1.0、工艺路线 21/R1.0，与工单上原来的 98/99 不同
        when(snapshotService.requireEffectiveBom(any(), any())).thenReturn(MesTestFixtures.bom());
        when(snapshotService.requireEffectiveRoute(any(), any())).thenReturn(MesTestFixtures.route());
        // 只给生效路线挂工序，工单上那条过期路线取不到工序
        when(routeApi.getRouteOpList(MesTestFixtures.route().getId())).thenReturn(parallelRoute());
        when(opApi.getOpMap(any())).thenReturn(parallelRoute().stream().map(RouteOpRespDTO::getOpId).distinct()
                .collect(Collectors.toMap(opId -> opId, this::op)));
    }

    @Test
    void testReleaseProdOrder_lockVersionAndExpand() {
        // 准备：一张 DRAFT 工单
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);

        // 调用
        domainService.releaseProdOrder(releaseBO(order));

        // 断言：四个版本快照锁成生效版本，不再是建单时的过期编号
        ProdOrderDO released = prodOrderMapper.selectById(order.getId());
        assertEquals(MesTestFixtures.bom().getId(), released.getBomId());
        assertEquals(MesTestFixtures.bom().getVersion(), released.getBomVersion());
        assertEquals(MesTestFixtures.route().getId(), released.getRouteId());
        assertEquals(MesTestFixtures.route().getVersion(), released.getRouteVersion());
        // 状态推进到已下达，写下达时间，乐观锁版本自增（证明走的是带 version 条件的更新）
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), released.getStatus());
        assertNotNull(released.getReleaseTime());
        assertEquals(MesConstants.INIT_VERSION + 1, released.getVersion());
        // 取生效版本用的是工单物料与当天
        verify(snapshotService).requireEffectiveBom(eq(order.getMatId()), eq(LocalDate.now()));
        verify(snapshotService).requireEffectiveRoute(eq(order.getMatId()), eq(LocalDate.now()));
        // 作业工单数等于工作中心数（5 道工序分布在 4 个工作中心，装配回到加工中心），工序任务数等于路线工序数
        List<WorkOrderDO> workOrders = workOrderMapper.selectListByProdOrderIds(List.of(order.getId()));
        assertEquals(4, workOrders.size());
        assertEquals(5, tasksOf(order.getId()).size());
        assertEquals(MesWorkOrderStatusEnum.PENDING.name(), workOrders.get(0).getStatus());
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), workOrders.get(0).getKitStatus());
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), tasksOf(order.getId()).get(0).getStatus());
    }

    @Test
    void testReleaseProdOrder_parallelGroupChained() {
        // 准备：路线含一组两道并行工序（喷漆 ‖ 标识，parallel_group=A）
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);

        // 调用
        domainService.releaseProdOrder(releaseBO(order));

        // 断言：走完整下达入口后，并行组的前后序仍然正确
        List<OpTaskDO> tasks = tasksOf(order.getId());
        OpTaskDO machine = taskOf(tasks, ROUTE_OP_MACHINE);
        OpTaskDO paint = taskOf(tasks, ROUTE_OP_PAINT);
        OpTaskDO mark = taskOf(tasks, ROUTE_OP_MARK);
        OpTaskDO assemble = taskOf(tasks, ROUTE_OP_ASSEMBLE);
        // 验收硬指标：两道并行工序的 prev 指向同一个前序、next 指向同一个后序
        assertEquals(machine.getId(), paint.getPrevTaskId());
        assertEquals(machine.getId(), mark.getPrevTaskId());
        assertEquals(assemble.getId(), paint.getNextTaskId());
        assertEquals(assemble.getId(), mark.getNextTaskId());
        // 组内互不为前后序：谁都不指向对方
        assertTrue(!paint.getNextTaskId().equals(mark.getId()) && !paint.getPrevTaskId().equals(mark.getId()));
        // 邻居指向并行步骤的代表工序（组内 seq 最小、seq 相同时编号最小的那道）
        assertEquals(paint.getId(), machine.getNextTaskId());
        assertEquals(paint.getId(), assemble.getPrevTaskId());
        assertEquals(MesConstants.NONE_ID, assemble.getNextTaskId());
    }

    @Test
    void testReleaseProdOrder_wipTxnAndCommandRecorded() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);

        // 调用：不传幂等键，交给服务端按四段格式派生
        MesReleaseOrderBO bo = releaseBO(order);
        domainService.releaseProdOrder(bo);

        // 断言：在制流水一条，类型 START，记 DRAFT -> RELEASED 与计划数量
        String expectedKey = SOURCE_MODULE + ":" + ORDER_NO + ":"
                + MesCommandTypeEnum.RELEASE_PROD_ORDER.name() + ":0";
        assertEquals(1L, wipTxnMapper.selectCount());
        WipTxnDO txn = wipTxnMapper.selectList().get(0);
        assertEquals(order.getId(), txn.getProdOrderId());
        assertEquals(MesWipTxnTypeEnum.START.name(), txn.getTxnType());
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), txn.getFromStatus());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), txn.getToStatus());
        assertEquals(0, ORDER_QTY.compareTo(txn.getQty()));
        assertEquals(MesBizTypeEnum.PROD_ORDER.name(), txn.getBizType());
        assertEquals(ORDER_NO, txn.getBizNo());
        assertEquals(expectedKey, txn.getIdempotencyKey());
        // 工单级流水没有作业工单与工序任务维度，落 0 不落 null
        assertEquals(MesConstants.NONE_ID, txn.getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, txn.getOpTaskId());
        // 命令记录留痕，幂等键按 {来源模块}:{工单号}:{命令类型}:0 派生
        MesCommandRecordDO record = commandRecordMapper.selectByIdempotencyKey(expectedKey);
        assertNotNull(record);
        assertEquals(MesCommandTypeEnum.RELEASE_PROD_ORDER.name(), record.getCommandType());
        assertEquals(SOURCE_MODULE, record.getSourceModule());
    }

    @Test
    void testReleaseProdOrder_idempotentNoSecondBatch() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);
        MesReleaseOrderBO bo = releaseBO(order);

        // 调用：同一个幂等键下达两次
        domainService.releaseProdOrder(bo);
        domainService.releaseProdOrder(releaseBO(order));

        // 断言：不产生第二批作业工单与工序任务，也不重复留痕
        assertEquals(4, workOrderMapper.selectListByProdOrderIds(List.of(order.getId())).size());
        assertEquals(5, tasksOf(order.getId()).size());
        assertEquals(1L, wipTxnMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
        // 版本号只自增一次，证明第二次连状态更新都没执行
        assertEquals(MesConstants.INIT_VERSION + 1, prodOrderMapper.selectById(order.getId()).getVersion());
    }

    @Test
    void testReleaseProdOrder_effectiveBomMissing() {
        // 准备：物料当天没有生效 BOM 版本
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);
        when(snapshotService.requireEffectiveBom(any(), any())).thenThrow(exception(BOM_NOT_EXISTS));

        // 调用并断言：拒绝下达，不许拿一个不生效的版本开工
        assertServiceException(() -> domainService.releaseProdOrder(releaseBO(order)), BOM_NOT_EXISTS);
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(0L, workOrderMapper.selectCount());
    }

    @Test
    void testReleaseProdOrder_effectiveRouteMissing() {
        // 准备：物料当天没有生效工艺路线版本
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);
        when(snapshotService.requireEffectiveRoute(any(), any())).thenThrow(exception(ROUTE_NOT_EXISTS));

        // 调用并断言
        assertServiceException(() -> domainService.releaseProdOrder(releaseBO(order)), ROUTE_NOT_EXISTS);
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(0L, opTaskMapper.selectCount());
    }

    @Test
    void testReleaseProdOrder_notDraftRejected() {
        // 准备：已下达的工单再下达
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED);

        // 调用并断言：状态机没有 RELEASED --release--> 这条边
        assertServiceException(() -> domainService.releaseProdOrder(releaseBO(order)), PROD_ORDER_STATUS_INVALID);
        assertEquals(0L, workOrderMapper.selectCount());
    }

    @Test
    void testReleaseProdOrder_notExists() {
        MesReleaseOrderBO bo = new MesReleaseOrderBO();
        bo.setProdOrderId(randomLongId());
        bo.setSourceModule(SOURCE_MODULE);

        assertServiceException(() -> domainService.releaseProdOrder(bo), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    void testReleaseProdOrder_workshopUnassignedRejected() {
        // 准备：需求转单只到工厂粒度，车间可能落 0
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);
        prodOrderMapper.updateById(new ProdOrderDO().setId(order.getId()).setWorkshopId(MesConstants.NONE_ID));

        // 调用并断言：拒绝下达。展开出来的作业工单都从工单抄 workshop_id，
        // 落 0 就是一批没有归属的作业工单，车间派工按车间筛选时看不到它们
        assertServiceException(() -> domainService.releaseProdOrder(releaseBO(order)),
                PROD_ORDER_WORKSHOP_REQUIRED);
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(0L, workOrderMapper.selectCount());
    }

    @Test
    void testHoldProdOrder_success() {
        // 准备：生产中的工单
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);

        // 调用
        domainService.holdProdOrder(actionBO(order, "设备故障"));

        // 断言：状态挂起，版本自增，流水记 PAUSE 与原因
        ProdOrderDO held = prodOrderMapper.selectById(order.getId());
        assertEquals(MesProdOrderStatusEnum.HOLD.name(), held.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, held.getVersion());
        WipTxnDO txn = wipTxnMapper.selectList().get(0);
        assertEquals(MesWipTxnTypeEnum.PAUSE.name(), txn.getTxnType());
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), txn.getFromStatus());
        assertEquals(MesProdOrderStatusEnum.HOLD.name(), txn.getToStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.getQty()));
        assertEquals("设备故障", txn.getRemark());
    }

    @Test
    void testHoldProdOrder_invalidStatusRejected() {
        // 准备：新建状态的工单没有 hold 出边
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);

        assertServiceException(() -> domainService.holdProdOrder(actionBO(order, "设备故障")),
                PROD_ORDER_STATUS_INVALID);
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testHoldProdOrder_idempotent() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);

        // 调用：同键两次。第二次若真执行，状态机会因 HOLD 没有 hold 出边而抛错
        domainService.holdProdOrder(actionBO(order, "设备故障"));
        domainService.holdProdOrder(actionBO(order, "设备故障"));

        // 断言：只推进一次、只留一条流水
        assertEquals(MesProdOrderStatusEnum.HOLD.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, prodOrderMapper.selectById(order.getId()).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testResumeProdOrder_success() {
        // 准备：已挂起的工单
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.HOLD);

        // 调用
        domainService.resumeProdOrder(actionBO(order, "设备已修复"));

        // 断言：回到生产中，流水记 RESUME
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        WipTxnDO txn = wipTxnMapper.selectList().get(0);
        assertEquals(MesWipTxnTypeEnum.RESUME.name(), txn.getTxnType());
        assertEquals(MesProdOrderStatusEnum.HOLD.name(), txn.getFromStatus());
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), txn.getToStatus());
    }

    @Test
    void testResumeProdOrder_invalidStatusRejected() {
        // 准备：生产中的工单没有 resume 出边
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);

        assertServiceException(() -> domainService.resumeProdOrder(actionBO(order, "设备已修复")),
                PROD_ORDER_STATUS_INVALID);
    }

    @Test
    void testResumeProdOrder_idempotent() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.HOLD);

        // 调用：同键两次
        domainService.resumeProdOrder(actionBO(order, "设备已修复"));
        domainService.resumeProdOrder(actionBO(order, "设备已修复"));

        // 断言
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testCancelProdOrder_cascadeToWorkOrderAndOpTask() {
        // 准备：先下达，让作业工单与工序任务真的存在
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);
        domainService.releaseProdOrder(releaseBO(order));

        // 调用
        domainService.cancelProdOrder(actionBO(order, "客户撤单"));

        // 断言：三级一起取消。不级联的后果是工单已取消、工序任务还停在 PENDING，车间照样能开工
        assertEquals(MesProdOrderStatusEnum.CANCELLED.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        List<WorkOrderDO> workOrders = workOrderMapper.selectListByProdOrderIds(List.of(order.getId()));
        assertEquals(4, workOrders.size());
        assertTrue(workOrders.stream().allMatch(
                workOrder -> MesWorkOrderStatusEnum.CANCELLED.name().equals(workOrder.getStatus())));
        List<OpTaskDO> tasks = tasksOf(order.getId());
        assertEquals(5, tasks.size());
        assertTrue(tasks.stream().allMatch(
                task -> MesOpTaskStatusEnum.CANCELLED.name().equals(task.getStatus())));
        // 级联行也走乐观锁条件更新，版本随之自增
        assertTrue(workOrders.stream().allMatch(
                workOrder -> workOrder.getVersion() == MesConstants.INIT_VERSION + 1));
        // 下达与取消各一条流水，取消记 RELEASED -> CANCELLED
        assertEquals(2L, wipTxnMapper.selectCount());
        WipTxnDO cancelTxn = wipTxnMapper.selectList().stream()
                .filter(txn -> MesProdOrderStatusEnum.CANCELLED.name().equals(txn.getToStatus()))
                .findFirst().orElseThrow();
        assertEquals(MesWipTxnTypeEnum.SKIP.name(), cancelTxn.getTxnType());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), cancelTxn.getFromStatus());
        assertEquals("客户撤单", cancelTxn.getRemark());
    }

    @Test
    void testCancelProdOrder_invalidStatusRejected() {
        // 准备：已开工的工单不允许取消，在制品无处可去
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);

        assertServiceException(() -> domainService.cancelProdOrder(actionBO(order, "客户撤单")),
                PROD_ORDER_STATUS_INVALID);
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), prodOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    void testCancelProdOrder_idempotent() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);

        // 调用：同键两次。第二次若真执行，CANCELLED 是终态会直接抛错
        domainService.cancelProdOrder(actionBO(order, "客户撤单"));
        domainService.cancelProdOrder(actionBO(order, "客户撤单"));

        // 断言：只推进一次
        assertEquals(MesProdOrderStatusEnum.CANCELLED.name(), prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, prodOrderMapper.selectById(order.getId()).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testReleaseProdOrder_callerIdempotencyKeyWins() {
        // 准备：上游（例如 APS）用自己的重试作用域给了一个键
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT);
        MesReleaseOrderBO bo = releaseBO(order);
        bo.setIdempotencyKey("APS:SCH-001:RELEASE_PROD_ORDER:0");

        // 调用
        domainService.releaseProdOrder(bo);

        // 断言：以调用方给的键为准，命令记录与在制流水用同一个键
        assertNotNull(commandRecordMapper.selectByIdempotencyKey("APS:SCH-001:RELEASE_PROD_ORDER:0"));
        assertEquals("APS:SCH-001:RELEASE_PROD_ORDER:0", wipTxnMapper.selectList().get(0).getIdempotencyKey());
    }

    /**
     * 04 文档"工序串联规则"的原型路线：下料 -&gt; 加工 -&gt; （喷漆 ‖ 标识） -&gt; 装配。
     *
     * <p>装配特意回到加工的工作中心，5 道工序只应产出 4 张作业工单。
     */
    private List<RouteOpRespDTO> parallelRoute() {
        return List.of(
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null),
                routeOp(ROUTE_OP_MACHINE, 20, 42L, WC_MACHINE, null),
                routeOp(ROUTE_OP_PAINT, 30, 43L, WC_PAINT, GROUP_A),
                routeOp(ROUTE_OP_MARK, 30, 44L, WC_MARK, GROUP_A),
                routeOp(ROUTE_OP_ASSEMBLE, 40, 45L, WC_MACHINE, null));
    }

    private RouteOpRespDTO routeOp(Long id, int seq, Long opId, Long wcId, String parallelGroup) {
        RouteOpRespDTO routeOp = new RouteOpRespDTO();
        routeOp.setId(id);
        routeOp.setRouteId(MesTestFixtures.route().getId());
        routeOp.setSeq(seq);
        routeOp.setOpId(opId);
        routeOp.setWcId(wcId);
        routeOp.setParallelGroup(parallelGroup);
        routeOp.setKeyFlag(Boolean.FALSE);
        return routeOp;
    }

    private OpRespDTO op(Long opId) {
        OpRespDTO op = new OpRespDTO();
        op.setId(opId);
        op.setCode("OP" + opId);
        op.setName("工序" + opId);
        return op;
    }

    /**
     * 落一张生产工单。
     *
     * <p>{@code version} 必须显式给初始值：带 {@code WHERE version = ?} 条件的更新碰上 NULL 版本
     * 会一行都更不到，那是数据问题不是并发问题。生产上由 codegen 建单统一落 0。
     *
     * <p>BOM 与工艺路线故意落两个过期编号，用来验证下达确实换成了当天的生效版本。
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
                .qty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(1L)
                .bomId(STALE_BOM_ID)
                .bomVersion("B0.9")
                .routeId(STALE_ROUTE_ID)
                .routeVersion("R0.9")
                .priority(0)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    /** 下达参数：不带幂等键，走服务端按四段格式派生的那条路。 */
    private MesReleaseOrderBO releaseBO(ProdOrderDO order) {
        MesReleaseOrderBO bo = new MesReleaseOrderBO();
        bo.setProdOrderId(order.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(order.getOrderNo());
        bo.setRemark("计划员下达");
        return bo;
    }

    private MesOrderActionBO actionBO(ProdOrderDO order, String reason) {
        MesOrderActionBO bo = new MesOrderActionBO();
        bo.setProdOrderId(order.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(order.getOrderNo());
        bo.setReason(reason);
        return bo;
    }

    /** 取本工单展开出的全部工序任务。 */
    private List<OpTaskDO> tasksOf(Long prodOrderId) {
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds);
    }

    /** 按路线工序编号定位工序任务，不按下标，避免断言被生成顺序绑住。 */
    private OpTaskDO taskOf(List<OpTaskDO> tasks, Long routeOpId) {
        return tasks.stream().filter(task -> routeOpId.equals(task.getRouteOpId())).findFirst().orElseThrow();
    }

}
