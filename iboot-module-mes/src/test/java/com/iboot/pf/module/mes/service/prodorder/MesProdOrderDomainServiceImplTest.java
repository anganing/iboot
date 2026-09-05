package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.command.MesCommandRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderActionBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderCloseResultBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.CreateOutOrderReqDTO;
import com.iboot.pf.module.wms.api.reservation.InventoryReservationApi;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationHandleReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationRespDTO;
import com.iboot.pf.module.wms.api.warehouse.WarehouseApi;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 生产工单领域动作测试：下达锁版本与工序展开、挂起、恢复、取消级联，四个动作的状态校验、幂等与在制流水。
 *
 * <p>建单时故意把工单上的 BOM 与工艺路线编号写成两个过期值，只给"生效版本"的路线挂桩：
 * 下达如果沿用了工单原来的 routeId，展开会因取不到工序而失败，锁版本这件事就不会被漏掉。
 */
@Import({MesProdOrderDomainServiceImpl.class, MesOrderExpandHelper.class, MesOrderCloseHelper.class,
        MesKitRequirementExpander.class, MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesProdOrderDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal ORDER_QTY = new BigDecimal("100");
    private static final String GROUP_A = "A";

    /** 关闭用例的仓储维度。 */
    private static final Long INV_ORG_ID = 1L;
    private static final Long WH_ID = 301L;
    private static final Long LOC_ID = 401L;
    private static final Long UOM_ID = 1L;
    /** 关闭用例的物料：常规料、倒冲料。 */
    private static final Long MAT_PICK = 1001L;
    private static final Long MAT_BACKFLUSH = 1002L;

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
    @Resource
    private ProdDemandMapper prodDemandMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;

    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private OpApi opApi;
    @MockitoBean
    private BomApi bomApi;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private UomApi uomApi;
    @MockitoBean
    private InventoryReservationApi reservationApi;
    @MockitoBean
    private OutOrderApi outOrderApi;
    @MockitoBean
    private WarehouseApi warehouseApi;
    @MockitoBean
    private MesCodeService codeService;

    /** 三账替身：现存、预留、可用，恒满足 可用 = 现存 − 预留。 */
    private final Map<Long, BigDecimal> onhand = new HashMap<>();
    private final Map<Long, BigDecimal> reserved = new HashMap<>();
    private final Map<Long, BigDecimal> available = new HashMap<>();
    /** 预留台账：预留编号 -> 预留。 */
    private final Map<Long, FakeReservation> reservations = new LinkedHashMap<>();
    /** WMS 侧收到的出库单与已过账单据，用于断言反冲只发生一次。 */
    private final Map<Long, CreateOutOrderReqDTO> outOrders = new LinkedHashMap<>();
    private final List<Long> postedOrders = new ArrayList<>();
    /** WMS 侧收到的幂等键，用于断言键段分得开。 */
    private final List<String> wmsKeys = new ArrayList<>();
    /** 本用例的 BOM 明细，按需铺。 */
    private final List<BomLineRespDTO> bomLines = new ArrayList<>();

    private final AtomicLong reservationIdSeq = new AtomicLong();
    private final AtomicLong wmsOrderIdSeq = new AtomicLong();

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
        resetStockLedger();
        mockCloseDependencies();
    }

    /** 每个用例前清账，避免上一个用例的库存残留。 */
    private void resetStockLedger() {
        onhand.clear();
        reserved.clear();
        available.clear();
        reservations.clear();
        outOrders.clear();
        postedOrders.clear();
        wmsKeys.clear();
        bomLines.clear();
        reservationIdSeq.set(0);
        wmsOrderIdSeq.set(0);
    }

    /**
     * 关闭动作要用到的跨模块替身。
     *
     * <p>释放预留与出库过账都真的改账面，不用"验证 mock 被调用几次"代替：本任务要证明的是
     * 可用量回升与倒冲扣料，调用次数正确但账面算错的实现照样能骗过那种断言。
     */
    private void mockCloseDependencies() {
        when(bomApi.getBomLineList(any())).thenAnswer(invocation -> List.copyOf(bomLines));
        when(materialApi.getMaterialMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> material((Long) id)));
        });
        when(uomApi.getUomMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> uom((Long) id)));
        });
        when(warehouseApi.getWh(any())).thenAnswer(invocation -> {
            WhRespDTO wh = new WhRespDTO();
            wh.setId(invocation.getArgument(0));
            wh.setInvOrgId(INV_ORG_ID);
            return wh;
        });
        when(reservationApi.getReservationMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, ReservationRespDTO> result = new LinkedHashMap<>();
            ids.forEach(id -> {
                FakeReservation reservation = reservations.get(id);
                if (reservation != null) {
                    result.put((Long) id, reservation.toDTO());
                }
            });
            return result;
        });
        doAnswer(invocation -> {
            ReservationHandleReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            releaseReservation(reservations.get(reqDTO.getReservationId()), reqDTO.getQty());
            return null;
        }).when(reservationApi).releaseInventory(any());
        when(outOrderApi.createOutOrder(any())).thenAnswer(invocation -> {
            CreateOutOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            Long existed = outOrders.entrySet().stream()
                    .filter(entry -> entry.getValue().getIdempotencyKey().equals(reqDTO.getIdempotencyKey()))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            if (existed != null) {
                return existed;
            }
            // 与 WMS 的 OUT_ORDER_LOC_REQUIRED 同口径：不校验的话 MES 漏给库位在单测里看不出来
            reqDTO.getLines().forEach(line -> {
                if (line.getFromLocId() == null || MesConstants.NONE_ID.equals(line.getFromLocId())) {
                    throw new IllegalStateException("WMS 拒绝：出库明细未指定来源库位");
                }
            });
            long orderId = wmsOrderIdSeq.incrementAndGet();
            outOrders.put(orderId, reqDTO);
            return orderId;
        });
        doAnswer(invocation -> {
            ConfirmOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            postOutOrder(reqDTO.getOrderId());
            return null;
        }).when(outOrderApi).confirmOutOrder(any());
    }

    /** 释放预留：预留量下降、可用量等额回升，数量留空表示全部剩余。 */
    private void releaseReservation(FakeReservation reservation, BigDecimal qty) {
        if (reservation == null) {
            return;
        }
        BigDecimal remaining = reservation.qty.subtract(reservation.releasedQty);
        BigDecimal handleQty = qty == null ? remaining : qty.min(remaining);
        if (handleQty.signum() <= 0) {
            return;
        }
        reservation.releasedQty = reservation.releasedQty.add(handleQty);
        reserved.merge(reservation.matId, handleQty.negate(), BigDecimal::add);
        available.merge(reservation.matId, handleQty, BigDecimal::add);
        if (reservation.qty.subtract(reservation.releasedQty).signum() <= 0) {
            reservation.status = WmsReservationStatusEnum.RELEASED.name();
        } else {
            reservation.status = WmsReservationStatusEnum.PARTIAL.name();
        }
    }

    /** 出库过账：现存与可用同额下降。倒冲件没有预留可核销。已过账的单不许再过账。 */
    private void postOutOrder(Long orderId) {
        CreateOutOrderReqDTO order = outOrders.get(orderId);
        if (order == null) {
            throw new IllegalStateException("WMS 拒绝：出库单不存在");
        }
        if (postedOrders.contains(orderId)) {
            throw new IllegalStateException("WMS 拒绝：出库单已过账");
        }
        for (CreateOutOrderReqDTO.Line line : order.getLines()) {
            onhand.merge(line.getMatId(), line.getPlanQty().negate(), BigDecimal::add);
            available.merge(line.getMatId(), line.getPlanQty().negate(), BigDecimal::add);
        }
        postedOrders.add(orderId);
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

    @Test
    void testCloseProdOrder_releasesRemainingReservationAndAvailableRecovers() {
        // 准备（验收点名的构造）：一张已完工的工单，名下一张仍停在"已提交"的领料单占着 100 预留。
        // 三账起点是 现存 500 / 预留 100 / 可用 400
        ProdOrderDO order = doneOrder("100");
        givenInventory(MAT_PICK, "500");
        Long reservationId = givenReservation(MAT_PICK, "100");
        MaterialReqDO req = insertSubmittedReq(order, MAT_PICK, "100", reservationId, "0");
        assertAccounts(MAT_PICK, "500", "100", "400");

        // 调用
        MesOrderCloseResultBO result = domainService.closeProdOrder(closeBO(order));

        // 断言（验收点名）：可用量回升到 500，预留状态 RELEASED，恒等式成立
        assertAccounts(MAT_PICK, "500", "0", "500");
        assertEquals(WmsReservationStatusEnum.RELEASED.name(), reservations.get(reservationId).status);
        // 键段必须是 RELEASE_ON_CLOSE：与取消用的 RELEASE、部分确认用的 RELEASE_REMAIN 撞键的话，
        // WMS 会把这次释放当成重复请求丢掉，可用量永远回不来
        assertTrue(wmsKeys.contains("MES:" + req.getReqNo() + ":RELEASE_ON_CLOSE:1"));
        // 工单进终态，账目已收尾的领料单也一起作废——留在"已提交"会让车间以为还等着发料
        assertEquals(MesProdOrderStatusEnum.CLOSED.name(),
                prodOrderMapper.selectById(order.getId()).getStatus());
        assertEquals(MesMaterialReqStatusEnum.CANCELLED.name(),
                materialReqMapper.selectById(req.getId()).getStatus());
        assertEquals(MesMaterialReqLineStatusEnum.CANCELLED.name(),
                materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, req.getId()).get(0).getStatus());
        // 行上的 reservation_id 保留：它是这笔预留曾经存在过的唯一线索
        assertEquals(reservationId,
                materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, req.getId()).get(0)
                        .getReservationId());
        // 一件料都没发出去，因此没有多余领料
        assertTrue(result.getUnreturnedLines().isEmpty());
        // 关闭流水：COMPLETE 类型 + 整单完成数
        List<WipTxnDO> txns = wipTxnMapper.selectList().stream()
                .filter(txn -> MesWipTxnTypeEnum.COMPLETE.name().equals(txn.getTxnType())).toList();
        assertEquals(1, txns.size());
        assertEquals(MesProdOrderStatusEnum.DONE.name(), txns.get(0).getFromStatus());
        assertEquals(MesProdOrderStatusEnum.CLOSED.name(), txns.get(0).getToStatus());
        assertEquals(0, new BigDecimal("100").compareTo(txns.get(0).getQty()));
    }

    @Test
    void testCloseProdOrder_alreadyConfirmedReqNotReleasedTwice() {
        // 准备：领料单已经确认完（DONE），确认那一步已经跑过 RELEASE_REMAIN。
        // 这是"重复释放"最容易发生的地方：不按单据状态过滤就会对同一笔预留再释放一次
        ProdOrderDO order = doneOrder("100");
        givenInventory(MAT_PICK, "500");
        Long reservationId = givenReservation(MAT_PICK, "100");
        // 模拟确认发料：现存与可用各降 100、预留核销 100（可用回补），账面落到 400/0/400
        consumeReservation(reservationId);
        MaterialReqDO req = insertDoneReq(order, MAT_PICK, "100", reservationId, "100");
        assertAccounts(MAT_PICK, "400", "0", "400");

        // 调用
        domainService.closeProdOrder(closeBO(order));

        // 断言：一笔都不释放，账面不动；单据保持 DONE 不被改成 CANCELLED
        assertAccounts(MAT_PICK, "400", "0", "400");
        assertTrue(wmsKeys.stream().noneMatch(key -> key.contains("RELEASE_ON_CLOSE")));
        assertEquals(MesMaterialReqStatusEnum.DONE.name(),
                materialReqMapper.selectById(req.getId()).getStatus());
    }

    @Test
    void testCloseProdOrder_backflushByActualQty() {
        // 准备：BOM 一行倒冲件（单位用量 2、损耗率 0），实际产量 90 -> 需求 180。
        // 另一行常规料不参与反冲
        ProdOrderDO order = doneOrder("90");
        givenInventory(MAT_BACKFLUSH, "500");
        givenInventory(MAT_PICK, "500");
        bomLines.add(bomLine(MAT_BACKFLUSH, "2", "0", MesIssueMethodEnum.BACKFLUSH));
        bomLines.add(bomLine(MAT_PICK, "1", "0", MesIssueMethodEnum.PICK));

        // 调用
        domainService.closeProdOrder(closeBO(order));

        // 断言：只有倒冲件出库，数量按实际产量算而不是计划数量（按 100 算会是 200）
        assertEquals(1, outOrders.size());
        CreateOutOrderReqDTO outOrder = outOrders.values().iterator().next();
        assertEquals(WmsOutBizTypeEnum.PROD_ISSUE_OUT.name(), outOrder.getBizType());
        assertEquals(ORDER_NO, outOrder.getProdOrderNo());
        assertEquals(1, outOrder.getLines().size());
        CreateOutOrderReqDTO.Line line = outOrder.getLines().get(0);
        assertEquals(MAT_BACKFLUSH, line.getMatId());
        assertEquals(0, new BigDecimal("180").compareTo(line.getPlanQty()));
        assertEquals(LOC_ID, line.getFromLocId());
        // 倒冲件从来不预留，行上的预留编号必须是 0，带上别人的预留会误核销
        assertEquals(MesConstants.NONE_ID, line.getReservationId());
        // 过账真的扣了现存与可用
        assertEquals(1, postedOrders.size());
        assertAccounts(MAT_BACKFLUSH, "320", "0", "320");
        // 常规料一分没动：它的消耗在领料出库那一步已经入账
        assertAccounts(MAT_PICK, "500", "0", "500");
    }

    @Test
    void testCloseProdOrder_noBackflushNoOutOrder() {
        // 准备：BOM 里只有常规料
        ProdOrderDO order = doneOrder("100");
        bomLines.add(bomLine(MAT_PICK, "1", "0", MesIssueMethodEnum.PICK));

        domainService.closeProdOrder(closeBO(order));

        // 断言：不去 WMS 建一张空出库单
        assertTrue(outOrders.isEmpty());
        assertEquals(MesProdOrderStatusEnum.CLOSED.name(),
                prodOrderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    void testCloseProdOrder_unreturnedMaterialsListedButNotReturned() {
        // 准备：常规料发了 100（其中退回 10，净发 90），实际产量 60 -> 应耗 60，多余 30
        ProdOrderDO order = doneOrder("60");
        bomLines.add(bomLine(MAT_PICK, "1", "0", MesIssueMethodEnum.PICK));
        givenInventory(MAT_PICK, "500");
        insertDoneReq(order, MAT_PICK, "100", MesConstants.NONE_ID, "100");
        insertReturnReq(order, MAT_PICK, "10", "10");

        // 调用
        MesOrderCloseResultBO result = domainService.closeProdOrder(closeBO(order));

        // 断言：清单列出多余量，但不自动退料——车间可能还在用这批料
        assertEquals(1, result.getUnreturnedLines().size());
        MesOrderCloseResultBO.UnreturnedLine unreturned = result.getUnreturnedLines().get(0);
        assertEquals(MAT_PICK, unreturned.getMatId());
        assertEquals("MAT-" + MAT_PICK, unreturned.getMatCode());
        assertEquals(0, new BigDecimal("90").compareTo(unreturned.getIssuedQty()));
        assertEquals(0, new BigDecimal("60").compareTo(unreturned.getRequiredQty()));
        assertEquals(0, new BigDecimal("30").compareTo(unreturned.getOverQty()));
        // 没有任何退料出入库动作：清单只是提示
        assertTrue(outOrders.isEmpty());
        assertAccounts(MAT_PICK, "500", "0", "500");
    }

    @Test
    void testCloseProdOrder_demandDoneWhenAllOrdersSettled() {
        // 准备：一条需求下两张工单，一张已取消、一张待关闭
        ProdDemandDO demand = insertDemand();
        ProdOrderDO closing = doneOrder("100");
        bindDemand(closing, demand.getId());
        ProdOrderDO cancelled = insertOrder(MesProdOrderStatusEnum.CANCELLED);
        cancelled.setOrderNo("PO-0002");
        prodOrderMapper.updateById(new ProdOrderDO().setId(cancelled.getId()).setOrderNo("PO-0002")
                .setDemandId(demand.getId()));

        // 调用
        domainService.closeProdOrder(closeBO(closing));

        // 断言：需求置已完成。被取消的工单永远不会关闭，只认 CLOSED 会让需求永久停在"已下达"
        assertEquals(MesDemandStatusEnum.DONE.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
    }

    @Test
    void testCloseProdOrder_demandStaysWhenSiblingRunning() {
        // 准备：同一条需求下还有一张在产工单
        ProdDemandDO demand = insertDemand();
        ProdOrderDO closing = doneOrder("100");
        bindDemand(closing, demand.getId());
        ProdOrderDO running = insertOrder(MesProdOrderStatusEnum.RUNNING);
        prodOrderMapper.updateById(new ProdOrderDO().setId(running.getId()).setOrderNo("PO-0003")
                .setDemandId(demand.getId()));

        domainService.closeProdOrder(closeBO(closing));

        // 断言：还有工单在跑，需求不能提前完成
        assertEquals(MesDemandStatusEnum.RELEASED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
    }

    @Test
    void testCloseProdOrder_notDoneRejected() {
        // 准备：在产工单直接关闭。状态机里只有 DONE 有 close 出边
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING);
        givenInventory(MAT_PICK, "500");
        Long reservationId = givenReservation(MAT_PICK, "100");
        insertSubmittedReq(order, MAT_PICK, "100", reservationId, "0");

        assertServiceException(() -> domainService.closeProdOrder(closeBO(order)), PROD_ORDER_STATUS_INVALID);

        // 断言：状态校验在收尾之前，一笔预留都不许释放
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                prodOrderMapper.selectById(order.getId()).getStatus());
        assertAccounts(MAT_PICK, "500", "100", "400");
        assertEquals(0L, commandRecordMapper.selectCount());
    }

    @Test
    void testCloseProdOrder_idempotentNoSecondRelease() {
        // 准备：一张待关闭工单，带一笔未发料预留与一行倒冲件
        ProdOrderDO order = doneOrder("90");
        givenInventory(MAT_PICK, "500");
        givenInventory(MAT_BACKFLUSH, "500");
        Long reservationId = givenReservation(MAT_PICK, "100");
        insertSubmittedReq(order, MAT_PICK, "100", reservationId, "0");
        bomLines.add(bomLine(MAT_BACKFLUSH, "2", "0", MesIssueMethodEnum.BACKFLUSH));

        // 调用：同键两次。第二次若真执行，可用量会回升两次（凭空多出 100 个料）、倒冲会扣两遍
        domainService.closeProdOrder(closeBO(order));
        domainService.closeProdOrder(closeBO(order));

        // 断言：账面只动一次，命令记录一条，出库单一张且只过账一次
        assertAccounts(MAT_PICK, "500", "0", "500");
        assertAccounts(MAT_BACKFLUSH, "320", "0", "320");
        assertEquals(1, outOrders.size());
        assertEquals(1, postedOrders.size());
        assertEquals(1L, commandRecordMapper.selectCount());
        assertEquals(MesConstants.INIT_VERSION + 1, prodOrderMapper.selectById(order.getId()).getVersion());
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

    /** 一张已完工、可以关闭的工单，BOM 编号落生效版本（关闭要按它取倒冲件）。 */
    private ProdOrderDO doneOrder(String completedQty) {
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE);
        order.setBomId(MesTestFixtures.bom().getId()).setCompletedQty(new BigDecimal(completedQty));
        prodOrderMapper.updateById(new ProdOrderDO().setId(order.getId())
                .setBomId(order.getBomId()).setCompletedQty(order.getCompletedQty()));
        return order;
    }

    private MesCloseOrderBO closeBO(ProdOrderDO order) {
        MesCloseOrderBO bo = new MesCloseOrderBO();
        bo.setProdOrderId(order.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(order.getOrderNo());
        bo.setReason("生产结束，账目收尾");
        return bo;
    }

    /** 落一条已下达的生产需求。 */
    private ProdDemandDO insertDemand() {
        ProdDemandDO demand = ProdDemandDO.builder()
                .demandNo("DM-0001")
                .sourceType("MANUAL")
                .plantId(1L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(1L)
                .matCode("MAT-001")
                .qty(ORDER_QTY)
                .uomId(UOM_ID)
                .priority(0)
                .status(MesDemandStatusEnum.RELEASED.name())
                .idempotencyKey("UI:DM-0001:CREATE_DEMAND:0")
                .build();
        prodDemandMapper.insert(demand);
        return demand;
    }

    private void bindDemand(ProdOrderDO order, Long demandId) {
        order.setDemandId(demandId);
        prodOrderMapper.updateById(new ProdOrderDO().setId(order.getId()).setDemandId(demandId));
    }

    /** 一张仍停在"已提交"的领料单：预留占着、一件没发。 */
    private MaterialReqDO insertSubmittedReq(ProdOrderDO order, Long matId, String qty,
                                             Long reservationId, String processedQty) {
        return insertReq(order, MesMaterialReqTypeEnum.ISSUE, MesMaterialReqStatusEnum.SUBMITTED,
                MesMaterialReqLineStatusEnum.SUBMITTED, matId, qty, reservationId, processedQty);
    }

    /** 一张确认发料完成的领料单：预留已核销，确认那一步已经释放过未发部分。 */
    private MaterialReqDO insertDoneReq(ProdOrderDO order, Long matId, String qty,
                                        Long reservationId, String processedQty) {
        return insertReq(order, MesMaterialReqTypeEnum.ISSUE, MesMaterialReqStatusEnum.DONE,
                MesMaterialReqLineStatusEnum.DONE, matId, qty, reservationId, processedQty);
    }

    /** 一张已完成的退料单：净发料量要把它减掉。 */
    private MaterialReqDO insertReturnReq(ProdOrderDO order, Long matId, String qty, String processedQty) {
        return insertReq(order, MesMaterialReqTypeEnum.RETURN, MesMaterialReqStatusEnum.DONE,
                MesMaterialReqLineStatusEnum.DONE, matId, qty, MesConstants.NONE_ID, processedQty);
    }

    private MaterialReqDO insertReq(ProdOrderDO order, MesMaterialReqTypeEnum reqType,
                                    MesMaterialReqStatusEnum status, MesMaterialReqLineStatusEnum lineStatus,
                                    Long matId, String qty, Long reservationId, String processedQty) {
        MaterialReqDO req = MaterialReqDO.builder()
                .reqNo("MR-" + (materialReqMapper.selectCount() + 1))
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .workOrderId(MesConstants.NONE_ID)
                .opTaskId(MesConstants.NONE_ID)
                .reqType(reqType.name())
                .invOrgId(INV_ORG_ID)
                .whId(WH_ID)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .status(status.name())
                .idempotencyKey("UI:" + order.getOrderNo() + ":CREATE_REQ:" + materialReqMapper.selectCount())
                .build();
        materialReqMapper.insert(req);
        materialReqLineMapper.insert(MaterialReqLineDO.builder()
                .reqId(req.getId())
                .lineNo(MesConstants.FIRST_LINE_NO)
                .routeOpMatId(MesConstants.NONE_ID)
                .matId(matId)
                .matCode("MAT-" + matId)
                .matName("物料" + matId)
                .qty(new BigDecimal(qty))
                .processedQty(new BigDecimal(processedQty))
                .uomId(UOM_ID)
                .uomCode("EA")
                .batchId(MesConstants.NONE_ID)
                .reservationId(reservationId)
                .mustIssueBeforeStart(Boolean.TRUE)
                .status(lineStatus.name())
                .build());
        return req;
    }

    /** 铺初始库存：现存与可用同额、无预留。 */
    private void givenInventory(Long matId, String qty) {
        onhand.put(matId, new BigDecimal(qty));
        available.put(matId, new BigDecimal(qty));
        reserved.put(matId, BigDecimal.ZERO);
    }

    /** 占一笔预留：可用量下降、现存不动，返回预留编号。 */
    private Long givenReservation(Long matId, String qty) {
        BigDecimal reserveQty = new BigDecimal(qty);
        available.merge(matId, reserveQty.negate(), BigDecimal::add);
        reserved.merge(matId, reserveQty, BigDecimal::add);
        long reservationId = reservationIdSeq.incrementAndGet();
        reservations.put(reservationId, new FakeReservation(reservationId, matId, reserveQty));
        return reservationId;
    }

    /** 模拟确认发料：出库扣现存与可用，紧接着核销预留把可用补回来，与真实 WMS 的两步顺序一致。 */
    private void consumeReservation(Long reservationId) {
        FakeReservation reservation = reservations.get(reservationId);
        onhand.merge(reservation.matId, reservation.qty.negate(), BigDecimal::add);
        available.merge(reservation.matId, reservation.qty.negate(), BigDecimal::add);
        reserved.merge(reservation.matId, reservation.qty.negate(), BigDecimal::add);
        available.merge(reservation.matId, reservation.qty, BigDecimal::add);
        reservation.releasedQty = reservation.qty;
        reservation.status = WmsReservationStatusEnum.CONSUMED.name();
    }

    /**
     * 三个账一起断言，并校验 <b>可用量 = 现存 − 预留</b> 这个恒等式。
     *
     * <p>只断言可用量回升是个太容易过的断言：什么都不做的实现、以及"预留释放了但现存也被错扣"的实现
     * 都可能让可用量看起来对。三个账一起对，账才是对的。
     */
    private void assertAccounts(Long matId, String onhandQty, String reservedQty, String availableQty) {
        assertEquals(0, new BigDecimal(onhandQty).compareTo(onhand.getOrDefault(matId, BigDecimal.ZERO)),
                "现存量不符");
        assertEquals(0, new BigDecimal(reservedQty).compareTo(reserved.getOrDefault(matId, BigDecimal.ZERO)),
                "预留量不符");
        assertEquals(0, new BigDecimal(availableQty).compareTo(available.getOrDefault(matId, BigDecimal.ZERO)),
                "可用量不符");
        assertEquals(0, new BigDecimal(onhandQty).subtract(new BigDecimal(reservedQty))
                        .compareTo(available.getOrDefault(matId, BigDecimal.ZERO)),
                "可用量必须等于现存减预留");
    }

    private BomLineRespDTO bomLine(Long matId, String qty, String scrapRate, MesIssueMethodEnum issueMethod) {
        BomLineRespDTO line = new BomLineRespDTO();
        line.setId(matId);
        line.setBomId(MesTestFixtures.bom().getId());
        line.setMatId(matId);
        line.setQty(new BigDecimal(qty));
        line.setScrapRate(new BigDecimal(scrapRate));
        line.setIssueMethod(issueMethod.name());
        line.setUomId(UOM_ID);
        return line;
    }

    private MaterialRespDTO material(Long matId) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(matId);
        material.setCode("MAT-" + matId);
        material.setName("物料" + matId);
        material.setBaseUomId(UOM_ID);
        material.setDefaultWhId(WH_ID);
        material.setDefaultLocId(LOC_ID);
        return material;
    }

    private UomRespDTO uom(Long uomId) {
        UomRespDTO uom = new UomRespDTO();
        uom.setId(uomId);
        uom.setCode("EA");
        return uom;
    }

    /** 预留台账的一笔记录。 */
    private static final class FakeReservation {

        private final Long id;
        private final Long matId;
        private final BigDecimal qty;
        private BigDecimal releasedQty = BigDecimal.ZERO;
        private String status = WmsReservationStatusEnum.ACTIVE.name();

        private FakeReservation(Long id, Long matId, BigDecimal qty) {
            this.id = id;
            this.matId = matId;
            this.qty = qty;
        }

        private ReservationRespDTO toDTO() {
            ReservationRespDTO dto = new ReservationRespDTO();
            dto.setId(id);
            dto.setMatId(matId);
            dto.setQty(qty);
            dto.setReleasedQty(releasedQty);
            dto.setLocId(LOC_ID);
            dto.setWhId(WH_ID);
            dto.setInvOrgId(INV_ORG_ID);
            dto.setStatus(status);
            return dto;
        }

    }

}
