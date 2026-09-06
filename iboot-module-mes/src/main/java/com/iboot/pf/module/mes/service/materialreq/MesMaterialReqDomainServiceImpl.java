package com.iboot.pf.module.mes.service.materialreq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitRequirementBO;
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
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_CONFIRM_QTY_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_CONFIRM_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LOC_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_RETURN_BATCH_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_WAREHOUSE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MAT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.UOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * 领退补料申请领域动作实现。
 *
 * <p>四个动作的库存后果泾渭分明：创建只落草稿（无后果）、提交占预留（可用量下降）、
 * 确认出库扣现存并核销预留（可用量不变）、取消释放预留（可用量回升）。
 *
 * <p>与 WMS 的交互全部走 {@code api} 门面并带幂等键。逐行调用的动作把行号进键
 * （{@code MES:{申请单号}:RESERVE:{行号}}），整单一次调用的动作用单据级键
 * （{@code MES:{申请单号}:CREATE_OUT_ORDER:0}）：行号进键的原因是预留是逐行的，
 * 整单一个键会让第二行被当成重复提交丢掉；反过来出库单是一次带多行创建的，
 * 按行派生反而没地方安放那些键，行级幂等由 WMS 内部再派生。
 *
 * <p><b>{@code wms_biz_no} 落的是 WMS 单据编号而不是单号</b>：
 * {@code OutOrderApi.createOutOrder} 与 {@code InOrderApi.createInOrder} 的返回类型是
 * {@code Long}（单据编号），两个门面都没有"按编号查单号"的只读方法，MES 侧拿不到单号本身。
 * 编号同样能唯一定位到那张 WMS 单据，追溯可用；等门面补上查询方法或让创建返回单号后改成真单号。
 */
@Service
public class MesMaterialReqDomainServiceImpl implements MesMaterialReqDomainService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 整单动作的行号段，单据级动作没有行号，固定 0。 */
    private static final String WHOLE_ORDER_LINE_NO = "0";
    /** 调 WMS 时的来源模块标识，也是给 WMS 的幂等键第一段。 */
    private static final String SOURCE_MODULE_MES = "MES";
    /** 创建动作的键段。创建有落库载体，不进 {@code MesCommandTypeEnum}，键段单独定义。 */
    private static final String ACTION_CREATE_REQ = "CREATE_REQ";
    /** 预留与释放的键段。 */
    private static final String ACTION_RESERVE = "RESERVE";
    private static final String ACTION_RELEASE = "RELEASE";
    /**
     * 确认发料时释放"本次没发出去的那部分预留"的键段。
     *
     * <p>与取消用的 {@link #ACTION_RELEASE} 分开：同一张单可能先部分确认（释放未发部分）
     * 而不再走取消，两个动作用同一个键会让第二次被 WMS 当成重复请求丢掉。
     */
    private static final String ACTION_RELEASE_REMAIN = "RELEASE_REMAIN";
    /** 确认发料时创建与过账 WMS 单据的键段。 */
    private static final String ACTION_CREATE_OUT_ORDER = "CREATE_OUT_ORDER";
    private static final String ACTION_CONFIRM_OUT_ORDER = "CONFIRM_OUT_ORDER";
    private static final String ACTION_CREATE_IN_ORDER = "CREATE_IN_ORDER";
    private static final String ACTION_CONFIRM_IN_ORDER = "CONFIRM_IN_ORDER";

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
    private MesKitRequirementExpander requirementExpander;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private BatchApi batchApi;
    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private InventoryReservationApi reservationApi;
    @Resource
    private OutOrderApi outOrderApi;
    @Resource
    private InOrderApi inOrderApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMaterialReq(MesCreateMaterialReqBO bo) {
        ProdOrderDO order = requireProdOrder(bo.getProdOrderId());
        Assert.notBlank(bo.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        validateReqType(bo.getReqType());
        if (isNone(bo.getInvOrgId()) || isNone(bo.getWhId())) {
            throw exception(MATERIAL_REQ_WAREHOUSE_REQUIRED);
        }
        // 创建有落库载体：按 mes_material_req.idempotency_key 判重，不占用命令记录表
        String idempotencyKey = StrUtil.isBlank(bo.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, bo.getSourceModule(), order.getOrderNo(),
                ACTION_CREATE_REQ, WHOLE_ORDER_LINE_NO)
                : bo.getIdempotencyKey();
        MaterialReqDO existed = materialReqMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed != null) {
            return existed.getId();
        }

        List<MesCreateMaterialReqBO.Line> lines = resolveLines(order, bo);
        if (CollUtil.isEmpty(lines)) {
            throw exception(MATERIAL_REQ_LINE_EMPTY);
        }
        MaterialReqDO req = MaterialReqDO.builder()
                .reqNo(codeService.generate(MesCodeRuleConstants.MATERIAL_REQ, null))
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .workOrderId(nvl(bo.getWorkOrderId()))
                .opTaskId(nvl(bo.getOpTaskId()))
                .reqType(bo.getReqType())
                .invOrgId(bo.getInvOrgId())
                .whId(bo.getWhId())
                // 项目维度从工单继承：领的料要落到工单所属项目上，专用库存才不会被别的项目领走
                .projectId(order.getProjectId())
                .projectTaskId(order.getProjectTaskId())
                .status(MesMaterialReqStatusEnum.DRAFT.name())
                .idempotencyKey(idempotencyKey)
                .remark(bo.getRemark())
                .build();
        materialReqMapper.insert(req);
        insertLines(req, lines);
        return req.getId();
    }

    /**
     * 明细来源：调用方给了就用它，留空时按用料关系展开。
     *
     * <p>三档展开与齐套检查的三档一致，走同一个 {@link MesKitRequirementExpander}：
     * 齐套按什么算需求，领料就按什么开明细，两处口径必须是同一份代码。
     *
     * <p>退料不能推：退多少是车间数出来的，没有用料关系可依据，必须显式给行。
     */
    private List<MesCreateMaterialReqBO.Line> resolveLines(ProdOrderDO order, MesCreateMaterialReqBO bo) {
        if (CollUtil.isNotEmpty(bo.getLines())) {
            return bo.getLines();
        }
        if (MesMaterialReqTypeEnum.isReturn(bo.getReqType())) {
            throw exception(MATERIAL_REQ_LINE_EMPTY);
        }
        return expandRequirements(order, bo).stream().map(this::toLine).toList();
    }

    /** 工序级取该工序用料，作业工单级取该段覆盖工序的用料，工单级取整单 BOM。 */
    private List<MesKitRequirementBO> expandRequirements(ProdOrderDO order, MesCreateMaterialReqBO bo) {
        if (!isNone(bo.getOpTaskId())) {
            OpTaskDO opTask = requireOpTask(bo.getOpTaskId());
            return requirementExpander.expandByRouteOps(List.of(opTask.getRouteOpId()), opTask.getPlannedQty());
        }
        if (!isNone(bo.getWorkOrderId())) {
            WorkOrderDO workOrder = requireWorkOrder(bo.getWorkOrderId());
            List<Long> routeOpIds = opTaskMapper.selectListByWorkOrderIds(List.of(workOrder.getId()))
                    .stream().map(OpTaskDO::getRouteOpId).distinct().toList();
            return requirementExpander.expandByRouteOps(routeOpIds, workOrder.getPlannedQty());
        }
        // 整单领料按下达时锁定的 BOM 展开，未锁版本时拒绝：拿空 BOM 建单会开出一张没有明细的领料申请
        if (isNone(order.getBomId())) {
            throw exception(BOM_NOT_EXISTS);
        }
        return requirementExpander.expandByBom(order.getBomId(), order.getQty());
    }

    private MesCreateMaterialReqBO.Line toLine(MesKitRequirementBO requirement) {
        MesCreateMaterialReqBO.Line line = new MesCreateMaterialReqBO.Line();
        line.setMatId(requirement.getMatId());
        line.setQty(requirement.getReqQty());
        line.setUomId(requirement.getUomId());
        line.setRouteOpMatId(requirement.getRouteOpMatId());
        line.setMustIssueBeforeStart(requirement.getMustIssueBeforeStart());
        return line;
    }

    /**
     * 逐行落库，物料、单位、批次三样快照一次批量补齐。
     *
     * <p>快照的意义是列表查询不再回查 mdm，主数据改名也不影响历史单据；批量取数是为了不产生 N+1。
     */
    private void insertLines(MaterialReqDO req, List<MesCreateMaterialReqBO.Line> lines) {
        List<Long> matIds = lines.stream().map(MesCreateMaterialReqBO.Line::getMatId).distinct().toList();
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(matIds);
        // 单位没给时落物料主档的基本单位：单位为空的明细行到了 WMS 那边无法换算
        Map<Long, Long> uomIdByLine = new HashMap<>();
        for (MesCreateMaterialReqBO.Line line : lines) {
            MaterialRespDTO material = materialMap.get(line.getMatId());
            if (material == null) {
                throw exception(MAT_NOT_EXISTS);
            }
            uomIdByLine.put(line.getMatId(),
                    isNone(line.getUomId()) ? material.getBaseUomId() : line.getUomId());
        }
        Map<Long, UomRespDTO> uomMap = uomApi.getUomMap(uomIdByLine.values().stream().distinct().toList());
        List<Long> batchIds = lines.stream().map(MesCreateMaterialReqBO.Line::getBatchId)
                .filter(batchId -> !isNone(batchId)).distinct().toList();
        Map<Long, BatchRespDTO> batchMap = CollUtil.isEmpty(batchIds)
                ? Map.of() : batchApi.getBatchMap(batchIds);

        int lineNo = MesConstants.FIRST_LINE_NO;
        for (MesCreateMaterialReqBO.Line line : lines) {
            if (line.getQty() == null || line.getQty().signum() <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            Long batchId = nvl(line.getBatchId());
            // 退料必须指定批次：退回去的库存要落到原批次上，否则批次台账对不上
            if (MesMaterialReqTypeEnum.isReturn(req.getReqType()) && isNone(batchId)) {
                throw exception(MATERIAL_REQ_RETURN_BATCH_REQUIRED);
            }
            MaterialRespDTO material = materialMap.get(line.getMatId());
            Long uomId = uomIdByLine.get(line.getMatId());
            UomRespDTO uom = uomMap.get(uomId);
            if (uom == null) {
                throw exception(UOM_NOT_EXISTS);
            }
            BatchRespDTO batch = batchMap.get(batchId);
            materialReqLineMapper.insert(MaterialReqLineDO.builder()
                    .reqId(req.getId())
                    .lineNo(lineNo++)
                    .routeOpMatId(nvl(line.getRouteOpMatId()))
                    .matId(line.getMatId())
                    .matCode(material.getCode())
                    .matName(material.getName())
                    .matSpec(material.getSpec())
                    .qty(line.getQty())
                    .processedQty(MesConstants.ZERO_QTY)
                    .uomId(uomId)
                    .uomCode(uom.getCode())
                    .batchId(batchId)
                    .lotNo(batch == null ? null : batch.getLotNo())
                    // 展开出来的行没给就按必发处理：漏发一种开工前必需的料，工序到现场才发现干不了
                    .mustIssueBeforeStart(!Boolean.FALSE.equals(line.getMustIssueBeforeStart()))
                    // 预留在提交阶段才产生，草稿行先落 0
                    .reservationId(MesConstants.NONE_ID)
                    .status(MesMaterialReqLineStatusEnum.DRAFT.name())
                    .remark(line.getRemark())
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMaterialReq(MesMaterialReqActionBO bo) {
        MaterialReqDO req = requireMaterialReq(bo.getMaterialReqId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.SUBMIT_MATERIAL_REQ, req.getReqNo());
        commandService.executeVoid(MesCommandTypeEnum.SUBMIT_MATERIAL_REQ, command, () -> doSubmit(req));
    }

    /**
     * 提交：领料与补料逐行占预留，退料只推状态。
     *
     * <p>预留前先按物料汇总做一次可用量前置校验，目的是把错误信息里的物料编码给出来——
     * WMS 侧的可用量不足只知道物料编号，车间看不懂。并发下真正的最终防线仍是 WMS 的预留动作。
     *
     * <p>任一行失败整个方法抛异常，事务回滚，已经建立的预留随之作废：留一半预留比全不留更难收拾。
     */
    private void doSubmit(MaterialReqDO req) {
        if (!MesMaterialReqStatusEnum.isDraft(req.getStatus())) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
        List<MaterialReqLineDO> lines = linesOf(req.getId());
        boolean isReturn = MesMaterialReqTypeEnum.isReturn(req.getReqType());
        if (!isReturn) {
            validateAvailableQty(req, lines);
        }
        for (MaterialReqLineDO line : lines) {
            // 退料不预留：退料是往仓库还货，占用可用量没有意义
            Long reservationId = isReturn ? MesConstants.NONE_ID : reserve(req, line);
            materialReqLineMapper.updateById(new MaterialReqLineDO()
                    .setId(line.getId())
                    .setReservationId(reservationId)
                    .setStatus(MesMaterialReqLineStatusEnum.SUBMITTED.name()));
        }
        materialReqMapper.updateById(new MaterialReqDO()
                .setId(req.getId())
                .setStatus(MesMaterialReqStatusEnum.SUBMITTED.name()));
    }

    /**
     * 预留前的可用量前置校验，按物料汇总一次批量查。
     *
     * <p>汇总而不是逐行比：同一物料出现在两行时，各自都够但加起来不够，逐行比会全部放过去。
     */
    private void validateAvailableQty(MaterialReqDO req, List<MaterialReqLineDO> lines) {
        Map<Long, BigDecimal> demandByMat = new HashMap<>();
        lines.forEach(line -> demandByMat.merge(line.getMatId(), line.getQty(), BigDecimal::add));
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatIds(demandByMat.keySet());
        query.setInvOrgId(req.getInvOrgId());
        query.setWhId(req.getWhId());
        query.setQualifiedOnly(Boolean.TRUE);
        Map<Long, BigDecimal> availableMap = inventoryApi.getAvailableQtyMap(query);
        for (MaterialReqLineDO line : lines) {
            BigDecimal demandQty = demandByMat.get(line.getMatId());
            BigDecimal availableQty = availableMap == null ? BigDecimal.ZERO
                    : availableMap.getOrDefault(line.getMatId(), BigDecimal.ZERO);
            if (availableQty.compareTo(demandQty) < 0) {
                throw exception(MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT,
                        line.getMatCode(), plain(demandQty), plain(availableQty));
            }
        }
    }

    /** 数量转可读文本：库表是 decimal(20,6)，直接拼会给车间看到"100.000000"这种噪声。 */
    private String plain(BigDecimal qty) {
        return qty.stripTrailingZeros().toPlainString();
    }

    /** 一行占一笔预留，幂等键 {@code MES:{申请单号}:RESERVE:{行号}}，返回 WMS 给的预留编号。 */
    private Long reserve(MaterialReqDO req, MaterialReqLineDO line) {
        CreateReservationReqDTO reqDTO = new CreateReservationReqDTO();
        reqDTO.setIdempotencyKey(lineKey(req.getReqNo(), ACTION_RESERVE, line.getLineNo()));
        reqDTO.setSourceModule(SOURCE_MODULE_MES);
        reqDTO.setSourceBizNo(req.getReqNo());
        reqDTO.setSourceBizLineNo(String.valueOf(line.getLineNo()));
        reqDTO.setBizType(WmsSourceBizTypeEnum.PROD_ISSUE.name());
        reqDTO.setInvOrgId(req.getInvOrgId());
        reqDTO.setWhId(req.getWhId());
        reqDTO.setMatId(line.getMatId());
        reqDTO.setQty(line.getQty());
        // 项目维度传下去，专用库存的预留才不会占到公共库存上
        reqDTO.setProjectId(req.getProjectId());
        reqDTO.setProjectTaskId(req.getProjectTaskId());
        if (!isNone(line.getBatchId())) {
            reqDTO.setBatchId(line.getBatchId());
        }
        reqDTO.setRemark(req.getProdOrderNo() + " 领料预留");
        return reservationApi.reserveInventory(reqDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmMaterialReq(MesConfirmMaterialReqBO bo) {
        MaterialReqDO req = requireMaterialReq(bo.getMaterialReqId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.CONFIRM_MATERIAL_REQ, req.getReqNo());
        commandService.executeVoid(MesCommandTypeEnum.CONFIRM_MATERIAL_REQ, command, () -> doConfirm(req, bo));
    }

    /**
     * 确认：把料真正搬出仓库（退料则搬回），三段里唯一改动在库量的一段。
     *
     * <p>库存后果（以领 100、初始现存 500、提交后已预留 100 为例）：出库过账把现存 500 降到 400，
     * 紧接着核销那 100 预留，两者抵消，可用量仍是 400。漏传预留编号就会变成现存 400 而预留仍占 100，
     * 可用量凭空少 100 且永远回不来。
     *
     * <p>本次没发出去的那部分预留必须释放：确认是一次性动作，单据确认完就进终态，
     * 留着的预留再没有履约机会，不释放等于把可用量永久占住。
     */
    private void doConfirm(MaterialReqDO req, MesConfirmMaterialReqBO bo) {
        if (!ObjUtil.equal(MesMaterialReqStatusEnum.SUBMITTED.name(), req.getStatus())) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
        List<ConfirmLine> plan = buildConfirmPlan(req, linesOf(req.getId()), bo);
        // 交给 WMS 之前先落中间态：事务内的标记，WMS 调用抛异常时随事务回滚到 SUBMITTED
        materialReqMapper.updateById(new MaterialReqDO().setId(req.getId())
                .setStatus(MesMaterialReqStatusEnum.WMS_PROCESSING.name()));

        boolean isReturn = MesMaterialReqTypeEnum.isReturn(req.getReqType());
        Long wmsOrderId = isReturn ? postReturnIn(req, plan, bo) : postIssueOut(req, plan, bo);
        if (!isReturn) {
            releaseRemainingReservations(req, plan);
        }
        boolean allDone = true;
        for (ConfirmLine item : plan) {
            BigDecimal processedQty = nvlQty(item.line.getProcessedQty()).add(item.confirmQty);
            allDone = allDone && processedQty.compareTo(item.line.getQty()) >= 0;
            materialReqLineMapper.updateById(new MaterialReqLineDO()
                    .setId(item.line.getId())
                    .setProcessedQty(processedQty)
                    .setStatus(lineStatusOf(item.line, processedQty)));
        }
        materialReqMapper.updateById(new MaterialReqDO()
                .setId(req.getId())
                // 门面返回的是 WMS 单据编号而不是单号，见类注释与自查偏差说明
                .setWmsBizNo(String.valueOf(wmsOrderId))
                .setStatus(allDone ? MesMaterialReqStatusEnum.DONE.name()
                        : MesMaterialReqStatusEnum.PARTIAL.name())
                .setRemark(bo.getRemark()));
    }

    /**
     * 明细行的终态：发满了是完成，发了一部分是部分完成，一点没发的作废。
     *
     * <p>没发的行不留在 {@code SUBMITTED}：它的预留已经在本次确认里释放掉了，
     * 停在已提交会让人以为还等着发料，而这张单已经收尾了。
     */
    private String lineStatusOf(MaterialReqLineDO line, BigDecimal processedQty) {
        if (processedQty.compareTo(line.getQty()) >= 0) {
            return MesMaterialReqLineStatusEnum.DONE.name();
        }
        return processedQty.signum() > 0 ? MesMaterialReqLineStatusEnum.PARTIAL.name()
                : MesMaterialReqLineStatusEnum.CANCELLED.name();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMaterialReq(MesMaterialReqActionBO bo) {
        MaterialReqDO req = requireMaterialReq(bo.getMaterialReqId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.CANCEL_MATERIAL_REQ, req.getReqNo());
        commandService.executeVoid(MesCommandTypeEnum.CANCEL_MATERIAL_REQ, command,
                () -> doCancel(req, bo.getReason()));
    }

    /**
     * 取消：草稿直接置取消，已提交的先逐行释放预留再置取消。
     *
     * <p>不释放预留就等于把可用量永久占住，别的工单再也领不到这批料，而且没人知道是谁占的。
     * 已交给 WMS 之后不允许取消：料已经在路上或已出库，MES 单方面取消会让两侧数量对不上。
     */
    private void doCancel(MaterialReqDO req, String reason) {
        boolean draft = MesMaterialReqStatusEnum.isDraft(req.getStatus());
        boolean submitted = ObjUtil.equal(MesMaterialReqStatusEnum.SUBMITTED.name(), req.getStatus());
        if (!draft && !submitted) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
        List<MaterialReqLineDO> lines = linesOf(req.getId());
        for (MaterialReqLineDO line : lines) {
            if (submitted && !isNone(line.getReservationId())) {
                releaseReservation(req, line);
            }
            materialReqLineMapper.updateById(new MaterialReqLineDO()
                    .setId(line.getId())
                    .setStatus(MesMaterialReqLineStatusEnum.CANCELLED.name()));
        }
        materialReqMapper.updateById(new MaterialReqDO()
                .setId(req.getId())
                .setStatus(MesMaterialReqStatusEnum.CANCELLED.name())
                .setRemark(reason));
    }

    @Override
    public void validateReqDraft(Long materialReqId) {
        requireDraft(requireMaterialReq(materialReqId));
    }

    @Override
    public void validateReqDraft(Collection<Long> materialReqIds) {
        if (CollUtil.isEmpty(materialReqIds)) {
            return;
        }
        // 一次查回全部申请，不按 ID 逐个查库
        Map<Long, MaterialReqDO> reqMap = convertMap(materialReqMapper.selectByIds(materialReqIds),
                MaterialReqDO::getId);
        for (Long materialReqId : materialReqIds) {
            MaterialReqDO req = reqMap.get(materialReqId);
            if (req == null) {
                throw exception(MATERIAL_REQ_NOT_EXISTS);
            }
            requireDraft(req);
        }
    }

    @Override
    public void validateLineDraft(Collection<Long> lineIds) {
        if (CollUtil.isEmpty(lineIds)) {
            return;
        }
        List<MaterialReqLineDO> lines = materialReqLineMapper.selectByIds(lineIds);
        if (lines.size() != CollUtil.newHashSet(lineIds).size()) {
            throw exception(MATERIAL_REQ_LINE_NOT_EXISTS);
        }
        // 行反查所属申请也批量做：一张单几十行时逐行查库就是 N+1
        validateReqDraft(lines.stream().map(MaterialReqLineDO::getReqId).distinct().toList());
    }

    /**
     * 只有新建状态的申请允许表单改动与删除。
     *
     * <p>{@code SUBMITTED} 必须拦住：那一刻 WMS 已经逐行占了预留，改数量、加行、删行都会让预留与明细脱节，
     * 预留按旧数量占着可用量而确认发料按新数量走，多出来的那部分永远没人释放。
     * 要改先取消（取消会把预留还回去）再重开一张。
     */
    private void requireDraft(MaterialReqDO req) {
        if (!MesMaterialReqStatusEnum.isDraft(req.getStatus())) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
    }

    /**
     * 释放一行的预留，幂等键 {@code MES:{申请单号}:RELEASE:{行号}}，数量留空表示释放全部剩余。
     *
     * <p>行上的 {@code reservation_id} 不清零：它是"这笔预留曾经存在过"的唯一线索，
     * 清掉之后再对账就查不出可用量为什么波动过。
     */
    private void releaseReservation(MaterialReqDO req, MaterialReqLineDO line) {
        ReservationHandleReqDTO reqDTO = new ReservationHandleReqDTO();
        reqDTO.setIdempotencyKey(lineKey(req.getReqNo(), ACTION_RELEASE, line.getLineNo()));
        reqDTO.setSourceModule(SOURCE_MODULE_MES);
        reqDTO.setSourceBizNo(req.getReqNo());
        reqDTO.setSourceBizLineNo(String.valueOf(line.getLineNo()));
        reqDTO.setBizType(WmsSourceBizTypeEnum.PROD_ISSUE.name());
        reqDTO.setReservationId(line.getReservationId());
        reqDTO.setRemark("领料申请取消，释放预留");
        reservationApi.releaseInventory(reqDTO);
    }

    /**
     * 按入参与明细现状算出本次每行发多少、从哪个库位发。
     *
     * <p>入参 {@code lines} 留空表示全部行按剩余量全额确认；给了就只发列出来的行，
     * 没列出来的按 0 处理。一行都发不出去时直接拒绝，不去 WMS 建一张空单。
     *
     * <p>预留详情一次批量取回（{@code getReservationMap}），不逐行查。
     */
    private List<ConfirmLine> buildConfirmPlan(MaterialReqDO req, List<MaterialReqLineDO> lines,
                                               MesConfirmMaterialReqBO bo) {
        Map<Integer, MesConfirmMaterialReqBO.Line> params = new HashMap<>();
        if (CollUtil.isNotEmpty(bo.getLines())) {
            bo.getLines().stream().filter(line -> line.getLineNo() != null)
                    .forEach(line -> params.put(line.getLineNo(), line));
        }
        boolean confirmAll = params.isEmpty();
        Map<Long, ReservationRespDTO> reservationMap = loadReservations(req, lines);
        List<ConfirmLine> plan = new ArrayList<>();
        boolean anyEffective = false;
        for (MaterialReqLineDO line : lines) {
            MesConfirmMaterialReqBO.Line param = params.get(line.getLineNo());
            BigDecimal remainingQty = line.getQty().subtract(nvlQty(line.getProcessedQty()));
            BigDecimal confirmQty = resolveConfirmQty(param, remainingQty, confirmAll);
            if (confirmQty.signum() <= 0) {
                plan.add(new ConfirmLine(line, BigDecimal.ZERO, MesConstants.NONE_ID, MesConstants.NONE_ID));
                continue;
            }
            if (confirmQty.compareTo(remainingQty) > 0) {
                throw exception(MATERIAL_REQ_CONFIRM_QTY_EXCEEDED,
                        line.getMatCode(), plain(confirmQty), plain(remainingQty));
            }
            anyEffective = true;
            ReservationRespDTO reservation = reservationMap.get(line.getReservationId());
            plan.add(new ConfirmLine(line, confirmQty,
                    resolveLocId(line, param, reservation), resolveBatchId(line, reservation)));
        }
        if (!anyEffective) {
            throw exception(MATERIAL_REQ_CONFIRM_QTY_EMPTY);
        }
        return plan;
    }

    /** 本次确认量：整单确认或行上没给数量时按剩余量全额，未列出的行按 0。 */
    private BigDecimal resolveConfirmQty(MesConfirmMaterialReqBO.Line param, BigDecimal remainingQty,
                                         boolean confirmAll) {
        if (confirmAll) {
            return remainingQty;
        }
        if (param == null) {
            return BigDecimal.ZERO;
        }
        return param.getConfirmQty() == null ? remainingQty : param.getConfirmQty();
    }

    /**
     * 发料库位：入参给了用它，否则回落到预留记的库位。
     *
     * <p>回落到预留而不是随便挑一个有货的库位：预留占的是哪条库存记录，出库就必须从那条记录出。
     * 从别处拿会让那个库位的现存减了、而预留占住的库位现存没减却把预留核销掉，两个库位的账都错。
     * 挑库位是 WMS 拣选策略的职责（{@code pickStrategyId}），MES 不自己实现。
     */
    private Long resolveLocId(MaterialReqLineDO line, MesConfirmMaterialReqBO.Line param,
                              ReservationRespDTO reservation) {
        if (param != null && !isNone(param.getLocId())) {
            return param.getLocId();
        }
        Long locId = reservation == null ? null : reservation.getLocId();
        if (isNone(locId)) {
            throw exception(MATERIAL_REQ_LOC_REQUIRED, line.getMatCode());
        }
        return locId;
    }

    /** 批次：行上指定优先（退料必填），否则用预留记的批次，两者都没有表示不限批次。 */
    private Long resolveBatchId(MaterialReqLineDO line, ReservationRespDTO reservation) {
        if (!isNone(line.getBatchId())) {
            return line.getBatchId();
        }
        return reservation == null ? MesConstants.NONE_ID : nvl(reservation.getBatchId());
    }

    /** 批量取回本单各行的预留详情。退料没有预留，直接空表。 */
    private Map<Long, ReservationRespDTO> loadReservations(MaterialReqDO req, List<MaterialReqLineDO> lines) {
        if (MesMaterialReqTypeEnum.isReturn(req.getReqType())) {
            return Map.of();
        }
        List<Long> reservationIds = lines.stream().map(MaterialReqLineDO::getReservationId)
                .filter(id -> !isNone(id)).distinct().toList();
        if (CollUtil.isEmpty(reservationIds)) {
            return Map.of();
        }
        return ObjUtil.defaultIfNull(reservationApi.getReservationMap(reservationIds), Map.of());
    }

    /**
     * 领料与补料：建 {@code PROD_ISSUE_OUT} 出库单再过账，返回 WMS 单据编号。
     *
     * <p>整单一次创建、一次过账，所以幂等键是单据级的。WMS 内部再按行派生过账与核销的键
     * （{@code :POST:{行号}}、{@code :{行号}:CONSUME}），MES 不重复做这件事。
     *
     * <p>行上必须带 {@code reservationId}：这是预留核销的唯一入口，缺了它 WMS 的
     * {@code consumeReservation} 直接走空分支，预留永不核销。
     */
    private Long postIssueOut(MaterialReqDO req, List<ConfirmLine> plan, MesConfirmMaterialReqBO bo) {
        CreateOutOrderReqDTO createDTO = new CreateOutOrderReqDTO();
        createDTO.setIdempotencyKey(orderKey(req.getReqNo(), ACTION_CREATE_OUT_ORDER));
        createDTO.setSourceModule(SOURCE_MODULE_MES);
        createDTO.setSourceBizNo(req.getReqNo());
        createDTO.setBizType(WmsOutBizTypeEnum.PROD_ISSUE_OUT.name());
        createDTO.setProdOrderNo(req.getProdOrderNo());
        createDTO.setWorkOrderNo(workOrderNoOf(req));
        createDTO.setOpTaskId(nvl(req.getOpTaskId()));
        createDTO.setMaterialReqNo(req.getReqNo());
        createDTO.setInvOrgId(req.getInvOrgId());
        createDTO.setWhId(req.getWhId());
        createDTO.setProjectId(nvl(req.getProjectId()));
        createDTO.setProjectTaskId(nvl(req.getProjectTaskId()));
        createDTO.setRemark(bo.getRemark());
        createDTO.setLines(plan.stream().filter(ConfirmLine::isEffective).map(item -> {
            CreateOutOrderReqDTO.Line line = new CreateOutOrderReqDTO.Line();
            line.setLineNo(item.line.getLineNo());
            line.setSourceLineId(item.line.getId());
            line.setSourceLineNo(String.valueOf(item.line.getLineNo()));
            line.setMatId(item.line.getMatId());
            line.setMatCode(item.line.getMatCode());
            line.setMatName(item.line.getMatName());
            line.setMatSpec(item.line.getMatSpec());
            line.setBatchId(isNone(item.batchId) ? null : item.batchId);
            line.setLotNo(item.line.getLotNo());
            line.setUomId(item.line.getUomId());
            line.setUomCode(item.line.getUomCode());
            line.setFromLocId(item.locId);
            line.setPlanQty(item.confirmQty);
            line.setReservationId(nvl(item.line.getReservationId()));
            line.setRemark(item.line.getRemark());
            return line;
        }).toList());
        Long orderId = outOrderApi.createOutOrder(createDTO);
        outOrderApi.confirmOutOrder(confirmOrderDTO(req, orderId, ACTION_CONFIRM_OUT_ORDER, bo.getRemark()));
        return orderId;
    }

    /**
     * 退料：建 {@code PROD_RETURN_IN} 入库单再过账，返回 WMS 单据编号。
     *
     * <p>退料没有预留可核销（提交时就不预留，退料是往仓库还货），过账只让现存上升。
     */
    private Long postReturnIn(MaterialReqDO req, List<ConfirmLine> plan, MesConfirmMaterialReqBO bo) {
        CreateInOrderReqDTO createDTO = new CreateInOrderReqDTO();
        createDTO.setIdempotencyKey(orderKey(req.getReqNo(), ACTION_CREATE_IN_ORDER));
        createDTO.setSourceModule(SOURCE_MODULE_MES);
        createDTO.setSourceBizNo(req.getReqNo());
        createDTO.setBizType(WmsInBizTypeEnum.PROD_RETURN_IN.name());
        createDTO.setProdOrderNo(req.getProdOrderNo());
        createDTO.setWorkOrderNo(workOrderNoOf(req));
        createDTO.setInvOrgId(req.getInvOrgId());
        createDTO.setWhId(req.getWhId());
        createDTO.setProjectId(nvl(req.getProjectId()));
        createDTO.setProjectTaskId(nvl(req.getProjectTaskId()));
        // 退回来的是车间没用完的原料，仍是合格品，不需要再质检
        createDTO.setNeedQc(Boolean.FALSE);
        createDTO.setRemark(bo.getRemark());
        createDTO.setLines(plan.stream().filter(ConfirmLine::isEffective).map(item -> {
            CreateInOrderReqDTO.Line line = new CreateInOrderReqDTO.Line();
            line.setLineNo(item.line.getLineNo());
            line.setSourceLineId(item.line.getId());
            line.setSourceLineNo(String.valueOf(item.line.getLineNo()));
            line.setMatId(item.line.getMatId());
            line.setMatCode(item.line.getMatCode());
            line.setMatName(item.line.getMatName());
            line.setMatSpec(item.line.getMatSpec());
            line.setBatchId(isNone(item.batchId) ? null : item.batchId);
            line.setLotNo(item.line.getLotNo());
            line.setUomId(item.line.getUomId());
            line.setUomCode(item.line.getUomCode());
            line.setLocId(item.locId);
            line.setPlanQty(item.confirmQty);
            line.setRemark(item.line.getRemark());
            return line;
        }).toList());
        Long orderId = inOrderApi.createInOrder(createDTO);
        inOrderApi.confirmInOrder(confirmOrderDTO(req, orderId, ACTION_CONFIRM_IN_ORDER, bo.getRemark()));
        return orderId;
    }

    /** 过账命令，出入库共用。 */
    private ConfirmOrderReqDTO confirmOrderDTO(MaterialReqDO req, Long orderId, String action, String remark) {
        ConfirmOrderReqDTO confirmDTO = new ConfirmOrderReqDTO();
        confirmDTO.setIdempotencyKey(orderKey(req.getReqNo(), action));
        confirmDTO.setSourceModule(SOURCE_MODULE_MES);
        confirmDTO.setSourceBizNo(req.getReqNo());
        confirmDTO.setOrderId(orderId);
        confirmDTO.setRemark(remark);
        return confirmDTO;
    }

    /**
     * 释放本次没发出去的那部分预留。
     *
     * <p>确认是一次性动作，单据确认完就进终态，剩下的预留再没有履约机会。不释放的话
     * 那部分可用量被一张已经收尾的单据永久占着，别的工单领不到，也没人查得出是谁占的。
     */
    private void releaseRemainingReservations(MaterialReqDO req, List<ConfirmLine> plan) {
        for (ConfirmLine item : plan) {
            if (isNone(item.line.getReservationId())) {
                continue;
            }
            BigDecimal remainingQty = item.line.getQty()
                    .subtract(nvlQty(item.line.getProcessedQty())).subtract(item.confirmQty);
            if (remainingQty.signum() <= 0) {
                continue;
            }
            ReservationHandleReqDTO reqDTO = new ReservationHandleReqDTO();
            reqDTO.setIdempotencyKey(lineKey(req.getReqNo(), ACTION_RELEASE_REMAIN, item.line.getLineNo()));
            reqDTO.setSourceModule(SOURCE_MODULE_MES);
            reqDTO.setSourceBizNo(req.getReqNo());
            reqDTO.setSourceBizLineNo(String.valueOf(item.line.getLineNo()));
            reqDTO.setBizType(WmsSourceBizTypeEnum.PROD_ISSUE.name());
            reqDTO.setReservationId(item.line.getReservationId());
            reqDTO.setQty(remainingQty);
            reqDTO.setRemark("确认发料未发足，释放剩余预留");
            reservationApi.releaseInventory(reqDTO);
        }
    }

    /** 作业工单号快照，工单级申请没有作业工单时留空。 */
    private String workOrderNoOf(MaterialReqDO req) {
        if (isNone(req.getWorkOrderId())) {
            return null;
        }
        WorkOrderDO workOrder = workOrderMapper.selectById(req.getWorkOrderId());
        return workOrder == null ? null : workOrder.getWorkOrderNo();
    }

    /** 单据级幂等键：{@code MES:{申请单号}:{动作}:0}。 */
    private String orderKey(String reqNo, String action) {
        return String.join(KEY_SEPARATOR, SOURCE_MODULE_MES, reqNo, action, WHOLE_ORDER_LINE_NO);
    }

    /** 本次确认的一行：发多少、从哪个库位、哪个批次。 */
    private static final class ConfirmLine {

        private final MaterialReqLineDO line;
        private final BigDecimal confirmQty;
        private final Long locId;
        private final Long batchId;

        private ConfirmLine(MaterialReqLineDO line, BigDecimal confirmQty, Long locId, Long batchId) {
            this.line = line;
            this.confirmQty = confirmQty;
            this.locId = locId;
            this.batchId = batchId;
        }

        /** 本次是否真的要发这一行。发 0 的行不进 WMS 单据。 */
        private boolean isEffective() {
            return confirmQty.signum() > 0;
        }

    }

    /** 数量兜 null：{@code processed_qty} 是历史行可能为空的进度列。 */
    private BigDecimal nvlQty(BigDecimal qty) {
        return qty == null ? BigDecimal.ZERO : qty;
    }

    /** 按行号升序取明细。行号决定预留的幂等键，顺序必须稳定。 */
    private List<MaterialReqLineDO> linesOf(Long reqId) {
        List<MaterialReqLineDO> lines = new ArrayList<>(
                materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, reqId));
        lines.sort(Comparator.comparingInt(MaterialReqLineDO::getLineNo));
        if (CollUtil.isEmpty(lines)) {
            throw exception(MATERIAL_REQ_LINE_EMPTY);
        }
        return lines;
    }

    /** 给 WMS 的行级幂等键：{@code MES:{申请单号}:{动作}:{行号}}。 */
    private String lineKey(String reqNo, String action, Integer lineNo) {
        return String.join(KEY_SEPARATOR, SOURCE_MODULE_MES, reqNo, action, String.valueOf(lineNo));
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了以它为准，留空时按四段格式 {@code {来源模块}:{申请单号}:{命令类型}:0} 派生。
     * 动作段取 {@link MesCommandTypeEnum} 的枚举名而不是字面量：键会落进 {@code mes_command_record}，
     * 字面量拼出来的历史键在命令改名后就与枚举对不上了。
     */
    private MesCommandReqDTO buildCommand(MesCommandReqDTO source, MesCommandTypeEnum commandType, String reqNo) {
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        MaterialReqCommand command = new MaterialReqCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? reqNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), reqNo,
                commandType.name(), WHOLE_ORDER_LINE_NO)
                : source.getIdempotencyKey());
        return command;
    }

    /** 申请类型必须是三种之一：类型决定走预留还是不走，取值错了整条链路都是错的。 */
    private void validateReqType(String reqType) {
        boolean legal = false;
        for (MesMaterialReqTypeEnum type : MesMaterialReqTypeEnum.values()) {
            legal = legal || ObjUtil.equal(type.name(), reqType);
        }
        Assert.isTrue(legal, "领退补料申请类型({})不合法", reqType);
    }

    private ProdOrderDO requireProdOrder(Long id) {
        ProdOrderDO order = id == null ? null : prodOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        return order;
    }

    private WorkOrderDO requireWorkOrder(Long id) {
        WorkOrderDO workOrder = isNone(id) ? null : workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    private OpTaskDO requireOpTask(Long id) {
        OpTaskDO opTask = isNone(id) ? null : opTaskMapper.selectById(id);
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        return opTask;
    }

    private MaterialReqDO requireMaterialReq(Long id) {
        MaterialReqDO req = id == null ? null : materialReqMapper.selectById(id);
        if (req == null) {
            throw exception(MATERIAL_REQ_NOT_EXISTS);
        }
        return req;
    }

    /** 可选维度落 0 不落 null：这些列在库表里是 NOT NULL DEFAULT 0。 */
    private Long nvl(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

    private boolean isNone(Long id) {
        return id == null || MesConstants.NONE_ID.equals(id);
    }

    /**
     * 幂等命令载体。
     *
     * <p>{@link MesCommandReqDTO} 是抽象类，这里给它一个最小实现，避免把派生出的幂等键写回调用方的 BO。
     */
    private static class MaterialReqCommand extends MesCommandReqDTO {
    }

}
