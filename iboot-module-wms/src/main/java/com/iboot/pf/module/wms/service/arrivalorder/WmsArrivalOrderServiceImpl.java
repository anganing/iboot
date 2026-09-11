package com.iboot.pf.module.wms.service.arrivalorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.dal.mysql.arrival.ArrivalMapper;
import com.iboot.pf.module.wms.dal.mysql.arrivalline.ArrivalLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.api.arrivalorder.dto.CompleteIqcReqDTO;
import com.iboot.pf.module.wms.enums.WmsArrivalLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalStatusEnum;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsConfirmArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsCreateArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsUpdateArrivalBO;
import com.iboot.pf.module.wms.service.batch.WmsBatchService;
import com.iboot.pf.module.wms.service.batch.bo.RegisterBatchBO;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandService;
import com.iboot.pf.module.wms.service.command.bo.WmsCommandBO;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.arrivalorder.event.ArrivalConfirmedEvent;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_MAT_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_NOT_MATCH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_NOT_READY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_UOM_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.ARRIVAL;

/** 到货登记 Service 实现。 */
@Service
public class WmsArrivalOrderServiceImpl implements WmsArrivalOrderService {

    @Resource
    private ArrivalMapper arrivalMapper;
    @Resource
    private ArrivalLineMapper arrivalLineMapper;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsBatchService batchService;
    @Resource
    private WmsInOrderService inOrderService;
    @Resource
    private WmsInOrderMapper wmsInOrderMapper;
    @Resource
    private WmsInOrderLineMapper wmsInOrderLineMapper;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsCommandService commandService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArrival(WmsCreateArrivalBO bo) {
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(ARRIVAL_LINE_EMPTY);
        }
        warehouseService.validateWh(bo.getWhId());

        ArrivalDO arrival = BeanUtils.toBean(bo, ArrivalDO.class);
        arrival.setId(null);
        arrival.setArrivalNo(codeService.generate(ARRIVAL, bo.getArrivalNo()));
        validateArrivalNoUnique(arrival.getArrivalNo(), null);
        arrival.setStatus(WmsArrivalStatusEnum.DRAFT.name());
        arrivalMapper.insert(arrival);

        insertLines(arrival.getId(), bo);
        return arrival.getId();
    }

    /** 按参数建到货明细。创建与改单共用：改单走行的全量替换，建行规则必须与创建时一致。 */
    private void insertLines(Long arrivalId, WmsCreateArrivalBO bo) {
        Set<Long> matIds = bo.getLines().stream().map(WmsCreateArrivalBO.Line::getMatId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> uomIds = bo.getLines().stream().map(WmsCreateArrivalBO.Line::getUomId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 快照一次批量取回，不在明细循环里逐条访问 mdm
        materialApi.validateMaterialList(matIds);
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(matIds);
        Map<Long, UomRespDTO> uomMap = uomApi.getUomMap(uomIds);

        for (WmsCreateArrivalBO.Line line : bo.getLines()) {
            if (line.getArrivalQty() == null || line.getArrivalQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            MaterialRespDTO material = materialMap.get(line.getMatId());
            if (material == null) {
                throw exception(ARRIVAL_LINE_MAT_NOT_EXISTS);
            }
            UomRespDTO uom = uomMap.get(line.getUomId());
            if (uom == null) {
                throw exception(ARRIVAL_LINE_UOM_NOT_EXISTS);
            }

            ArrivalLineDO lineDO = BeanUtils.toBean(line, ArrivalLineDO.class);
            lineDO.setId(null);
            lineDO.setArrivalId(arrivalId);
            lineDO.setMatCode(material.getCode());
            lineDO.setMatName(material.getName());
            lineDO.setMatSpec(material.getSpec());
            lineDO.setUomCode(uom.getCode());
            // need_qc 是 NOT NULL 列，为空按不需要质检处理
            lineDO.setNeedQc(Boolean.TRUE.equals(line.getNeedQc()));
            lineDO.setQualifiedQty(BigDecimal.ZERO);
            lineDO.setRejectedQty(BigDecimal.ZERO);
            lineDO.setStatus(WmsArrivalLineStatusEnum.PENDING.name());
            arrivalLineMapper.insert(lineDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArrival(WmsUpdateArrivalBO bo) {
        ArrivalDO arrival = requireDraft(bo.getArrivalId());
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(ARRIVAL_LINE_EMPTY);
        }
        warehouseService.validateWh(bo.getWhId());

        // 单号、状态、实际到货时间保持原值：它们是单据身份与动作留痕，不随内容改。
        // updateById 只写非空字段，与 codegen 侧行为一致，清空某个可选外键要靠调用方传 0 而不是 null。
        ArrivalDO update = BeanUtils.toBean(bo, ArrivalDO.class);
        update.setId(arrival.getId());
        update.setArrivalNo(arrival.getArrivalNo());
        update.setStatus(arrival.getStatus());
        update.setArrivalTime(arrival.getArrivalTime());
        arrivalMapper.updateById(update);

        // 明细全量替换：逐行比对容易漏掉删行的情况，且行号可能整体重排
        for (ArrivalLineDO old : lineList(arrival.getId())) {
            arrivalLineMapper.deleteById(old.getId());
        }
        insertLines(arrival.getId(), bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArrival(Long arrivalId) {
        requireDraft(arrivalId);
        for (ArrivalLineDO line : lineList(arrivalId)) {
            arrivalLineMapper.deleteById(line.getId());
        }
        arrivalMapper.deleteById(arrivalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmArrival(WmsConfirmArrivalBO bo) {
        commandService.executeVoid(WmsCommandTypeEnum.CONFIRM_ARRIVAL,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    ArrivalDO arrival = requireArrival(bo.getArrivalId(), bo.getArrivalNo());
                    if (!WmsArrivalStatusEnum.isDraft(arrival.getStatus())) {
                        throw exception(ARRIVAL_CANNOT_CONFIRM);
                    }
                    List<ArrivalLineDO> lines = lineList(arrival.getId());
                    if (CollUtil.isEmpty(lines)) {
                        throw exception(ARRIVAL_LINE_EMPTY);
                    }
                    warehouseService.validateWh(arrival.getWhId());

                    Set<Long> matIds = lines.stream().map(ArrivalLineDO::getMatId)
                            .filter(Objects::nonNull).collect(Collectors.toSet());
                    materialApi.validateMaterialList(matIds);
                    Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(matIds);

                    for (ArrivalLineDO line : lines) {
                        // 批次在确认时注册而不是登记时：登记只是预告，确认才是"实物到了"的事实
                        WmsBatchDO batch = registerBatch(arrival, line, materialMap.get(line.getMatId()));
                        line.setLotNo(batch.getLotNo());
                        // 是否检验最终由 MES 规则裁决；先按既有提示给默认状态，监听器会同步回写最终结果。
                        line.setStatus(Boolean.TRUE.equals(line.getNeedQc())
                                ? WmsArrivalLineStatusEnum.QC_PENDING.name()
                                : WmsArrivalLineStatusEnum.READY.name());
                        arrivalLineMapper.updateById(line);
                        eventPublisher.publishEvent(new ArrivalConfirmedEvent(line.getId(), arrival.getArrivalNo(),
                                arrival.getSupplierId(), line.getMatId(), line.getUomId(), batch.getId(),
                                line.getArrivalQty(), line.getLotNo()));
                    }

                    arrival.setStatus(WmsArrivalStatusEnum.ARRIVED.name());
                    arrival.setArrivalTime(bo.getArrivalTime() == null
                            ? LocalDateTime.now() : bo.getArrivalTime());
                    if (bo.getRemark() != null) {
                        arrival.setRemark(bo.getRemark());
                    }
                    arrivalMapper.updateById(arrival);
                });
    }

    /**
     * 注册到货批次。
     *
     * <p>供应商维度取自单头、供应商批次号取自明细：一张到货单只对一个供应商，但可以有多个供应商批次。
     * 批次的自然键是（物料，批次号，序列号），同键重复调用返回原批次，所以重复确认不会多出批次。
     */
    private WmsBatchDO registerBatch(ArrivalDO arrival, ArrivalLineDO line, MaterialRespDTO material) {
        RegisterBatchBO registerBO = new RegisterBatchBO();
        registerBO.setMatId(line.getMatId());
        registerBO.setMatCode(line.getMatCode());
        registerBO.setMatName(line.getMatName());
        // 无批次物料落默认批次号：registerBatch 对空批次号直接抛错
        registerBO.setLotNo(line.getLotNo() == null || line.getLotNo().isEmpty()
                ? WmsConstants.DEFAULT_LOT_NO : line.getLotNo());
        registerBO.setSupplierId(arrival.getSupplierId());
        registerBO.setSupplierCode(arrival.getSupplierCode());
        registerBO.setSupplierLotNo(line.getSupplierLotNo());
        return batchService.registerBatch(registerBO, material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelArrival(WmsCancelOrderBO bo) {
        ArrivalDO arrival = requireArrival(bo.getOrderId(), bo.getOrderNo());
        if (WmsArrivalStatusEnum.DONE.name().equals(arrival.getStatus())
                || WmsArrivalStatusEnum.CANCELLED.name().equals(arrival.getStatus())) {
            throw exception(ARRIVAL_CANNOT_CANCEL);
        }
        arrival.setStatus(WmsArrivalStatusEnum.CANCELLED.name());
        arrival.setRemark(bo.getReason());
        arrivalMapper.updateById(arrival);
        for (ArrivalLineDO line : lineList(arrival.getId())) {
            // 已生成入库单的明细不回退：入库单有自己的取消动作，这里改掉会让两边对不上
            if (WmsArrivalLineStatusEnum.DONE.name().equals(line.getStatus())) {
                continue;
            }
            line.setStatus(WmsArrivalLineStatusEnum.CANCELLED.name());
            arrivalLineMapper.updateById(line);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInOrderFromArrival(Long arrivalId, List<Long> lineIds) {
        ArrivalDO arrival = requireArrival(arrivalId, null);
        List<ArrivalLineDO> selected = selectReadyLines(lineList(arrival.getId()), lineIds);

        Long inOrderId = inOrderService.createInOrder(buildInOrderBO(arrival, selected));

        for (ArrivalLineDO line : selected) {
            line.setStatus(WmsArrivalLineStatusEnum.DONE.name());
            arrivalLineMapper.updateById(line);
        }
        // 全部明细都生成了入库单，到货这件事就结束了；剩余明细还在待检或待入库时保持 ARRIVED
        boolean allDone = lineList(arrival.getId()).stream()
                .allMatch(line -> WmsArrivalLineStatusEnum.DONE.name().equals(line.getStatus()));
        if (allDone) {
            arrival.setStatus(WmsArrivalStatusEnum.DONE.name());
            arrivalMapper.updateById(arrival);
        }
        return inOrderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveIqcTask(Long arrivalLineId, Long qcTaskId) {
        ArrivalLineDO line = arrivalLineId == null ? null : arrivalLineMapper.selectById(arrivalLineId);
        if (line == null) {
            return;
        }
        line.setQcTaskId(qcTaskId);
        // 规则未命中直接放行；先入待检模式命中后也要放行建入库单，但库存质量状态仍保持 QC_PENDING。
        if (qcTaskId == null || WmsIqcModeEnum.POST_INBOUND_PENDING.name().equals(line.getIqcMode())) {
            line.setStatus(WmsArrivalLineStatusEnum.READY.name());
        } else {
            line.setStatus(WmsArrivalLineStatusEnum.QC_PENDING.name());
        }
        arrivalLineMapper.updateById(line);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeIqc(CompleteIqcReqDTO reqDTO) {
        ArrivalLineDO line = reqDTO.getArrivalLineId() == null ? null
                : arrivalLineMapper.selectById(reqDTO.getArrivalLineId());
        // 先入待检的行在规则命中后已经放行到 READY 并可能完成入库，不能因状态不是 QC_PENDING 而丢结论。
        boolean postInbound = line != null
                && WmsIqcModeEnum.POST_INBOUND_PENDING.name().equals(line.getIqcMode());
        if (line == null || (!WmsArrivalLineStatusEnum.QC_PENDING.name().equals(line.getStatus())
                && !(postInbound && WmsArrivalLineStatusEnum.READY.name().equals(line.getStatus())))) {
            return;
        }
        line.setQcTaskId(reqDTO.getQcTaskId());
        line.setQualifiedQty(reqDTO.getQtyQualified());
        line.setRejectedQty(reqDTO.getQtyRejected());
        if (!"QUALIFIED".equals(reqDTO.getConclusion()) && !"CONCESSION".equals(reqDTO.getConclusion())) {
            arrivalLineMapper.updateById(line);
            return;
        }
        line.setStatus(WmsArrivalLineStatusEnum.READY.name());
        arrivalLineMapper.updateById(line);
        if (WmsIqcModeEnum.POST_INBOUND_PENDING.name().equals(line.getIqcMode())) {
            changePendingInboundQuality(line, reqDTO.getQcTaskId());
        }
    }

    /** 已过账的待检入库行按 IQC 结论迁移质量状态；尚未过账的行会在后续入库时直接带合格状态。 */
    private void changePendingInboundQuality(ArrivalLineDO arrivalLine, Long qcTaskId) {
        for (WmsInOrderLineDO inLine : wmsInOrderLineMapper.selectListBySourceLineId(arrivalLine.getId())) {
            if (inLine.getPostedQty() == null || inLine.getPostedQty().signum() <= 0) {
                inLine.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
                inLine.setQcTaskId(qcTaskId);
                wmsInOrderLineMapper.updateById(inLine);
                continue;
            }
            WmsInOrderDO order = wmsInOrderMapper.selectById(inLine.getOrderId());
            WmsInvPostBO postBO = new WmsInvPostBO();
            postBO.setTxnType(WmsTxnTypeEnum.QUALITY_CHANGE.name());
            postBO.setBizType(order.getBizType());
            postBO.setBizNo(order.getInNo());
            postBO.setBizLineNo(String.valueOf(inLine.getLineNo()));
            postBO.setIdempotencyKey("MES:" + qcTaskId + ":QUALITY_CHANGE:" + inLine.getLineNo());
            postBO.setQty(inLine.getPostedQty());
            postBO.setInvOrgId(order.getInvOrgId());
            postBO.setPlantId(order.getPlantId());
            postBO.setWhId(order.getWhId());
            postBO.setFromLocId(inLine.getLocId());
            postBO.setFromLocCode(inLine.getLocCode());
            postBO.setToLocId(inLine.getLocId());
            postBO.setToLocCode(inLine.getLocCode());
            postBO.setQualityStatusFrom(WmsQualityStatusEnum.QC_PENDING.name());
            postBO.setQualityStatusTo(WmsQualityStatusEnum.QUALIFIED.name());
            postBO.setMatId(inLine.getMatId());
            postBO.setBatchId(inLine.getBatchId());
            postBO.setContainerId(inLine.getContainerId());
            postBO.setUomId(inLine.getUomId());
            postBO.setMatCode(inLine.getMatCode());
            postBO.setMatName(inLine.getMatName());
            postBO.setUomCode(inLine.getUomCode());
            postBO.setLotNo(inLine.getLotNo());
            inventoryService.post(postBO);
            inLine.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
            inLine.setQcTaskId(qcTaskId);
            wmsInOrderLineMapper.updateById(inLine);
        }
    }

    /** 挑出本次要生成入库单的明细。不指定编号时取全部待入库明细，界面上"整单生成"是常用操作。 */
    private List<ArrivalLineDO> selectReadyLines(List<ArrivalLineDO> all, List<Long> lineIds) {
        List<ArrivalLineDO> selected = new ArrayList<>();
        if (CollUtil.isEmpty(lineIds)) {
            for (ArrivalLineDO line : all) {
                if (WmsArrivalLineStatusEnum.READY.name().equals(line.getStatus())) {
                    selected.add(line);
                }
            }
        } else {
            for (Long lineId : lineIds) {
                ArrivalLineDO line = all.stream().filter(it -> it.getId().equals(lineId)).findFirst()
                        .orElseThrow(() -> exception(ARRIVAL_LINE_NOT_MATCH));
                // 待质检的明细必须等 IQC 结论，未确认到货的明细还没有批次，都不能直接入库
                if (!WmsArrivalLineStatusEnum.READY.name().equals(line.getStatus())) {
                    throw exception(ARRIVAL_LINE_NOT_READY);
                }
                selected.add(line);
            }
        }
        if (selected.isEmpty()) {
            throw exception(ARRIVAL_LINE_NOT_READY);
        }
        return selected;
    }

    /** 按到货明细拼采购入库单参数。 */
    private WmsCreateInOrderBO buildInOrderBO(ArrivalDO arrival, List<ArrivalLineDO> lines) {
        WmsCreateInOrderBO createBO = new WmsCreateInOrderBO();
        // 幂等键带上本次选中的行号：同一批明细重复生成命中原单，不同批明细各生成一张入库单
        String lineNos = lines.stream().map(line -> String.valueOf(line.getLineNo()))
                .collect(Collectors.joining("-"));
        createBO.setIdempotencyKey(String.format("%s:%s:PO_IN:%s",
                WmsSourceModuleEnum.WMS.name(), arrival.getArrivalNo(), lineNos));
        createBO.setSourceModule(WmsSourceModuleEnum.WMS.name());
        createBO.setSourceBizNo(arrival.getArrivalNo());
        createBO.setBizType(WmsInBizTypeEnum.PO_IN.name());
        createBO.setOriginType(arrival.getSourceType());
        createBO.setOriginNo(arrival.getSourceNo());
        createBO.setArrivalId(arrival.getId());
        createBO.setSupplierId(arrival.getSupplierId());
        createBO.setInvOrgId(arrival.getInvOrgId());
        createBO.setPlantId(arrival.getPlantId());
        createBO.setWhId(arrival.getWhId());
        createBO.setRemark("到货登记 " + arrival.getArrivalNo() + " 生成");

        boolean anyPending = false;
        List<WmsCreateInOrderBO.Line> inLines = new ArrayList<>();
        for (ArrivalLineDO line : lines) {
            String qualityStatus = qualityStatusOf(line);
            anyPending = anyPending || WmsQualityStatusEnum.QC_PENDING.name().equals(qualityStatus);

            WmsCreateInOrderBO.Line inLine = new WmsCreateInOrderBO.Line();
            inLine.setLineNo(line.getLineNo());
            inLine.setSourceLineId(line.getId());
            inLine.setSourceLineNo(String.valueOf(line.getLineNo()));
            inLine.setMatId(line.getMatId());
            inLine.setMatCode(line.getMatCode());
            inLine.setMatName(line.getMatName());
            inLine.setMatSpec(line.getMatSpec());
            // 批次号是确认到货时注册并回写的，入库单按同一自然键复用同一批次，不会新建
            inLine.setLotNo(line.getLotNo());
            inLine.setUomId(line.getUomId());
            inLine.setUomCode(line.getUomCode());
            inLine.setQualityStatus(qualityStatus);
            // 计划入库量取到货量；先检后入按合格量入库要等 IQC 结论，在 T2-06 接入后再分流
            inLine.setPlanQty(line.getArrivalQty());
            inLine.setSupplierId(arrival.getSupplierId());
            inLine.setSupplierCode(arrival.getSupplierCode());
            inLine.setSupplierLotNo(line.getSupplierLotNo());
            // 目标库位不在这里定：上架时才知道货放哪，confirmInOrder 会强制要求库位
            inLines.add(inLine);
        }
        // 单头质量状态只是明细的默认值（明细已逐行显式指定）；带待检明细时单头也标待检，列表上一眼能看出
        createBO.setQualityStatus(anyPending
                ? WmsQualityStatusEnum.QC_PENDING.name() : WmsQualityStatusEnum.QUALIFIED.name());
        createBO.setLines(inLines);
        return createBO;
    }

    /** 入库质量状态按来料检验模式取：先入待检的进 QC 库区标待检，其余按合格入账。 */
    private String qualityStatusOf(ArrivalLineDO line) {
        return WmsIqcModeEnum.POST_INBOUND_PENDING.name().equals(line.getIqcMode())
                ? WmsQualityStatusEnum.QC_PENDING.name() : WmsQualityStatusEnum.QUALIFIED.name();
    }

    /** 取到货登记并校验处于新建状态。改单与删单的共同前置：已确认到货的单据不许改物料与数量。 */
    private ArrivalDO requireDraft(Long arrivalId) {
        ArrivalDO arrival = requireArrival(arrivalId, null);
        if (!WmsArrivalStatusEnum.isDraft(arrival.getStatus())) {
            throw exception(ARRIVAL_CANNOT_MODIFY);
        }
        return arrival;
    }

    private ArrivalDO requireArrival(Long arrivalId, String arrivalNo) {
        ArrivalDO arrival = arrivalId != null ? arrivalMapper.selectById(arrivalId) : null;
        if (arrival == null && arrivalNo != null && !arrivalNo.isEmpty()) {
            arrival = arrivalMapper.selectByArrivalNo(arrivalNo);
        }
        if (arrival == null) {
            throw exception(ARRIVAL_NOT_EXISTS);
        }
        return arrival;
    }

    /** 到货单号唯一：唯一索引是 (arrival_no, deleted)，这里做前置校验给出可读提示。 */
    private void validateArrivalNoUnique(String arrivalNo, Long excludeId) {
        ArrivalDO existed = arrivalMapper.selectByArrivalNo(arrivalNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(ARRIVAL_NO_DUPLICATE);
    }

    /** 按行号顺序取到货明细。 */
    private List<ArrivalLineDO> lineList(Long arrivalId) {
        return arrivalLineMapper.selectList(new LambdaQueryWrapperX<ArrivalLineDO>()
                .eq(ArrivalLineDO::getArrivalId, arrivalId)
                .orderByAsc(ArrivalLineDO::getLineNo));
    }

    @Override
    public ArrivalDO getArrival(Long id) {
        return arrivalMapper.selectById(id);
    }

    @Override
    public List<ArrivalLineDO> getArrivalLineList(Long arrivalId) {
        return lineList(arrivalId);
    }

}
