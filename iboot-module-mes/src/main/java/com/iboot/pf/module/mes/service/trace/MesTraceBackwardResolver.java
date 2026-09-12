package com.iboot.pf.module.mes.service.trace;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.module.mdm.api.partner.PartnerApi;
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
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesTraceNodeTypeEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TRACE_START_NOT_FOUND;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TRACE_START_TYPE_INVALID;

/**
 * 批次反向追溯解析器：给定成品批次，反查到原料供应商的完整链路（08 文档"反向追溯"流程图）。
 *
 * <p>树形：成品批次 → 完工入库单 → 生产工单 →（各检验点质检任务 + 在制流水 + 原料批次 →
 * （来料检验 + 供应商））。与正向解析器的三点差异：
 * <ol>
 *   <li>方向相反：正向从原料批次往下走到客户，反向从成品批次往上走到供应商；</li>
 *   <li>工单归属的桥不同：正向是"领料行 → 领料申请 → 工单"，反向是"完工收货流水 → 入库单号 →
 *       {@code wip_txn(IN_ORDER)} 桥 → 工单"，入库单编号到单号的换算只能走
 *       {@code InOrderApi.getInOrderMap}；</li>
 *   <li>流水过滤下推到 SQL：正向按物料分片再内存过滤（种子批次要等流水回来才定得出来），
 *       反查的起点就是批次，批次编号进链路前已知，直接用 {@code batchIds} 条件一次查回。</li>
 * </ol>
 *
 * <p><b>链路桥接的落库口径</b>（与正向解析器同一套事实，逐处核实过写入方）：
 * <ul>
 *   <li>成品批次 → 完工入库单：{@code wms_inv_txn(biz_type = PROD_FINISH_IN, txn_type = RECEIPT)}
 *       的 {@code biz_no} 存 WMS 入库单<b>单号</b>，行上带批次编号与批次号快照。</li>
 *   <li>完工入库单 → 生产工单：{@code mes_wip_txn(biz_type = IN_ORDER)} 的 {@code biz_no} 存 WMS
 *       入库单<b>编号</b>（{@code MesFinishInboundServiceImpl#doInbound} 口径），经
 *       {@code InOrderApi.getInOrderMap} 换回单号后与流水的 {@code biz_no} 对齐；入库单的
 *       {@code source_biz_no} 即工单号，两者双保险匹配，避免同物料多张工单串行。</li>
 *   <li>生产工单 → 原料批次：{@code mes_material_req_line}（{@code batch_id} / {@code lot_no} 快照）
 *       → {@code mes_material_req.prod_order_no}；退料申请方向相反，不进反查链路。领料申请上的
 *       {@code wms_biz_no} 存 WMS 出库单编号，经 {@code OutOrderApi.getOutOrderMap} 换回单号，
 *       只作为原料批次节点的"从哪张出库单领走"快照，不参与树形——出库单没过账时原料批次照样要
 *       反查得出来，不能因为单据缺失把上游弄丢。</li>
 *   <li>原料批次 → 供应商：{@code wms_batch.supplier_id}，名称经 {@code PartnerApi.getSupplierMap}
 *       批量补全（任务书里的 {@code SupplierApi} 在 mdm 侧的实际落地形态就是 {@code PartnerApi}
 *       的供应商方法）。</li>
 *   <li>各检验点结论：{@code mes_qc_task} 按两个维度各一次批量——工单名下的按
 *       {@code biz_no} ∈ {工单号 ∪ 工序任务号 ∪ 报工单号} 查一次，再按 {@code (bizType, bizNo)}
 *       配对收窄（覆盖 FQC / FAI / IPQC）；来料检验按 {@code batch_id} ∈ 原料批次查一次，
 *       挂在原料批次节点上。</li>
 * </ul>
 *
 * <p><b>无 N+1</b>：MES 侧全部是 {@code IN}/{@code eq} 批量查（工单、领料申请、领料行、作业工单、
 * 工序任务、报工单、质检任务 ×2、在制流水），门面侧每个方法至多一次批量调用（批次主档按编号、
 * 批次主档按批次号、库存流水、入库单、出库单、供应商各一次），调用次数与链路上的行数无关，
 * 全部在 {@code MesTraceServiceImplTest} 的反向用例里逐方法断言。
 *
 * <p><b>与任务书的一处取数差异</b>：任务书第 2 条第一步写"InOrderApi 查该批次的完工入库单"，
 * 但 {@code InOrderApi} 只有按单据编号的 {@code getInOrderMap(ids)}，既不能按批次查也不能按单号查。
 * 因此"批次 → 入库单"这一跳由 {@code InventoryApi} 的完工收货流水承载（流水行上有批次与入库单号），
 * 入库单事实仍经 {@code InOrderApi} 批量取回，无跨库 JOIN。门面若补出 {@code getInOrderMapByNos}，
 * 本类的候选工单按物料筛（{@link #candidateOrders}）可以整段删掉，已记入任务自查的遗留项。
 */
@Component
public class MesTraceBackwardResolver {

    /**
     * 链路深度上限：与正向同口径，防异常数据把树撑爆，超限截断并标注 truncated。
     *
     * <p>本类独立持有一份而非复用正向常量：正向解析器是 T5-01 的独占文件，不许改动。
     * package-private 是为了让测试收紧上限验证截断行为后能复位（本类同样是 Spring 单例）。
     */
    static final int DEFAULT_MAX_DEPTH = 10;

    /** 批次身份键前缀：有批次编号用编号，否则退回批次号。 */
    private static final String KEY_BATCH = "B:";
    private static final String KEY_LOT = "L:";
    /** 无批次编号时的领料行批次键要带物料编号：批次号只在物料内唯一，跨物料撞号不能互相顶掉。 */
    private static final String KEY_MAT_LOT = "M:";

    /** 链路深度上限，测试收紧以验证截断行为。 */
    private int maxDepth = DEFAULT_MAX_DEPTH;

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private BatchApi batchApi;
    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private InOrderApi inOrderApi;
    @Resource
    private OutOrderApi outOrderApi;
    @Resource
    private PartnerApi partnerApi;

    /** 反向追溯入口：按起点类型分发。 */
    public MesTraceNodeBO resolve(MesTraceQueryBO query) {
        MesTraceStartTypeEnum startType = MesTraceStartTypeEnum.ofName(query.getStartType());
        if (startType == null) {
            throw exception(TRACE_START_TYPE_INVALID, query.getStartType());
        }
        return switch (startType) {
            case LOT -> fromLot(query.getStartValue());
            case PROD_ORDER -> fromProdOrder(query.getStartValue());
            case SALES_OUT -> fromSalesOut(query.getStartValue());
        };
    }

    // ========== 三种起点：都归一到"成品批次 + 完工收货流水 + 生产工单"这条主干 ==========

    /**
     * 成品批次起点。纯数字优先按批次编号走批次主档，否则按批次号反查。
     *
     * <p>按批次号拿到的是<b>列表</b>：{@code wms_batch} 唯一索引是 {@code (mat_id, lot_no)}，
     * 同一批次号合法地属于多个物料。此时把每个物料的批次都当起点各查一棵树，多棵树包虚拟根，
     * 不做"取第一条"的猜测。
     */
    private MesTraceNodeBO fromLot(String startValue) {
        List<BatchRespDTO> startBatches = new ArrayList<>();
        if (StrUtil.isNumeric(startValue)) {
            BatchRespDTO batch = batchApi.getBatch(Long.parseLong(startValue));
            if (batch != null) {
                startBatches.add(batch);
            }
        }
        if (startBatches.isEmpty()) {
            batchApi.getBatchMapByLotNos(List.of(startValue)).values().forEach(startBatches::addAll);
        }
        List<FinishSeed> seeds = startBatches.stream().map(FinishSeed::ofBatch)
                .filter(seed -> seed.batchId() != null).toList();
        if (seeds.isEmpty()) {
            throw exception(TRACE_START_NOT_FOUND, "成品批次", startValue);
        }
        return buildBackwardTree(chainFromSeeds(seeds, startBatches));
    }

    /**
     * 工单号起点：工单本身即锚点，向下取它的完工入库单与成品批次，再反查原料与供应商。
     * 工单尚未完工入库时没有成品批次，树根退化为工单节点，原料批次与质检结论照样反查得出来。
     */
    private MesTraceNodeBO fromProdOrder(String orderNo) {
        ProdOrderDO order = prodOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw exception(TRACE_START_NOT_FOUND, "生产工单", orderNo);
        }
        List<ProdOrderDO> orders = List.of(order);
        List<WipTxnDO> wipTxns = wipTxnsOf(orders);
        Map<Long, InOrderRespDTO> inOrderById = inOrderMap(inOrderIds(wipTxns));
        Set<String> inNos = inOrderNos(wipTxns, inOrderById);
        if (inNos.isEmpty()) {
            return buildBackwardTree(new Chain(List.of(), List.of(), orders, wipTxns, inOrderById, Map.of()));
        }
        // 入库单名下的完工收货流水：入库单不能按单号查批次，按工单物料查一次流水再筛单号
        List<InventoryTxnRespDTO> receipts = listTxns(query -> {
            query.setMatId(order.getMatId());
            query.setBizType(WmsInBizTypeEnum.PROD_FINISH_IN.name());
            query.setTxnType(WmsTxnTypeEnum.RECEIPT.name());
        }).stream().filter(txn -> inNos.contains(txn.getBizNo())).toList();
        return buildBackwardTree(new Chain(
                dedupSeeds(receipts.stream().map(FinishSeed::ofTxn).toList()),
                receipts, orders, wipTxns, inOrderById, Map.of()));
    }

    /**
     * 销售出库单号起点：发货流水给出成品批次，之后与成品批次起点同路。
     *
     * <p>反查方向的终点是供应商，销售出库单只是成品批次的一个下游来源，这里用它定位批次，
     * 不在树上挂销售出库节点。
     */
    private MesTraceNodeBO fromSalesOut(String outNo) {
        List<InventoryTxnRespDTO> deliveryTxns = listTxns(query -> {
            query.setBizNo(outNo);
            query.setBizType(WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name());
            query.setTxnType(WmsTxnTypeEnum.ISSUE.name());
        });
        List<FinishSeed> seeds = dedupSeeds(deliveryTxns.stream().map(FinishSeed::ofTxn)
                .filter(seed -> seed.batchId() != null).toList());
        if (seeds.isEmpty()) {
            throw exception(TRACE_START_NOT_FOUND, "销售出库单的成品批次", outNo);
        }
        return buildBackwardTree(chainFromSeeds(seeds, List.of()));
    }

    /**
     * 成品批次 → 生产工单：完工收货流水给出入库单号，再用 {@code wip_txn(IN_ORDER)} 桥换回工单。
     *
     * <p>候选工单按流水上的物料编号一次批量查回（同物料的历史工单会一起回来），再用"该工单的
     * 入库单桥 ∩ 本次成品批次的入库单号"与"入库单 {@code source_biz_no} = 工单号"双重匹配收窄。
     * 流水与桥都只查一次，收窄在内存里做，不许在过滤里逐工单回查。
     */
    private Chain chainFromSeeds(List<FinishSeed> seeds, List<BatchRespDTO> startBatches) {
        List<Long> batchIds = seeds.stream().map(FinishSeed::batchId).filter(Objects::nonNull).toList();
        List<String> lotNos = seeds.stream().map(FinishSeed::lotNo).filter(StrUtil::isNotBlank)
                .distinct().toList();
        List<InventoryTxnRespDTO> receipts = listTxns(query -> {
            query.setBizType(WmsInBizTypeEnum.PROD_FINISH_IN.name());
            query.setTxnType(WmsTxnTypeEnum.RECEIPT.name());
            if (CollUtil.isNotEmpty(batchIds)) {
                // 批次编号全局唯一，拿得到就按它把过滤下推到 SQL
                query.setBatchIds(batchIds);
            } else {
                // 只有批次号时按 lot_no 下推，跨物料撞号靠种子上的物料编号在内存里甄别
                query.setLotNos(lotNos);
            }
        }).stream().filter(txn -> seeds.stream().anyMatch(seed -> seed.matches(txn))).toList();
        if (CollUtil.isEmpty(receipts)) {
            throw exception(TRACE_START_NOT_FOUND, "成品批次的完工入库", String.join(",", lotNos));
        }
        // 起点按批次号反查时可能带回跨物料撞号的批次：没有完工入库流水的那几个不是本次成品批次，剔除
        List<FinishSeed> matchedSeeds = seeds.stream()
                .filter(seed -> receipts.stream().anyMatch(seed::matches)).toList();
        Set<String> finishInNos = receipts.stream().map(InventoryTxnRespDTO::getBizNo)
                .filter(StrUtil::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
        List<ProdOrderDO> candidates = candidateOrders(receipts);
        List<WipTxnDO> wipTxns = wipTxnsOf(candidates);
        Map<Long, InOrderRespDTO> inOrderById = inOrderMap(inOrderIds(wipTxns));
        Map<Long, List<WipTxnDO>> wipTxnsByOrderId = wipTxns.stream()
                .filter(txn -> txn.getProdOrderId() != null)
                .collect(Collectors.groupingBy(WipTxnDO::getProdOrderId));
        List<ProdOrderDO> orders = candidates.stream().filter(order ->
                // 双保险：该工单的入库单桥里存在一张单号命中本次成品批次的入库单，且该入库单的
                // 来源业务单号就是这张工单——同物料多张工单时单靠单号交集会串行
                inOrderBridges(wipTxnsByOrderId.getOrDefault(order.getId(), List.of()), inOrderById).stream()
                        .anyMatch(dto -> finishInNos.contains(dto.getInNo())
                                && order.getOrderNo().equals(dto.getSourceBizNo()))).toList();
        if (CollUtil.isEmpty(orders)) {
            throw exception(TRACE_START_NOT_FOUND, "成品批次的完工入库单", String.join(",", finishInNos));
        }
        Set<Long> matchedBatchIds = matchedSeeds.stream().map(FinishSeed::batchId)
                .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, BatchRespDTO> knownBatches = startBatches.stream()
                .filter(batch -> matchedBatchIds.contains(batch.getId()))
                .collect(Collectors.toMap(BatchRespDTO::getId, batch -> batch, (a, b) -> a));
        return new Chain(matchedSeeds, receipts, orders, wipTxns, inOrderById, knownBatches);
    }

    /** 候选工单：完工收货流水上的物料编号一次批量查工单，收窄交给调用方。 */
    private List<ProdOrderDO> candidateOrders(List<InventoryTxnRespDTO> receipts) {
        Set<Long> matIds = receipts.stream().map(InventoryTxnRespDTO::getMatId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        return matIds.isEmpty() ? List.of() : prodOrderMapper.selectList(ProdOrderDO::getMatId, matIds);
    }

    // ========== 主链路 ==========

    /** 从已解析的主干构建反查树：成品批次为根，原料批次与供应商为叶。 */
    private MesTraceNodeBO buildBackwardTree(Chain chain) {
        BackwardData data = prefetch(chain);
        Set<String> visited = new LinkedHashSet<>();
        List<MesTraceNodeBO> roots = new ArrayList<>();
        for (FinishSeed seed : chain.finishSeeds()) {
            MesTraceNodeBO node = buildFinishBatchNode(seed, data, visited);
            if (node != null) {
                roots.add(node);
            }
        }
        if (roots.isEmpty()) {
            // 成品批次与起点层级拿不到（例如工单尚未入库）时，树根就是工单本身，反查不许空手而归
            roots = data.orderByNo().values().stream()
                    .map(order -> buildOrderNode(order, data, 1, visited))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return singleRoot(roots);
    }

    /** 多根时包一个虚拟根保持返回值是单节点；单根直接返回。 */
    private MesTraceNodeBO singleRoot(List<MesTraceNodeBO> roots) {
        if (roots.size() == 1) {
            return roots.get(0);
        }
        if (CollUtil.isEmpty(roots)) {
            throw exception(TRACE_START_NOT_FOUND, "追溯链路", "");
        }
        MesTraceNodeBO root = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.BATCH.name())
                .setBizNo("MULTI")
                .setNodeName("多批次起点")
                .setDepth(0)
                .setChildren(roots);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("lotNos", roots.stream().map(MesTraceNodeBO::getBizNo).toList());
        return root.setSnapshot(snapshot);
    }

    // ========== 树节点组装（自成品批次向原料方向） ==========

    /** 成品批次节点（根）：挂它入库到的各张完工入库单。 */
    private MesTraceNodeBO buildFinishBatchNode(FinishSeed seed, BackwardData data, Set<String> visited) {
        // 成品批次是反查树的根，层级固定为 1，不作为参数传来传去
        int depth = 1;
        if (!visited.add(seed.identityKey())) {
            return null;
        }
        BatchRespDTO batch = seed.batchId() == null ? null : data.batchById().get(seed.batchId());
        List<InventoryTxnRespDTO> receipts = data.finishReceiptsByBatchKey()
                .getOrDefault(seed.identityKey(), List.of());
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.BATCH.name())
                .setBizNo(seed.lotNo())
                .setNodeName(StrUtil.blankToDefault(seed.matName(), batch == null ? null : batch.getMatName()))
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("batchId", seed.batchId());
        snapshot.put("matId", seed.matId());
        snapshot.put("matCode", seed.matCode());
        snapshot.put("matName", seed.matName());
        snapshot.put("lotNo", seed.lotNo());
        putBatchMaster(snapshot, batch);
        snapshot.put("qty", sumQty(receipts));
        snapshot.put("txnTime", maxTime(receipts));
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        for (InOrderRespDTO inOrder : distinctInOrders(receipts, data)) {
            MesTraceNodeBO inNode = buildInOrderNode(inOrder, receipts, data, depth + 1, visited);
            if (inNode != null) {
                node.getChildren().add(inNode);
            }
        }
        return node;
    }

    /** 完工入库单节点：挂它归的那张生产工单。 */
    private MesTraceNodeBO buildInOrderNode(InOrderRespDTO inOrder, List<InventoryTxnRespDTO> receipts,
                                            BackwardData data, int depth, Set<String> visited) {
        if (!visited.add("I:" + inOrder.getInNo())) {
            return null;
        }
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.IN_ORDER.name())
                .setBizNo(inOrder.getInNo())
                .setNodeName("生产完工入库")
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("inNo", inOrder.getInNo());
        snapshot.put("bizType", inOrder.getBizType());
        snapshot.put("status", inOrder.getStatus());
        snapshot.put("whId", inOrder.getWhId());
        snapshot.put("sourceBizNo", inOrder.getSourceBizNo());
        snapshot.put("qty", sumQty(receipts.stream()
                .filter(txn -> inOrder.getInNo().equals(txn.getBizNo())).toList()));
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        ProdOrderDO order = data.orderByNo().get(inOrder.getSourceBizNo());
        if (order != null) {
            node.getChildren().add(buildOrderNode(order, data, depth + 1, visited));
        }
        return node;
    }

    /**
     * 生产工单节点：挂该工单名下的各检验点质检任务、在制流水，以及领用的原料批次。
     *
     * <p>同一张工单在树里只展开一次，第二处只留工单快照。
     */
    private MesTraceNodeBO buildOrderNode(ProdOrderDO order, BackwardData data, int depth,
                                         Set<String> visited) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.PROD_ORDER.name())
                .setBizNo(order.getOrderNo())
                .setNodeName(order.getMatName())
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderNo", order.getOrderNo());
        snapshot.put("matId", order.getMatId());
        snapshot.put("matCode", order.getMatCode());
        snapshot.put("matName", order.getMatName());
        snapshot.put("qty", order.getQty());
        snapshot.put("completedQty", order.getCompletedQty());
        snapshot.put("scrapQty", order.getScrapQty());
        snapshot.put("status", order.getStatus());
        snapshot.put("kitStatus", order.getKitStatus());
        // 任务书第 2 条：工单节点带两个版本号，客户审核要看当时用的哪一版 BOM 与工艺
        snapshot.put("bomVersion", order.getBomVersion());
        snapshot.put("routeVersion", order.getRouteVersion());
        snapshot.put("actualStartTime", order.getActualStartTime());
        snapshot.put("actualEndTime", order.getActualEndTime());
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        if (!visited.add("O:" + order.getOrderNo())) {
            return node;
        }
        data.qcTasksByOrderNo().getOrDefault(order.getOrderNo(), List.of())
                .forEach(task -> node.getChildren().add(buildQcTaskNode(task, depth + 1)));
        data.wipTxnsByProdOrderId().getOrDefault(order.getId(), List.of()).stream()
                .sorted(Comparator.comparing(WipTxnDO::getTxnTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(txn -> node.getChildren().add(buildWipTxnNode(txn, depth + 1)));
        data.rawIssuesByOrderNo().getOrDefault(order.getOrderNo(), List.of()).stream()
                .map(issue -> buildRawBatchNode(issue, data, depth + 1, visited))
                .filter(Objects::nonNull).forEach(batchNode -> node.getChildren().add(batchNode));
        return node;
    }

    /** 原料批次节点：挂来料检验结论与供应商（链路终点）。 */
    private MesTraceNodeBO buildRawBatchNode(RawIssue issue, BackwardData data, int depth, Set<String> visited) {
        if (!visited.add(issue.visitedKey())) {
            return null;
        }
        BatchRespDTO batch = issue.batchId() == null ? null : data.batchById().get(issue.batchId());
        if (batch == null && StrUtil.isNotBlank(issue.lotNo())) {
            batch = data.batchByLotKey().get(issue.identityKey());
        }
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.BATCH.name())
                .setBizNo(issue.lotNo())
                .setNodeName(StrUtil.blankToDefault(issue.matName(), batch == null ? null : batch.getMatName()))
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        // 行上没存批次编号时，回填按批次号 + 物料定位到的批次编号：给用户的快照要能唯一定位批次
        snapshot.put("batchId", issue.batchId() != null ? issue.batchId()
                : (batch == null ? null : batch.getId()));
        snapshot.put("matId", issue.matId());
        snapshot.put("matCode", issue.matCode());
        snapshot.put("matName", issue.matName());
        snapshot.put("lotNo", issue.lotNo());
        putBatchMaster(snapshot, batch);
        // 领用事实：哪张领料申请的哪一行、领了多少、从哪张出库单出库
        snapshot.put("materialReqNo", issue.reqNo());
        snapshot.put("lineNo", issue.lineNo());
        snapshot.put("qty", issue.qty());
        snapshot.put("outNo", issue.outNo());
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        if (issue.batchId() != null) {
            data.qcTasksByBatchId().getOrDefault(issue.batchId(), List.of())
                    .forEach(task -> node.getChildren().add(buildQcTaskNode(task, depth + 1)));
        }
        Long supplierId = batch == null ? null : batch.getSupplierId();
        SupplierRespDTO supplier = supplierId == null ? null : data.supplierById().get(supplierId);
        if (supplier != null) {
            node.getChildren().add(buildSupplierNode(supplier, batch, depth + 1));
        }
        return node;
    }

    /** 质检任务节点：检验点、结论与锁定的检验方案版本。 */
    private MesTraceNodeBO buildQcTaskNode(QcTaskDO task, int depth) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.QC_TASK.name())
                .setBizNo(task.getTaskNo())
                .setNodeName(triggerPointName(task.getTriggerPoint()))
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("taskNo", task.getTaskNo());
        snapshot.put("triggerPoint", task.getTriggerPoint());
        snapshot.put("bizType", task.getBizType());
        snapshot.put("bizNo", task.getBizNo());
        snapshot.put("matCode", task.getMatCode());
        snapshot.put("matName", task.getMatName());
        snapshot.put("lotNo", task.getLotNo());
        snapshot.put("qcPlanId", task.getQcPlanId());
        snapshot.put("qcPlanVersion", task.getQcPlanVersion());
        snapshot.put("conclusion", task.getConclusion());
        snapshot.put("status", task.getStatus());
        snapshot.put("qty", task.getQty());
        snapshot.put("qtyQualified", task.getQtyQualified());
        snapshot.put("qtyRejected", task.getQtyRejected());
        snapshot.put("inspectStartTime", task.getInspectStartTime());
        snapshot.put("inspectEndTime", task.getInspectEndTime());
        node.setSnapshot(snapshot);
        return node;
    }

    /** 在制流水节点：工序执行轨迹，事实来自 {@code mes_wip_txn}。 */
    private MesTraceNodeBO buildWipTxnNode(WipTxnDO txn, int depth) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.WIP_TXN.name())
                .setBizNo(txn.getTxnNo())
                .setNodeName(wipTxnTypeName(txn.getTxnType()))
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("txnNo", txn.getTxnNo());
        snapshot.put("txnType", txn.getTxnType());
        snapshot.put("qty", txn.getQty());
        snapshot.put("fromStatus", txn.getFromStatus());
        snapshot.put("toStatus", txn.getToStatus());
        snapshot.put("bizType", txn.getBizType());
        snapshot.put("bizNo", txn.getBizNo());
        snapshot.put("txnTime", txn.getTxnTime());
        snapshot.put("remark", txn.getRemark());
        node.setSnapshot(snapshot);
        return node;
    }

    /** 供应商节点：反查链路终点。 */
    private MesTraceNodeBO buildSupplierNode(SupplierRespDTO supplier, BatchRespDTO batch, int depth) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.SUPPLIER.name())
                .setBizNo(supplier.getCode())
                .setNodeName(supplier.getName())
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("supplierId", supplier.getId());
        snapshot.put("supplierCode", supplier.getCode());
        snapshot.put("supplierName", supplier.getName());
        snapshot.put("status", supplier.getStatus());
        // 供应商批次号与炉批号存在批次主档上，跟着供应商一起交给审核方
        snapshot.put("lotNo", batch.getLotNo());
        snapshot.put("supplierLotNo", batch.getSupplierLotNo());
        snapshot.put("furnaceLotNo", batch.getFurnaceLotNo());
        node.setSnapshot(snapshot);
        return node;
    }

    // ========== 批量预取 ==========

    /** 一次反查的批量预取结果，树组装只读这份数据，不再发起任何查询。 */
    private record BackwardData(
            Map<String, ProdOrderDO> orderByNo,
            Map<String, List<QcTaskDO>> qcTasksByOrderNo,
            Map<Long, List<QcTaskDO>> qcTasksByBatchId,
            Map<Long, List<WipTxnDO>> wipTxnsByProdOrderId,
            Map<String, List<RawIssue>> rawIssuesByOrderNo,
            Map<String, List<InventoryTxnRespDTO>> finishReceiptsByBatchKey,
            Map<String, InOrderRespDTO> inOrderByInNo,
            Map<Long, BatchRespDTO> batchById,
            Map<String, BatchRespDTO> batchByLotKey,
            Map<Long, SupplierRespDTO> supplierById) {
    }

    /** 主干确定后一次批量取全树要用的数据。 */
    private BackwardData prefetch(Chain chain) {
        List<ProdOrderDO> orders = chain.orders();
        Map<String, ProdOrderDO> orderByNo = orders.stream()
                .filter(order -> StrUtil.isNotBlank(order.getOrderNo()))
                .collect(Collectors.toMap(ProdOrderDO::getOrderNo, order -> order, (a, b) -> a));
        List<String> orderNos = new ArrayList<>(orderByNo.keySet());
        // 领料申请与领料行：工单号一次批量查申请，再按申请编号一次批量查行
        Map<Long, MaterialReqDO> reqById = orderNos.isEmpty() ? Map.of()
                : materialReqMapper.selectList(MaterialReqDO::getProdOrderNo, orderNos).stream()
                .filter(req -> !MesMaterialReqTypeEnum.isReturn(req.getReqType()))
                .collect(Collectors.toMap(MaterialReqDO::getId, req -> req, (a, b) -> a));
        List<MaterialReqLineDO> lines = reqById.isEmpty() ? List.of()
                : materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, reqById.keySet());
        // 出库单：申请上的 WMS 出库单编号一次批量换回单号
        Map<Long, OutOrderRespDTO> outOrderById = outOrderMap(reqById.values().stream()
                .map(req -> parseId(req.getWmsBizNo())).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        List<RawIssue> rawIssues = rawIssuesOf(lines, reqById, outOrderById);
        // 批次主档：原料批次 + 成品批次合并一次批量（起点已取回的批次不重复要）
        Set<Long> batchIds = new LinkedHashSet<>();
        rawIssues.forEach(issue -> {
            if (issue.batchId() != null) {
                batchIds.add(issue.batchId());
            }
        });
        chain.finishSeeds().forEach(seed -> {
            if (seed.batchId() != null) {
                batchIds.add(seed.batchId());
            }
        });
        batchIds.removeAll(chain.knownBatches().keySet());
        Map<Long, BatchRespDTO> batchById = new LinkedHashMap<>(chain.knownBatches());
        batchMap(batchIds).forEach(batchById::putIfAbsent);
        // 行上批次编号为 0 的领料行只能按批次号取主档；批次号跨物料不唯一，靠物料编号定位那一条
        List<String> lotNosOnly = rawIssues.stream()
                .filter(issue -> issue.batchId() == null && StrUtil.isNotBlank(issue.lotNo()))
                .map(RawIssue::lotNo).distinct().toList();
        Map<String, List<BatchRespDTO>> batchesByLotNo = CollUtil.isEmpty(lotNosOnly) ? Map.of()
                : batchApi.getBatchMapByLotNos(lotNosOnly);
        Map<String, BatchRespDTO> batchByLotKey = new LinkedHashMap<>();
        rawIssues.stream()
                .filter(issue -> issue.batchId() == null && StrUtil.isNotBlank(issue.lotNo()))
                .forEach(issue -> batchesByLotNo.getOrDefault(issue.lotNo(), List.of()).stream()
                        .filter(batch -> Objects.equals(issue.matId(), batch.getMatId()))
                        .findFirst().ifPresent(batch -> batchByLotKey.putIfAbsent(issue.identityKey(), batch)));
        // 供应商：批次主档上的 supplier_id 去重后一次批量
        Set<Long> supplierIds = new LinkedHashSet<>();
        batchById.values().stream().map(BatchRespDTO::getSupplierId)
                .filter(Objects::nonNull).forEach(supplierIds::add);
        batchByLotKey.values().stream().map(BatchRespDTO::getSupplierId)
                .filter(Objects::nonNull).forEach(supplierIds::add);
        Map<Long, SupplierRespDTO> supplierById = supplierMap(supplierIds);
        // 各检验点质检任务：候选 biz_no 要覆盖工单号（FQC）、工序任务号（FAI）、报工单号（IPQC），
        // 三者都批量取回后建"候选单号 → 归属工单号"反向索引
        Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId = workOrdersByProdOrderId(orders);
        Map<Long, List<OpTaskDO>> opTasksByWorkOrderId = opTasksByWorkOrderId(workOrdersByProdOrderId);
        Map<String, List<QcTaskDO>> qcTasksByOrderNo = qcTasksByOrderNo(
                qcTaskIndex(orders, workOrdersByProdOrderId, opTasksByWorkOrderId));
        Set<Long> rawBatchIds = rawIssues.stream().map(RawIssue::batchId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<QcTaskDO>> qcTasksByBatchId = qcTasksByBatchId(rawBatchIds);
        // 完工收货流水按成品批次身份归组；入库单按单号建索引供树组装取
        Map<String, List<InventoryTxnRespDTO>> receiptsByBatchKey = new LinkedHashMap<>();
        chain.finishReceipts().forEach(txn -> receiptsByBatchKey
                .computeIfAbsent(batchKey(txn.getBatchId(), txn.getLotNo()), key -> new ArrayList<>()).add(txn));
        Map<String, InOrderRespDTO> inOrderByInNo = new LinkedHashMap<>();
        chain.inOrderById().values().forEach(dto -> {
            if (dto != null && StrUtil.isNotBlank(dto.getInNo())) {
                inOrderByInNo.putIfAbsent(dto.getInNo(), dto);
            }
        });
        Map<Long, List<WipTxnDO>> wipTxnsByProdOrderId = chain.wipTxns().stream()
                .filter(txn -> txn.getProdOrderId() != null)
                .collect(Collectors.groupingBy(WipTxnDO::getProdOrderId));
        return new BackwardData(orderByNo, qcTasksByOrderNo, qcTasksByBatchId, wipTxnsByProdOrderId,
                rawIssues.stream().collect(Collectors.groupingBy(RawIssue::orderNo, LinkedHashMap::new,
                        Collectors.toList())),
                receiptsByBatchKey, inOrderByInNo, batchById, batchByLotKey, supplierById);
    }

    /** 领料行转原料批次，带上工单号、领料申请号、出库单号与数量；同一工单内按批次身份去重、数量累加。 */
    private List<RawIssue> rawIssuesOf(List<MaterialReqLineDO> lines, Map<Long, MaterialReqDO> reqById,
                                       Map<Long, OutOrderRespDTO> outOrderById) {
        Map<String, RawIssue> issues = new LinkedHashMap<>();
        for (MaterialReqLineDO line : lines) {
            MaterialReqDO req = reqById.get(line.getReqId());
            if (req == null || StrUtil.isBlank(req.getProdOrderNo()) || StrUtil.isBlank(line.getLotNo())) {
                continue;
            }
            Long batchId = line.getBatchId() == null || line.getBatchId() <= 0 ? null : line.getBatchId();
            RawIssue current = new RawIssue(req.getProdOrderNo(), batchId, line.getMatId(), line.getLotNo(),
                    line.getMatCode(), line.getMatName(), req.getReqNo(), line.getLineNo(), line.getQty(),
                    outNoOf(req, outOrderById));
            // 合并键与批次身份键同源：同一工单里同一批次只出一个原料节点，数量累加
            issues.merge(current.visitedKey(), current, (kept, next) -> new RawIssue(kept.orderNo(),
                    kept.batchId() != null ? kept.batchId() : next.batchId(), kept.matId(), kept.lotNo(),
                    kept.matCode(), kept.matName(), kept.reqNo(), kept.lineNo(),
                    nvl(kept.qty()).add(nvl(next.qty())), StrUtil.blankToDefault(kept.outNo(), next.outNo())));
        }
        return new ArrayList<>(issues.values());
    }

    private String outNoOf(MaterialReqDO req, Map<Long, OutOrderRespDTO> outOrderById) {
        Long outOrderId = parseId(req.getWmsBizNo());
        OutOrderRespDTO outOrder = outOrderId == null ? null : outOrderById.get(outOrderId);
        return outOrder == null ? null : outOrder.getOutNo();
    }

    /**
     * 工单名下的各检验点质检任务：一次 {@code biz_no IN} 批量查回，再按 {@code (bizType, bizNo)}
     * 配对收窄，最后按"归属工单号"归组。
     *
     * <p>配对不可省：IQC 的 {@code biz_no} 是到货单号、OQC 的是出库单号，都不在候选集合里；
     * 而工单号、工序任务号、报工单号同库同表，只按 {@code biz_no} 收会把无关检验点的结论挂错位置。
     *
     * <p>归组键必须是工单号：FAI 挂在工序任务上（{@code bizType = OP_TASK}）、IPQC 挂在报工单上
     * （{@code bizType = PROD_REPORT}），它们的 {@code biz_no} 不是工单号，直接拿 {@code biz_no}
     * 当键这两类结论在树里永远取不出来。{@code ownerByBizNo} 就是候选单号到归属工单号的反向索引。
     */
    private Map<String, List<QcTaskDO>> qcTasksByOrderNo(QcIndex index) {
        if (index.ownerByBizNo().isEmpty()) {
            return Map.of();
        }
        List<QcTaskDO> tasks = qcTaskMapper.selectList(new LambdaQueryWrapper<QcTaskDO>()
                .in(QcTaskDO::getBizNo, index.ownerByBizNo().keySet()));
        return tasks.stream()
                .filter(task -> StrUtil.isNotBlank(task.getBizType()))
                .filter(task -> Objects.equals(index.bizTypeByBizNo().get(task.getBizNo()), task.getBizType()))
                .filter(task -> index.ownerByBizNo().containsKey(task.getBizNo()))
                .collect(Collectors.groupingBy(task -> index.ownerByBizNo().get(task.getBizNo()),
                        LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 建"候选 {@code biz_no} → 归属工单号"与"候选 {@code biz_no} → 业务类型"两张反向索引。
     *
     * <p>三类候选单号一次批量备齐，之后质检任务只查一次 SQL：
     * <ul>
     *   <li>工单号：FQC 任务的 {@code bizType = PROD_ORDER}、{@code bizNo =} 工单号，归属自己；</li>
     *   <li>工序任务号：FAI 任务的 {@code bizType = OP_TASK}、{@code bizNo =} 工序任务号，
     *       归属该任务所在作业工单对应的生产工单；</li>
     *   <li>报工单号：IPQC 任务的 {@code bizType = PROD_REPORT}、{@code bizNo =} 报工单号，
     *       归属报工单上的 {@code prod_order_id} 对应的生产工单。</li>
     * </ul>
     */
    private QcIndex qcTaskIndex(List<ProdOrderDO> orders,
                                Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId,
                                Map<Long, List<OpTaskDO>> opTasksByWorkOrderId) {
        Map<String, String> ownerByBizNo = new LinkedHashMap<>();
        Map<String, String> bizTypeByBizNo = new LinkedHashMap<>();
        Map<Long, String> orderNoById = new LinkedHashMap<>();
        orders.forEach(order -> {
            if (order.getId() == null || StrUtil.isBlank(order.getOrderNo())) {
                return;
            }
            orderNoById.put(order.getId(), order.getOrderNo());
            ownerByBizNo.put(order.getOrderNo(), order.getOrderNo());
            bizTypeByBizNo.put(order.getOrderNo(), MesBizTypeEnum.PROD_ORDER.name());
        });
        workOrdersByProdOrderId.forEach((prodOrderId, workOrders) -> {
            String ownerOrderNo = orderNoById.get(prodOrderId);
            if (ownerOrderNo == null) {
                return;
            }
            workOrders.stream().map(WorkOrderDO::getId)
                    .map(workOrderId -> opTasksByWorkOrderId.getOrDefault(workOrderId, List.of()))
                    .flatMap(List::stream).map(OpTaskDO::getTaskNo).filter(StrUtil::isNotBlank)
                    .forEach(taskNo -> {
                        ownerByBizNo.putIfAbsent(taskNo, ownerOrderNo);
                        bizTypeByBizNo.putIfAbsent(taskNo, MesBizTypeEnum.OP_TASK.name());
                    });
        });
        List<Long> prodOrderIds = orders.stream().map(ProdOrderDO::getId).filter(Objects::nonNull).toList();
        if (!prodOrderIds.isEmpty()) {
            prodReportMapper.selectList(ProdReportDO::getProdOrderId, prodOrderIds).stream()
                    .filter(report -> StrUtil.isNotBlank(report.getReportNo()))
                    .forEach(report -> {
                        String ownerOrderNo = orderNoById.get(report.getProdOrderId());
                        if (ownerOrderNo == null) {
                            return;
                        }
                        ownerByBizNo.putIfAbsent(report.getReportNo(), ownerOrderNo);
                        bizTypeByBizNo.putIfAbsent(report.getReportNo(), MesBizTypeEnum.PROD_REPORT.name());
                    });
        }
        return new QcIndex(ownerByBizNo, bizTypeByBizNo);
    }

    /** 原料批次的来料检验任务：按批次编号一次批量查，结论挂在批次节点上。 */
    private Map<Long, List<QcTaskDO>> qcTasksByBatchId(Set<Long> batchIds) {
        if (CollUtil.isEmpty(batchIds)) {
            return Map.of();
        }
        return qcTaskMapper.selectList(new LambdaQueryWrapper<QcTaskDO>().in(QcTaskDO::getBatchId, batchIds))
                .stream().filter(task -> task.getBatchId() != null)
                .collect(Collectors.groupingBy(QcTaskDO::getBatchId, LinkedHashMap::new, Collectors.toList()));
    }

    // ========== 门面批量调用（每个方法至多一次） ==========

    private Map<Long, InOrderRespDTO> inOrderMap(Set<Long> inOrderIds) {
        if (CollUtil.isEmpty(inOrderIds)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(inOrderApi.getInOrderMap(inOrderIds), Map.of());
    }

    private Map<Long, OutOrderRespDTO> outOrderMap(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(outOrderApi.getOutOrderMap(ids), Map.of());
    }

    private Map<Long, SupplierRespDTO> supplierMap(Set<Long> supplierIds) {
        if (CollUtil.isEmpty(supplierIds)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(partnerApi.getSupplierMap(supplierIds), Map.of());
    }

    /** 批次主档批量取回；集合为空时不发调用，避免空跑一次 SQL。 */
    private Map<Long, BatchRespDTO> batchMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(batchApi.getBatchMap(ids), Map.of());
    }

    // ========== 流水与 MES 批量查询 ==========

    private List<InventoryTxnRespDTO> listTxns(Consumer<InventoryTxnQueryReqDTO> customizer) {
        InventoryTxnQueryReqDTO query = new InventoryTxnQueryReqDTO();
        customizer.accept(query);
        return ObjUtil.defaultIfNull(inventoryApi.getInventoryTxnList(query), List.of());
    }

    private List<WipTxnDO> wipTxnsOf(List<ProdOrderDO> orders) {
        List<Long> ids = orders.stream().map(ProdOrderDO::getId).filter(Objects::nonNull).toList();
        return ids.isEmpty() ? List.of() : wipTxnMapper.selectList(WipTxnDO::getProdOrderId, ids);
    }

    private Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId(List<ProdOrderDO> orders) {
        List<Long> ids = orders.stream().map(ProdOrderDO::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return workOrderMapper.selectListByProdOrderIds(ids).stream()
                .filter(workOrder -> workOrder.getProdOrderId() != null)
                .collect(Collectors.groupingBy(WorkOrderDO::getProdOrderId));
    }

    private Map<Long, List<OpTaskDO>> opTasksByWorkOrderId(Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId) {
        List<Long> workOrderIds = workOrdersByProdOrderId.values().stream().flatMap(List::stream)
                .map(WorkOrderDO::getId).filter(Objects::nonNull).toList();
        if (workOrderIds.isEmpty()) {
            return Map.of();
        }
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .filter(task -> task.getWorkOrderId() != null)
                .collect(Collectors.groupingBy(OpTaskDO::getWorkOrderId));
    }

    /** 在制流水里 IN_ORDER 桥的入库单编号集合（biz_no 存的是入库单编号）。 */
    private Set<Long> inOrderIds(List<WipTxnDO> wipTxns) {
        return wipTxns.stream()
                .filter(txn -> MesBizTypeEnum.IN_ORDER.name().equals(txn.getBizType()))
                .map(WipTxnDO::getBizNo).filter(MesTraceBackwardResolver::isNumeric)
                .map(Long::parseLong).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** 一批在制流水桥到的入库单号集合。 */
    private Set<String> inOrderNos(List<WipTxnDO> wipTxns, Map<Long, InOrderRespDTO> inOrderById) {
        return inOrderBridges(wipTxns, inOrderById).stream()
                .map(InOrderRespDTO::getInNo).filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** 一批在制流水桥到的入库单事实（去重，保持流水出现顺序）。 */
    private List<InOrderRespDTO> inOrderBridges(List<WipTxnDO> wipTxns, Map<Long, InOrderRespDTO> inOrderById) {
        Map<Long, InOrderRespDTO> result = new LinkedHashMap<>();
        wipTxns.stream().filter(txn -> MesBizTypeEnum.IN_ORDER.name().equals(txn.getBizType()))
                .map(txn -> parseId(txn.getBizNo())).filter(Objects::nonNull)
                .map(inOrderById::get).filter(Objects::nonNull)
                .forEach(dto -> result.putIfAbsent(dto.getId(), dto));
        return new ArrayList<>(result.values());
    }

    /** 成品批次名下的完工入库单，按流水出现的先后去重。 */
    private List<InOrderRespDTO> distinctInOrders(List<InventoryTxnRespDTO> receipts, BackwardData data) {
        Map<String, InOrderRespDTO> result = new LinkedHashMap<>();
        receipts.stream().map(InventoryTxnRespDTO::getBizNo).filter(StrUtil::isNotBlank)
                .forEach(inNo -> {
                    InOrderRespDTO inOrder = data.inOrderByInNo().get(inNo);
                    if (inOrder != null) {
                        result.putIfAbsent(inNo, inOrder);
                    }
                });
        return new ArrayList<>(result.values());
    }

    // ========== 辅助 ==========

    private void putBatchMaster(Map<String, Object> snapshot, BatchRespDTO batch) {
        if (batch == null) {
            return;
        }
        snapshot.put("supplierId", batch.getSupplierId());
        snapshot.put("supplierCode", batch.getSupplierCode());
        snapshot.put("supplierLotNo", batch.getSupplierLotNo());
        snapshot.put("furnaceLotNo", batch.getFurnaceLotNo());
        snapshot.put("produceDate", batch.getProduceDate());
        snapshot.put("expiryDate", batch.getExpiryDate());
    }

    private String triggerPointName(String triggerPoint) {
        for (MesQcTriggerPointEnum point : MesQcTriggerPointEnum.values()) {
            if (point.name().equals(triggerPoint)) {
                return point.getName();
            }
        }
        return null;
    }

    private String wipTxnTypeName(String txnType) {
        for (MesWipTxnTypeEnum type : MesWipTxnTypeEnum.values()) {
            if (type.name().equals(txnType)) {
                return type.getName();
            }
        }
        return null;
    }

    private BigDecimal sumQty(List<InventoryTxnRespDTO> txns) {
        return txns.stream().map(InventoryTxnRespDTO::getQty)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDateTime maxTime(List<InventoryTxnRespDTO> txns) {
        return txns.stream().map(InventoryTxnRespDTO::getCreateTime)
                .filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
    }

    private BigDecimal nvl(BigDecimal qty) {
        return qty == null ? BigDecimal.ZERO : qty;
    }

    /** 批次身份键：有批次编号用编号，否则退回批次号。种子与流水行都用它归组。 */
    private static String batchKey(Long batchId, String lotNo) {
        return batchId != null ? KEY_BATCH + batchId : KEY_LOT + lotNo;
    }

    /** 单据编号字符串转 Long，空或非数字返回 null。 */
    private Long parseId(String id) {
        return isNumeric(id) ? Long.parseLong(id) : null;
    }

    private static boolean isNumeric(String value) {
        return StrUtil.isNotBlank(value) && value.chars().allMatch(Character::isDigit);
    }

    /** 多来源种子去重：同批次身份只留一条。 */
    private List<FinishSeed> dedupSeeds(List<FinishSeed> seeds) {
        Map<String, FinishSeed> merged = new LinkedHashMap<>();
        seeds.forEach(seed -> merged.putIfAbsent(seed.identityKey(), seed));
        return new ArrayList<>(merged.values());
    }

    // ========== 内部数据结构 ==========

    /** 主干：入口解析一次成型的六件事——成品批次种子、完工收货流水、生产工单、在制流水、
     * 入库单、起点已知的批次主档。
     *
     * <p>三种起点各走各的路径拿到同一份结构，核心组装不再区分起点，也不再回查这些事实。
     */
    private record Chain(List<FinishSeed> finishSeeds,
                         List<InventoryTxnRespDTO> finishReceipts,
                         List<ProdOrderDO> orders,
                         List<WipTxnDO> wipTxns,
                         Map<Long, InOrderRespDTO> inOrderById,
                         Map<Long, BatchRespDTO> knownBatches) {
    }

    /** 质检任务索引：候选 {@code biz_no} → 归属工单号，以及候选 {@code biz_no} → 该单号的业务类型。 */
    private record QcIndex(Map<String, String> ownerByBizNo, Map<String, String> bizTypeByBizNo) {
    }

    /** 成品批次种子：反查链路的根。 */
    private record FinishSeed(Long batchId, Long matId, String matCode, String matName, String lotNo) {

        static FinishSeed ofBatch(BatchRespDTO batch) {
            return new FinishSeed(batch.getId(), batch.getMatId(), batch.getMatCode(),
                    batch.getMatName(), batch.getLotNo());
        }

        static FinishSeed ofTxn(InventoryTxnRespDTO txn) {
            return new FinishSeed(txn.getBatchId(), txn.getMatId(), txn.getMatCode(),
                    txn.getMatName(), txn.getLotNo());
        }

        String identityKey() {
            return batchKey(batchId, lotNo);
        }

        /** 流水行是否就是这个成品批次的收货：批次编号优先，否则批次号 + 物料编号。 */
        boolean matches(InventoryTxnRespDTO txn) {
            if (batchId != null) {
                return batchId.equals(txn.getBatchId());
            }
            return lotNo != null && lotNo.equals(txn.getLotNo())
                    && (matId == null || matId.equals(txn.getMatId()));
        }
    }

    /** 原料批次领用事实：批次 + 工单 + 领料申请 + 出库单 + 数量。 */
    private record RawIssue(String orderNo, Long batchId, Long matId, String lotNo,
                           String matCode, String matName, String reqNo, Integer lineNo,
                           BigDecimal qty, String outNo) {

        /** 批次身份键：批次编号全局唯一，行上没有编号时退回"物料 + 批次号"（批次号仅在物料内唯一）。 */
        String identityKey() {
            return batchId != null ? KEY_BATCH + batchId : KEY_MAT_LOT + matId + ":" + lotNo;
        }

        /** 防环键：按"工单 + 批次"去重，同一批次被两张工单领用时两边都要挂出来。 */
        String visitedKey() {
            return orderNo + "|" + identityKey();
        }
    }

    /** 测试用：收紧深度上限验证截断行为。 */
    void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

}
