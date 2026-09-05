package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.prodorder.bo.MesFinishInboundBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchReqDTO;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchRespDTO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.warehouse.WarehouseApi;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_INBOUND_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_INBOUND_STOCK_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * 完工入库测试。
 *
 * <p><b>三账替身是"成品库余额增加"这个断言有算术含义的前提</b>：只维护一个"余额"账的话，
 * 断言余额上升是个恒真命题——入库过账替身随便加个数就能过。这里维护现存、预留、可用三个账，
 * 每次断言三个值外加 {@code 可用量 = 现存 − 预留} 恒等式，且入库过账替身只认"先建单再过账"的顺序：
 * 实现漏了 {@code confirmInOrder}，单建了但余额一分不动，用例立刻红。
 *
 * <p>真实的账实一致由阶段 6 的跨模块集成测试覆盖，MES 单模块单测的 H2 里没有 wms 表。
 */
@Import({MesFinishInboundServiceImpl.class, MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesFinishInboundServiceImplTest extends BaseDbUnitTest {

    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal ORDER_QTY = new BigDecimal("100");
    private static final Long FIN_MAT_ID = 9001L;
    private static final Long INV_ORG_ID = 1L;
    private static final Long PLANT_ID = 1L;
    private static final Long WH_ID = 301L;
    private static final Long LOC_ID = 401L;
    private static final Long UOM_ID = 1L;
    /** 调用方指定的仓库与库位，用来验证入参优先于物料默认值。 */
    private static final Long OTHER_WH_ID = 302L;
    private static final Long OTHER_LOC_ID = 402L;

    @Resource
    private MesFinishInboundService finishInboundService;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private UomApi uomApi;
    @MockitoBean
    private BatchApi batchApi;
    @MockitoBean
    private InOrderApi inOrderApi;
    @MockitoBean
    private WarehouseApi warehouseApi;
    @MockitoBean
    private MesCodeService codeService;

    /** 三账替身：现存、预留、可用，按库位分开记，恒满足 可用 = 现存 − 预留。 */
    private final Map<Long, BigDecimal> onhand = new HashMap<>();
    private final Map<Long, BigDecimal> reserved = new HashMap<>();
    private final Map<Long, BigDecimal> available = new HashMap<>();
    /** 批次台账：批次号 -> 批次编号，注册按批次号幂等，与 WMS 侧一致。 */
    private final Map<String, Long> batches = new LinkedHashMap<>();
    /** WMS 侧收到的入库单与已过账单据。 */
    private final Map<Long, CreateInOrderReqDTO> inOrders = new LinkedHashMap<>();
    private final List<Long> postedOrders = new ArrayList<>();
    private final List<String> wmsKeys = new ArrayList<>();
    /** 成品物料的默认仓库与库位，用例可以清掉它来验证"配置缺失"两条分支。 */
    private Long defaultWhId = WH_ID;
    private Long defaultLocId = LOC_ID;

    private final AtomicLong batchIdSeq = new AtomicLong();
    private final AtomicLong wmsOrderIdSeq = new AtomicLong();

    @BeforeEach
    void mockExternalDependencies() {
        onhand.clear();
        reserved.clear();
        available.clear();
        batches.clear();
        inOrders.clear();
        postedOrders.clear();
        wmsKeys.clear();
        defaultWhId = WH_ID;
        defaultLocId = LOC_ID;
        batchIdSeq.set(0);
        wmsOrderIdSeq.set(0);
        Map<String, AtomicInteger> counters = new HashMap<>();
        when(codeService.generate(anyString(), any())).thenAnswer(invocation -> {
            String ruleCode = invocation.getArgument(0);
            return invocation.getArgument(1) != null ? invocation.getArgument(1)
                    : ruleCode + "-" + counters.computeIfAbsent(ruleCode, key -> new AtomicInteger())
                    .incrementAndGet();
        });
        when(materialApi.getMaterial(any())).thenAnswer(invocation -> material(invocation.getArgument(0)));
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
        // 批次注册按"物料 + 批次号"幂等，与 WMS 侧的注册语义一致：同一张工单分批入库落到同一个批次上
        when(batchApi.registerBatch(any())).thenAnswer(invocation -> {
            RegisterBatchReqDTO reqDTO = invocation.getArgument(0);
            Long batchId = batches.computeIfAbsent(reqDTO.getMatId() + "|" + reqDTO.getLotNo(),
                    key -> batchIdSeq.incrementAndGet());
            RegisterBatchRespDTO resp = new RegisterBatchRespDTO();
            resp.setId(batchId);
            resp.setLotNo(reqDTO.getLotNo());
            return resp;
        });
        mockInOrderApi();
    }

    /**
     * WMS 入库门面替身：创建按幂等键判重，过账让现存与可用同额上升。
     *
     * <p>建单时校验目标库位与数量，与 WMS 的 {@code IN_ORDER_LOC_REQUIRED} 同口径：不校验的话
     * MES 漏给库位在单测里看不出来，到生产才炸。
     */
    private void mockInOrderApi() {
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
                if (line.getPlanQty() == null || line.getPlanQty().signum() <= 0) {
                    throw new IllegalStateException("WMS 拒绝：入库数量必须大于零");
                }
            });
            long orderId = wmsOrderIdSeq.incrementAndGet();
            inOrders.put(orderId, reqDTO);
            return orderId;
        });
        doAnswer(invocation -> {
            ConfirmOrderReqDTO reqDTO = invocation.getArgument(0);
            wmsKeys.add(reqDTO.getIdempotencyKey());
            postInOrder(reqDTO.getOrderId());
            return null;
        }).when(inOrderApi).confirmInOrder(any());
    }

    /** 入库过账：现存与可用同额上升。已过账的单不许再过账。 */
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
            available.merge(line.getMatId(), line.getPlanQty(), BigDecimal::add);
        }
        postedOrders.add(orderId);
    }

    @Test
    void testCreateFinishInbound_finishedGoodsBalanceIncreases() {
        // 准备（验收点名的构造）：一张已完工 100 件的工单，成品库起点是 现存 0 / 预留 0 / 可用 0
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        WorkOrderDO workOrder = insertWorkOrder(order);
        givenInventory(FIN_MAT_ID, "0");

        // 调用：不给数量，取"已完工减已入库"的全部剩余
        Long inOrderId = finishInboundService.createFinishInbound(inboundBO(order, workOrder, null, null));

        // 断言（验收点名）：成品库余额增加 100，三账自洽
        assertNotNull(inOrderId);
        assertAccounts(FIN_MAT_ID, "100", "0", "100");
        // 入库单业务类型与质量状态：完工入库不再走入库质检，FQC 在 MES 侧检（阶段 2）
        CreateInOrderReqDTO inOrder = inOrders.get(inOrderId);
        assertEquals(WmsInBizTypeEnum.PROD_FINISH_IN.name(), inOrder.getBizType());
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), inOrder.getQualityStatus());
        assertEquals(Boolean.FALSE, inOrder.getNeedQc());
        // 头上带两级单号，这是成品到工单的追溯入口
        assertEquals(ORDER_NO, inOrder.getProdOrderNo());
        assertEquals(workOrder.getWorkOrderNo(), inOrder.getWorkOrderNo());
        assertEquals(INV_ORG_ID, inOrder.getInvOrgId());
        assertEquals(WH_ID, inOrder.getWhId());
        // 行上带批次编号：批次已注册，成品入库房时批次台账对得上
        CreateInOrderReqDTO.Line line = inOrder.getLines().get(0);
        assertEquals(FIN_MAT_ID, line.getMatId());
        assertEquals(LOC_ID, line.getLocId());
        assertEquals(0, ORDER_QTY.compareTo(line.getPlanQty()));
        assertNotNull(line.getBatchId());
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), line.getQualityStatus());
        // 批号缺省规则 {工单号}-{yyyyMMdd}
        String expectedLotNo = ORDER_NO + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertEquals(expectedLotNo, line.getLotNo());
        assertEquals(1, batches.size());
        // 真的过账了：只建单不过账的话余额一分不动
        assertEquals(1, postedOrders.size());
        // 账本：一条 IN_ORDER 流水，qty 记本次入库量，biz_no 记 WMS 入库单编号
        List<WipTxnDO> txns = inboundTxns();
        assertEquals(1, txns.size());
        assertEquals(MesWipTxnTypeEnum.COMPLETE.name(), txns.get(0).getTxnType());
        assertEquals(String.valueOf(inOrderId), txns.get(0).getBizNo());
        assertEquals(0, ORDER_QTY.compareTo(txns.get(0).getQty()));
        assertEquals(order.getId(), txns.get(0).getProdOrderId());
        assertEquals(workOrder.getId(), txns.get(0).getWorkOrderId());
        assertEquals(0, ORDER_QTY.compareTo(finishInboundService.inboundedQty(order.getId())));
        // 幂等键按 MES:{工单号}:FINISH_INBOUND:{第几次} 派生，命令记录的 result_id 落入库单编号
        assertEquals(1L, commandRecordMapper.selectCount());
        assertEquals("MES:" + ORDER_NO + ":FINISH_INBOUND:1",
                commandRecordMapper.selectList().get(0).getIdempotencyKey());
        assertEquals(inOrderId, commandRecordMapper.selectList().get(0).getResultId());
    }

    @Test
    void testCreateFinishInbound_runningOrderAllowed() {
        // 准备：末道工序还在跑但已经产出 40 件，分批入库不必等整单完工——等下去要压很久的库
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING, "40");
        givenInventory(FIN_MAT_ID, "0");

        finishInboundService.createFinishInbound(inboundBO(order, null, null, null));

        assertAccounts(FIN_MAT_ID, "40", "0", "40");
    }

    @Test
    void testCreateFinishInbound_twoBatchesNotExceedCompletedQty() {
        // 准备：已完工 100，先入 60 再入剩余
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        givenInventory(FIN_MAT_ID, "0");

        finishInboundService.createFinishInbound(inboundBO(order, null, "60", null));
        assertAccounts(FIN_MAT_ID, "60", "0", "60");
        assertEquals(0, new BigDecimal("60").compareTo(finishInboundService.inboundedQty(order.getId())));

        // 第二次不给数量：剩余量算出来是 40，不是又来一次 100
        finishInboundService.createFinishInbound(inboundBO(order, null, null, null));

        // 断言：累计入库恰好等于已完工数量，两次的幂等键按次数递增（同键会被第一次拦掉）
        assertAccounts(FIN_MAT_ID, "100", "0", "100");
        assertEquals(2, inOrders.size());
        assertEquals(2, postedOrders.size());
        assertTrue(wmsKeys.contains("MES:" + ORDER_NO + ":FINISH_INBOUND:1:CREATE_IN_ORDER"));
        assertTrue(wmsKeys.contains("MES:" + ORDER_NO + ":FINISH_INBOUND:2:CREATE_IN_ORDER"));
        // 两次入的是同一个批次号，因此批次台账只有一条：一张工单的成品属于同一批
        assertEquals(1, batches.size());
        // 入满之后再不给数量地调一次：算出剩余 0，什么都不做
        assertNull(finishInboundService.createFinishInbound(inboundBO(order, null, null, null)));
        assertAccounts(FIN_MAT_ID, "100", "0", "100");
        assertEquals(2, inOrders.size());
    }

    @Test
    void testCreateFinishInbound_qtyExceededRejected() {
        // 准备：已完工 100，先入 60，再显式要入 50 就超了
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        givenInventory(FIN_MAT_ID, "0");
        finishInboundService.createFinishInbound(inboundBO(order, null, "60", null));

        // 断言：累计入库量不许超过已完工数量，超了就是凭空多出成品
        assertServiceException(() -> finishInboundService.createFinishInbound(
                        inboundBO(order, null, "50", null)),
                PROD_ORDER_INBOUND_QTY_EXCEEDED, ORDER_NO, "50", "110", "100");
        assertAccounts(FIN_MAT_ID, "60", "0", "60");
        assertEquals(1, inOrders.size());
    }

    @Test
    void testCreateFinishInbound_idempotentNoSecondPost() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        givenInventory(FIN_MAT_ID, "0");
        MesFinishInboundBO bo = inboundBO(order, null, "100", null);
        bo.setIdempotencyKey("MES:" + ORDER_NO + ":FINISH_INBOUND:1");

        // 调用：同键两次。第二次若真执行，替身对已过账单据抛错，用例必然红
        Long first = finishInboundService.createFinishInbound(bo);
        MesFinishInboundBO retry = inboundBO(order, null, "100", null);
        retry.setIdempotencyKey("MES:" + ORDER_NO + ":FINISH_INBOUND:1");
        Long second = finishInboundService.createFinishInbound(retry);

        // 断言：返回同一张单，余额只增一次，流水与命令记录都只有一条
        assertEquals(first, second);
        assertAccounts(FIN_MAT_ID, "100", "0", "100");
        assertEquals(1, inOrders.size());
        assertEquals(1, postedOrders.size());
        assertEquals(1, inboundTxns().size());
        assertEquals(1L, commandRecordMapper.selectCount());
    }

    @Test
    void testCreateFinishInbound_callerWarehouseAndLotWin() {
        // 准备：调用方显式指定仓库、库位与批号
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        givenInventory(FIN_MAT_ID, "0");
        MesFinishInboundBO bo = inboundBO(order, null, "100", "LOT-SPECIAL");
        bo.setWhId(OTHER_WH_ID);
        bo.setLocId(OTHER_LOC_ID);

        Long inOrderId = finishInboundService.createFinishInbound(bo);

        // 断言：入参优先于物料默认值，批号原样用
        CreateInOrderReqDTO inOrder = inOrders.get(inOrderId);
        assertEquals(OTHER_WH_ID, inOrder.getWhId());
        assertEquals(OTHER_LOC_ID, inOrder.getLines().get(0).getLocId());
        assertEquals("LOT-SPECIAL", inOrder.getLines().get(0).getLotNo());
    }

    @Test
    void testCreateFinishInbound_stockNotConfiguredRejected() {
        // 准备：成品物料没配默认仓库与库位，调用方也没给
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        defaultWhId = null;
        defaultLocId = null;

        // 断言：手工入库是人发起的动作，配置缺失必须报出来才会被修好
        assertServiceException(() -> finishInboundService.createFinishInbound(
                inboundBO(order, null, null, null)), PROD_ORDER_INBOUND_STOCK_REQUIRED, "MAT-" + FIN_MAT_ID);
        assertTrue(inOrders.isEmpty());
    }

    @Test
    void testAutoInboundOnOrderDone_skipsWhenStockNotConfigured() {
        // 准备：同一个配置缺失场景，但走自动入库
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        defaultWhId = null;
        defaultLocId = null;

        // 调用：静默跳过而不是抛错——完工是车间现场动作，不该被成品仓主数据挡住
        assertNull(finishInboundService.autoInboundOnOrderDone(order.getId(), MesConstants.NONE_ID));

        // 断言：没入库，但差额还在，事后手工补入即可
        assertTrue(inOrders.isEmpty());
        assertEquals(0L, commandRecordMapper.selectCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(finishInboundService.inboundedQty(order.getId())));
    }

    @Test
    void testAutoInboundOnOrderDone_postsRemaining() {
        // 准备：已完工 100，其中 30 件已经手工分批入过
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        WorkOrderDO workOrder = insertWorkOrder(order);
        givenInventory(FIN_MAT_ID, "0");
        finishInboundService.createFinishInbound(inboundBO(order, null, "30", null));

        // 调用
        Long inOrderId = finishInboundService.autoInboundOnOrderDone(order.getId(), workOrder.getId());

        // 断言：只入剩下的 70，不是又来一次 100
        assertNotNull(inOrderId);
        assertEquals(0, new BigDecimal("70").compareTo(inOrders.get(inOrderId).getLines().get(0).getPlanQty()));
        assertAccounts(FIN_MAT_ID, "100", "0", "100");
    }

    @Test
    void testAutoInboundOnOrderDone_skipsWhenNothingRemaining() {
        // 准备：已经入满
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");
        givenInventory(FIN_MAT_ID, "0");
        finishInboundService.createFinishInbound(inboundBO(order, null, "100", null));

        assertNull(finishInboundService.autoInboundOnOrderDone(order.getId(), MesConstants.NONE_ID));

        assertEquals(1, inOrders.size());
        assertAccounts(FIN_MAT_ID, "100", "0", "100");
    }

    @Test
    void testCreateFinishInbound_closedOrderRejected() {
        // 准备：已关闭的工单。关闭已经做过账目收尾，再入库等于在已封版的账上加一笔
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.CLOSED, "100");

        assertServiceException(() -> finishInboundService.createFinishInbound(
                inboundBO(order, null, null, null)), PROD_ORDER_STATUS_INVALID);
        assertTrue(inOrders.isEmpty());
    }

    @Test
    void testCreateFinishInbound_nonPositiveQtyRejected() {
        // 准备：显式给零。不给数量时"没有可入的量"是正常情形，显式给零就是调用方错了
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DONE, "100");

        assertServiceException(() -> finishInboundService.createFinishInbound(
                inboundBO(order, null, "0", null)), QTY_MUST_BE_POSITIVE);
        assertTrue(inOrders.isEmpty());
    }

    @Test
    void testCreateFinishInbound_orderNotExists() {
        MesFinishInboundBO bo = new MesFinishInboundBO();
        bo.setProdOrderId(randomLongId());
        bo.setSourceModule("MES");

        assertServiceException(() -> finishInboundService.createFinishInbound(bo), PROD_ORDER_NOT_EXISTS);
    }

    /** 落一张生产工单，{@code version} 显式给初始值。 */
    private ProdOrderDO insertOrder(MesProdOrderStatusEnum status, String completedQty) {
        ProdOrderDO order = ProdOrderDO.builder()
                .orderNo(ORDER_NO)
                .demandId(MesConstants.NONE_ID)
                .plantId(PLANT_ID)
                .workshopId(2L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(FIN_MAT_ID)
                .matCode("MAT-" + FIN_MAT_ID)
                .qty(ORDER_QTY)
                .completedQty(new BigDecimal(completedQty))
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(UOM_ID)
                .priority(0)
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    private WorkOrderDO insertWorkOrder(ProdOrderDO order) {
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
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(MesWorkOrderStatusEnum.DONE.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    /** 入库参数：数量与批号留空走服务端派生。 */
    private MesFinishInboundBO inboundBO(ProdOrderDO order, WorkOrderDO workOrder, String qty, String lotNo) {
        MesFinishInboundBO bo = new MesFinishInboundBO();
        bo.setProdOrderId(order.getId());
        bo.setWorkOrderId(workOrder == null ? MesConstants.NONE_ID : workOrder.getId());
        bo.setSourceModule("MES");
        bo.setSourceBizNo(order.getOrderNo());
        bo.setQty(qty == null ? null : new BigDecimal(qty));
        bo.setLotNo(lotNo);
        bo.setRemark("完工入库");
        return bo;
    }

    /** 铺初始库存：现存与可用同额、无预留。 */
    private void givenInventory(Long matId, String qty) {
        onhand.put(matId, new BigDecimal(qty));
        available.put(matId, new BigDecimal(qty));
        reserved.put(matId, BigDecimal.ZERO);
    }

    /**
     * 三个账一起断言，并校验 <b>可用量 = 现存 − 预留</b> 这个恒等式。
     *
     * <p>只断言"余额增加"是个恒真命题：替身随便加个数就能过。三个账一起对，账才是对的。
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

    /** 本工单的完工入库流水，来源业务类型是 WMS 入库单。 */
    private List<WipTxnDO> inboundTxns() {
        return wipTxnMapper.selectList().stream()
                .filter(txn -> MesBizTypeEnum.IN_ORDER.name().equals(txn.getBizType())).toList();
    }

    private MaterialRespDTO material(Long matId) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(matId);
        material.setCode("MAT-" + matId);
        material.setName("成品" + matId);
        material.setSpec("规格A");
        material.setBaseUomId(UOM_ID);
        material.setDefaultWhId(defaultWhId);
        material.setDefaultLocId(defaultLocId);
        return material;
    }

    private UomRespDTO uom(Long uomId) {
        UomRespDTO uom = new UomRespDTO();
        uom.setId(uomId);
        uom.setCode("EA");
        return uom;
    }

}
