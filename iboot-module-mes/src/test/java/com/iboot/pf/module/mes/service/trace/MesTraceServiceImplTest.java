package com.iboot.pf.module.mes.service.trace;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.partner.PartnerApi;
import com.iboot.pf.module.mdm.api.partner.dto.CustomerRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesTraceNodeTypeEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.InOrderRespDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TRACE_START_NOT_FOUND;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TRACE_START_TYPE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 批次正向追溯测试。
 *
 * <p>造一条完整链路（原料批次 → 领料出库 → 生产工单 → 报工 → 完工入库成品批次 → 销售出库），
 * 断言树的层级与节点数正确；门面全部 mock 并逐方法计数，断言无 N+1。
 *
 * <p>WMS 的 wms_inv_txn 在 MES 单测的 H2 里没有表（与领料、完工入库测试同口径），
 * 流水用替身台账模拟 WMS 的落库语义——{@code biz_no} 存单号、行上带批次与物料——这与
 * {@code WmsOutOrderServiceImpl.buildPostBO} / {@code WmsInOrderServiceImpl} 的真实写入口径一致。
 * 流水的真实落库由阶段 6 跨模块集成测试覆盖。
 */
@Import({MesTraceServiceImpl.class, MesTraceForwardResolver.class})
class MesTraceServiceImplTest extends BaseDbUnitTest {

    private static final String ORDER_NO = "PO-TRACE-0001";
    private static final String OUT_NO_ISSUE = "OUT-ISSUE-0001";
    private static final String OUT_NO_SALES = "OUT-SALES-0001";
    private static final String IN_NO_FINISH = "IN-FINISH-0001";
    private static final String REQ_NO = "MR-0001";
    private static final String RAW_LOT = "LOT-RAW-001";
    private static final String FINISH_LOT = "PO-TRACE-0001-20260901";

    private static final Long RAW_MAT_ID = 1001L;
    private static final Long FINISH_MAT_ID = 9001L;
    private static final Long RAW_BATCH_ID = 501L;
    private static final Long FINISH_BATCH_ID = 601L;
    private static final Long WMS_ISSUE_ORDER_ID = 701L;
    private static final Long WMS_IN_ORDER_ID = 801L;
    private static final Long CUSTOMER_ID = 3001L;
    private static final Long WORK_ORDER_ID = 21L;

    @Resource
    private MesTraceService traceService;
    @Resource
    private MesTraceForwardResolver forwardResolver;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;

    @MockitoBean
    private BatchApi batchApi;
    @MockitoBean
    private InventoryApi inventoryApi;
    @MockitoBean
    private OutOrderApi outOrderApi;
    @MockitoBean
    private InOrderApi inOrderApi;
    @MockitoBean
    private PartnerApi partnerApi;

    /** WMS 流水替身台账：领料 ISSUE、完工收货 RECEIPT、销售发货 ISSUE，落库口径与 WMS 一致。 */
    private final List<InventoryTxnRespDTO> rawIssues = new ArrayList<>();
    private final List<InventoryTxnRespDTO> finishReceipts = new ArrayList<>();
    private final List<InventoryTxnRespDTO> salesIssues = new ArrayList<>();
    /** 门面方法调用计数，断言无 N+1 用。 */
    private final Map<String, AtomicInteger> facadeCalls = new HashMap<>();

    /**
     * 每个用例开始前把深度上限复位到默认值。
     *
     * <p>{@link MesTraceForwardResolver} 是 Spring 单例，整个测试上下文共享一个实例；
     * 截断用例 {@link #testTraceForward_loopTruncatedByDepth()} 会把它收紧到 2，
     * 不复位就会按方法执行顺序泄漏进后续用例，把完整链路的树在第二层截断掉
     * （表现为工单节点、出库单子节点凭空消失，是测试污染而非业务缺陷）。
     */
    @BeforeEach
    void resetMaxDepth() {
        forwardResolver.setMaxDepth(MesTraceForwardResolver.DEFAULT_MAX_DEPTH);
    }

    @Test
    void testTraceForward_fullChainFromRawLot() {
        // 准备：完整链路
        prepareChain();

        // 调用：从原料批次号出发
        MesTraceNodeBO root = traceService.traceForward(query("LOT", RAW_LOT));

        // 断言层级与节点数：1 批次 → 1 领料出库单 → 1 工单 →（2 工序任务 + 1 完工入库单）→ 1 成品批次 → 1 销售出库单
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), root.getNodeType());
        assertEquals(RAW_LOT, root.getBizNo());
        assertEquals(RAW_BATCH_ID, root.getSnapshot().get("batchId"));
        assertEquals(1, root.getChildren().size(), "根批次下只有一张领料出库单");

        MesTraceNodeBO issueOutNode = root.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.OUT_ORDER.name(), issueOutNode.getNodeType());
        assertEquals(OUT_NO_ISSUE, issueOutNode.getBizNo());
        assertEquals(0, new BigDecimal("200").compareTo((BigDecimal) issueOutNode.getSnapshot().get("qty")),
                "出库单节点的数量取该批次名下的 ISSUE 流水之和");
        assertEquals(1, issueOutNode.getChildren().size());

        MesTraceNodeBO orderNode = issueOutNode.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.PROD_ORDER.name(), orderNode.getNodeType());
        assertEquals(ORDER_NO, orderNode.getBizNo());
        // 任务书第 2 条：工单节点带 BOM 与工艺路线版本
        assertEquals("V1.0", orderNode.getSnapshot().get("bomVersion"));
        assertEquals("R1.0", orderNode.getSnapshot().get("routeVersion"));
        assertEquals(3, orderNode.getChildren().size(), "工单下挂 2 道工序任务 + 1 张完工入库单");

        MesTraceNodeBO opTask1 = orderNode.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.OP_TASK.name(), opTask1.getNodeType());
        assertEquals(10, opTask1.getSnapshot().get("seq"));
        MesTraceNodeBO opTask2 = orderNode.getChildren().get(1);
        assertEquals(20, opTask2.getSnapshot().get("seq"));

        MesTraceNodeBO inOrderNode = orderNode.getChildren().get(2);
        assertEquals(MesTraceNodeTypeEnum.IN_ORDER.name(), inOrderNode.getNodeType());
        assertEquals(IN_NO_FINISH, inOrderNode.getBizNo());
        assertEquals(1, inOrderNode.getChildren().size());

        MesTraceNodeBO finishBatchNode = inOrderNode.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), finishBatchNode.getNodeType());
        assertEquals(FINISH_LOT, finishBatchNode.getBizNo());
        assertEquals(1, finishBatchNode.getChildren().size());

        MesTraceNodeBO salesNode = finishBatchNode.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.OUT_ORDER.name(), salesNode.getNodeType());
        assertEquals(OUT_NO_SALES, salesNode.getBizNo());
        // 客户节点：OutOrderRespDTO 缺 customer_id 字段（门面缺口，见自查遗留），客户节点暂不挂载
        assertEquals(0, salesNode.getChildren().size());

        // 断言无 N+1：出库单、入库单各一次批量调用；流水按物料分片恰好 3 次（原料 ISSUE + 完工 RECEIPT + 销售 ISSUE）
        verify(outOrderApi, atMostOnce()).getOutOrderMap(anyCollection());
        verify(inOrderApi, atMostOnce()).getInOrderMap(anyCollection());
        verify(inventoryApi, atMost(3)).getInventoryTxnList(any(InventoryTxnQueryReqDTO.class));
        assertEquals(3, facadeCalls.get("txn").get());
    }

    @Test
    void testTraceForward_fromProdOrderMiddleOfChain() {
        // 准备
        prepareChain();

        // 调用：从工单号切入（链路中间），向上补原料批次、向下补完工入库与出货
        MesTraceNodeBO root = traceService.traceForward(query("PROD_ORDER", ORDER_NO));

        // 断言：树根仍是原料批次，链路上下游双向补全
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), root.getNodeType());
        assertEquals(RAW_LOT, root.getBizNo());
        MesTraceNodeBO orderNode = root.getChildren().get(0).getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.PROD_ORDER.name(), orderNode.getNodeType());
        assertEquals(ORDER_NO, orderNode.getBizNo());
        assertEquals(3, orderNode.getChildren().size());
        MesTraceNodeBO salesNode = orderNode.getChildren().get(2).getChildren().get(0).getChildren().get(0);
        assertEquals(OUT_NO_SALES, salesNode.getBizNo());
    }

    @Test
    void testTraceForward_fromSalesOutMiddleOfChain() {
        // 准备
        prepareChain();

        // 调用：从销售出库单号切入（链路末端），向上补完工入库、工单、原料批次
        MesTraceNodeBO root = traceService.traceForward(query("SALES_OUT", OUT_NO_SALES));

        // 断言：树根是原料批次，整条链路补全后与销售起点无关（同一棵树）
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), root.getNodeType());
        assertEquals(RAW_LOT, root.getBizNo());
        MesTraceNodeBO orderNode = root.getChildren().get(0).getChildren().get(0);
        assertEquals(ORDER_NO, orderNode.getBizNo());
        // 终点销售出库单在成品批次之下，起点单号在树里再次出现
        MesTraceNodeBO salesNode = orderNode.getChildren().get(2).getChildren().get(0).getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.OUT_ORDER.name(), salesNode.getNodeType());
        assertEquals(OUT_NO_SALES, salesNode.getBizNo());
        assertEquals(0, new BigDecimal("100").compareTo((BigDecimal) salesNode.getSnapshot().get("qty")));
    }

    @Test
    void testTraceForward_numericLotUsesBatchMaster() {
        // 准备：纯数字起点按批次编号走批次主档
        prepareChain();

        MesTraceNodeBO root = traceService.traceForward(query("LOT", String.valueOf(RAW_BATCH_ID)));

        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), root.getNodeType());
        assertEquals(RAW_LOT, root.getBizNo());
        // 批次主档快照（供应商等）只在编号路径可得
        assertEquals(2001L, root.getSnapshot().get("supplierId"));
    }

    @Test
    void testTraceForward_startNotFound() {
        prepareChain();

        assertServiceException(() -> traceService.traceForward(query("LOT", "LOT-NOT-EXIST")),
                TRACE_START_NOT_FOUND, "批次", "LOT-NOT-EXIST");
        assertServiceException(() -> traceService.traceForward(query("PROD_ORDER", "PO-NOT-EXIST")),
                TRACE_START_NOT_FOUND, "生产工单", "PO-NOT-EXIST");
        assertServiceException(() -> traceService.traceForward(query("SALES_OUT", "OUT-NOT-EXIST")),
                TRACE_START_NOT_FOUND, "销售出库单", "OUT-NOT-EXIST");
    }

    @Test
    void testTraceForward_startTypeInvalid() {
        assertServiceException(() -> traceService.traceForward(query("BACKWARD", "whatever")),
                TRACE_START_TYPE_INVALID, "BACKWARD");
    }

    @Test
    void testTraceForward_loopTruncatedByDepth() {
        // 准备链路后把深度上限收到 2，验证超限截断
        prepareChain();
        forwardResolver.setMaxDepth(2);

        MesTraceNodeBO root = traceService.traceForward(query("LOT", RAW_LOT));

        // 根批次(深度 1) → 出库单(深度 2 达上限)：标注截断且不再展开工单
        assertEquals(1, root.getChildren().size());
        MesTraceNodeBO issueOutNode = root.getChildren().get(0);
        assertTrue(Boolean.TRUE.equals(issueOutNode.getTruncated()), "深度达到上限的节点应标注截断");
        assertEquals(0, issueOutNode.getChildren().size(), "截断节点不再展开子节点");
    }

    // ========== 数据准备 ==========

    /**
     * 造一条完整链路：
     * 原料批次(501, LOT-RAW-001) → 领料申请 MR-0001(wms_biz_no=701) → 出库单 OUT-ISSUE-0001 →
     * 生产工单 PO-TRACE-0001（BOM V1.0 / 工艺 R1.0）→ 工序任务 ×2 →
     * 完工入库单 IN-FINISH-0001（wip_txn 桥，biz_no=801）→ 成品批次(601) → 销售出库 OUT-SALES-0001。
     */
    private void prepareChain() {
        rawIssues.clear();
        finishReceipts.clear();
        salesIssues.clear();
        facadeCalls.clear();

        // MES 侧：生产工单
        ProdOrderDO order = ProdOrderDO.builder()
                .orderNo(ORDER_NO)
                .demandId(MesConstants.NONE_ID)
                .plantId(1L)
                .workshopId(2L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(FINISH_MAT_ID)
                .matCode("MAT-FINISH")
                .matName("成品")
                .qty(new BigDecimal("100"))
                .completedQty(new BigDecimal("100"))
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(1L)
                .uomCode("EA")
                .bomId(11L)
                .bomVersion("V1.0")
                .routeId(21L)
                .routeVersion("R1.0")
                .priority(0)
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(MesProdOrderStatusEnum.DONE.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);

        // 作业工单：工序任务的挂靠点
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .id(WORK_ORDER_ID)
                .workOrderNo("WO-0001")
                .prodOrderId(order.getId())
                .prodOrderNo(ORDER_NO)
                .plantId(1L)
                .workshopId(2L)
                .wcId(201L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(new BigDecimal("100"))
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(MesWorkOrderStatusEnum.DONE.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);

        // 领料申请与明细：明细行带原料批次快照，req 上带 WMS 出库单编号（T1-11 口径）
        MaterialReqDO req = MaterialReqDO.builder()
                .reqNo(REQ_NO)
                .prodOrderId(order.getId())
                .prodOrderNo(ORDER_NO)
                .workOrderId(WORK_ORDER_ID)
                .opTaskId(MesConstants.NONE_ID)
                .reqType(MesMaterialReqTypeEnum.ISSUE.name())
                .invOrgId(1L)
                .whId(1L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .wmsBizNo(String.valueOf(WMS_ISSUE_ORDER_ID))
                .status(MesMaterialReqStatusEnum.DONE.name())
                .idempotencyKey("UI:" + REQ_NO + ":CREATE_REQ:0")
                .build();
        materialReqMapper.insert(req);
        MaterialReqLineDO line = MaterialReqLineDO.builder()
                .reqId(req.getId())
                .lineNo(1)
                .routeOpMatId(MesConstants.NONE_ID)
                .matId(RAW_MAT_ID)
                .matCode("MAT-RAW")
                .matName("原料")
                .qty(new BigDecimal("200"))
                .processedQty(new BigDecimal("200"))
                .uomId(1L)
                .uomCode("EA")
                .batchId(RAW_BATCH_ID)
                .lotNo(RAW_LOT)
                .reservationId(MesConstants.NONE_ID)
                .mustIssueBeforeStart(Boolean.TRUE)
                .status(MesMaterialReqLineStatusEnum.DONE.name())
                .build();
        materialReqLineMapper.insert(line);

        // 工序任务两道
        insertOpTask("OP-T-001", 10, "下料", MesOpTaskStatusEnum.DONE);
        insertOpTask("OP-T-002", 20, "装配", MesOpTaskStatusEnum.DONE);

        // 完工入库流水桥：wip_txn(IN_ORDER) 的 biz_no 存入库单编号
        WipTxnDO inboundTxn = WipTxnDO.builder()
                .txnNo("WTX-001")
                .prodOrderId(order.getId())
                .workOrderId(WORK_ORDER_ID)
                .opTaskId(MesConstants.NONE_ID)
                .txnType(MesWipTxnTypeEnum.FINISH_INBOUND.name())
                .qty(new BigDecimal("100"))
                .toStatus(MesOpTaskStatusEnum.DONE.name())
                .bizType(MesBizTypeEnum.IN_ORDER.name())
                .bizNo(String.valueOf(WMS_IN_ORDER_ID))
                .fromOpTaskId(MesConstants.NONE_ID)
                .toOpTaskId(MesConstants.NONE_ID)
                .txnTime(LocalDateTime.now())
                .idempotencyKey("MES:" + ORDER_NO + ":FINISH_INBOUND:1")
                .build();
        wipTxnMapper.insert(inboundTxn);

        // WMS 流水替身：领料 ISSUE、完工 RECEIPT、销售 ISSUE
        rawIssues.add(txn(1L, RAW_MAT_ID, "MAT-RAW", "原料", RAW_BATCH_ID, RAW_LOT,
                WmsOutBizTypeEnum.PROD_ISSUE_OUT.name(), WmsTxnTypeEnum.ISSUE.name(), OUT_NO_ISSUE,
                new BigDecimal("200")));
        finishReceipts.add(txn(2L, FINISH_MAT_ID, "MAT-FINISH", "成品", FINISH_BATCH_ID, FINISH_LOT,
                WmsInBizTypeEnum.PROD_FINISH_IN.name(), WmsTxnTypeEnum.RECEIPT.name(), IN_NO_FINISH,
                new BigDecimal("100")));
        salesIssues.add(txn(3L, FINISH_MAT_ID, "MAT-FINISH", "成品", FINISH_BATCH_ID, FINISH_LOT,
                WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name(), WmsTxnTypeEnum.ISSUE.name(), OUT_NO_SALES,
                new BigDecimal("100")));

        // 门面 mock：全部走批量语义，计数验证无 N+1
        when(batchApi.getBatch(any())).thenAnswer(invocation -> {
            count("batch.get");
            Long id = invocation.getArgument(0);
            return RAW_BATCH_ID.equals(id) ? batch(RAW_BATCH_ID, RAW_MAT_ID, "MAT-RAW", "原料", RAW_LOT)
                    : FINISH_BATCH_ID.equals(id) ? batch(FINISH_BATCH_ID, FINISH_MAT_ID, "MAT-FINISH", "成品", FINISH_LOT)
                    : null;
        });
        when(batchApi.getBatchMap(anyCollection())).thenAnswer(invocation -> {
            count("batch.map");
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, BatchRespDTO> result = new HashMap<>();
            if (ids.contains(RAW_BATCH_ID)) {
                result.put(RAW_BATCH_ID, batch(RAW_BATCH_ID, RAW_MAT_ID, "MAT-RAW", "原料", RAW_LOT));
            }
            if (ids.contains(FINISH_BATCH_ID)) {
                result.put(FINISH_BATCH_ID, batch(FINISH_BATCH_ID, FINISH_MAT_ID, "MAT-FINISH", "成品", FINISH_LOT));
            }
            return result;
        });
        when(outOrderApi.getOutOrderMap(anyCollection())).thenAnswer(invocation -> {
            count("out.map");
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, OutOrderRespDTO> result = new HashMap<>();
            if (ids.contains(WMS_ISSUE_ORDER_ID)) {
                result.put(WMS_ISSUE_ORDER_ID, outOrder(WMS_ISSUE_ORDER_ID, OUT_NO_ISSUE,
                        WmsOutBizTypeEnum.PROD_ISSUE_OUT.name(), "DONE"));
            }
            return result;
        });
        when(inOrderApi.getInOrderMap(anyCollection())).thenAnswer(invocation -> {
            count("in.map");
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, InOrderRespDTO> result = new HashMap<>();
            if (ids.contains(WMS_IN_ORDER_ID)) {
                InOrderRespDTO dto = new InOrderRespDTO();
                dto.setId(WMS_IN_ORDER_ID);
                dto.setInNo(IN_NO_FINISH);
                dto.setBizType(WmsInBizTypeEnum.PROD_FINISH_IN.name());
                dto.setSourceType("MES");
                dto.setSourceBizNo(ORDER_NO);
                dto.setStatus("DONE");
                result.put(WMS_IN_ORDER_ID, dto);
            }
            return result;
        });
        when(inventoryApi.getInventoryTxnList(any(InventoryTxnQueryReqDTO.class))).thenAnswer(invocation -> {
            count("txn");
            InventoryTxnQueryReqDTO query = invocation.getArgument(0);
            // 替身按查询条件过滤，与真实 WMS 的查询语义一致
            List<InventoryTxnRespDTO> pool;
            if (WmsInBizTypeEnum.PROD_FINISH_IN.name().equals(query.getBizType())) {
                pool = finishReceipts;
            } else if (WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name().equals(query.getBizType())) {
                pool = salesIssues;
            } else {
                pool = rawIssues;
            }
            return pool.stream()
                    .filter(txn -> query.getMatId() == null || query.getMatId().equals(txn.getMatId()))
                    .filter(txn -> query.getBizNo() == null || query.getBizNo().equals(txn.getBizNo()))
                    .filter(txn -> query.getTxnType() == null || query.getTxnType().equals(txn.getTxnType()))
                    .collect(Collectors.toList());
        });
        when(partnerApi.getCustomerMap(anyCollection())).thenAnswer(invocation -> {
            count("customer.map");
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, CustomerRespDTO> result = new HashMap<>();
            if (ids.contains(CUSTOMER_ID)) {
                CustomerRespDTO customer = new CustomerRespDTO();
                customer.setId(CUSTOMER_ID);
                customer.setCode("CUST-001");
                customer.setName("客户一");
                result.put(CUSTOMER_ID, customer);
            }
            return result;
        });
    }

    private void insertOpTask(String taskNo, int seq, String opName, MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo(taskNo)
                .workOrderId(WORK_ORDER_ID)
                .workOrderNo("WO-0001")
                .routeOpId(MesConstants.NONE_ID)
                .opId(MesConstants.NONE_ID)
                .opCode("OP" + seq)
                .opName(opName)
                .seq(seq)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(new BigDecimal("100"))
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
    }

    private InventoryTxnRespDTO txn(Long id, Long matId, String matCode, String matName,
                                    Long batchId, String lotNo, String bizType, String txnType,
                                    String bizNo, BigDecimal qty) {
        InventoryTxnRespDTO txn = new InventoryTxnRespDTO();
        txn.setId(id);
        txn.setTxnNo("TXN-" + id);
        txn.setTxnType(txnType);
        txn.setBizType(bizType);
        txn.setBizNo(bizNo);
        txn.setIoType(WmsTxnTypeEnum.ISSUE.name().equals(txnType) ? "OUT" : "IN");
        txn.setMatId(matId);
        txn.setMatCode(matCode);
        txn.setMatName(matName);
        txn.setBatchId(batchId);
        txn.setLotNo(lotNo);
        txn.setQty(qty);
        txn.setCreateTime(LocalDateTime.now());
        return txn;
    }

    private BatchRespDTO batch(Long id, Long matId, String matCode, String matName, String lotNo) {
        BatchRespDTO batch = new BatchRespDTO();
        batch.setId(id);
        batch.setMatId(matId);
        batch.setMatCode(matCode);
        batch.setMatName(matName);
        batch.setLotNo(lotNo);
        batch.setSupplierId(2001L);
        batch.setSupplierLotNo("SUP-LOT-001");
        return batch;
    }

    private OutOrderRespDTO outOrder(Long id, String outNo, String bizType, String status) {
        OutOrderRespDTO dto = new OutOrderRespDTO();
        dto.setId(id);
        dto.setOutNo(outNo);
        dto.setBizType(bizType);
        dto.setStatus(status);
        return dto;
    }

    private void count(String method) {
        facadeCalls.computeIfAbsent(method, key -> new AtomicInteger()).incrementAndGet();
    }

    private MesTraceQueryBO query(String startType, String startValue) {
        MesTraceQueryBO query = new MesTraceQueryBO();
        query.setStartType(startType);
        query.setStartValue(startValue);
        return query;
    }

}
