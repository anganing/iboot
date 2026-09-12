package com.iboot.pf.module.mes.service.trace;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
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
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesTraceNodeTypeEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TRACE_START_NOT_FOUND;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TRACE_START_TYPE_INVALID;

/**
 * 批次正向追溯解析器。
 *
 * <p>树形：原料批次 → 领料出库单 → 生产工单 →（工序任务 + 完工入库单）→ 成品批次 → 销售出库单 →（客户）。
 * 三种起点（批次号 / 生产工单号 / 销售出库单号）最终都归一到这条主链路；从中间切入时先定位链路上的
 * 锚点，再向上游补原料批次、向下游补出货，得到同一棵完整树。一次调用返回全部层级，前端不许逐层调接口。
 *
 * <p><b>链路桥接的落库口径</b>（08 文档五张事实表 + 逐处核实的写入方）：
 *
 * <ul>
 *   <li>原料批次 → 领料申请 → 生产工单：{@code mes_material_req_line}（{@code batch_id} /
 *       {@code lot_no} 快照，08 文档指定它承载"哪个工单领了哪个批次"）→
 *       {@code mes_material_req.prod_order_no} 工单号快照。退料申请方向相反，不进正向链路。</li>
 *   <li>领料申请 → WMS 出库单：{@code mes_material_req.wms_biz_no} 存的是 WMS 出库单<b>编号</b>
 *       而非单号（T1-11 口径），经 {@code OutOrderApi.getOutOrderMap} 换回单号。</li>
 *   <li>工单 → 完工入库单：{@code mes_wip_txn(biz_type = IN_ORDER)} 的 {@code biz_no} 存 WMS 入库单
 *       <b>编号</b>，qty 记本次入库量；经 {@code InOrderApi.getInOrderMap} 换回单号。</li>
 *   <li>入库单 → 成品批次 / 成品批次 → 销售出库单：查 {@code wms_inv_txn}（完工收货
 *       {@code biz_type = PROD_FINISH_IN} + {@code txn_type = RECEIPT}；销售发货
 *       {@code biz_type = SALES_DELIVERY_OUT} + {@code txn_type = ISSUE}），行上带批次与批次号快照。
 *       流水查询条件已支持 {@code batchIds} / {@code lotNos} 集合下推，本解析器仍按物料分片查询后
 *       内存过滤——种子与成品批次的批次编号要等流水回来才定得出来，属链路上的先后次序而非门面缺口；
 *       分片数等于链路上的物料数，不是行数。</li>
 * </ul>
 *
 * <p><b>无 N+1</b>：MES 侧全部按 {@code IN} 批量查回后内存组装；门面侧每类数据至多一次批量调用
 * （出库单按编号、出库单按单号、入库单、批次主档、客户各一次），流水按物料分片（每物料两到三次）。
 * 全部调用次数在 {@code MesTraceServiceImplTest} 里逐方法断言。
 */
@Component
public class MesTraceForwardResolver {

    /**
     * 链路深度上限：防异常数据把树撑爆，超限截断并标注 truncated。
     *
     * <p>package-private 是为了让测试在收紧上限验证截断行为后，能把字段复位到默认值——
     * 本类是 Spring 单例，{@link #setMaxDepth} 的副作用若不复位会泄漏到同上下文里的其他用例。
     */
    static final int DEFAULT_MAX_DEPTH = 10;

    /** 批次身份键前缀：有批次编号用编号，否则退回批次号。 */
    private static final String KEY_BATCH = "B:";
    private static final String KEY_LOT = "L:";

    /** 链路深度上限，测试收紧以验证截断行为。 */
    private int maxDepth = DEFAULT_MAX_DEPTH;

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
    private WipTxnMapper wipTxnMapper;
    @Resource
    private BatchApi batchApi;
    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private OutOrderApi outOrderApi;
    @Resource
    private InOrderApi inOrderApi;
    @Resource
    private PartnerApi partnerApi;

    /** 正向追溯入口：按起点类型分发。 */
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

    // ========== 三种起点 ==========

    /**
     * 批次号起点。纯数字优先按批次编号走批次主档（拿得到供应商、炉批号等主档快照），
     * 查不到再回落按批次号从领料行反查；非数字直接按批次号反查。
     */
    private MesTraceNodeBO fromLot(String startValue) {
        List<BatchSeed> seeds;
        if (StrUtil.isNumeric(startValue)) {
            BatchRespDTO batch = batchApi.getBatch(Long.parseLong(startValue));
            if (batch != null) {
                return buildForwardTree(List.of(new BatchSeed(batch.getId(), batch.getMatId(),
                        batch.getLotNo(), batch.getMatCode(), batch.getMatName())));
            }
        }
        seeds = seedsByLotNo(startValue);
        if (CollUtil.isEmpty(seeds)) {
            throw exception(TRACE_START_NOT_FOUND, "批次", startValue);
        }
        return buildForwardTree(seeds);
    }

    /**
     * 按批次号解析种子：用 {@code mes_material_req_line.lot_no} 快照反查，不走批次主档。
     *
     * <p>原因不是门面缺方法（{@code BatchApi.getBatchMapByLotNos} 已补齐），而是<b>正向种子必须是
     * "被哪个工单领用过的哪个批次"这一事实</b>，批次主档只给得出批次自身的属性，给不出下游链路。
     * 因此还没被领用过的批次在这里解析不出种子、报起点不存在——正向语义成立（没有领用就没有下游）；
     * 真要按批次号取批次属性（供应商、炉批号），那是反向追溯 T5-02 的取数，用新补的门面方法。
     */
    private List<BatchSeed> seedsByLotNo(String lotNo) {
        return toSeeds(materialReqLineMapper.selectList(MaterialReqLineDO::getLotNo, lotNo));
    }

    /** 领料行转批次种子，按批次身份键去重；行上批次编号为 0 的用批次号作身份。 */
    private List<BatchSeed> toSeeds(List<MaterialReqLineDO> lines) {
        Map<String, BatchSeed> seeds = new LinkedHashMap<>();
        for (MaterialReqLineDO line : lines) {
            boolean hasBatchId = line.getBatchId() != null && line.getBatchId() > 0;
            String key = hasBatchId ? KEY_BATCH + line.getBatchId() : KEY_LOT + line.getLotNo();
            seeds.computeIfAbsent(key, k -> new BatchSeed(
                    hasBatchId ? line.getBatchId() : null,
                    line.getMatId(), line.getLotNo(), line.getMatCode(), line.getMatName()));
        }
        return new ArrayList<>(seeds.values());
    }

    /** 多来源种子去重：同批次号合并为一条，批次编号取首个非空。 */
    private List<BatchSeed> dedupSeeds(List<BatchSeed> seeds) {
        Map<String, BatchSeed> merged = new LinkedHashMap<>();
        for (BatchSeed seed : seeds) {
            merged.merge(seed.identityKey(), seed, (kept, next) -> kept.batchId() != null ? kept
                    : new BatchSeed(next.batchId(), kept.matId(), kept.lotNo(), kept.matCode(), kept.matName()));
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * 工单号起点：向上补原料批次（领料行解析种子后走主链路），向下补完工入库与出货。
     * 树根仍是原料批次；没有领料行（例如全部倒冲的工单）时树根就是工单本身。
     */
    private MesTraceNodeBO fromProdOrder(String orderNo) {
        ProdOrderDO order = prodOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw exception(TRACE_START_NOT_FOUND, "生产工单", orderNo);
        }
        List<BatchSeed> seeds = toSeeds(issueLinesOfOrders(List.of(orderNo)));
        if (CollUtil.isEmpty(seeds)) {
            return orderRootTree(order);
        }
        return buildForwardTree(seeds);
    }

    /**
     * 销售出库单号起点：出库流水拿成品批次 → 完工收货流水按批次反查完工入库单号 →
     * 工单完工入库流水桥换回工单 → 领料行解析原料种子 → 主链路。起点单号本身会在
     * 成品批次的销售出库子节点里再次出现，树仍然是完整的一条。
     */
    private MesTraceNodeBO fromSalesOut(String outNo) {
        List<InventoryTxnRespDTO> deliveryTxns = listTxns(query -> {
            query.setBizNo(outNo);
            query.setBizType(WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name());
            query.setTxnType(WmsTxnTypeEnum.ISSUE.name());
        });
        if (CollUtil.isEmpty(deliveryTxns)) {
            throw exception(TRACE_START_NOT_FOUND, "销售出库单", outNo);
        }
        Set<Long> finishBatchIds = deliveryTxns.stream().map(InventoryTxnRespDTO::getBatchId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> matIds = deliveryTxns.stream().map(InventoryTxnRespDTO::getMatId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 成品批次 → 完工入库单号：按物料分片查完工收货流水，内存按批次过滤
        Set<String> finishInNos = new LinkedHashSet<>();
        for (Long matId : matIds) {
            listMatTxns(matId, WmsInBizTypeEnum.PROD_FINISH_IN.name(), WmsTxnTypeEnum.RECEIPT.name())
                    .stream().filter(txn -> txn.getBatchId() != null && finishBatchIds.contains(txn.getBatchId()))
                    .map(InventoryTxnRespDTO::getBizNo).filter(StrUtil::isNotBlank).forEach(finishInNos::add);
        }
        if (CollUtil.isEmpty(finishInNos)) {
            throw exception(TRACE_START_NOT_FOUND, "销售出库单的上游完工入库", outNo);
        }
        // 完工入库单号 → 生产工单：按成品物料查工单，工单的 IN_ORDER 流水桥拿入库单编号，
        // 入库单的 source_biz_no（完工入库即工单号）与工单号双保险匹配。
        // 流水一次查回按工单分组，不许在过滤里逐工单查（N+1 红线）
        List<ProdOrderDO> orders = matIds.isEmpty() ? List.of()
                : prodOrderMapper.selectList(ProdOrderDO::getMatId, matIds);
        List<WipTxnDO> wipTxns = wipTxnsOf(orders);
        Map<Long, InOrderRespDTO> inOrderMap = inOrderMap(inOrderIds(wipTxns));
        Map<Long, List<WipTxnDO>> inTxnsByOrderId = wipTxns.stream()
                .filter(txn -> txn.getProdOrderId() != null
                        && MesBizTypeEnum.IN_ORDER.name().equals(txn.getBizType()))
                .collect(Collectors.groupingBy(WipTxnDO::getProdOrderId));
        List<ProdOrderDO> upstreamOrders = orders.stream().filter(order ->
                inTxnsByOrderId.getOrDefault(order.getId(), List.of()).stream()
                        .map(txn -> inOrderMap.get(parseId(txn.getBizNo())))
                        .anyMatch(dto -> dto != null && finishInNos.contains(dto.getInNo())
                                && order.getOrderNo().equals(dto.getSourceBizNo())))
                .toList();
        if (CollUtil.isEmpty(upstreamOrders)) {
            throw exception(TRACE_START_NOT_FOUND, "销售出库单的上游工单", outNo);
        }
        List<BatchSeed> seeds = toSeeds(issueLinesOfOrders(
                upstreamOrders.stream().map(ProdOrderDO::getOrderNo).toList()));
        if (CollUtil.isEmpty(seeds)) {
            // 上游工单没有领料行（倒冲工单）：树根是工单本身
            List<MesTraceNodeBO> roots = upstreamOrders.stream()
                    .map(order -> orderRootTree(order)).collect(Collectors.toList());
            return singleRoot(roots);
        }
        return buildForwardTree(dedupSeeds(seeds));
    }

    // ========== 主链路 ==========

    /**
     * 从原料批次种子构建完整正向链路树。
     * 全部数据先批量预取，再在内存里组装；同一批次不重复展开（防环），深度超限标注截断。
     */
    private MesTraceNodeBO buildForwardTree(List<BatchSeed> seeds) {
        List<BatchSeed> uniqueSeeds = dedupSeeds(seeds);
        TraceData data = prefetchForSeeds(uniqueSeeds);
        Set<String> visited = new LinkedHashSet<>();
        List<MesTraceNodeBO> roots = new ArrayList<>();
        for (BatchSeed seed : uniqueSeeds) {
            MesTraceNodeBO root = buildBatchNode(seed, data, 1, visited);
            if (root != null) {
                roots.add(root);
            }
        }
        return singleRoot(roots);
    }

    /** 没有领料行的工单起点：树根是工单本身，向下补工序任务、完工入库与出货。 */
    private MesTraceNodeBO orderRootTree(ProdOrderDO order) {
        TraceData data = prefetchForOrder(order);
        return buildOrderNode(order, data, 1, new LinkedHashSet<>());
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

    // ========== 树节点组装（自上而下） ==========

    /** 批次节点：挂该批次被领走的各张领料出库单。同一批次只展开一次（防环）。 */
    private MesTraceNodeBO buildBatchNode(BatchSeed seed, TraceData data, int depth, Set<String> visited) {
        if (!visited.add(seed.identityKey())) {
            return null;
        }
        BatchRespDTO batch = seed.batchId() == null ? null : data.batchById().get(seed.batchId());
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.BATCH.name())
                .setBizNo(seed.lotNo())
                .setNodeName(seed.matName())
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("batchId", seed.batchId());
        snapshot.put("matId", seed.matId());
        snapshot.put("matCode", seed.matCode());
        snapshot.put("matName", seed.matName());
        snapshot.put("lotNo", seed.lotNo());
        if (batch != null) {
            snapshot.put("supplierId", batch.getSupplierId());
            snapshot.put("supplierLotNo", batch.getSupplierLotNo());
            snapshot.put("furnaceLotNo", batch.getFurnaceLotNo());
            snapshot.put("produceDate", batch.getProduceDate());
        }
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        for (Long reqId : data.reqIdsBySeedKey().getOrDefault(seed.identityKey(), Set.of())) {
            MaterialReqDO req = data.reqById().get(reqId);
            MesTraceNodeBO outNode = buildIssueOutNode(req, seed, data, depth + 1, visited);
            if (outNode != null) {
                node.getChildren().add(outNode);
            }
        }
        return node;
    }

    /** 领料出库单节点：申请上的 WMS 出库单编号换回单号，挂生产工单子节点。 */
    private MesTraceNodeBO buildIssueOutNode(MaterialReqDO req, BatchSeed seed, TraceData data,
                                             int depth, Set<String> visited) {
        OutOrderRespDTO outOrder = data.outOrderById().get(parseId(req.getWmsBizNo()));
        if (outOrder == null) {
            // 未走到 WMS（未确认的申请）没有出库凭据，链路在这里截断
            return null;
        }
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.OUT_ORDER.name())
                .setBizNo(outOrder.getOutNo())
                .setNodeName("生产领料出库")
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("outNo", outOrder.getOutNo());
        snapshot.put("bizType", outOrder.getBizType());
        snapshot.put("status", outOrder.getStatus());
        snapshot.put("materialReqNo", req.getReqNo());
        List<InventoryTxnRespDTO> seedTxns = matchedTxns(data.rawIssueTxns(), txn ->
                outOrder.getOutNo().equals(txn.getBizNo()) && seedMatchesTxn(seed, txn));
        snapshot.put("qty", sumQty(seedTxns));
        snapshot.put("txnTime", maxTime(seedTxns));
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        ProdOrderDO order = data.orderByNo().get(req.getProdOrderNo());
        if (order != null) {
            MesTraceNodeBO orderNode = buildOrderNode(order, data, depth + 1, visited);
            if (orderNode != null) {
                node.getChildren().add(orderNode);
            }
        }
        return node;
    }

    /** 生产工单节点：挂工序任务与完工入库单，快照带 BOM 与工艺路线版本。 */
    private MesTraceNodeBO buildOrderNode(ProdOrderDO order, TraceData data, int depth, Set<String> visited) {
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
        snapshot.put("status", order.getStatus());
        snapshot.put("bomVersion", order.getBomVersion());
        snapshot.put("routeVersion", order.getRouteVersion());
        List<WorkOrderDO> workOrders = data.workOrdersByProdOrderId().getOrDefault(order.getId(), List.of());
        snapshot.put("workOrderNos", workOrders.stream().map(WorkOrderDO::getWorkOrderNo).toList());
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        if (!visited.add(KEY_BATCH + "ORDER:" + order.getOrderNo())) {
            // 同一张工单在树里只展开一次：两个批次汇入同一工单时，第二个批次不再重复挂全量子树
            return node;
        }
        workOrders.stream().map(WorkOrderDO::getId)
                .map(workOrderId -> data.opTasksByWorkOrderId().getOrDefault(workOrderId, List.of()))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(OpTaskDO::getSeq, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(opTask -> node.getChildren().add(buildOpTaskNode(opTask, depth + 1)));
        // 完工入库：wip_txn(IN_ORDER) 桥到入库单，qty 取该入库单名下 FINISH_INBOUND 流水之和
        Map<String, BigDecimal> qtyByInOrderId = new LinkedHashMap<>();
        data.wipTxnsByProdOrderId().getOrDefault(order.getId(), List.of()).stream()
                .filter(txn -> MesBizTypeEnum.IN_ORDER.name().equals(txn.getBizType()))
                .forEach(txn -> qtyByInOrderId.merge(txn.getBizNo(), nvl(txn.getQty()), BigDecimal::add));
        for (Map.Entry<String, BigDecimal> entry : qtyByInOrderId.entrySet()) {
            InOrderRespDTO inOrder = data.inOrderById().get(parseId(entry.getKey()));
            if (inOrder != null) {
                node.getChildren().add(buildInOrderNode(inOrder, entry.getValue(), data, depth + 1, visited));
            }
        }
        return node;
    }

    /** 工序任务节点：叶子，展示工序执行事实。 */
    private MesTraceNodeBO buildOpTaskNode(OpTaskDO opTask, int depth) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.OP_TASK.name())
                .setBizNo(opTask.getTaskNo())
                .setNodeName(opTask.getOpName())
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("taskNo", opTask.getTaskNo());
        snapshot.put("workOrderNo", opTask.getWorkOrderNo());
        snapshot.put("opCode", opTask.getOpCode());
        snapshot.put("opName", opTask.getOpName());
        snapshot.put("seq", opTask.getSeq());
        snapshot.put("status", opTask.getStatus());
        snapshot.put("plannedQty", opTask.getPlannedQty());
        snapshot.put("completedQty", opTask.getCompletedQty());
        snapshot.put("actualStartTime", opTask.getActualStartTime());
        snapshot.put("actualEndTime", opTask.getActualEndTime());
        node.setSnapshot(snapshot);
        return node;
    }

    /** 完工入库单节点：挂成品批次子节点，成品批次从完工收货流水按入库单号取。 */
    private MesTraceNodeBO buildInOrderNode(InOrderRespDTO inOrder, BigDecimal qty, TraceData data,
                                            int depth, Set<String> visited) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.IN_ORDER.name())
                .setBizNo(inOrder.getInNo())
                .setNodeName("生产完工入库")
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("inNo", inOrder.getInNo());
        snapshot.put("bizType", inOrder.getBizType());
        snapshot.put("status", inOrder.getStatus());
        snapshot.put("sourceBizNo", inOrder.getSourceBizNo());
        snapshot.put("qty", qty);
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        // 成品批次：完工收货流水按入库单号过滤，同批次多笔收货去重
        Map<Long, InventoryTxnRespDTO> batchTxns = new LinkedHashMap<>();
        data.finishTxns().stream()
                .filter(txn -> inOrder.getInNo().equals(txn.getBizNo()) && txn.getBatchId() != null)
                .forEach(txn -> batchTxns.putIfAbsent(txn.getBatchId(), txn));
        for (InventoryTxnRespDTO txn : batchTxns.values()) {
            MesTraceNodeBO batchNode = buildFinishedBatchNode(txn.getBatchId(), txn.getLotNo(),
                    txn.getMatId(), txn.getMatCode(), txn.getMatName(), data, depth + 1, visited);
            if (batchNode != null) {
                node.getChildren().add(batchNode);
            }
        }
        return node;
    }

    /** 成品批次节点：挂销售出库子节点。同一批次只展开一次（防环：异常数据下成品批次与原料批次同号）。 */
    private MesTraceNodeBO buildFinishedBatchNode(Long batchId, String lotNo, Long matId,
                                                  String matCode, String matName,
                                                  TraceData data, int depth, Set<String> visited) {
        String visitedKey = batchId != null ? KEY_BATCH + batchId : KEY_LOT + lotNo;
        if (!visited.add(visitedKey)) {
            return null;
        }
        BatchRespDTO batch = batchId == null ? null : data.batchById().get(batchId);
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.BATCH.name())
                .setBizNo(lotNo)
                .setNodeName(matName)
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("batchId", batchId);
        snapshot.put("matId", matId);
        snapshot.put("matCode", matCode);
        snapshot.put("matName", matName);
        snapshot.put("lotNo", lotNo);
        if (batch != null) {
            snapshot.put("supplierId", batch.getSupplierId());
            snapshot.put("supplierLotNo", batch.getSupplierLotNo());
            snapshot.put("furnaceLotNo", batch.getFurnaceLotNo());
            snapshot.put("produceDate", batch.getProduceDate());
        }
        snapshot.put("qty", sumQty(matchedTxns(data.finishTxns(), txn ->
                Objects.equals(batchId, txn.getBatchId()) && java.util.Objects.equals(lotNo, txn.getLotNo()))));
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        // 销售出库：发货流水按成品批次过滤，按出库单号分组（一单可能发多个批次，这里每个单号一个节点）
        Map<String, List<InventoryTxnRespDTO>> txnsByOutNo = new LinkedHashMap<>();
        data.salesTxns().stream()
                .filter(txn -> Objects.equals(batchId, txn.getBatchId()) && StrUtil.isNotBlank(txn.getBizNo()))
                .forEach(txn -> txnsByOutNo.computeIfAbsent(txn.getBizNo(), key -> new ArrayList<>()).add(txn));
        for (Map.Entry<String, List<InventoryTxnRespDTO>> entry : txnsByOutNo.entrySet()) {
            node.getChildren().add(buildSalesOutNode(entry.getKey(), entry.getValue(), data, depth + 1));
        }
        return node;
    }

    /** 销售出库单节点：客户编号可解析时挂客户子节点（取数见 {@link #salesCustomerIds}）。 */
    private MesTraceNodeBO buildSalesOutNode(String outNo, List<InventoryTxnRespDTO> txns,
                                             TraceData data, int depth) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.OUT_ORDER.name())
                .setBizNo(outNo)
                .setNodeName("销售发货出库")
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("outNo", outNo);
        snapshot.put("bizType", WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name());
        snapshot.put("qty", sumQty(txns));
        snapshot.put("txnTime", maxTime(txns));
        Long customerId = data.salesCustomerIds().get(outNo);
        snapshot.put("customerId", customerId);
        node.setSnapshot(snapshot);
        if (depth >= maxDepth) {
            return node.setTruncated(true);
        }
        if (customerId != null) {
            CustomerRespDTO customer = data.customerById().get(customerId);
            if (customer != null) {
                node.getChildren().add(buildCustomerNode(customer, depth + 1));
            }
        }
        return node;
    }

    /** 客户节点：链路终点。 */
    private MesTraceNodeBO buildCustomerNode(CustomerRespDTO customer, int depth) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(MesTraceNodeTypeEnum.CUSTOMER.name())
                .setBizNo(customer.getCode())
                .setNodeName(customer.getName())
                .setDepth(depth);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("customerId", customer.getId());
        snapshot.put("customerCode", customer.getCode());
        snapshot.put("customerName", customer.getName());
        node.setSnapshot(snapshot);
        return node;
    }

    // ========== 批量预取 ==========

    /** 一次追溯的批量预取结果，树组装只读这份数据，不再发起任何查询。 */
    private record TraceData(
            Map<String, ProdOrderDO> orderByNo,
            Map<Long, MaterialReqDO> reqById,
            Map<String, Set<Long>> reqIdsBySeedKey,
            Map<Long, OutOrderRespDTO> outOrderById,
            Map<Long, InOrderRespDTO> inOrderById,
            Map<Long, BatchRespDTO> batchById,
            Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId,
            Map<Long, List<OpTaskDO>> opTasksByWorkOrderId,
            Map<Long, List<WipTxnDO>> wipTxnsByProdOrderId,
            List<InventoryTxnRespDTO> rawIssueTxns,
            List<InventoryTxnRespDTO> finishTxns,
            List<InventoryTxnRespDTO> salesTxns,
            Map<String, Long> salesCustomerIds,
            Map<Long, CustomerRespDTO> customerById) {
    }

    /** 种子链路的批量预取。 */
    private TraceData prefetchForSeeds(List<BatchSeed> seeds) {
        // 领料行：按批次编号与批次号两个维度一次查回，内存与种子匹配
        List<Long> batchIds = seeds.stream().map(BatchSeed::batchId).filter(Objects::nonNull).toList();
        List<String> lotNos = seeds.stream().map(BatchSeed::lotNo).filter(StrUtil::isNotBlank).distinct().toList();
        Map<Long, MaterialReqLineDO> lineById = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(batchIds)) {
            materialReqLineMapper.selectList(MaterialReqLineDO::getBatchId, batchIds)
                    .forEach(line -> lineById.putIfAbsent(line.getId(), line));
        }
        if (CollUtil.isNotEmpty(lotNos)) {
            materialReqLineMapper.selectList(MaterialReqLineDO::getLotNo, lotNos)
                    .forEach(line -> lineById.putIfAbsent(line.getId(), line));
        }
        // 行必须命中某个种子（批次编号或批次号匹配），且所属申请不是退料
        Map<String, Set<Long>> reqIdsBySeedKey = new LinkedHashMap<>();
        for (MaterialReqLineDO line : lineById.values()) {
            seeds.stream().filter(seed -> seedMatchesLine(seed, line))
                    .forEach(seed -> reqIdsBySeedKey.computeIfAbsent(seed.identityKey(),
                            key -> new LinkedHashSet<>()).add(line.getReqId()));
        }
        Set<Long> reqIds = reqIdsBySeedKey.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<Long, MaterialReqDO> reqById = reqIds.isEmpty() ? Map.of()
                : materialReqMapper.selectList(MaterialReqDO::getId, reqIds).stream()
                .filter(req -> !MesMaterialReqTypeEnum.isReturn(req.getReqType()))
                .collect(Collectors.toMap(MaterialReqDO::getId, req -> req, (a, b) -> a));
        // 工单
        Set<String> orderNos = reqById.values().stream().map(MaterialReqDO::getProdOrderNo)
                .filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        Map<String, ProdOrderDO> orderByNo = orderNos.isEmpty() ? Map.of()
                : prodOrderMapper.selectList(ProdOrderDO::getOrderNo, orderNos).stream()
                .filter(order -> StrUtil.isNotBlank(order.getOrderNo()))
                .collect(Collectors.toMap(ProdOrderDO::getOrderNo, order -> order, (a, b) -> a));
        // 出库单：申请上的 WMS 出库单编号一次批量反查
        Map<Long, OutOrderRespDTO> outOrderById = getOutOrderMap(reqById.values().stream()
                .map(req -> parseId(req.getWmsBizNo())).filter(Objects::nonNull).collect(Collectors.toSet()));
        List<ProdOrderDO> orders = new ArrayList<>(orderByNo.values());
        // 工序任务与在制流水
        Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId = workOrdersByProdOrderId(orders);
        Map<Long, List<OpTaskDO>> opTasksByWorkOrderId = opTasksByWorkOrderId(orders, workOrdersByProdOrderId);
        List<WipTxnDO> wipTxns = wipTxnsOf(orders);
        Map<Long, List<WipTxnDO>> wipTxnsByProdOrderId = wipTxns.stream()
                .filter(txn -> txn.getProdOrderId() != null)
                .collect(Collectors.groupingBy(WipTxnDO::getProdOrderId));
        Map<Long, InOrderRespDTO> inOrderById = inOrderMap(inOrderIds(wipTxns));
        // 流水分片：原料 ISSUE（按原料物料）、完工收货与销售发货（按成品物料）
        List<InventoryTxnRespDTO> rawIssueTxns = new ArrayList<>();
        seeds.stream().map(BatchSeed::matId).filter(Objects::nonNull).distinct().forEach(matId ->
                rawIssueTxns.addAll(listMatTxns(matId, WmsOutBizTypeEnum.PROD_ISSUE_OUT.name(),
                        WmsTxnTypeEnum.ISSUE.name())));
        List<InventoryTxnRespDTO> finishTxns = new ArrayList<>();
        List<InventoryTxnRespDTO> salesTxns = new ArrayList<>();
        orders.stream().map(ProdOrderDO::getMatId).filter(Objects::nonNull).distinct().forEach(matId -> {
            finishTxns.addAll(listMatTxns(matId, WmsInBizTypeEnum.PROD_FINISH_IN.name(),
                    WmsTxnTypeEnum.RECEIPT.name()));
            salesTxns.addAll(listMatTxns(matId, WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name(),
                    WmsTxnTypeEnum.ISSUE.name()));
        });
        // 批次主档：种子批次 + 成品批次，一次批量
        Set<Long> allBatchIds = new LinkedHashSet<>(batchIds);
        finishTxns.stream().map(InventoryTxnRespDTO::getBatchId).filter(Objects::nonNull).forEach(allBatchIds::add);
        Map<Long, BatchRespDTO> batchById = getBatchMap(allBatchIds);
        // 客户：出库单号 → 客户编号（按单号批量反查出库单），再批量补客户名称
        Map<String, Long> salesCustomerIds = salesCustomerIds(salesTxns);
        Map<Long, CustomerRespDTO> customerById = customerMap(salesCustomerIds);
        return new TraceData(orderByNo, reqById, reqIdsBySeedKey, outOrderById, inOrderById, batchById,
                workOrdersByProdOrderId, opTasksByWorkOrderId, wipTxnsByProdOrderId,
                rawIssueTxns, finishTxns, salesTxns, salesCustomerIds, customerById);
    }

    /** 无领料行工单的批量预取（工单为根）。 */
    private TraceData prefetchForOrder(ProdOrderDO order) {
        List<ProdOrderDO> orders = List.of(order);
        Map<String, ProdOrderDO> orderByNo = Map.of(order.getOrderNo(), order);
        List<WipTxnDO> wipTxns = wipTxnsOf(orders);
        Map<Long, List<WipTxnDO>> wipTxnsByProdOrderId = wipTxns.stream()
                .filter(txn -> txn.getProdOrderId() != null)
                .collect(Collectors.groupingBy(WipTxnDO::getProdOrderId));
        Map<Long, InOrderRespDTO> inOrderById = inOrderMap(inOrderIds(wipTxns));
        List<InventoryTxnRespDTO> finishTxns = new ArrayList<>();
        List<InventoryTxnRespDTO> salesTxns = new ArrayList<>();
        if (order.getMatId() != null) {
            finishTxns.addAll(listMatTxns(order.getMatId(), WmsInBizTypeEnum.PROD_FINISH_IN.name(),
                    WmsTxnTypeEnum.RECEIPT.name()));
            salesTxns.addAll(listMatTxns(order.getMatId(), WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name(),
                    WmsTxnTypeEnum.ISSUE.name()));
        }
        Set<Long> batchIds = finishTxns.stream().map(InventoryTxnRespDTO::getBatchId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BatchRespDTO> batchById = getBatchMap(batchIds);
        Map<String, Long> salesCustomerIds = salesCustomerIds(salesTxns);
        Map<Long, CustomerRespDTO> customerById = customerMap(salesCustomerIds);
        Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId = workOrdersByProdOrderId(orders);
        return new TraceData(orderByNo, Map.of(), Map.of(), Map.of(), inOrderById, batchById,
                workOrdersByProdOrderId, opTasksByWorkOrderId(orders, workOrdersByProdOrderId),
                wipTxnsByProdOrderId, List.of(), finishTxns, salesTxns, salesCustomerIds, customerById);
    }

    /** 原料批次 → 生产工单的桥：领料行按工单号反查。只取领料与补料，退料方向相反不进正向链路。 */
    private List<MaterialReqLineDO> issueLinesOfOrders(Collection<String> orderNos) {
        if (CollUtil.isEmpty(orderNos)) {
            return List.of();
        }
        List<MaterialReqDO> reqs = materialReqMapper.selectList(MaterialReqDO::getProdOrderNo, orderNos).stream()
                .filter(req -> !MesMaterialReqTypeEnum.isReturn(req.getReqType())).toList();
        if (reqs.isEmpty()) {
            return List.of();
        }
        return materialReqLineMapper.selectList(MaterialReqLineDO::getReqId,
                reqs.stream().map(MaterialReqDO::getId).toList());
    }

    // ========== 门面批量调用（每类至多一次） ==========

    private Map<Long, OutOrderRespDTO> getOutOrderMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(outOrderApi.getOutOrderMap(ids), Map.of());
    }

    private Map<Long, InOrderRespDTO> inOrderMap(Set<Long> inOrderIds) {
        if (CollUtil.isEmpty(inOrderIds)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(inOrderApi.getInOrderMap(inOrderIds), Map.of());
    }

    private Map<Long, BatchRespDTO> getBatchMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(batchApi.getBatchMap(ids), Map.of());
    }

    private Map<Long, CustomerRespDTO> customerMap(Map<String, Long> customerIdsByOutNo) {
        if (CollUtil.isEmpty(customerIdsByOutNo)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(partnerApi.getCustomerMap(customerIdsByOutNo.values()), Map.of());
    }

    /**
     * 销售出库单号 → 客户编号，客户节点的唯一权威来源是 {@code wms_out_order.customer_id}。
     *
     * <p>流水行上只有单号，用 {@code OutOrderApi.getOutOrderMapByNos} 一次批量反查换回客户编号
     * （T5-01 收尾时该字段与该方法都是门面缺口，现已补齐）。没有销售流水时不发调用，
     * 避免空跑一次 SQL。
     */
    private Map<String, Long> salesCustomerIds(List<InventoryTxnRespDTO> salesTxns) {
        List<String> outNos = salesTxns.stream().map(InventoryTxnRespDTO::getBizNo)
                .filter(StrUtil::isNotBlank).distinct().toList();
        if (CollUtil.isEmpty(outNos)) {
            return Map.of();
        }
        Map<String, OutOrderRespDTO> outOrderById = ObjUtil.defaultIfNull(
                outOrderApi.getOutOrderMapByNos(outNos), Map.of());
        Map<String, Long> result = new LinkedHashMap<>();
        outOrderById.forEach((outNo, order) -> {
            if (order != null && order.getCustomerId() != null) {
                result.put(outNo, order.getCustomerId());
            }
        });
        return result;
    }

    // ========== 流水查询（按物料分片） ==========

    private List<InventoryTxnRespDTO> listMatTxns(Long matId, String bizType, String txnType) {
        return listTxns(query -> {
            query.setMatId(matId);
            query.setBizType(bizType);
            query.setTxnType(txnType);
        });
    }

    private List<InventoryTxnRespDTO> listTxns(Consumer<InventoryTxnQueryReqDTO> customizer) {
        InventoryTxnQueryReqDTO query = new InventoryTxnQueryReqDTO();
        customizer.accept(query);
        return ObjUtil.defaultIfNull(inventoryApi.getInventoryTxnList(query), List.of());
    }

    // ========== 辅助 ==========

    /** 该种子命中的领料出库流水（分片查询已按物料过滤，这里再按批次身份过滤）。 */
    private boolean seedMatchesTxn(BatchSeed seed, InventoryTxnRespDTO txn) {
        if (seed.batchId() != null) {
            return seed.batchId().equals(txn.getBatchId());
        }
        return seed.lotNo() != null && seed.lotNo().equals(txn.getLotNo());
    }

    /** 种子与领料行匹配：批次编号相等，或批次号相等（行上批次编号可能为 0）。 */
    private boolean seedMatchesLine(BatchSeed seed, MaterialReqLineDO line) {
        if (seed.batchId() != null && seed.batchId().equals(line.getBatchId())) {
            return true;
        }
        return seed.lotNo() != null && seed.lotNo().equals(line.getLotNo());
    }

    private List<InventoryTxnRespDTO> matchedTxns(List<InventoryTxnRespDTO> txns,
                                                  Predicate<InventoryTxnRespDTO> predicate) {
        return txns.stream().filter(predicate).toList();
    }

    private Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId(List<ProdOrderDO> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        return workOrderMapper.selectListByProdOrderIds(
                        orders.stream().map(ProdOrderDO::getId).toList()).stream()
                .filter(wo -> wo.getProdOrderId() != null)
                .collect(Collectors.groupingBy(WorkOrderDO::getProdOrderId));
    }

    private Map<Long, List<OpTaskDO>> opTasksByWorkOrderId(List<ProdOrderDO> orders,
                                                           Map<Long, List<WorkOrderDO>> workOrdersByProdOrderId) {
        List<Long> workOrderIds = orders.stream()
                .map(order -> workOrdersByProdOrderId.getOrDefault(order.getId(), List.<WorkOrderDO>of()))
                .flatMap(List::stream).map(WorkOrderDO::getId).toList();
        if (workOrderIds.isEmpty()) {
            return Map.of();
        }
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .filter(task -> task.getWorkOrderId() != null)
                .collect(Collectors.groupingBy(OpTaskDO::getWorkOrderId));
    }

    private List<WipTxnDO> wipTxnsOf(List<ProdOrderDO> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        return wipTxnMapper.selectList(WipTxnDO::getProdOrderId,
                orders.stream().map(ProdOrderDO::getId).toList());
    }

    /** 在制流水里 IN_ORDER 桥的入库单编号集合（biz_no 存的是入库单编号）。 */
    private Set<Long> inOrderIds(List<WipTxnDO> wipTxns) {
        return wipTxns.stream()
                .filter(txn -> MesBizTypeEnum.IN_ORDER.name().equals(txn.getBizType()))
                .map(WipTxnDO::getBizNo)
                .filter(MesTraceForwardResolver::isNumeric)
                .map(Long::parseLong).collect(Collectors.toSet());
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

    /** 单据编号字符串转 Long，空或非数字返回 null。 */
    private Long parseId(String id) {
        return isNumeric(id) ? Long.parseLong(id) : null;
    }

    private static boolean isNumeric(String value) {
        return StrUtil.isNotBlank(value) && value.chars().allMatch(Character::isDigit);
    }

    /** 批次种子：原料批次在链路根上的身份与快照。 */
    private record BatchSeed(Long batchId, Long matId, String lotNo, String matCode, String matName) {

        /** 身份键：有批次编号用编号，否则用批次号。防环与去重都用它。 */
        String identityKey() {
            return batchId != null ? KEY_BATCH + batchId : KEY_LOT + lotNo;
        }
    }

    /** 测试用：收紧深度上限验证截断行为。 */
    void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

}
