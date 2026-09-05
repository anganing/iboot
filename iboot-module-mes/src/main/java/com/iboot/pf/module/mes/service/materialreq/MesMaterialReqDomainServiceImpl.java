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
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.reservation.InventoryReservationApi;
import com.iboot.pf.module.wms.api.reservation.dto.CreateReservationReqDTO;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationHandleReqDTO;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_EMPTY;
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
 * <p>三个动作的库存后果泾渭分明：创建只落草稿（无后果）、提交占预留（可用量下降）、
 * 取消释放预留（可用量回升）。确认出库在 T1-11。
 *
 * <p>与 WMS 的交互全部走 {@code api} 门面并带幂等键：预留键 {@code MES:{申请单号}:RESERVE:{行号}}，
 * 释放键 {@code MES:{申请单号}:RELEASE:{行号}}。行号进键的原因是预留是逐行的，
 * 整单一个键会让第二行被当成重复提交丢掉。
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
