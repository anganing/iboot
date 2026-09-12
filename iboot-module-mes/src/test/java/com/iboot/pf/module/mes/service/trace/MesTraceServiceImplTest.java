package com.iboot.pf.module.mes.service.trace;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.partner.PartnerApi;
import com.iboot.pf.module.mdm.api.partner.dto.CustomerRespDTO;
import com.iboot.pf.module.mdm.api.partner.dto.SupplierRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
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
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
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
import java.util.LinkedHashMap;
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
 * 批次追溯测试：正向（T5-01）与反向（T5-02）共用同一条链路数据。
 *
 * <p>造一条完整链路（原料批次 → 领料出库 → 生产工单 → 报工 → 完工入库成品批次 → 销售出库），
 * 正向断言树的层级与节点数正确，反向断言从成品批次能找回全部原料批次与供应商、质检结论已挂上；
 * 门面全部 mock 并逐方法计数，断言无 N+1。
 *
 * <p>WMS 的 wms_inv_txn 在 MES 单测的 H2 里没有表（与领料、完工入库测试同口径），
 * 流水用替身台账模拟 WMS 的落库语义——{@code biz_no} 存单号、行上带批次与物料——这与
 * {@code WmsOutOrderServiceImpl.buildPostBO} / {@code WmsInOrderServiceImpl} 的真实写入口径一致。
 * 流水的真实落库由阶段 6 跨模块集成测试覆盖。
 */
@Import({MesTraceServiceImpl.class, MesTraceForwardResolver.class, MesTraceBackwardResolver.class})
class MesTraceServiceImplTest extends BaseDbUnitTest {

    private static final String ORDER_NO = "PO-TRACE-0001";
    private static final String OUT_NO_ISSUE = "OUT-ISSUE-0001";
    private static final String OUT_NO_SALES = "OUT-SALES-0001";
    private static final String IN_NO_FINISH = "IN-FINISH-0001";
    private static final String REQ_NO = "MR-0001";
    private static final String RAW_LOT = "LOT-RAW-001";
    private static final String FINISH_LOT = "PO-TRACE-0001-20260901";
    private static final String ARRIVAL_NO = "ARR-0001";
    private static final String REPORT_NO = "PR-0001";
    /** 跨物料撞号的批次号：验证按批次号取主档时用物料编号定位，而不是取列表第一条。 */
    private static final String COLLIDE_LOT = "LOT-COLLIDE";

    private static final Long RAW_MAT_ID = 1001L;
    private static final Long FINISH_MAT_ID = 9001L;
    private static final Long COLLIDE_MAT_ID = 1002L;
    private static final Long RAW_BATCH_ID = 501L;
    private static final Long FINISH_BATCH_ID = 601L;
    private static final Long COLLIDE_BATCH_ID = 502L;
    private static final Long DECOY_BATCH_ID = 599L;
    private static final Long WMS_ISSUE_ORDER_ID = 701L;
    private static final Long WMS_IN_ORDER_ID = 801L;
    private static final Long CUSTOMER_ID = 3001L;
    private static final Long SUPPLIER_ID = 2001L;
    private static final Long WORK_ORDER_ID = 21L;

    @Resource
    private MesTraceService traceService;
    @Resource
    private MesTraceForwardResolver forwardResolver;
    @Resource
    private MesTraceBackwardResolver backwardResolver;
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
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;

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
    /** {@code wms_batch} 替身台账：批次编号 → 批次主档，按引用被门面 stub 捕获。
     * 用 LinkedHashMap 是为了让"按批次号取回列表"的顺序可预期——撞号用例要求干扰项排在前面。 */
    private final Map<Long, BatchRespDTO> batchMaster = new LinkedHashMap<>();
    /** 门面方法调用计数，断言无 N+1 用。 */
    private final Map<String, AtomicInteger> facadeCalls = new HashMap<>();

    /**
     * 每个用例开始前把深度上限复位到默认值。
     *
     * <p>两个解析器都是 Spring 单例，整个测试上下文共享一个实例；截断用例会把上限收到 2，
     * 不复位就会按方法执行顺序泄漏进后续用例，把完整链路的树在第二层截断掉
     * （表现为工单节点、出库单子节点凭空消失，是测试污染而非业务缺陷）。
     */
    @BeforeEach
    void resetMaxDepth() {
        forwardResolver.setMaxDepth(MesTraceForwardResolver.DEFAULT_MAX_DEPTH);
        backwardResolver.setMaxDepth(MesTraceBackwardResolver.DEFAULT_MAX_DEPTH);
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
        // 客户节点：靠 OutOrderApi.getOutOrderMapByNos 换回 customer_id 后挂载（门面缺口已补齐）
        assertEquals(CUSTOMER_ID, salesNode.getSnapshot().get("customerId"));
        assertEquals(1, salesNode.getChildren().size(), "销售出库单下挂客户");
        MesTraceNodeBO customerNode = salesNode.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.CUSTOMER.name(), customerNode.getNodeType());
        assertEquals("CUST-001", customerNode.getBizNo());
        assertEquals("客户一", customerNode.getNodeName());

        // 断言无 N+1：出库单、入库单、客户各一次批量调用；流水按物料分片恰好 3 次（原料 ISSUE + 完工 RECEIPT + 销售 ISSUE）
        verify(outOrderApi, atMostOnce()).getOutOrderMap(anyCollection());
        verify(outOrderApi, atMostOnce()).getOutOrderMapByNos(anyCollection());
        verify(inOrderApi, atMostOnce()).getInOrderMap(anyCollection());
        verify(partnerApi, atMostOnce()).getCustomerMap(anyCollection());
        verify(inventoryApi, atMost(3)).getInventoryTxnList(any(InventoryTxnQueryReqDTO.class));
        assertEquals(3, facadeCalls.get("txn").get());
        assertEquals(1, facadeCalls.get("out.mapByNos").get(), "客户编号按单号一次批量反查");
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

    // ========== 反向追溯（T5-02） ==========

    @Test
    void testTraceBackward_fullChainFromFinishedLot() {
        // 准备：同一条链路 + 反查特有的三张质检任务、报工单、第二张原料批次、开工在制流水
        prepareChain();
        prepareBackwardExtras();

        // 调用：从成品批次号出发
        MesTraceNodeBO root = traceService.traceBackward(query("LOT", FINISH_LOT));

        // 断言层级：成品批次(1) → 完工入库单(2) → 工单(3) →（3 张质检任务 + 2 条在制流水 + 2 个原料批次）
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), root.getNodeType());
        assertEquals(FINISH_LOT, root.getBizNo());
        assertEquals(FINISH_BATCH_ID, root.getSnapshot().get("batchId"));
        assertEquals(0, new BigDecimal("100").compareTo((BigDecimal) root.getSnapshot().get("qty")),
                "成品批次数量取完工收货流水之和");
        assertEquals(1, root.getChildren().size(), "成品批次下只有一张完工入库单");

        MesTraceNodeBO inOrderNode = root.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.IN_ORDER.name(), inOrderNode.getNodeType());
        assertEquals(IN_NO_FINISH, inOrderNode.getBizNo());
        assertEquals(ORDER_NO, inOrderNode.getSnapshot().get("sourceBizNo"));
        assertEquals(1, inOrderNode.getChildren().size());

        MesTraceNodeBO orderNode = inOrderNode.getChildren().get(0);
        assertEquals(MesTraceNodeTypeEnum.PROD_ORDER.name(), orderNode.getNodeType());
        assertEquals(ORDER_NO, orderNode.getBizNo());
        // 任务书第 2 条：工单节点带两个版本号
        assertEquals("V1.0", orderNode.getSnapshot().get("bomVersion"));
        assertEquals("R1.0", orderNode.getSnapshot().get("routeVersion"));
        // 3 质检任务（FQC + FAI + IPQC）+ 2 在制流水（完工入库 + 开工）+ 2 原料批次
        assertEquals(7, orderNode.getChildren().size(), "工单下挂质检任务、在制流水与全部原料批次");

        // 断言质检结论已挂上：三种检验点各一条，都带结论与锁定的方案版本
        List<MesTraceNodeBO> qcNodes = orderNode.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.QC_TASK.name().equals(node.getNodeType())).toList();
        assertEquals(3, qcNodes.size(), "FQC / FAI / IPQC 三个检验点的结论都要挂上");
        qcNodes.forEach(qc -> {
            assertEquals(MesQcConclusionEnum.QUALIFIED.name(), qc.getSnapshot().get("conclusion"));
            assertEquals("PLAN-V3", qc.getSnapshot().get("qcPlanVersion"), "质检节点要带锁定的检验方案版本");
        });
        // 节点名是检验点中文名（字典口径），不给原始枚举值
        assertEquals("完工检验", childByBizNo(qcNodes, "QC-FQC-001").getNodeName());
        assertEquals("首件检验", childByBizNo(qcNodes, "QC-FAI-001").getNodeName());
        assertEquals("过程检验", childByBizNo(qcNodes, "QC-IPQC-001").getNodeName());
        assertTrue(qcNodes.stream().anyMatch(qc -> MesQcTriggerPointEnum.FQC.name().equals(qc.getSnapshot().get("triggerPoint"))),
                "挂在工单号上的 FQC");
        assertTrue(qcNodes.stream().anyMatch(qc -> MesQcTriggerPointEnum.FAI.name().equals(qc.getSnapshot().get("triggerPoint"))),
                "挂在工序任务号上的 FAI 也要归到工单节点下");
        assertTrue(qcNodes.stream().anyMatch(qc -> MesQcTriggerPointEnum.IPQC.name().equals(qc.getSnapshot().get("triggerPoint"))),
                "挂在报工单号上的 IPQC 也要归到工单节点下");

        // 断言在制流水已挂上
        List<MesTraceNodeBO> wipNodes = orderNode.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.WIP_TXN.name().equals(node.getNodeType())).toList();
        assertEquals(2, wipNodes.size(), "完工入库 + 开工两条在制流水");
        assertTrue(wipNodes.stream().allMatch(node -> node.getSnapshot().get("txnType") != null));

        // 断言能找到全部原料批次与供应商（含跨物料撞号与行上无批次编号的两处定位）
        List<MesTraceNodeBO> rawNodes = orderNode.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.BATCH.name().equals(node.getNodeType())).toList();
        assertEquals(2, rawNodes.size(), "两张领料行的原料批次都要反查出来");
        MesTraceNodeBO raw1 = childByBizNo(rawNodes, RAW_LOT);
        assertEquals(RAW_BATCH_ID, raw1.getSnapshot().get("batchId"));
        assertEquals(SUPPLIER_ID, raw1.getSnapshot().get("supplierId"));
        assertEquals(REQ_NO, raw1.getSnapshot().get("materialReqNo"));
        assertEquals(OUT_NO_ISSUE, raw1.getSnapshot().get("outNo"));
        assertEquals(0, new BigDecimal("200").compareTo((BigDecimal) raw1.getSnapshot().get("qty")));
        MesTraceNodeBO raw2 = childByBizNo(rawNodes, COLLIDE_LOT);
        // 行上 batch_id = 0：批次主档按批次号取回的是列表，靠物料编号定位到本物料那条（不是撞号的那条）
        assertEquals(COLLIDE_BATCH_ID, raw2.getSnapshot().get("batchId"),
                "按批次号取回的是列表（同批次号还挂着另一物料的干扰批次），必须按物料编号定位，不能取第一条");
        assertEquals(0, new BigDecimal("50").compareTo((BigDecimal) raw2.getSnapshot().get("qty")));
        assertEquals(SUPPLIER_ID, raw2.getSnapshot().get("supplierId"));
        assertEquals(1, raw2.getChildren().size(), "该批次只挂供应商：干扰批次的检验结论不许挂过来");
        assertEquals(MesTraceNodeTypeEnum.SUPPLIER.name(), raw2.getChildren().get(0).getNodeType());

        // 来料检验挂在原料批次节点上
        List<MesTraceNodeBO> iqcNodes = raw1.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.QC_TASK.name().equals(node.getNodeType())).toList();
        assertEquals(1, iqcNodes.size(), "IQC 按 batch_id 挂在原料批次下");
        assertEquals(MesQcTriggerPointEnum.IQC.name(), iqcNodes.get(0).getSnapshot().get("triggerPoint"));
        assertEquals(MesQcConclusionEnum.QUALIFIED.name(), iqcNodes.get(0).getSnapshot().get("conclusion"));

        MesTraceNodeBO supplierNode = raw1.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.SUPPLIER.name().equals(node.getNodeType()))
                .findFirst().orElseThrow();
        assertEquals("SUP-001", supplierNode.getBizNo());
        assertEquals("供应商一", supplierNode.getNodeName());
        assertEquals("SUP-LOT-001", supplierNode.getSnapshot().get("supplierLotNo"));
        assertEquals("HEAT-001", supplierNode.getSnapshot().get("furnaceLotNo"));

        // 断言无 N+1：每类门面数据至多一次批量调用，流水一次（batchIds 已下推，不再按物料分片）
        verify(inventoryApi, atMostOnce()).getInventoryTxnList(any(InventoryTxnQueryReqDTO.class));
        assertEquals(1, facadeCalls.get("txn").get(), "完工收货流水按批次编号一次查回，不分片");
        verify(batchApi, atMostOnce()).getBatchMap(anyCollection());
        // 按批次号取主档有两个用途（起点批次号解析 + 行上无批次编号的领料行），各一次、与行数无关
        verify(batchApi, atMost(2)).getBatchMapByLotNos(anyCollection());
        assertEquals(2, facadeCalls.get("batch.mapByLotNos").get());
        verify(inOrderApi, atMostOnce()).getInOrderMap(anyCollection());
        verify(outOrderApi, atMostOnce()).getOutOrderMap(anyCollection());
        verify(partnerApi, atMostOnce()).getSupplierMap(anyCollection());
        assertEquals(1, facadeCalls.get("in.map").get());
        assertEquals(1, facadeCalls.get("out.map").get());
        assertEquals(1, facadeCalls.get("supplier.map").get());
    }

    @Test
    void testTraceBackward_numericStartUsesBatchMasterSnapshot() {
        // 准备
        prepareChain();
        prepareBackwardExtras();

        // 调用：纯数字起点按批次编号走批次主档
        MesTraceNodeBO root = traceService.traceBackward(query("LOT", String.valueOf(FINISH_BATCH_ID)));

        // 断言：起点主档快照直接可用（供应商、炉批号只有主档才给得出），且没有为已知批次重复调门面
        assertEquals(FINISH_LOT, root.getBizNo());
        assertEquals(SUPPLIER_ID, root.getSnapshot().get("supplierId"));
        assertEquals("HEAT-001", root.getSnapshot().get("furnaceLotNo"));
        assertEquals(1, facadeCalls.get("batch.get").get(), "起点批次走 getBatch");
        assertEquals(1, facadeCalls.get("batch.map").get(), "起点批次不重复要，只剩原料批次这一次批量");
        assertEquals(1, facadeCalls.get("batch.mapByLotNos").get(), "只有行上无批次编号的领料行需要按批次号取主档");
    }

    @Test
    void testTraceBackward_fromProdOrderAndSalesOutMiddleOfChain() {
        // 准备
        prepareChain();
        prepareBackwardExtras();

        // 调用一：从工单号切入（链路中间）
        MesTraceNodeBO fromOrder = traceService.traceBackward(query("PROD_ORDER", ORDER_NO));

        // 断言：树根仍是成品批次，下游入库单与上游原料都补全
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), fromOrder.getNodeType());
        assertEquals(FINISH_LOT, fromOrder.getBizNo());
        MesTraceNodeBO orderNode = fromOrder.getChildren().get(0).getChildren().get(0);
        assertEquals(ORDER_NO, orderNode.getBizNo());
        assertEquals(2, orderNode.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.BATCH.name().equals(node.getNodeType())).count(),
                "工单起点照样反查出两张原料批次");

        // 调用二：从销售出库单号切入（链路末端）
        MesTraceNodeBO fromSales = traceService.traceBackward(query("SALES_OUT", OUT_NO_SALES));

        // 断言：与成品批次起点得到同一棵树
        assertEquals(MesTraceNodeTypeEnum.BATCH.name(), fromSales.getNodeType());
        assertEquals(FINISH_LOT, fromSales.getBizNo());
        assertEquals(fromOrder.getChildren().get(0).getBizNo(), fromSales.getChildren().get(0).getBizNo());
    }

    @Test
    void testTraceBackward_orderWithoutInboundStillReturnsMaterialChain() {
        // 准备：链路里没有该工单的完工入库单时，树根退化为工单本身（把 wip_txn 桥删掉模拟未入库）
        prepareChain();
        prepareBackwardExtras();
        wipTxnMapper.delete(WipTxnDO::getTxnNo, "WTX-001");

        MesTraceNodeBO root = traceService.traceBackward(query("PROD_ORDER", ORDER_NO));

        // 断言：没有成品批次也要交出原料与供应商，反查不许空手而归
        assertEquals(MesTraceNodeTypeEnum.PROD_ORDER.name(), root.getNodeType());
        assertEquals(ORDER_NO, root.getBizNo());
        assertEquals(2, root.getChildren().stream()
                .filter(node -> MesTraceNodeTypeEnum.BATCH.name().equals(node.getNodeType())).count());
    }

    @Test
    void testTraceBackward_startNotFoundAndTypeInvalid() {
        // 准备
        prepareChain();
        prepareBackwardExtras();

        // 批次主档查不到：起点不存在
        assertServiceException(() -> traceService.traceBackward(query("LOT", "LOT-NOT-EXIST")),
                TRACE_START_NOT_FOUND, "成品批次", "LOT-NOT-EXIST");
        // 起点批次存在但没有完工入库流水（这里是原料批次，它只被领用不会被入库）
        assertServiceException(() -> traceService.traceBackward(query("LOT", RAW_LOT)),
                TRACE_START_NOT_FOUND, "成品批次的完工入库", RAW_LOT);
        // 工单不存在
        assertServiceException(() -> traceService.traceBackward(query("PROD_ORDER", "PO-NOT-EXIST")),
                TRACE_START_NOT_FOUND, "生产工单", "PO-NOT-EXIST");
        // 起点类型不合法
        assertServiceException(() -> traceService.traceBackward(query("BACKWARD", "whatever")),
                TRACE_START_TYPE_INVALID, "BACKWARD");
    }

    @Test
    void testTraceBackward_truncatedByDepth() {
        // 准备：把深度上限收到 2，验证超限截断（与正向各自持有上限，互不影响）
        prepareChain();
        prepareBackwardExtras();
        backwardResolver.setMaxDepth(2);

        MesTraceNodeBO root = traceService.traceBackward(query("LOT", FINISH_LOT));

        // 成品批次(1) → 完工入库单(2 达上限)：标注截断且不再展开工单
        assertEquals(1, root.getChildren().size());
        MesTraceNodeBO inOrderNode = root.getChildren().get(0);
        assertTrue(Boolean.TRUE.equals(inOrderNode.getTruncated()), "深度达到上限的节点应标注截断");
        assertEquals(0, inOrderNode.getChildren().size(), "截断节点不再展开子节点");
        // 正向解析器不受影响：两个方向各自持有上限
        assertEquals(1, traceService.traceForward(query("LOT", RAW_LOT)).getChildren().size());
    }

    // ========== 数据准备 ==========

    /**
     * 反查用例特有的数据：正向链路之外再补三张质检任务、一张报工单、第二条领料行与一条开工流水。
     *
     * <p>三张质检任务分别挂在工单号（FQC）、工序任务号（FAI）、报工单号（IPQC）上，用来验证
     * "各检验点结论都归到工单节点"这段索引逻辑；再来一张挂在到货单号 + 批次上的 IQC，
     * 验证它挂在原料批次节点而不会被误挂到工单下。第二条领料的批次号故意跨物料撞号，
     * 用来验证 {@code getBatchMapByLotNos} 返回值是列表时必须按物料编号定位。
     */
    private void prepareBackwardExtras() {
        ProdOrderDO order = prodOrderMapper.selectByOrderNo(ORDER_NO);

        // 报工单：IPQC 的 biz_no 承载它
        prodReportMapper.insert(ProdReportDO.builder()
                .reportNo(REPORT_NO)
                .prodOrderId(order.getId())
                .workOrderId(WORK_ORDER_ID)
                .opTaskId(MesConstants.NONE_ID)
                .workerId(MesConstants.NONE_ID)
                .resId(MesConstants.NONE_ID)
                .shiftId(MesConstants.NONE_ID)
                .qtyGood(new BigDecimal("100"))
                .qtyScrap(MesConstants.ZERO_QTY)
                .qtyRework(MesConstants.ZERO_QTY)
                .reportTime(LocalDateTime.now())
                .status(MesProdReportStatusEnum.CONFIRMED.name())
                .idempotencyKey("UI:" + ORDER_NO + ":REPORT:1")
                .build());

        // 开工在制流水：反查要在工单下看到工序执行轨迹
        wipTxnMapper.insert(WipTxnDO.builder()
                .txnNo("WTX-002")
                .prodOrderId(order.getId())
                .workOrderId(WORK_ORDER_ID)
                .opTaskId(MesConstants.NONE_ID)
                .txnType(MesWipTxnTypeEnum.START.name())
                .qty(MesConstants.ZERO_QTY)
                .fromStatus(MesOpTaskStatusEnum.READY.name())
                .toStatus(MesOpTaskStatusEnum.RUNNING.name())
                .bizType(MesBizTypeEnum.OP_TASK.name())
                .bizNo("OP-T-001")
                .fromOpTaskId(MesConstants.NONE_ID)
                .toOpTaskId(MesConstants.NONE_ID)
                .txnTime(LocalDateTime.now().minusHours(1))
                .idempotencyKey("UI:OP-T-001:START:0")
                .build());

        // 三张挂在工单链路上的质检任务：FQC 挂工单号、FAI 挂工序任务号、IPQC 挂报工单号
        insertQcTask("QC-FQC-001", MesQcTriggerPointEnum.FQC, MesBizTypeEnum.PROD_ORDER, ORDER_NO,
                FINISH_MAT_ID, FINISH_LOT, MesConstants.NONE_ID);
        insertQcTask("QC-FAI-001", MesQcTriggerPointEnum.FAI, MesBizTypeEnum.OP_TASK, "OP-T-001",
                FINISH_MAT_ID, null, MesConstants.NONE_ID);
        insertQcTask("QC-IPQC-001", MesQcTriggerPointEnum.IPQC, MesBizTypeEnum.PROD_REPORT, REPORT_NO,
                FINISH_MAT_ID, null, MesConstants.NONE_ID);
        // IQC 挂在到货单号 + 原料批次上：结论挂原料批次节点，不该混进工单节点
        insertQcTask("QC-IQC-001", MesQcTriggerPointEnum.IQC, MesBizTypeEnum.ARRIVAL, ARRIVAL_NO,
                RAW_MAT_ID, RAW_LOT, RAW_BATCH_ID);
        // 干扰项：别的到货单的 IQC，单号与批次都不在链路上
        insertQcTask("QC-IQC-999", MesQcTriggerPointEnum.IQC, MesBizTypeEnum.ARRIVAL, "ARR-9999",
                RAW_MAT_ID, "LOT-OTHER", DECOY_BATCH_ID);

        // 第二条领料行：批次号跨物料撞号，且行上不存批次编号（只能按批次号 + 物料编号定位主档）。
        // 干扰批次先入台账，保证按批次号取回的列表里干扰项排在第一条——取首条的错误实现必然失败
        batchMaster.put(DECOY_BATCH_ID, batch(DECOY_BATCH_ID, 7777L, "MAT-DECOY", "干扰物料", COLLIDE_LOT));
        batchMaster.put(COLLIDE_BATCH_ID, batch(COLLIDE_BATCH_ID, COLLIDE_MAT_ID, "MAT-COLLIDE",
                "撞号物料", COLLIDE_LOT));
        MaterialReqDO req = materialReqMapper.selectByReqNo(REQ_NO);
        materialReqLineMapper.insert(MaterialReqLineDO.builder()
                .reqId(req.getId())
                .lineNo(2)
                .routeOpMatId(MesConstants.NONE_ID)
                .matId(COLLIDE_MAT_ID)
                .matCode("MAT-COLLIDE")
                .matName("撞号物料")
                .qty(new BigDecimal("50"))
                .processedQty(MesConstants.ZERO_QTY)
                .uomId(1L)
                .uomCode("EA")
                .batchId(MesConstants.NONE_ID)
                .lotNo(COLLIDE_LOT)
                .reservationId(MesConstants.NONE_ID)
                .mustIssueBeforeStart(Boolean.FALSE)
                .status(MesMaterialReqLineStatusEnum.SUBMITTED.name())
                .build());
    }

    private void insertQcTask(String taskNo, MesQcTriggerPointEnum triggerPoint, MesBizTypeEnum bizType,                              String bizNo, Long matId, String lotNo, Long batchId) {
        qcTaskMapper.insert(QcTaskDO.builder()
                .taskNo(taskNo)
                .ruleId(MesConstants.NONE_ID)
                .qcPlanId(31L)
                .qcPlanVersion("PLAN-V3")
                .triggerPoint(triggerPoint.name())
                .bizType(bizType.name())
                .bizNo(bizNo)
                .bizLineId(MesConstants.NONE_ID)
                .matId(matId)
                .matCode("MAT-QC")
                .matName("检验物料")
                .batchId(batchId)
                .lotNo(lotNo)
                .qty(new BigDecimal("100"))
                .sampleQty(new BigDecimal("10"))
                .uomId(1L)
                .uomCode("EA")
                .assignedInspectorId(MesConstants.NONE_ID)
                .conclusion(MesQcConclusionEnum.QUALIFIED.name())
                .qtyQualified(new BigDecimal("100"))
                .qtyRejected(MesConstants.ZERO_QTY)
                .status(MesQcTaskStatusEnum.DONE.name())
                .idempotencyKey("MES:" + bizNo + ":QC_CREATE:" + triggerPoint.name())
                .version(MesConstants.INIT_VERSION)
                .build());
    }

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
        batchMaster.clear();
        facadeCalls.clear();
        batchMaster.put(RAW_BATCH_ID, batch(RAW_BATCH_ID, RAW_MAT_ID, "MAT-RAW", "原料", RAW_LOT));
        batchMaster.put(FINISH_BATCH_ID, batch(FINISH_BATCH_ID, FINISH_MAT_ID, "MAT-FINISH", "成品", FINISH_LOT));

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

        // 门面 mock：全部走批量语义，计数验证无 N+1。批次台账按引用捕获，
        // prepareBackwardExtras 后续往里加的批次对这里的 stub 直接可见
        when(batchApi.getBatch(any())).thenAnswer(invocation -> {
            count("batch.get");
            return batchMaster.get(invocation.<Long>getArgument(0));
        });
        when(batchApi.getBatchMap(anyCollection())).thenAnswer(invocation -> {
            count("batch.map");
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, BatchRespDTO> result = new HashMap<>();
            ids.forEach(id -> {
                BatchRespDTO batch = batchMaster.get(id);
                if (batch != null) {
                    result.put(id, batch);
                }
            });
            return result;
        });
        when(batchApi.getBatchMapByLotNos(anyCollection())).thenAnswer(invocation -> {
            count("batch.mapByLotNos");
            Collection<String> lotNos = invocation.getArgument(0);
            Map<String, List<BatchRespDTO>> result = new HashMap<>();
            lotNos.forEach(lotNo -> {
                List<BatchRespDTO> matched = batchMaster.values().stream()
                        .filter(batch -> lotNo.equals(batch.getLotNo())).collect(Collectors.toList());
                if (!matched.isEmpty()) {
                    result.put(lotNo, matched);
                }
            });
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
        when(outOrderApi.getOutOrderMapByNos(anyCollection())).thenAnswer(invocation -> {
            count("out.mapByNos");
            Collection<String> orderNos = invocation.getArgument(0);
            Map<String, OutOrderRespDTO> result = new HashMap<>();
            if (orderNos.contains(OUT_NO_SALES)) {
                // 销售出库单的客户编号来自 wms_out_order.customer_id（门面已暴露该字段）
                result.put(OUT_NO_SALES, outOrder(0L, OUT_NO_SALES,
                        WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name(), "DONE", CUSTOMER_ID));
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
                    .filter(txn -> CollUtil.isEmpty(query.getBatchIds())
                            || query.getBatchIds().contains(txn.getBatchId()))
                    .filter(txn -> CollUtil.isEmpty(query.getLotNos())
                            || query.getLotNos().contains(txn.getLotNo()))
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
        when(partnerApi.getSupplierMap(anyCollection())).thenAnswer(invocation -> {
            count("supplier.map");
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, SupplierRespDTO> result = new HashMap<>();
            if (ids.contains(SUPPLIER_ID)) {
                SupplierRespDTO supplier = new SupplierRespDTO();
                supplier.setId(SUPPLIER_ID);
                supplier.setCode("SUP-001");
                supplier.setName("供应商一");
                result.put(SUPPLIER_ID, supplier);
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
        batch.setSupplierId(SUPPLIER_ID);
        batch.setSupplierLotNo("SUP-LOT-001");
        batch.setFurnaceLotNo("HEAT-001");
        return batch;
    }

    private OutOrderRespDTO outOrder(Long id, String outNo, String bizType, String status) {
        return outOrder(id, outNo, bizType, status, null);
    }

    private OutOrderRespDTO outOrder(Long id, String outNo, String bizType, String status, Long customerId) {
        OutOrderRespDTO dto = new OutOrderRespDTO();
        dto.setId(id);
        dto.setOutNo(outNo);
        dto.setBizType(bizType);
        dto.setStatus(status);
        dto.setCustomerId(customerId);
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

    /** 从子节点列表里按业务单号取节点：兄弟节点次序不承诺，断言不依赖它。 */
    private MesTraceNodeBO childByBizNo(List<MesTraceNodeBO> nodes, String lotNo) {
        return nodes.stream().filter(node -> lotNo.equals(node.getBizNo()))
                .findFirst().orElseThrow(() -> new AssertionError("找不到批次节点 " + lotNo));
    }

}
