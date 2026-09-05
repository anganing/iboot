package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.MesBusinessRules;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitRequirementBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderCloseResultBO;
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
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_BACKFLUSH_STOCK_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_BACKFLUSH_WH_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.UOM_NOT_EXISTS;

/**
 * 关闭生产工单的账目收尾。三件会改数据的事都在这里，第四件（写在制流水）由动作本体做。
 *
 * <p>关闭必须做这些事，否则预留会永久挂着占用可用量、倒冲件的消耗永远不入账
 * （见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程七"）。
 *
 * <p><b>释放剩余预留的边界是这个类里最容易写错的地方</b>。同一笔预留在三个时点可能被释放，
 * 三者的键段必须分开，覆盖的单据状态也必须互不相交：
 * <table>
 *     <tr><th>时点</th><th>键段</th><th>覆盖的单据状态</th><th>释放的是</th></tr>
 *     <tr><td>取消领料申请</td><td>{@code RELEASE}</td><td>{@code SUBMITTED} -&gt; {@code CANCELLED}</td>
 *         <td>整单预留</td></tr>
 *     <tr><td>确认发料未发足</td><td>{@code RELEASE_REMAIN}</td><td>{@code SUBMITTED} -&gt;
 *         {@code PARTIAL}/{@code DONE}</td><td>本次没发出去的那部分</td></tr>
 *     <tr><td><b>关闭工单</b></td><td>{@code RELEASE_ON_CLOSE}</td><td>仍停在 {@code SUBMITTED} 的单</td>
 *         <td>整单预留</td></tr>
 * </table>
 * 关闭这一支处理的是"提交了预留、却既没确认也没取消就跟着工单一起收尾"的申请，与前两支的单据状态没有交集。
 * 键段不分开的后果是 WMS 按幂等键把第二次释放当成重复请求丢掉（该释放的没释放）；
 * 状态过滤不做的后果是同一笔预留被释放两次，WMS 侧按剩余量兜得住，但账上会留下两条释放记录，
 * 事后没人说得清哪条是真的。第三道保险是拿 WMS 的预留状态再筛一遍，只对 {@code ACTIVE} / {@code PARTIAL} 动手。
 */
@Component
public class MesOrderCloseHelper {

    /** 幂等键的段分隔符。 */
    private static final String KEY_SEPARATOR = ":";
    /** 整单动作的行号段。 */
    private static final String WHOLE_ORDER_LINE_NO = "0";
    /** 调 WMS 时的来源模块标识，也是给 WMS 的幂等键第一段。 */
    private static final String SOURCE_MODULE_MES = "MES";
    /** 关闭工单时释放剩余预留的键段，与取消（RELEASE）、部分确认（RELEASE_REMAIN）分开，见类注释。 */
    private static final String ACTION_RELEASE_ON_CLOSE = "RELEASE_ON_CLOSE";
    /** 倒冲反冲建单与过账的键段。 */
    private static final String ACTION_BACKFLUSH_OUT = "BACKFLUSH_OUT";
    private static final String ACTION_CONFIRM_BACKFLUSH_OUT = "CONFIRM_BACKFLUSH_OUT";

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private ProdDemandMapper prodDemandMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private MesKitRequirementExpander requirementExpander;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private BomApi bomApi;
    @Resource
    private InventoryReservationApi reservationApi;
    @Resource
    private OutOrderApi outOrderApi;
    @Resource
    private WarehouseApi warehouseApi;

    /**
     * 账目收尾三件事，顺序有讲究：先释放预留把可用量还回去，再做倒冲扣料（反冲要用可用量），最后回写需求。
     *
     * @param order          已经推到 {@code CLOSED} 的生产工单
     * @param bo             关闭参数，提供倒冲发料的仓库与库位
     * @param idempotencyKey 关闭动作的幂等键，给 WMS 的键由它派生
     */
    public void settle(ProdOrderDO order, MesCloseOrderBO bo, String idempotencyKey) {
        releaseRemainingReservations(order);
        backflush(order, bo, idempotencyKey);
        completeDemand(order);
    }

    /**
     * 释放仍停在 {@code SUBMITTED} 的领料申请上的预留，并把这些单据与明细一起作废。
     *
     * <p>为什么顺手把单据置 {@code CANCELLED}：预留已经没了，这张单再也发不出料，
     * 留在"已提交"会让车间以为还等着发货，而工单都关了。与确认发料时"未发出的行落 CANCELLED"同口径。
     *
     * <p>退料单不在范围内：退料从不预留（退料是往仓库还货）。
     */
    private void releaseRemainingReservations(ProdOrderDO order) {
        List<MaterialReqDO> reqs = materialReqMapper.selectList(
                        MaterialReqDO::getProdOrderId, order.getId()).stream()
                .filter(req -> ObjUtil.equal(MesMaterialReqStatusEnum.SUBMITTED.name(), req.getStatus()))
                .filter(req -> !MesMaterialReqTypeEnum.isReturn(req.getReqType()))
                .toList();
        if (CollUtil.isEmpty(reqs)) {
            return;
        }
        List<Long> reqIds = reqs.stream().map(MaterialReqDO::getId).toList();
        List<MaterialReqLineDO> lines = materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, reqIds)
                .stream().filter(line -> !isNone(line.getReservationId()))
                .filter(line -> ObjUtil.equal(MesMaterialReqLineStatusEnum.SUBMITTED.name(), line.getStatus()))
                .toList();
        Map<Long, ReservationRespDTO> reservationMap = loadReservations(lines);
        Map<Long, String> reqNoById = new LinkedHashMap<>();
        reqs.forEach(req -> reqNoById.put(req.getId(), req.getReqNo()));
        for (MaterialReqLineDO line : lines) {
            ReservationRespDTO reservation = reservationMap.get(line.getReservationId());
            // 只对生效与部分处理中的预留动手：已释放、已核销、已取消的再释放一次只会多一条流水
            if (reservation != null && !isReleasable(reservation.getStatus())) {
                continue;
            }
            ReservationHandleReqDTO reqDTO = new ReservationHandleReqDTO();
            reqDTO.setIdempotencyKey(String.join(KEY_SEPARATOR, SOURCE_MODULE_MES,
                    reqNoById.get(line.getReqId()), ACTION_RELEASE_ON_CLOSE, String.valueOf(line.getLineNo())));
            reqDTO.setSourceModule(SOURCE_MODULE_MES);
            reqDTO.setSourceBizNo(reqNoById.get(line.getReqId()));
            reqDTO.setSourceBizLineNo(String.valueOf(line.getLineNo()));
            reqDTO.setBizType(WmsSourceBizTypeEnum.PROD_ISSUE.name());
            reqDTO.setReservationId(line.getReservationId());
            // 数量留空表示释放全部剩余：这张单不会再发料，剩多少都该还回去
            reqDTO.setRemark(order.getOrderNo() + " 关闭，释放未发料预留");
            reservationApi.releaseInventory(reqDTO);
            // 行上的 reservation_id 保留不清零：它是这笔预留曾经存在过的唯一线索
            materialReqLineMapper.updateById(new MaterialReqLineDO()
                    .setId(line.getId())
                    .setStatus(MesMaterialReqLineStatusEnum.CANCELLED.name()));
        }
        reqs.forEach(req -> materialReqMapper.updateById(new MaterialReqDO()
                .setId(req.getId())
                .setStatus(MesMaterialReqStatusEnum.CANCELLED.name())));
    }

    /** 只有生效与部分处理中的预留还占着可用量，才有释放的意义。 */
    private boolean isReleasable(String status) {
        return ObjUtil.equal(WmsReservationStatusEnum.ACTIVE.name(), status)
                || ObjUtil.equal(WmsReservationStatusEnum.PARTIAL.name(), status);
    }

    /** 批量取回预留详情，不逐行查。 */
    private Map<Long, ReservationRespDTO> loadReservations(List<MaterialReqLineDO> lines) {
        List<Long> ids = lines.stream().map(MaterialReqLineDO::getReservationId).distinct().toList();
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(reservationApi.getReservationMap(ids), Map.of());
    }

    /**
     * 倒冲件反冲扣料：按实际产量算需求量，建 {@code PROD_ISSUE_OUT} 出库单并过账。
     *
     * <p>倒冲件的库存扣减时机就是这里（见 {@code MesIssueMethodEnum} 的行为差异表）：齐套不算它、
     * 领料不领它，因此在关闭之前它的消耗一分钱都没进账。放在关闭而不是每次报工，是为了按整单实际产量
     * 一次扣完，避免几十次小额过账把库存流水刷爆。
     *
     * <p>需求量一律走 {@link MesBusinessRules#requiredQty}，与齐套、领料同一个公式，不在这里重写。
     * 筛选倒冲件不能复用 {@code MesKitRequirementExpander}：那个类的语义正好相反——它负责把倒冲件<b>剔除</b>。
     */
    private void backflush(ProdOrderDO order, MesCloseOrderBO bo, String idempotencyKey) {
        BigDecimal actualQty = nvl(order.getCompletedQty());
        if (isNone(order.getBomId()) || actualQty.signum() <= 0) {
            // 没锁 BOM（没下达过）或一件都没产出，都没有可反冲的消耗
            return;
        }
        Map<Long, BigDecimal> requirements = backflushRequirements(order.getBomId(), actualQty);
        if (requirements.isEmpty()) {
            return;
        }
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(requirements.keySet());
        Long whId = resolveBackflushWh(bo, requirements.keySet(), materialMap);
        WhRespDTO wh = warehouseApi.getWh(whId);
        Map<Long, UomRespDTO> uomMap = uomApi.getUomMap(requirements.keySet().stream()
                .map(matId -> baseUomId(materialMap.get(matId))).distinct().toList());

        CreateOutOrderReqDTO createDTO = new CreateOutOrderReqDTO();
        createDTO.setIdempotencyKey(orderKey(order.getOrderNo(), ACTION_BACKFLUSH_OUT));
        createDTO.setSourceModule(SOURCE_MODULE_MES);
        createDTO.setSourceBizNo(order.getOrderNo());
        createDTO.setBizType(WmsOutBizTypeEnum.PROD_ISSUE_OUT.name());
        createDTO.setProdOrderNo(order.getOrderNo());
        createDTO.setInvOrgId(wh == null ? null : wh.getInvOrgId());
        createDTO.setPlantId(order.getPlantId());
        createDTO.setWhId(whId);
        createDTO.setProjectId(nvl(order.getProjectId()));
        createDTO.setProjectTaskId(nvl(order.getProjectTaskId()));
        createDTO.setRemark(order.getOrderNo() + " 关闭，倒冲件按实际产量 " + plain(actualQty) + " 反冲扣料");
        List<CreateOutOrderReqDTO.Line> outLines = new ArrayList<>();
        int lineNo = MesConstants.FIRST_LINE_NO;
        for (Map.Entry<Long, BigDecimal> entry : requirements.entrySet()) {
            MaterialRespDTO material = materialMap == null ? null : materialMap.get(entry.getKey());
            UomRespDTO uom = uomMap == null ? null : uomMap.get(baseUomId(material));
            if (uom == null) {
                throw exception(UOM_NOT_EXISTS);
            }
            Long locId = isNone(bo.getLocId()) ? defaultLocId(material) : bo.getLocId();
            if (isNone(locId)) {
                throw exception(PROD_ORDER_BACKFLUSH_STOCK_REQUIRED, matText(entry.getKey(), material));
            }
            CreateOutOrderReqDTO.Line line = new CreateOutOrderReqDTO.Line();
            line.setLineNo(lineNo++);
            line.setMatId(entry.getKey());
            line.setMatCode(material == null ? null : material.getCode());
            line.setMatName(material == null ? null : material.getName());
            line.setMatSpec(material == null ? null : material.getSpec());
            line.setUomId(uom.getId());
            line.setUomCode(uom.getCode());
            line.setFromLocId(locId);
            line.setPlanQty(entry.getValue());
            // 倒冲件从来不预留，没有预留可核销，落 0 与 WMS 侧的"无预留"分支对齐
            line.setReservationId(MesConstants.NONE_ID);
            line.setRemark("倒冲反冲");
            outLines.add(line);
        }
        createDTO.setLines(outLines);
        Long outOrderId = outOrderApi.createOutOrder(createDTO);

        ConfirmOrderReqDTO confirmDTO = new ConfirmOrderReqDTO();
        confirmDTO.setIdempotencyKey(orderKey(order.getOrderNo(), ACTION_CONFIRM_BACKFLUSH_OUT));
        confirmDTO.setSourceModule(SOURCE_MODULE_MES);
        confirmDTO.setSourceBizNo(order.getOrderNo());
        confirmDTO.setOrderId(outOrderId);
        confirmDTO.setRemark(createDTO.getRemark());
        outOrderApi.confirmOutOrder(confirmDTO);
    }

    /**
     * 取 BOM 里的倒冲件并按实际产量算需求量，同一物料多行合并。
     *
     * <p>合并的理由与齐套展开一致：库存对一个物料只有一份，不合并会开出两行分别扣，
     * 中间任何一行失败都留下半笔账。
     */
    private Map<Long, BigDecimal> backflushRequirements(Long bomId, BigDecimal actualQty) {
        Map<Long, BigDecimal> merged = new LinkedHashMap<>();
        for (BomLineRespDTO line : bomApi.getBomLineList(bomId)) {
            if (!MesIssueMethodEnum.isBackflush(line.getIssueMethod())) {
                continue;
            }
            BigDecimal reqQty = MesBusinessRules.requiredQty(actualQty, line.getQty(),
                    line.getScrapRate() == null ? BigDecimal.ZERO : line.getScrapRate());
            if (reqQty.signum() <= 0) {
                continue;
            }
            merged.merge(line.getMatId(), reqQty, BigDecimal::add);
        }
        return merged;
    }

    /**
     * 反冲的发料仓库：入参优先，否则取倒冲物料的默认仓库并要求各物料一致。
     *
     * <p>出库单头上只有一个仓库。默认仓库不一致时报错而不是挑第一个：挑错仓库就是从别人的库存里扣料，
     * 车间事后对不上账还查不出原因。
     */
    private Long resolveBackflushWh(MesCloseOrderBO bo, java.util.Collection<Long> matIds,
                                    Map<Long, MaterialRespDTO> materialMap) {
        if (!isNone(bo.getWhId())) {
            return bo.getWhId();
        }
        Long resolved = null;
        for (Long matId : matIds) {
            MaterialRespDTO material = materialMap == null ? null : materialMap.get(matId);
            Long whId = material == null ? null : material.getDefaultWhId();
            if (isNone(whId)) {
                throw exception(PROD_ORDER_BACKFLUSH_STOCK_REQUIRED, matText(matId, material));
            }
            if (resolved != null && !resolved.equals(whId)) {
                throw exception(PROD_ORDER_BACKFLUSH_WH_CONFLICT, matText(matId, material));
            }
            resolved = whId;
        }
        return resolved;
    }

    /**
     * 需求全部工单收尾时把需求置 {@code DONE}。
     *
     * <p>"全部收尾"把 {@code CANCELLED} 也算进去：被取消的工单永远不会关闭，只认 {@code CLOSED} 会让需求
     * 永久停在"已下达"，看板上永远显示未完成。
     *
     * <p>更新带 {@code WHERE status = 读到的状态} 条件，{@code mes_prod_demand} 没有 version 列，
     * 这个条件就是它的乐观锁。更新 0 行不报错：需求状态是派生结论，并发的另一方拿的是更新的事实，
     * 为一个派生值把整个关闭动作回滚不值得。
     */
    private void completeDemand(ProdOrderDO order) {
        if (isNone(order.getDemandId())) {
            return;
        }
        boolean allSettled = prodOrderMapper.selectList(ProdOrderDO::getDemandId, order.getDemandId())
                .stream().allMatch(sibling -> isSettled(sibling.getStatus()));
        if (!allSettled) {
            return;
        }
        ProdDemandDO demand = prodDemandMapper.selectById(order.getDemandId());
        if (demand == null || MesDemandStatusEnum.isFinal(demand.getStatus())) {
            return;
        }
        LambdaUpdateWrapper<ProdDemandDO> wrapper = new LambdaUpdateWrapper<ProdDemandDO>()
                .eq(ProdDemandDO::getId, demand.getId())
                .eq(ProdDemandDO::getStatus, demand.getStatus());
        prodDemandMapper.update(new ProdDemandDO()
                .setStatus(MesDemandStatusEnum.DONE.name()), wrapper);
    }

    private boolean isSettled(String status) {
        return ObjUtil.equal(MesProdOrderStatusEnum.CLOSED.name(), status)
                || ObjUtil.equal(MesProdOrderStatusEnum.CANCELLED.name(), status);
    }

    /**
     * 发下去没用完、也没退回的物料清单。只读，不改任何数据。
     *
     * <p>口径：{@code 净发料量 − 按实际产量算的应耗量}，大于零的才进清单。
     * <ul>
     *     <li>净发料量取 {@code processed_qty}（真的搬出仓库的量），领料与补料加、退料减，与开工门禁同口径</li>
     *     <li>应耗量走 {@code MesKitRequirementExpander}，父项数量用<b>实际产量</b>而不是计划数量——
     *     计划 100 只做出 90，按 100 算会把本来该退的料算成"用掉了"</li>
     *     <li>倒冲件与虚拟件不在清单里：展开器把它们剔除/下展，而倒冲件的消耗由反冲一次扣清</li>
     * </ul>
     */
    public MesOrderCloseResultBO inspectUnreturned(ProdOrderDO order) {
        MesOrderCloseResultBO result = new MesOrderCloseResultBO();
        result.setUnreturnedLines(List.of());
        List<MaterialReqDO> reqs = materialReqMapper.selectList(MaterialReqDO::getProdOrderId, order.getId());
        if (CollUtil.isEmpty(reqs)) {
            return result;
        }
        Map<Long, Boolean> returnFlagByReq = new LinkedHashMap<>();
        reqs.forEach(req -> returnFlagByReq.put(req.getId(),
                MesMaterialReqTypeEnum.isReturn(req.getReqType())));
        Map<Long, BigDecimal> issuedByMat = new LinkedHashMap<>();
        Map<Long, MaterialReqLineDO> sampleByMat = new LinkedHashMap<>();
        for (MaterialReqLineDO line : materialReqLineMapper.selectList(
                MaterialReqLineDO::getReqId, returnFlagByReq.keySet())) {
            BigDecimal processedQty = nvl(line.getProcessedQty());
            if (processedQty.signum() == 0) {
                continue;
            }
            boolean isReturn = Boolean.TRUE.equals(returnFlagByReq.get(line.getReqId()));
            issuedByMat.merge(line.getMatId(), isReturn ? processedQty.negate() : processedQty, BigDecimal::add);
            sampleByMat.putIfAbsent(line.getMatId(), line);
        }
        if (issuedByMat.isEmpty()) {
            return result;
        }
        Map<Long, BigDecimal> requiredByMat = new LinkedHashMap<>();
        if (!isNone(order.getBomId())) {
            for (MesKitRequirementBO requirement
                    : requirementExpander.expandByBom(order.getBomId(), nvl(order.getCompletedQty()))) {
                requiredByMat.merge(requirement.getMatId(), requirement.getReqQty(), BigDecimal::add);
            }
        }
        List<MesOrderCloseResultBO.UnreturnedLine> unreturned = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : issuedByMat.entrySet()) {
            BigDecimal requiredQty = requiredByMat.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            BigDecimal overQty = entry.getValue().subtract(requiredQty);
            if (overQty.signum() <= 0) {
                continue;
            }
            MaterialReqLineDO sample = sampleByMat.get(entry.getKey());
            MesOrderCloseResultBO.UnreturnedLine item = new MesOrderCloseResultBO.UnreturnedLine();
            item.setMatId(entry.getKey());
            item.setMatCode(sample == null ? null : sample.getMatCode());
            item.setMatName(sample == null ? null : sample.getMatName());
            item.setIssuedQty(entry.getValue());
            item.setRequiredQty(requiredQty);
            item.setOverQty(overQty);
            unreturned.add(item);
        }
        result.setUnreturnedLines(unreturned);
        return result;
    }

    /** 单据级幂等键：{@code MES:{工单号}:{动作}:0}。 */
    private String orderKey(String orderNo, String action) {
        return String.join(KEY_SEPARATOR, SOURCE_MODULE_MES, orderNo, action, WHOLE_ORDER_LINE_NO);
    }

    /** 消息里给物料编码，车间照着编码就能找到料；主数据查不到时退化成物料编号。 */
    private String matText(Long matId, MaterialRespDTO material) {
        return material == null ? String.valueOf(matId) : material.getCode();
    }

    private Long baseUomId(MaterialRespDTO material) {
        return material == null ? null : material.getBaseUomId();
    }

    private Long defaultLocId(MaterialRespDTO material) {
        return material == null ? null : material.getDefaultLocId();
    }

    private String plain(BigDecimal qty) {
        return qty.stripTrailingZeros().toPlainString();
    }

    private BigDecimal nvl(BigDecimal qty) {
        return qty == null ? BigDecimal.ZERO : qty;
    }

    private Long nvl(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

    private boolean isNone(Long id) {
        return id == null || ObjUtil.equal(MesConstants.NONE_ID, id);
    }

}
