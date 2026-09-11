package com.iboot.pf.module.mes.service.materialreq;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.bpm.api.task.BpmProcessInstanceApi;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.CreateOutOrderReqDTO;
import com.iboot.pf.module.wms.api.reservation.InventoryReservationApi;
import com.iboot.pf.module.wms.api.reservation.dto.CreateReservationReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationHandleReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationRespDTO;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_CONFIRM_QTY_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_CONFIRM_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LOC_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_RETURN_BATCH_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_WAREHOUSE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 领退补料申请领域动作测试：创建展开、提交预留、确认发料、取消释放。
 *
 * <p>WMS 侧用一个受控替身，维护三个账而不是一个：{@link #onhand} 现存量、{@link #reservedQty} 预留量、
 * {@link #stock} 可用量，且始终满足 <b>可用量 = 现存 − 预留</b>。预留时扣可用量不动现存，
 * 释放时回补可用量，出库过账时先扣现存与可用量、再按行上的预留编号核销预留把可用量补回来——
 * 与真实 WMS 的动作顺序一致。
 *
 * <p>三个账分开维护是为了让"确认后可用量与确认前相同"这个断言真的有算术含义：
 * 只有现存下降与预留核销同时发生、两者抵消，可用量才会不变。如果实现漏传 {@code reservationId}，
 * 替身就不会核销，可用量会掉下去，用例立刻变红。只断言"可用量不变"而账目不分开的话，
 * 实现里什么都不做也能过。
 *
 * <p>真实的库存过账账实一致由阶段 6 的跨模块集成测试覆盖，MES 单测里没有 wms 表可用。
 */
@Import({MesMaterialReqDomainServiceImpl.class, MesKitRequirementExpander.class,
        MesCommandServiceImpl.class})
class MesMaterialReqDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String WMS_SOURCE_MODULE = "MES";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal ORDER_QTY = new BigDecimal("100");

    private static final Long BOM_ID = 11L;
    private static final Long MAT_A = 1001L;
    private static final Long MAT_B = 1002L;
    private static final Long MAT_BACKFLUSH = 1005L;
    private static final Long ROUTE_OP_CUT = 101L;
    private static final Long ROUTE_OP_MAT_A = 401L;
    private static final Long ROUTE_OP_MAT_BACKFLUSH = 402L;
    private static final Long UOM_ID = 1L;
    private static final Long BATCH_ID = 501L;
    private static final Long INV_ORG_ID = 1L;
    private static final Long WH_ID = 1L;
    private static final Long LOC_ID = 601L;

    @Resource
    private MesMaterialReqDomainService domainService;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private BomApi bomApi;
    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private UomApi uomApi;
    @MockitoBean
    private BatchApi batchApi;
    @MockitoBean
    private InventoryApi inventoryApi;
    @MockitoBean
    private InventoryReservationApi reservationApi;
    @MockitoBean
    private OutOrderApi outOrderApi;
    @MockitoBean
    private InOrderApi inOrderApi;
    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;

    /** 物料到可用量的账面，恒等于现存 − 预留。 */
    private final Map<Long, BigDecimal> stock = new HashMap<>();
    /** 物料到现存量的账面，只有出入库过账才改它。 */
    private final Map<Long, BigDecimal> onhand = new HashMap<>();
    /** 物料到预留量的账面，预留加、释放与核销减。 */
    private final Map<Long, BigDecimal> reservedQty = new HashMap<>();
    /** 预留台账：预留编号 -> 预留。 */
    private final Map<Long, FakeReservation> reservations = new LinkedHashMap<>();
    /** WMS 侧收到的预留与释放幂等键，用于断言键格式。 */
    private final List<String> wmsKeys = new ArrayList<>();
    /** WMS 侧收到的出库单，按单据编号存。 */
    private final Map<Long, CreateOutOrderReqDTO> outOrders = new LinkedHashMap<>();
    /** WMS 侧收到的入库单，按单据编号存。 */
    private final Map<Long, CreateInOrderReqDTO> inOrders = new LinkedHashMap<>();
    /** 已过账的 WMS 单据编号，用于断言不会二次过账。 */
    private final List<Long> postedOrders = new ArrayList<>();
    /** 让 WMS 在第 N 次预留时拒绝，用于模拟并发下前置校验通过但预留失败。 */
    private int rejectReserveAt = -1;

    /** 预留编号发号器与预留次数计数器。 */
    private final AtomicLong reservationIdSeq = new AtomicLong();
    private final AtomicLong wmsOrderIdSeq = new AtomicLong();
    private final AtomicInteger reserveCount = new AtomicInteger();

    @BeforeEach
    void mockExternalDependencies() {
        stock.clear();
        onhand.clear();
        reservedQty.clear();
        reservations.clear();
        wmsKeys.clear();
        outOrders.clear();
        inOrders.clear();
        postedOrders.clear();
        rejectReserveAt = -1;
        reservationIdSeq.set(0);
        wmsOrderIdSeq.set(0);
        reserveCount.set(0);
        Map<String, AtomicInteger> counters = new HashMap<>();
        when(codeService.generate(anyString(), any())).thenAnswer(invocation -> {
            String ruleCode = invocation.getArgument(0);
            return invocation.getArgument(1) != null ? invocation.getArgument(1)
                    : ruleCode + "-" + counters.computeIfAbsent(ruleCode, key -> new AtomicInteger())
                    .incrementAndGet();
        });
        when(materialApi.getMaterialMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> material((Long) id)));
        });
        when(uomApi.getUomMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> uom((Long) id)));
        });
        when(batchApi.getBatchMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> batch((Long) id)));
        });
        when(inventoryApi.getAvailableQtyMap(any())).thenAnswer(invocation -> {
            InventoryQueryReqDTO query = invocation.getArgument(0);
            Map<Long, BigDecimal> result = new LinkedHashMap<>();
            query.getMatIds().forEach(matId -> {
                BigDecimal qty = stock.get(matId);
                if (qty != null) {
                    result.put((Long) matId, qty);
                }
            });
            return result;
        });
        when(bomApi.getBomLineList(any())).thenReturn(List.of());
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of());
        when(processInstanceApi.createProcessInstance(any(), any())).thenReturn("supplement-process-1");
        mockReservationApi();
        mockOrderApi();
    }

    /**
     * WMS 预留门面替身：预留占可用量不动现存，释放与核销都把可用量还回来。
     *
     * <p>不用"验证 mock 被调用几次"代替，因为本任务真正要证明的是三个账的升降，
     * 调用次数正确但账面算错的实现照样能骗过那种断言。
     */
    private void mockReservationApi() {
        when(reservationApi.reserveInventory(any())).thenAnswer(invocation -> {
            CreateReservationReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            FakeReservation existed = reservations.values().stream()
                    .filter(item -> item.key.equals(reqDTO.getIdempotencyKey())).findFirst().orElse(null);
            if (existed != null) {
                return existed.id;
            }
            // 模拟并发：前置校验通过之后，WMS 在指定的第几次预留上拒绝
            if (reserveCount.incrementAndGet() == rejectReserveAt) {
                throw new IllegalStateException("WMS 拒绝：可用量不足");
            }
            BigDecimal available = stock.getOrDefault(reqDTO.getMatId(), BigDecimal.ZERO);
            if (available.compareTo(reqDTO.getQty()) < 0) {
                throw new IllegalStateException("WMS 拒绝：可用量不足");
            }
            // 预留只占可用量，现存不动
            stock.put(reqDTO.getMatId(), available.subtract(reqDTO.getQty()));
            reservedQty.merge(reqDTO.getMatId(), reqDTO.getQty(), BigDecimal::add);
            long reservationId = reservationIdSeq.incrementAndGet();
            reservations.put(reservationId, new FakeReservation(reservationId, reqDTO.getIdempotencyKey(),
                    reqDTO.getMatId(), reqDTO.getQty(), LOC_ID, reqDTO.getBatchId()));
            return reservationId;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            ReservationHandleReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            handleReservation(reservations.get(reqDTO.getReservationId()), reqDTO.getQty(), false);
            return null;
        }).when(reservationApi).releaseInventory(any());
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
    }

    /**
     * 释放或核销一笔预留：预留量下降，可用量等额回升。
     *
     * <p>两者对可用量的效果相同，区别在追溯口径（{@code RELEASED} 是作废、{@code CONSUMED} 是履约），
     * 所以只用 {@code consumed} 标记区分，账目走同一段算术。数量留空表示全部剩余。
     */
    private void handleReservation(FakeReservation reservation, BigDecimal qty, boolean consume) {
        if (reservation == null) {
            return;
        }
        BigDecimal remaining = reservation.qty.subtract(reservation.releasedQty);
        BigDecimal handleQty = qty == null ? remaining : qty.min(remaining);
        if (handleQty.signum() <= 0) {
            return;
        }
        reservation.releasedQty = reservation.releasedQty.add(handleQty);
        reservedQty.merge(reservation.matId, handleQty.negate(), BigDecimal::add);
        stock.merge(reservation.matId, handleQty, BigDecimal::add);
        reservation.consumed = reservation.consumed || consume;
        if (reservation.qty.subtract(reservation.releasedQty).signum() <= 0) {
            reservation.released = true;
        }
    }

    /**
     * WMS 出入库单门面替身：创建按幂等键判重，过账改现存并核销行上的预留。
     *
     * <p>过账故意分两步走，与真实 WMS 的顺序一致：先按出库量扣现存（可用量随之下降），
     * 再按行上的 {@code reservationId} 核销预留（可用量回升）。实现漏传预留编号时第二步不发生，
     * 可用量就掉下去了——这正是"确认后可用量不变"这个断言要抓的东西。
     *
     * <p>出库行缺库位直接拒绝，与 WMS 的 {@code OUT_ORDER_LOC_REQUIRED} 同口径：
     * 不校验的话 MES 漏给库位在单测里看不出来，到生产才炸。
     */
    private void mockOrderApi() {
        when(outOrderApi.createOutOrder(any())).thenAnswer(invocation -> {
            CreateOutOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            Long existed = outOrders.entrySet().stream()
                    .filter(entry -> entry.getValue().getIdempotencyKey().equals(reqDTO.getIdempotencyKey()))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            if (existed != null) {
                return existed;
            }
            reqDTO.getLines().forEach(line -> {
                if (line.getFromLocId() == null || MesConstants.NONE_ID.equals(line.getFromLocId())) {
                    throw new IllegalStateException("WMS 拒绝：出库明细未指定来源库位");
                }
            });
            long orderId = wmsOrderIdSeq.incrementAndGet();
            outOrders.put(orderId, reqDTO);
            return orderId;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            ConfirmOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            postOutOrder(reqDTO.getOrderId());
            return null;
        }).when(outOrderApi).confirmOutOrder(any());
        when(inOrderApi.createInOrder(any())).thenAnswer(invocation -> {
            CreateInOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            Long existed = inOrders.entrySet().stream()
                    .filter(entry -> entry.getValue().getIdempotencyKey().equals(reqDTO.getIdempotencyKey()))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            if (existed != null) {
                return existed;
            }
            reqDTO.getLines().forEach(line -> {
                if (line.getLocId() == null || MesConstants.NONE_ID.equals(line.getLocId())) {
                    throw new IllegalStateException("WMS 拒绝：入库明细未指定目标库位");
                }
            });
            long orderId = wmsOrderIdSeq.incrementAndGet();
            inOrders.put(orderId, reqDTO);
            return orderId;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            ConfirmOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            postInOrder(reqDTO.getOrderId());
            return null;
        }).when(inOrderApi).confirmInOrder(any());
    }

    /** 出库过账：先扣现存与可用量，再核销行上的预留把可用量补回来。已过账的单不许再过账。 */
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
            stock.merge(line.getMatId(), line.getPlanQty().negate(), BigDecimal::add);
            if (line.getReservationId() != null && !MesConstants.NONE_ID.equals(line.getReservationId())) {
                handleReservation(reservations.get(line.getReservationId()), line.getPlanQty(), true);
            }
        }
        postedOrders.add(orderId);
    }

    /** 入库过账：现存与可用量同额上升，退料没有预留可核销。 */
    private void postInOrder(Long orderId) {
        CreateInOrderReqDTO order = inOrders.get(orderId);
        if (order == null) {
            throw new IllegalStateException("WMS 拒绝：入库单不存在");
        }
        if (postedOrders.contains(orderId)) {
            throw new IllegalStateException("WMS 拒绝：入库单已过账");
        }
        for (CreateInOrderReqDTO.Line line : order.getLines()) {
            onhand.merge(line.getMatId(), line.getPlanQty(), BigDecimal::add);
            stock.merge(line.getMatId(), line.getPlanQty(), BigDecimal::add);
        }
        postedOrders.add(orderId);
    }

    /** 预留台账的一笔记录。 */
    private static final class FakeReservation {

        private final Long id;
        private final String key;
        private final Long matId;
        private final BigDecimal qty;
        private final Long locId;
        private final Long batchId;
        private BigDecimal releasedQty = BigDecimal.ZERO;
        private boolean released;
        private boolean consumed;

        private FakeReservation(Long id, String key, Long matId, BigDecimal qty, Long locId, Long batchId) {
            this.id = id;
            this.key = key;
            this.matId = matId;
            this.qty = qty;
            this.locId = locId;
            this.batchId = batchId;
        }

        private ReservationRespDTO toDTO() {
            ReservationRespDTO dto = new ReservationRespDTO();
            dto.setId(id);
            dto.setMatId(matId);
            dto.setQty(qty);
            dto.setReleasedQty(releasedQty);
            dto.setLocId(locId);
            dto.setBatchId(batchId);
            return dto;
        }

    }

    @Test
    void testCreate_expandByOpTaskRouteMat() {
        // 准备：工序有两种用料，一种常规料一种倒冲件
        ProdOrderDO order = insertOrder();
        OpTaskDO opTask = insertOpTask(order);
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of(
                routeOpMat(ROUTE_OP_MAT_A, MAT_A, "2", MesIssueMethodEnum.PICK, true),
                routeOpMat(ROUTE_OP_MAT_BACKFLUSH, MAT_BACKFLUSH, "5", MesIssueMethodEnum.BACKFLUSH, false)));
        MesCreateMaterialReqBO bo = createBO(order, MesMaterialReqTypeEnum.ISSUE);
        bo.setOpTaskId(opTask.getId());

        // 调用
        Long reqId = domainService.createMaterialReq(bo);

        // 断言：按工序用料展开，倒冲件不出现在领料明细里（报工时反冲，领它是重复领用）
        List<MaterialReqLineDO> lines = linesOf(reqId);
        assertEquals(1, lines.size());
        MaterialReqLineDO line = lines.get(0);
        assertEquals(MAT_A, line.getMatId());
        // 需求量 = 工序计划数 100 × 单位用量 2 ÷ (1 − 0)
        assertEquals(0, new BigDecimal("200").compareTo(line.getQty()));
        assertEquals(MesConstants.FIRST_LINE_NO, line.getLineNo());
        // 行上快照物料编码、名称、规格、单位编码，列表查询不再回查 mdm
        assertEquals("MAT-" + MAT_A, line.getMatCode());
        assertEquals("物料" + MAT_A, line.getMatName());
        assertEquals("规格" + MAT_A, line.getMatSpec());
        assertEquals(UOM_ID, line.getUomId());
        assertEquals("PCS", line.getUomCode());
        // 开工前必发标记来自 mdm_route_op_mat，不是拍脑袋给的
        assertTrue(line.getMustIssueBeforeStart());
        assertEquals(ROUTE_OP_MAT_A, line.getRouteOpMatId());
        assertEquals(MesMaterialReqLineStatusEnum.DRAFT.name(), line.getStatus());
        // 草稿阶段无库存后果：没有预留编号、没有已处理数量
        assertEquals(MesConstants.NONE_ID, line.getReservationId());
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getProcessedQty()));
        assertTrue(reservations.isEmpty());
        // 单据头：单号走编码规则，工单号与项目维度从工单继承，状态草稿，幂等键落库
        MaterialReqDO req = reqOf(reqId);
        assertEquals("MES_MATERIAL_REQ_NO-1", req.getReqNo());
        assertEquals(ORDER_NO, req.getProdOrderNo());
        assertEquals(opTask.getId(), req.getOpTaskId());
        assertEquals(MesConstants.NONE_ID, req.getWorkOrderId());
        assertEquals(MesMaterialReqStatusEnum.DRAFT.name(), req.getStatus());
        assertEquals(SOURCE_MODULE + ":" + ORDER_NO + ":CREATE_REQ:0", req.getIdempotencyKey());
    }

    @Test
    void testCreate_expandByProdOrderBom() {
        // 准备：不给工序，按整单 BOM 展开，其中一行带损耗率
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "2", "0.05", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "1", "0", MesIssueMethodEnum.PICK)));

        // 调用
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 断言：两行明细，需求量与齐套用的是同一个公式（同一个展开器）
        assertEquals(2, linesOf(reqId).size());
        assertEquals(0, new BigDecimal("210.526316").compareTo(lineOf(reqId, MAT_A).getQty()));
        assertEquals(0, new BigDecimal("100").compareTo(lineOf(reqId, MAT_B).getQty()));
        // 整单展开没有工序用料编号
        assertEquals(MesConstants.NONE_ID, lineOf(reqId, MAT_A).getRouteOpMatId());
    }

    @Test
    void testCreate_supplementExpandsLikeIssue() {
        // 准备：补料与领料的技术路径相同，区别只在业务含义（超出计划量的额外领用）
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));

        // 调用
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.SUPPLEMENT));

        // 断言
        assertEquals(1, linesOf(reqId).size());
        assertEquals(MesMaterialReqTypeEnum.SUPPLEMENT.name(), reqOf(reqId).getReqType());
    }

    @Test
    void testCreate_returnRequiresExplicitLines() {
        // 准备：退料没有用料关系可推——退多少是车间数出来的
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));

        assertServiceException(
                () -> domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.RETURN)),
                MATERIAL_REQ_LINE_EMPTY);
        assertEquals(0L, materialReqMapper.selectCount());
    }

    @Test
    void testCreate_returnRequiresBatch() {
        // 准备：退料行没指定批次
        ProdOrderDO order = insertOrder();
        MesCreateMaterialReqBO bo = createBO(order, MesMaterialReqTypeEnum.RETURN);
        bo.setLines(List.of(line(MAT_A, "10", MesConstants.NONE_ID)));

        // 调用并断言：退回去的库存要落到原批次上，否则批次台账对不上
        assertServiceException(() -> domainService.createMaterialReq(bo), MATERIAL_REQ_RETURN_BATCH_REQUIRED);
        assertEquals(0L, materialReqLineMapper.selectCount());
    }

    @Test
    void testCreate_returnWithBatchSnapshotsLotNo() {
        // 准备：退料行指定批次
        ProdOrderDO order = insertOrder();
        MesCreateMaterialReqBO bo = createBO(order, MesMaterialReqTypeEnum.RETURN);
        bo.setLines(List.of(line(MAT_A, "10", BATCH_ID)));

        // 调用
        Long reqId = domainService.createMaterialReq(bo);

        // 断言：批次编号与批次号快照都落库
        MaterialReqLineDO line = linesOf(reqId).get(0);
        assertEquals(BATCH_ID, line.getBatchId());
        assertEquals("LOT-" + BATCH_ID, line.getLotNo());
        assertEquals(0, new BigDecimal("10").compareTo(line.getQty()));
    }

    @Test
    void testCreate_idempotent() {
        // 准备
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        MesCreateMaterialReqBO bo = createBO(order, MesMaterialReqTypeEnum.ISSUE);

        // 调用：同一个派生键创建两次
        Long first = domainService.createMaterialReq(bo);
        Long second = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 断言：只有一张单一份明细，返回同一个编号。创建有落库载体，判重走列不走命令记录表
        assertEquals(first, second);
        assertEquals(1L, materialReqMapper.selectCount());
        assertEquals(1L, materialReqLineMapper.selectCount());
        assertEquals(0L, commandRecordMapper.selectCount());
    }

    @Test
    void testCreate_warehouseRequired() {
        ProdOrderDO order = insertOrder();
        MesCreateMaterialReqBO bo = createBO(order, MesMaterialReqTypeEnum.ISSUE);
        bo.setWhId(MesConstants.NONE_ID);

        assertServiceException(() -> domainService.createMaterialReq(bo), MATERIAL_REQ_WAREHOUSE_REQUIRED);
    }

    @Test
    void testCreate_prodOrderNotExists() {
        MesCreateMaterialReqBO bo = new MesCreateMaterialReqBO();
        bo.setProdOrderId(randomLongId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setReqType(MesMaterialReqTypeEnum.ISSUE.name());

        assertServiceException(() -> domainService.createMaterialReq(bo), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    void testSubmit_reservesEachLineAndDropsAvailableQty() {
        // 准备：两行料，库存都充足
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "2", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        stock.put(MAT_B, new BigDecimal("500"));
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 调用
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));

        // 断言：验收硬指标——可用量按申请量下降（500−100、500−200），料被留住
        assertEquals(0, new BigDecimal("400").compareTo(stock.get(MAT_A)));
        assertEquals(0, new BigDecimal("300").compareTo(stock.get(MAT_B)));
        // 每行都记下 WMS 返回的预留编号：不记的话取消时找不到该释放哪笔预留
        assertEquals(2, reservations.size());
        for (MaterialReqLineDO line : linesOf(reqId)) {
            assertNotEquals(MesConstants.NONE_ID, line.getReservationId());
            assertNotNull(reservations.get(line.getReservationId()));
            assertEquals(MesMaterialReqLineStatusEnum.SUBMITTED.name(), line.getStatus());
        }
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), reqOf(reqId).getStatus());
        // 给 WMS 的预留幂等键带行号：整单一个键会让第二行被当成重复提交丢掉
        String reqNo = reqOf(reqId).getReqNo();
        assertTrue(wmsKeys.contains(WMS_SOURCE_MODULE + ":" + reqNo + ":RESERVE:1"));
        assertTrue(wmsKeys.contains(WMS_SOURCE_MODULE + ":" + reqNo + ":RESERVE:2"));
        // 单据级动作走命令记录表判重
        assertNotNull(commandRecordMapper.selectByIdempotencyKey(
                SOURCE_MODULE + ":" + reqNo + ":" + MesCommandTypeEnum.SUBMIT_MATERIAL_REQ.name() + ":0"));
    }

    @Test
    void testSubmit_returnNotReserved() {
        // 准备：退料单
        ProdOrderDO order = insertOrder();
        MesCreateMaterialReqBO bo = createBO(order, MesMaterialReqTypeEnum.RETURN);
        bo.setLines(List.of(line(MAT_A, "10", BATCH_ID)));
        Long reqId = domainService.createMaterialReq(bo);
        stock.put(MAT_A, new BigDecimal("500"));

        // 调用
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));

        // 断言：退料是往仓库还货，占用可用量没有意义——不产生预留、可用量不动
        assertTrue(reservations.isEmpty());
        assertEquals(0, new BigDecimal("500").compareTo(stock.get(MAT_A)));
        assertEquals(MesConstants.NONE_ID, linesOf(reqId).get(0).getReservationId());
        assertEquals(MesMaterialReqLineStatusEnum.SUBMITTED.name(), linesOf(reqId).get(0).getStatus());
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), reqOf(reqId).getStatus());
    }

    @Test
    void testSubmit_insufficientAvailableQtyRejectedWithMatCode() {
        // 准备：第一行够、第二行不够
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        stock.put(MAT_B, new BigDecimal("30"));
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 调用并断言：验收硬指标——错误信息带缺料物料编码、申请量与可用量，车间照着编码就能去查
        assertServiceException(() -> domainService.submitMaterialReq(actionBO(reqOf(reqId), null)),
                MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT, "MAT-" + MAT_B, "100", "30");
        // 整单没有任何库存后果：够的那一行也不许先占着
        assertTrue(reservations.isEmpty());
        assertEquals(0, new BigDecimal("500").compareTo(stock.get(MAT_A)));
        assertEquals(MesMaterialReqStatusEnum.DRAFT.name(), reqOf(reqId).getStatus());
        for (MaterialReqLineDO line : linesOf(reqId)) {
            assertEquals(MesConstants.NONE_ID, line.getReservationId());
            assertEquals(MesMaterialReqLineStatusEnum.DRAFT.name(), line.getStatus());
        }
    }

    @Test
    void testSubmit_wmsRejectOnSecondLineRollsBackWholeOrder() {
        // 准备：前置校验能过（账面都够），但 WMS 在第二次预留上拒绝，模拟并发抢料
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        stock.put(MAT_B, new BigDecimal("500"));
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));
        rejectReserveAt = 2;

        // 调用并断言：异常穿透领域动作，事务回滚
        assertThrows(IllegalStateException.class,
                () -> domainService.submitMaterialReq(actionBO(reqOf(reqId), null)));
        // 库里没留下半提交的痕迹：单据仍是草稿，第一行的预留编号没落库
        assertEquals(MesMaterialReqStatusEnum.DRAFT.name(), reqOf(reqId).getStatus());
        for (MaterialReqLineDO line : linesOf(reqId)) {
            assertEquals(MesConstants.NONE_ID, line.getReservationId());
            assertEquals(MesMaterialReqLineStatusEnum.DRAFT.name(), line.getStatus());
        }
        // 命令记录也一起回滚，重试才能重新执行
        assertEquals(0L, commandRecordMapper.selectCount());
    }

    @Test
    void testSubmit_notDraftRejected() {
        // 准备：已提交的单据再提交
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        MaterialReqDO req = submittedIssueReq(order);
        MesMaterialReqActionBO bo = actionBO(req, null);
        bo.setIdempotencyKey("UI:" + req.getReqNo() + ":SUBMIT_MATERIAL_REQ:RETRY");

        assertServiceException(() -> domainService.submitMaterialReq(bo), MATERIAL_REQ_STATUS_INVALID);
        // 不会二次预留
        assertEquals(1, reservations.size());
    }

    @Test
    void testSubmit_idempotent() {
        // 准备
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 调用：同键提交两次。第二次若真执行，状态已非草稿必然抛错
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));

        // 断言：可用量只降一次，预留只建一笔
        assertEquals(0, new BigDecimal("400").compareTo(stock.get(MAT_A)));
        assertEquals(1, reservations.size());
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), reqOf(reqId).getStatus());
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testConfirm_issueDropsOnhandConsumesReservationAndKeepsAvailableQty() {
        // 准备：领 100，初始现存 500，提交后已预留 100（可用量 400）
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);
        assertAccounts(MAT_A, "500", "100", "400");

        // 调用
        domainService.confirmMaterialReq(confirmBO(req));

        // 断言：验收硬指标——现存下降到 400、预留被核销、可用量仍是 400。三条一起才有意义：
        // 只断言可用量不变的话，什么都不做的实现也能过
        assertAccounts(MAT_A, "400", "0", "400");
        FakeReservation reservation = reservations.get(linesOf(req.getId()).get(0).getReservationId());
        assertTrue(reservation.consumed);
        assertEquals(0, new BigDecimal("100").compareTo(reservation.releasedQty));
        // 验收硬指标——wms_biz_no 已回写，能从 MES 单据定位到那张 WMS 单据
        MaterialReqDO confirmed = reqOf(req.getId());
        assertNotNull(confirmed.getWmsBizNo());
        assertEquals(String.valueOf(outOrders.keySet().iterator().next()), confirmed.getWmsBizNo());
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), confirmed.getStatus());
        // 行上回写已处理数量并置完成
        MaterialReqLineDO line = linesOf(req.getId()).get(0);
        assertEquals(0, new BigDecimal("100").compareTo(line.getProcessedQty()));
        assertEquals(MesMaterialReqLineStatusEnum.DONE.name(), line.getStatus());
        // 出库单创建与过账各一次，键是单据级的
        assertEquals(1, outOrders.size());
        assertEquals(1, postedOrders.size());
        assertTrue(wmsKeys.contains(WMS_SOURCE_MODULE + ":" + req.getReqNo() + ":CREATE_OUT_ORDER:0"));
        assertTrue(wmsKeys.contains(WMS_SOURCE_MODULE + ":" + req.getReqNo() + ":CONFIRM_OUT_ORDER:0"));
    }

    @Test
    void testConfirm_returnRaisesOnhand() {
        // 准备：退 10，初始现存 500。退料不预留，提交后账面不动
        ProdOrderDO order = insertOrder();
        givenInventory(MAT_A, "500");
        MesCreateMaterialReqBO createBO = createBO(order, MesMaterialReqTypeEnum.RETURN);
        createBO.setLines(List.of(line(MAT_A, "10", BATCH_ID)));
        Long reqId = domainService.createMaterialReq(createBO);
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));
        assertAccounts(MAT_A, "500", "0", "500");

        // 调用：退料没有预留可依据，库位必须显式给
        MesConfirmMaterialReqBO bo = confirmBO(reqOf(reqId));
        bo.setLines(List.of(confirmLine(1, null, LOC_ID)));
        domainService.confirmMaterialReq(bo);

        // 断言：验收硬指标——退料确认后现存上升到 510，没有预留被动过
        assertAccounts(MAT_A, "510", "0", "510");
        assertTrue(reservations.isEmpty());
        assertEquals(WmsInBizTypeEnum.PROD_RETURN_IN.name(),
                inOrders.values().iterator().next().getBizType());
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), reqOf(reqId).getStatus());
        // 退料走入库单，不该产生出库单
        assertTrue(outOrders.isEmpty());
        assertEquals(1, inOrders.size());
    }

    @Test
    void testConfirm_idempotentNoSecondPosting() {
        // 准备
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);

        // 调用：同键确认两次。第二次若真执行，状态已非已提交必然抛错，替身也会拒绝二次过账
        domainService.confirmMaterialReq(confirmBO(req));
        domainService.confirmMaterialReq(confirmBO(req));

        // 断言：验收硬指标——重复确认不产生第二笔过账。现存只降一次，出库单只有一张且只过账一次
        assertAccounts(MAT_A, "400", "0", "400");
        assertEquals(1, outOrders.size());
        assertEquals(1, postedOrders.size());
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), reqOf(req.getId()).getStatus());
        // 提交与确认各一条命令记录
        assertEquals(2L, commandRecordMapper.selectCount());
    }

    @Test
    void testConfirm_outOrderHeaderAndLineCarryTraceFields() {
        // 准备：工序级领料，单据上有作业工单与工序任务
        ProdOrderDO order = insertOrder();
        OpTaskDO opTask = insertOpTask(order);
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of(
                routeOpMat(ROUTE_OP_MAT_A, MAT_A, "1", MesIssueMethodEnum.PICK, true)));
        givenInventory(MAT_A, "500");
        MesCreateMaterialReqBO createBO = createBO(order, MesMaterialReqTypeEnum.ISSUE);
        createBO.setWorkOrderId(opTask.getWorkOrderId());
        createBO.setOpTaskId(opTask.getId());
        Long reqId = domainService.createMaterialReq(createBO);
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));
        MaterialReqDO req = reqOf(reqId);

        // 调用
        domainService.confirmMaterialReq(confirmBO(req));

        // 断言：头上带全套追溯字段，WMS 侧能反查到是哪张工单哪道工序领的料
        CreateOutOrderReqDTO outOrder = outOrders.values().iterator().next();
        assertEquals(WmsOutBizTypeEnum.PROD_ISSUE_OUT.name(), outOrder.getBizType());
        assertEquals(ORDER_NO, outOrder.getProdOrderNo());
        assertEquals("WO-0001", outOrder.getWorkOrderNo());
        assertEquals(opTask.getId(), outOrder.getOpTaskId());
        assertEquals(req.getReqNo(), outOrder.getMaterialReqNo());
        assertEquals(INV_ORG_ID, outOrder.getInvOrgId());
        assertEquals(WH_ID, outOrder.getWhId());
        assertEquals(WMS_SOURCE_MODULE, outOrder.getSourceModule());
        // 行上必须带预留编号：它是 WMS 核销预留的唯一入口，缺了预留永不核销、可用量永久偏低
        CreateOutOrderReqDTO.Line outLine = outOrder.getLines().get(0);
        MaterialReqLineDO line = linesOf(reqId).get(0);
        assertEquals(line.getReservationId(), outLine.getReservationId());
        assertNotEquals(MesConstants.NONE_ID, outLine.getReservationId());
        // 库位回落到预留记的库位：预留占的是哪条库存记录，出库就得从那条记录出
        assertEquals(LOC_ID, outLine.getFromLocId());
        assertEquals(line.getId(), outLine.getSourceLineId());
        assertEquals("MAT-" + MAT_A, outLine.getMatCode());
        assertEquals("PCS", outLine.getUomCode());
        assertEquals(0, new BigDecimal("100").compareTo(outLine.getPlanQty()));
    }

    @Test
    void testConfirm_paramLocIdOverridesReservationLoc() {
        // 准备：仓库现场决定换个货位发料
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);

        // 调用
        MesConfirmMaterialReqBO bo = confirmBO(req);
        bo.setLines(List.of(confirmLine(1, null, LOC_ID + 1)));
        domainService.confirmMaterialReq(bo);

        // 断言：入参给了库位就以入参为准，没给才回落到预留
        assertEquals(LOC_ID + 1, outOrders.values().iterator().next().getLines().get(0).getFromLocId());
        assertAccounts(MAT_A, "400", "0", "400");
    }

    @Test
    void testConfirm_partialQtyMarksPartialAndReleasesRemainingReservation() {
        // 准备：申请 100，仓库只拿得出 80
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);

        // 调用
        MesConfirmMaterialReqBO bo = confirmBO(req);
        bo.setLines(List.of(confirmLine(1, "80", null)));
        domainService.confirmMaterialReq(bo);

        // 断言：验收硬指标——部分处理置 PARTIAL。三账算术：出库 80 让现存降到 420，
        // 核销 80 预留，剩下没发的 20 预留必须释放——确认是一次性动作，留着它就是永久占住可用量
        assertAccounts(MAT_A, "420", "0", "420");
        assertTrue(wmsKeys.contains(WMS_SOURCE_MODULE + ":" + req.getReqNo() + ":RELEASE_REMAIN:1"));
        assertEquals(MesMaterialReqStatusEnum.PARTIAL.name(), reqOf(req.getId()).getStatus());
        MaterialReqLineDO line = linesOf(req.getId()).get(0);
        assertEquals(0, new BigDecimal("80").compareTo(line.getProcessedQty()));
        assertEquals(MesMaterialReqLineStatusEnum.PARTIAL.name(), line.getStatus());
        assertEquals(0, new BigDecimal("80")
                .compareTo(outOrders.values().iterator().next().getLines().get(0).getPlanQty()));
    }

    @Test
    void testConfirm_allLinesFullyIssuedMarksDone() {
        // 准备：两行料都全额发出
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "2", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        givenInventory(MAT_B, "500");
        MaterialReqDO req = submittedIssueReq(order);
        assertAccounts(MAT_A, "500", "100", "400");
        assertAccounts(MAT_B, "500", "200", "300");

        // 调用
        domainService.confirmMaterialReq(confirmBO(req));

        // 断言：验收硬指标——全部行处理完置 DONE，两种物料的可用量都与确认前相同
        assertAccounts(MAT_A, "400", "0", "400");
        assertAccounts(MAT_B, "300", "0", "300");
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), reqOf(req.getId()).getStatus());
        for (MaterialReqLineDO line : linesOf(req.getId())) {
            assertEquals(MesMaterialReqLineStatusEnum.DONE.name(), line.getStatus());
            assertEquals(0, line.getQty().compareTo(line.getProcessedQty()));
        }
        // 出库单一次带两行，不是逐行建单
        assertEquals(2, outOrders.values().iterator().next().getLines().size());
    }

    @Test
    void testConfirm_omittedLineNotIssuedAndReservationReleased() {
        // 准备：两行料，本次只发第一行
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        givenInventory(MAT_B, "500");
        MaterialReqDO req = submittedIssueReq(order);

        // 调用：只列第一行，第二行本次不发
        MesConfirmMaterialReqBO bo = confirmBO(req);
        bo.setLines(List.of(confirmLine(1, null, null)));
        domainService.confirmMaterialReq(bo);

        // 断言：发出去的那行走账，没发的那行预留全额释放、可用量回到 500
        assertAccounts(MAT_A, "400", "0", "400");
        assertAccounts(MAT_B, "500", "0", "500");
        assertEquals(MesMaterialReqStatusEnum.PARTIAL.name(), reqOf(req.getId()).getStatus());
        MaterialReqLineDO issued = lineOf(req.getId(), MAT_A);
        assertEquals(MesMaterialReqLineStatusEnum.DONE.name(), issued.getStatus());
        // 一点没发且预留已释放的行落取消，留在已提交会让人以为还等着发料，而这张单已经收尾了
        MaterialReqLineDO skipped = lineOf(req.getId(), MAT_B);
        assertEquals(MesMaterialReqLineStatusEnum.CANCELLED.name(), skipped.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(skipped.getProcessedQty()));
        // 出库单只带发出去的那一行
        assertEquals(1, outOrders.values().iterator().next().getLines().size());
    }

    @Test
    void testConfirm_notSubmittedRejected() {
        // 准备：草稿单直接确认
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 调用并断言：没预留过的单据直接发料，出库时无预留可核销，可用量会算错
        assertServiceException(() -> domainService.confirmMaterialReq(confirmBO(reqOf(reqId))),
                MATERIAL_REQ_STATUS_INVALID);
        assertTrue(outOrders.isEmpty());
        assertAccounts(MAT_A, "500", "0", "500");
        assertEquals(MesMaterialReqStatusEnum.DRAFT.name(), reqOf(reqId).getStatus());
    }

    @Test
    void testConfirm_locRequiredWhenNeitherParamNorReservationHasIt() {
        // 准备：退料没有预留可回落，入参又不给库位
        ProdOrderDO order = insertOrder();
        givenInventory(MAT_A, "500");
        MesCreateMaterialReqBO createBO = createBO(order, MesMaterialReqTypeEnum.RETURN);
        createBO.setLines(List.of(line(MAT_A, "10", BATCH_ID)));
        Long reqId = domainService.createMaterialReq(createBO);
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));

        // 调用并断言：错误信息带物料编码，车间照着编码就知道是哪个料没定货位
        assertServiceException(() -> domainService.confirmMaterialReq(confirmBO(reqOf(reqId))),
                MATERIAL_REQ_LOC_REQUIRED, "MAT-" + MAT_A);
        assertTrue(inOrders.isEmpty());
        assertAccounts(MAT_A, "500", "0", "500");
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), reqOf(reqId).getStatus());
    }

    @Test
    void testConfirm_qtyExceedingRemainingRejected() {
        // 准备：申请 100，却要确认 150
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);
        MesConfirmMaterialReqBO bo = confirmBO(req);
        bo.setLines(List.of(confirmLine(1, "150", null)));

        // 调用并断言：多发的部分没有预留背书，放过去就是账外出库。超发要走补料单
        assertServiceException(() -> domainService.confirmMaterialReq(bo),
                MATERIAL_REQ_CONFIRM_QTY_EXCEEDED, "MAT-" + MAT_A, "150", "100");
        assertTrue(outOrders.isEmpty());
        assertAccounts(MAT_A, "500", "100", "400");
    }

    @Test
    void testConfirm_allQtyZeroRejected() {
        // 准备：唯一一行确认 0
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);
        MesConfirmMaterialReqBO bo = confirmBO(req);
        bo.setLines(List.of(confirmLine(1, "0", null)));

        // 调用并断言：一行都发不出去就不该去 WMS 建一张空单，也不该把单据推进终态
        assertServiceException(() -> domainService.confirmMaterialReq(bo), MATERIAL_REQ_CONFIRM_QTY_EMPTY);
        assertTrue(outOrders.isEmpty());
        assertAccounts(MAT_A, "500", "100", "400");
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), reqOf(req.getId()).getStatus());
    }

    @Test
    void testConfirm_supplementGoesThroughIssueOut() {
        // 准备：补料与领料技术路径相同，都走出库单
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.SUPPLEMENT));
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));
        MaterialReqDO submitted = reqOf(reqId);
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), submitted.getApprovalStatus());
        assertEquals("supplement-process-1", submitted.getProcessInstanceId());
        domainService.updateSupplementApprovalStatus(reqId, submitted.getProcessInstanceId(),
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意补料");

        // 审批通过后才允许确认，补料与领料技术路径相同，都走出库单
        domainService.confirmMaterialReq(confirmBO(reqOf(reqId)));

        // 断言：补料同样是 PROD_ISSUE_OUT，现存下降、预留核销、可用量不变
        assertEquals(WmsOutBizTypeEnum.PROD_ISSUE_OUT.name(),
                outOrders.values().iterator().next().getBizType());
        assertAccounts(MAT_A, "400", "0", "400");
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), reqOf(reqId).getStatus());
    }

    @Test
    void testConfirm_wmsFailureRollsBackWholeConfirm() {
        // 准备：出库单建好了，过账时 WMS 拒绝（模拟库位库存被别人抢走）
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        givenInventory(MAT_A, "500");
        MaterialReqDO req = submittedIssueReq(order);
        org.mockito.Mockito.doThrow(new IllegalStateException("WMS 拒绝：库位可用量不足"))
                .when(outOrderApi).confirmOutOrder(any());

        // 调用并断言：异常穿透领域动作，事务回滚
        assertThrows(IllegalStateException.class, () -> domainService.confirmMaterialReq(confirmBO(req)));
        // 中间态 WMS_PROCESSING 不会留下来，单据回到已提交，预留还占着，可以重试
        MaterialReqDO rolledBack = reqOf(req.getId());
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), rolledBack.getStatus());
        assertNull(rolledBack.getWmsBizNo());
        assertEquals(0, BigDecimal.ZERO.compareTo(linesOf(req.getId()).get(0).getProcessedQty()));
        assertAccounts(MAT_A, "500", "100", "400");
        // 确认的命令记录一起回滚，重试才能重新执行
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testCancel_draftDirectly() {
        // 准备：草稿单
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));

        // 调用
        domainService.cancelMaterialReq(actionBO(reqOf(reqId), "计划变更"));

        // 断言：草稿没有库存后果，直接置取消，不用惊动 WMS
        assertEquals(MesMaterialReqStatusEnum.CANCELLED.name(), reqOf(reqId).getStatus());
        assertEquals("计划变更", reqOf(reqId).getRemark());
        assertEquals(MesMaterialReqLineStatusEnum.CANCELLED.name(), linesOf(reqId).get(0).getStatus());
        assertTrue(wmsKeys.isEmpty());
    }

    @Test
    void testCancel_submittedReleasesReservation() {
        // 准备：已提交的单据，料已被留住
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        MaterialReqDO req = submittedIssueReq(order);
        Long reservationId = linesOf(req.getId()).get(0).getReservationId();
        assertEquals(0, new BigDecimal("400").compareTo(stock.get(MAT_A)));

        // 调用
        domainService.cancelMaterialReq(actionBO(req, "车间不再需要"));

        // 断言：验收硬指标——预留被释放、可用量回升到 500。不释放就是把可用量永久占住
        assertTrue(reservations.get(reservationId).released);
        assertEquals(0, new BigDecimal("500").compareTo(stock.get(MAT_A)));
        assertTrue(wmsKeys.contains(WMS_SOURCE_MODULE + ":" + req.getReqNo() + ":RELEASE:1"));
        assertEquals(MesMaterialReqStatusEnum.CANCELLED.name(), reqOf(req.getId()).getStatus());
        // 预留编号保留：它是"这笔预留曾经存在过"的唯一线索，清掉就没法对账
        MaterialReqLineDO line = linesOf(req.getId()).get(0);
        assertEquals(reservationId, line.getReservationId());
        assertEquals(MesMaterialReqLineStatusEnum.CANCELLED.name(), line.getStatus());
    }

    @Test
    void testCancel_idempotent() {
        // 准备：已提交的单据
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        MaterialReqDO req = submittedIssueReq(order);

        // 调用：同键取消两次
        domainService.cancelMaterialReq(actionBO(req, "车间不再需要"));
        domainService.cancelMaterialReq(actionBO(req, "车间不再需要"));

        // 断言：可用量只回升一次。回升两次就等于凭空多出 100 个料
        assertEquals(0, new BigDecimal("500").compareTo(stock.get(MAT_A)));
        assertEquals(MesMaterialReqStatusEnum.CANCELLED.name(), reqOf(req.getId()).getStatus());
        // 提交与取消各一条命令记录
        assertEquals(2L, commandRecordMapper.selectCount());
    }

    @Test
    void testCancel_handedToWmsRejected() {
        // 准备：已经交给 WMS 处理的单据
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        MaterialReqDO req = submittedIssueReq(order);
        materialReqMapper.updateById(new MaterialReqDO().setId(req.getId())
                .setStatus(MesMaterialReqStatusEnum.WMS_PROCESSING.name()));

        // 调用并断言：料已经在路上或已出库，MES 单方面取消会让两侧数量对不上
        assertServiceException(() -> domainService.cancelMaterialReq(actionBO(req, "反悔了")),
                MATERIAL_REQ_STATUS_INVALID);
        // 预留没被释放，可用量仍是被占住的状态
        assertEquals(0, new BigDecimal("400").compareTo(stock.get(MAT_A)));
    }

    @Test
    void testCancel_runningSupplementApprovalRejected() {
        ProdOrderDO order = insertOrder();
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK)));
        stock.put(MAT_A, new BigDecimal("500"));
        MaterialReqDO req = submittedIssueReq(order);
        materialReqMapper.updateById(new MaterialReqDO().setId(req.getId())
                .setProcessInstanceId("supplement-process-running")
                .setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));

        assertServiceException(() -> domainService.cancelMaterialReq(actionBO(req, "审批中撤销")),
                MATERIAL_REQ_STATUS_INVALID);
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), reqOf(req.getId()).getStatus());
        assertEquals(0, new BigDecimal("400").compareTo(stock.get(MAT_A)));
    }

    private MaterialRespDTO material(Long matId) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(matId);
        material.setCode("MAT-" + matId);
        material.setName("物料" + matId);
        material.setSpec("规格" + matId);
        material.setBaseUomId(UOM_ID);
        return material;
    }

    private UomRespDTO uom(Long uomId) {
        UomRespDTO uomRespDTO = new UomRespDTO();
        uomRespDTO.setId(uomId);
        uomRespDTO.setCode("PCS");
        return uomRespDTO;
    }

    private BatchRespDTO batch(Long batchId) {
        BatchRespDTO batchRespDTO = new BatchRespDTO();
        batchRespDTO.setId(batchId);
        batchRespDTO.setLotNo("LOT-" + batchId);
        return batchRespDTO;
    }

    private BomLineRespDTO bomLine(int lineNo, Long matId, String qty, String scrapRate,
                                   MesIssueMethodEnum issueMethod) {
        BomLineRespDTO line = new BomLineRespDTO();
        line.setId(200L + lineNo);
        line.setBomId(BOM_ID);
        line.setLineNo(lineNo);
        line.setMatId(matId);
        line.setQty(new BigDecimal(qty));
        line.setScrapRate(new BigDecimal(scrapRate));
        line.setIssueMethod(issueMethod.name());
        line.setUomId(UOM_ID);
        return line;
    }

    private RouteOpMatRespDTO routeOpMat(Long id, Long matId, String qty, MesIssueMethodEnum issueMethod,
                                         boolean mustIssue) {
        RouteOpMatRespDTO opMat = new RouteOpMatRespDTO();
        opMat.setId(id);
        opMat.setRouteOpId(ROUTE_OP_CUT);
        opMat.setMatId(matId);
        opMat.setQty(new BigDecimal(qty));
        opMat.setScrapRate(BigDecimal.ZERO);
        opMat.setIssueMethod(issueMethod.name());
        opMat.setMustIssueBeforeStart(mustIssue);
        opMat.setUomId(UOM_ID);
        return opMat;
    }

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
                .qty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(UOM_ID)
                .bomId(BOM_ID)
                .bomVersion("B1.0")
                .routeId(21L)
                .routeVersion("R1.0")
                .priority(0)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                .status(MesProdOrderStatusEnum.READY.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    private OpTaskDO insertOpTask(ProdOrderDO order) {
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .workOrderNo("WO-0001")
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .plantId(order.getPlantId())
                .workshopId(order.getWorkshopId())
                .wcId(201L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                .status(MesWorkOrderStatusEnum.PENDING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        OpTaskDO opTask = OpTaskDO.builder()
                .taskNo("OT-0001")
                .workOrderId(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .routeOpId(ROUTE_OP_CUT)
                .opId(201L)
                .opCode("OP010")
                .opName("下料")
                .seq(10)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .status(MesOpTaskStatusEnum.READY.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(opTask);
        return opTask;
    }

    /** 创建参数：默认领料、不带明细（走服务端展开）、不带幂等键（走服务端派生）。 */
    private MesCreateMaterialReqBO createBO(ProdOrderDO order, MesMaterialReqTypeEnum reqType) {
        MesCreateMaterialReqBO bo = new MesCreateMaterialReqBO();
        bo.setProdOrderId(order.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(order.getOrderNo());
        bo.setReqType(reqType.name());
        bo.setInvOrgId(INV_ORG_ID);
        bo.setWhId(WH_ID);
        return bo;
    }

    private MesCreateMaterialReqBO.Line line(Long matId, String qty, Long batchId) {
        MesCreateMaterialReqBO.Line line = new MesCreateMaterialReqBO.Line();
        line.setMatId(matId);
        line.setQty(new BigDecimal(qty));
        line.setUomId(UOM_ID);
        line.setBatchId(batchId);
        return line;
    }

    private MesMaterialReqActionBO actionBO(MaterialReqDO req, String reason) {
        MesMaterialReqActionBO bo = new MesMaterialReqActionBO();
        bo.setMaterialReqId(req.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(req.getReqNo());
        bo.setReason(reason);
        return bo;
    }

    /** 确认参数：默认不带行（全部行按剩余量全额确认）、不带幂等键（走服务端派生）。 */
    private MesConfirmMaterialReqBO confirmBO(MaterialReqDO req) {
        MesConfirmMaterialReqBO bo = new MesConfirmMaterialReqBO();
        bo.setMaterialReqId(req.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(req.getReqNo());
        return bo;
    }

    private MesConfirmMaterialReqBO.Line confirmLine(int lineNo, String confirmQty, Long locId) {
        MesConfirmMaterialReqBO.Line line = new MesConfirmMaterialReqBO.Line();
        line.setLineNo(lineNo);
        line.setConfirmQty(confirmQty == null ? null : new BigDecimal(confirmQty));
        line.setLocId(locId);
        return line;
    }

    /** 铺初始库存：现存与可用量同额，无预留。 */
    private void givenInventory(Long matId, String qty) {
        onhand.put(matId, new BigDecimal(qty));
        stock.put(matId, new BigDecimal(qty));
        reservedQty.put(matId, BigDecimal.ZERO);
    }

    /**
     * 三个账一起断言，并校验 <b>可用量 = 现存 − 预留</b> 这个恒等式。
     *
     * <p>只断言其中一个账都会漏掉真正的错：只看可用量的话，"什么都不做"和"现存降了但预留也核销了"
     * 结果一样；只看现存的话，预留漏核销看不出来。三个一起对，账才是对的。
     */
    private void assertAccounts(Long matId, String onhandQty, String reservedValue, String availableQty) {
        assertEquals(0, new BigDecimal(onhandQty).compareTo(onhand.getOrDefault(matId, BigDecimal.ZERO)),
                "现存量不符");
        assertEquals(0, new BigDecimal(reservedValue).compareTo(reservedQty.getOrDefault(matId, BigDecimal.ZERO)),
                "预留量不符");
        assertEquals(0, new BigDecimal(availableQty).compareTo(stock.getOrDefault(matId, BigDecimal.ZERO)),
                "可用量不符");
        assertEquals(0, new BigDecimal(onhandQty).subtract(new BigDecimal(reservedValue))
                        .compareTo(stock.getOrDefault(matId, BigDecimal.ZERO)),
                "可用量必须等于现存减预留");
    }

    private MaterialReqDO reqOf(Long reqId) {
        return materialReqMapper.selectById(reqId);
    }

    /** 按行号升序取明细，断言不被插入顺序绑住。 */
    private List<MaterialReqLineDO> linesOf(Long reqId) {
        List<MaterialReqLineDO> lines = new ArrayList<>(
                materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, reqId));
        lines.sort((left, right) -> Integer.compare(left.getLineNo(), right.getLineNo()));
        return lines;
    }

    private MaterialReqLineDO lineOf(Long reqId, Long matId) {
        return linesOf(reqId).stream().filter(line -> matId.equals(line.getMatId())).findFirst().orElseThrow();
    }

    /** 一张已提交的领料单，供取消用例复用。 */
    private MaterialReqDO submittedIssueReq(ProdOrderDO order) {
        Long reqId = domainService.createMaterialReq(createBO(order, MesMaterialReqTypeEnum.ISSUE));
        domainService.submitMaterialReq(actionBO(reqOf(reqId), null));
        return reqOf(reqId);
    }

}
