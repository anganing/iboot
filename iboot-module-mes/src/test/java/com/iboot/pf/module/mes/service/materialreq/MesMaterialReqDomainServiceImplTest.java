package com.iboot.pf.module.mes.service.materialreq;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
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
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.reservation.InventoryReservationApi;
import com.iboot.pf.module.wms.api.reservation.dto.CreateReservationReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationHandleReqDTO;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_RETURN_BATCH_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_WAREHOUSE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 领退补料申请领域动作测试：创建展开、提交预留、取消释放。
 *
 * <p>WMS 侧用一个受控替身：{@link #stock} 是物料到可用量的账面，{@link #reservations} 是预留台账。
 * 预留时扣账面、释放时回补，并按幂等键判重——与真实 WMS 的行为一致。这样"提交后可用量下降"、
 * "取消后可用量回升"、"整单回滚不留残留"都是真断言，而不是对着 mock 数调用次数。
 * 真实的库存过账账实一致由阶段 6 的跨模块集成测试覆盖，MES 单测里没有 wms 表可用。
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
    private MesCodeService codeService;

    /** 物料到可用量的账面。 */
    private final Map<Long, BigDecimal> stock = new HashMap<>();
    /** 预留台账：预留编号 -> 预留。 */
    private final Map<Long, FakeReservation> reservations = new LinkedHashMap<>();
    /** WMS 侧收到的预留与释放幂等键，用于断言键格式。 */
    private final List<String> wmsKeys = new ArrayList<>();
    /** 让 WMS 在第 N 次预留时拒绝，用于模拟并发下前置校验通过但预留失败。 */
    private int rejectReserveAt = -1;

    /** 预留编号发号器与预留次数计数器。 */
    private final AtomicLong reservationIdSeq = new AtomicLong();
    private final AtomicInteger reserveCount = new AtomicInteger();

    @BeforeEach
    void mockExternalDependencies() {
        stock.clear();
        reservations.clear();
        wmsKeys.clear();
        rejectReserveAt = -1;
        reservationIdSeq.set(0);
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
        mockReservationApi();
    }

    /**
     * WMS 预留门面替身：预留扣账面、释放回补账面，两个动作都按幂等键判重。
     *
     * <p>不用"验证 mock 被调用几次"代替，因为本任务真正要证明的是可用量的升降，
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
            stock.put(reqDTO.getMatId(), available.subtract(reqDTO.getQty()));
            long reservationId = reservationIdSeq.incrementAndGet();
            reservations.put(reservationId, new FakeReservation(reservationId, reqDTO.getIdempotencyKey(),
                    reqDTO.getMatId(), reqDTO.getQty()));
            return reservationId;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            ReservationHandleReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            FakeReservation reservation = reservations.get(reqDTO.getReservationId());
            if (reservation != null && !reservation.released) {
                reservation.released = true;
                stock.merge(reservation.matId, reservation.qty, BigDecimal::add);
            }
            return null;
        }).when(reservationApi).releaseInventory(any());
    }

    /** 预留台账的一笔记录。 */
    private static final class FakeReservation {

        private final Long id;
        private final String key;
        private final Long matId;
        private final BigDecimal qty;
        private boolean released;

        private FakeReservation(Long id, String key, Long matId, BigDecimal qty) {
            this.id = id;
            this.key = key;
            this.matId = matId;
            this.qty = qty;
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
