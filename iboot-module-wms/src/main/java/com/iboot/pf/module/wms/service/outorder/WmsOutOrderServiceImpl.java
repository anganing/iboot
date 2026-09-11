package com.iboot.pf.module.wms.service.outorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandService;
import com.iboot.pf.module.wms.service.command.bo.WmsCommandBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.api.outorder.dto.ResolveOqcTaskReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.CompleteOqcReqDTO;
import com.iboot.pf.module.wms.service.outorder.event.OutOrderPickedEvent;
import com.iboot.pf.module.wms.service.reservation.WmsInventoryReservationService;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_AVAILABLE_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_SUBMIT;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LOC_REQUIRED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_OQC_NOT_PASSED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.OUT_ORDER;

/** 出库单 Service 实现。 */
@Service
public class WmsOutOrderServiceImpl implements WmsOutOrderService {

    @Resource
    private WmsOutOrderMapper wmsOutOrderMapper;
    @Resource
    private WmsOutOrderLineMapper wmsOutOrderLineMapper;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsInventoryReservationService reservationService;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsCommandService commandService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutOrder(WmsCreateOutOrderBO bo) {
        WmsOutOrderDO existed = wmsOutOrderMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(OUT_ORDER_LINE_EMPTY);
        }
        List<Long> materialIds = bo.getLines().stream().map(WmsCreateOutOrderBO.Line::getMatId).toList();
        materialApi.validateMaterialList(materialIds);
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(materialIds);
        warehouseService.validateWh(bo.getWhId());

        WmsWhDO wh = warehouseService.getWh(bo.getWhId());
        WmsOutOrderDO order = BeanUtils.toBean(bo, WmsOutOrderDO.class);
        order.setId(null);
        order.setOutNo(codeService.generate(OUT_ORDER, bo.getOutNo()));
        order.setWhCode(wh == null ? null : wh.getCode());
        order.setSourceType(bo.getSourceModule());
        order.setSourceBizNo(bo.getSourceBizNo());
        order.setStatus(WmsOrderStatusEnum.DRAFT.name());
        wmsOutOrderMapper.insert(order);

        insertLines(order, bo, materialMap);
        return order.getId();
    }

    /** 按参数建出库行。创建与修改共用：修改走行的全量替换，建行规则与创建时必须一致。 */
    private void insertLines(WmsOutOrderDO order, WmsCreateOutOrderBO bo,
                             Map<Long, MaterialRespDTO> materialMap) {
        for (WmsCreateOutOrderBO.Line line : bo.getLines()) {
            if (line.getPlanQty() == null || line.getPlanQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            fillMaterialSnapshot(line, materialMap.get(line.getMatId()));
            WmsOutOrderLineDO lineDO = BeanUtils.toBean(line, WmsOutOrderLineDO.class);
            lineDO.setId(null);
            lineDO.setOrderId(order.getId());
            lineDO.setQualityStatus(line.getQualityStatus() == null || line.getQualityStatus().isEmpty()
                    ? WmsQualityStatusEnum.QUALIFIED.name() : line.getQualityStatus());
            lineDO.setAllocatedQty(BigDecimal.ZERO);
            lineDO.setPickedQty(BigDecimal.ZERO);
            lineDO.setPostedQty(BigDecimal.ZERO);
            lineDO.setStatus(WmsOrderLineStatusEnum.DRAFT.name());
            wmsOutOrderLineMapper.insert(lineDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOutOrder(Long orderId, WmsCreateOutOrderBO bo) {
        WmsOutOrderDO order = requireDraft(orderId);
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(OUT_ORDER_LINE_EMPTY);
        }
        List<Long> materialIds = bo.getLines().stream().map(WmsCreateOutOrderBO.Line::getMatId).toList();
        materialApi.validateMaterialList(materialIds);
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(materialIds);
        warehouseService.validateWh(bo.getWhId());

        // 单号、状态、创建幂等键保持原值：它们是单据身份，不随内容改
        WmsWhDO wh = warehouseService.getWh(bo.getWhId());
        WmsOutOrderDO update = BeanUtils.toBean(bo, WmsOutOrderDO.class);
        update.setId(order.getId());
        update.setOutNo(order.getOutNo());
        update.setStatus(order.getStatus());
        update.setIdempotencyKey(order.getIdempotencyKey());
        update.setSourceType(bo.getSourceModule());
        update.setSourceBizNo(bo.getSourceBizNo());
        update.setWhCode(wh == null ? null : wh.getCode());
        wmsOutOrderMapper.updateById(update);

        for (WmsOutOrderLineDO old : wmsOutOrderLineMapper.selectListByOrderId(order.getId())) {
            wmsOutOrderLineMapper.deleteById(old.getId());
        }
        insertLines(update, bo, materialMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutOrder(Long orderId) {
        requireDraft(orderId);
        for (WmsOutOrderLineDO line : wmsOutOrderLineMapper.selectListByOrderId(orderId)) {
            wmsOutOrderLineMapper.deleteById(line.getId());
        }
        wmsOutOrderMapper.deleteById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOutOrder(WmsSubmitOrderBO bo) {
        commandService.executeVoid(WmsCommandTypeEnum.SUBMIT_OUT_ORDER,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsOutOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
                        throw exception(OUT_ORDER_CANNOT_SUBMIT);
                    }
                    if (CollUtil.isEmpty(wmsOutOrderLineMapper.selectListByOrderId(order.getId()))) {
                        throw exception(OUT_ORDER_LINE_EMPTY);
                    }
                    order.setStatus(WmsOrderStatusEnum.SUBMITTED.name());
                    if (bo.getRemark() != null) {
                        order.setRemark(bo.getRemark());
                    }
                    wmsOutOrderMapper.updateById(order);
                });
    }

    /** 取单据并校验处于新建状态。改单与删单的共同前置：已提交的单据不许改行、改数量、改库位。 */
    private WmsOutOrderDO requireDraft(Long orderId) {
        WmsOutOrderDO order = requireOrder(orderId, null);
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(OUT_ORDER_CANNOT_MODIFY);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutOrder(WmsCancelOrderBO bo) {
        WmsOutOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(OUT_ORDER_CANNOT_CANCEL);
        }
        order.setStatus(WmsOrderStatusEnum.CANCELLED.name());
        order.setRemark(bo.getReason());
        wmsOutOrderMapper.updateById(order);
        for (WmsOutOrderLineDO line : wmsOutOrderLineMapper.selectListByOrderId(order.getId())) {
            line.setStatus(WmsOrderLineStatusEnum.CANCELLED.name());
            wmsOutOrderLineMapper.updateById(line);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOutOrder(WmsConfirmOrderBO bo) {
        // 单据级命令幂等：同键重复确认原样返回，不再把 postedQty / pickedQty 加一遍；
        // 行级流水幂等键与预留核销键由过账层继续兜底。状态校验放幂等壳内，
        // 重复提交原样返回而非报非法状态，换键重试才真正走到状态机。
        commandService.executeVoid(WmsCommandTypeEnum.CONFIRM_OUT_ORDER,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsOutOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name().equals(order.getBizType())) {
                        if (!WmsOrderStatusEnum.READY.name().equals(order.getStatus())) {
                            throw exception(OUT_ORDER_OQC_NOT_PASSED);
                        }
                    } else if (!WmsOrderStatusEnum.canPost(order.getStatus())) {
                        throw exception(OUT_ORDER_CANNOT_CONFIRM);
                    }
                    List<WmsOutOrderLineDO> lines = wmsOutOrderLineMapper.selectListByOrderId(order.getId());
                    if (CollUtil.isEmpty(lines)) {
                        throw exception(OUT_ORDER_LINE_EMPTY);
                    }
                    warehouseService.validateWh(order.getWhId());

                    for (WmsOutOrderLineDO line : lines) {
                        if (line.getFromLocId() == null) {
                            throw exception(OUT_ORDER_LOC_REQUIRED);
                        }
                        warehouseService.validateLoc(line.getFromLocId());
                        BigDecimal qty = line.getPlanQty().subtract(nvl(line.getPostedQty()));
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            continue;
                        }
                        // 出库过账前必须校验库位、批次、数量可用
                        BigDecimal available = inventoryService.getAvailableQty(buildQuery(order, line));
                        if (available.compareTo(qty) < 0) {
                            throw exception(INV_AVAILABLE_QTY_NOT_ENOUGH);
                        }

                        String postKey = bo.getIdempotencyKey() + ":POST:" + line.getLineNo();
                        inventoryService.post(buildPostBO(order, line, qty, postKey, bo.getRemark()));
                        // 过账已扣现存，紧接着核销本行预留，否则预留与扣减会双重占用可用量
                        consumeReservation(order, line, qty, bo);

                        line.setPickedQty(nvl(line.getPickedQty()).add(qty));
                        line.setPostedQty(nvl(line.getPostedQty()).add(qty));
                        line.setStatus(WmsOrderLineStatusEnum.DONE.name());
                        wmsOutOrderLineMapper.updateById(line);
                    }

                    order.setStatus(WmsOrderStatusEnum.DONE.name());
                    wmsOutOrderMapper.updateById(order);
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickOutOrder(WmsConfirmOrderBO bo) {
        commandService.executeVoid(WmsCommandTypeEnum.PICK_OUT_ORDER,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null), () -> {
                    WmsOutOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (!WmsOrderStatusEnum.canPost(order.getStatus())) {
                        throw exception(OUT_ORDER_CANNOT_CONFIRM);
                    }
                    List<WmsOutOrderLineDO> lines = wmsOutOrderLineMapper.selectListByOrderId(order.getId());
                    if (CollUtil.isEmpty(lines)) {
                        throw exception(OUT_ORDER_LINE_EMPTY);
                    }
                    for (WmsOutOrderLineDO line : lines) {
                        line.setPickedQty(line.getPlanQty());
                        wmsOutOrderLineMapper.updateById(line);
                    }
                    order.setStatus(WmsOrderStatusEnum.IN_PROGRESS.name());
                    wmsOutOrderMapper.updateById(order);
                    if (WmsOutBizTypeEnum.SALES_DELIVERY_OUT.name().equals(order.getBizType())) {
                        WmsOutOrderLineDO line = lines.get(0);
                        eventPublisher.publishEvent(new OutOrderPickedEvent(order.getId(), order.getOutNo(),
                                line.getMatId(), line.getUomId(), line.getPlanQty()));
                    } else {
                        order.setStatus(WmsOrderStatusEnum.READY.name());
                        wmsOutOrderMapper.updateById(order);
                    }
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveOqcTask(ResolveOqcTaskReqDTO reqDTO) {
        WmsOutOrderDO order = reqDTO.getOutOrderId() == null ? null : wmsOutOrderMapper.selectById(reqDTO.getOutOrderId());
        if (order == null || !WmsOrderStatusEnum.IN_PROGRESS.name().equals(order.getStatus())) {
            return;
        }
        order.setOqcTaskId(reqDTO.getQcTaskId());
        if (reqDTO.getQcTaskId() == null) {
            // 统一“未命中/无任务”的持久化口径：0 而不是 NULL，后续状态判断和报表不用双分支。
            order.setOqcTaskId(0L);
            order.setStatus(WmsOrderStatusEnum.READY.name());
        }
        wmsOutOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOqc(CompleteOqcReqDTO reqDTO) {
        WmsOutOrderDO order = reqDTO.getOutOrderId() == null ? null : wmsOutOrderMapper.selectById(reqDTO.getOutOrderId());
        if (order == null || !WmsOrderStatusEnum.IN_PROGRESS.name().equals(order.getStatus())
                || !java.util.Objects.equals(order.getOqcTaskId(), reqDTO.getQcTaskId())) {
            return;
        }
        order.setStatus(WmsOrderStatusEnum.READY.name());
        wmsOutOrderMapper.updateById(order);
    }

    /**
     * 核销本行关联的预留。
     *
     * <p>出库过账只减 {@code qty_onhand}，预留若不核销就与扣减双重占用可用量：预留 100 出库 100 后
     * 现存已减 100 而 {@code qty_reserved} 仍是 100，可用量凭空少 100。核销后可用量与过账前相同，
     * 这正是"料已从仓库出到车间、仓库可用量本就该是这个数"的账目事实。
     *
     * <p>用 consume 而不是 release：{@code CONSUMED} 表示预留已履约转为实际出库，
     * {@code RELEASED} 表示预留作废库存归还，两者的追溯口径不同。
     *
     * @param order   出库单
     * @param line    出库明细
     * @param postQty 本行本次实际过账数量
     * @param bo      确认参数，提供出库单幂等键与备注
     */
    private void consumeReservation(WmsOutOrderDO order, WmsOutOrderLineDO line, BigDecimal postQty,
                                    WmsConfirmOrderBO bo) {
        Long reservationId = line.getReservationId();
        // 落库恒为 0 或有效编号，判 null 只为兜住 reservation_id 列新增之前的历史行
        if (reservationId == null || WmsConstants.NONE_ID.equals(reservationId)) {
            return;
        }
        WmsInvReservationDO reservation = reservationService.getReservation(reservationId);
        if (reservation == null) {
            // 行上记了预留却查不到，核销无从谈起，可用量会永久偏低，必须拦住而不是静默跳过
            throw exception(RESERVATION_NOT_EXISTS);
        }
        BigDecimal remaining = nvl(reservation.getQty()).subtract(nvl(reservation.getReleasedQty()));
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            // 预留已被核销或释放完（CONSUMED / RELEASED 终态），无需再核销
            return;
        }

        WmsReservationHandleBO handleBO = new WmsReservationHandleBO();
        // 幂等键与本行过账键同源不同后缀：重复确认时预留 Service 按流水判重直接返回，不会二次核销
        handleBO.setIdempotencyKey(bo.getIdempotencyKey() + ":" + line.getLineNo() + ":CONSUME");
        handleBO.setReservationId(reservationId);
        // 过账量超出剩余预留时只核销剩余部分：部分出库与超发出库都是正常业务，不抛异常
        handleBO.setQty(postQty.min(remaining));
        handleBO.setBizType(order.getBizType());
        handleBO.setBizNo(order.getOutNo());
        handleBO.setBizLineNo(String.valueOf(line.getLineNo()));
        handleBO.setRemark(bo.getRemark());
        reservationService.consume(handleBO);
    }

    private WmsInventoryQueryBO buildQuery(WmsOutOrderDO order, WmsOutOrderLineDO line) {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(order.getInvOrgId());
        query.setWhId(order.getWhId());
        query.setLocId(line.getFromLocId());
        query.setMatId(line.getMatId());
        query.setBatchId(line.getBatchId());
        query.setContainerId(line.getContainerId());
        query.setProjectId(order.getProjectId());
        query.setProjectTaskId(order.getProjectTaskId());
        query.setQualityStatus(line.getQualityStatus());
        return query;
    }

    private void fillMaterialSnapshot(WmsCreateOutOrderBO.Line line, MaterialRespDTO material) {
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
    }

    private WmsInvPostBO buildPostBO(WmsOutOrderDO order, WmsOutOrderLineDO line, BigDecimal qty,
                                     String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(WmsTxnTypeEnum.ISSUE.name());
        postBO.setBizType(order.getBizType());
        postBO.setBizNo(order.getOutNo());
        postBO.setBizLineNo(String.valueOf(line.getLineNo()));
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setFromLocId(line.getFromLocId());
        postBO.setFromLocCode(line.getFromLocCode());
        postBO.setQualityStatusFrom(line.getQualityStatus());
        postBO.setQualityStatusTo(line.getQualityStatus());
        postBO.setProjectIdFrom(order.getProjectId());
        postBO.setProjectTaskIdFrom(order.getProjectTaskId());
        postBO.setInvOrgId(order.getInvOrgId());
        postBO.setPlantId(order.getPlantId());
        postBO.setWhId(order.getWhId());
        postBO.setMatId(line.getMatId());
        postBO.setBatchId(line.getBatchId());
        postBO.setContainerId(line.getContainerId());
        postBO.setUomId(line.getUomId());
        postBO.setMatCode(line.getMatCode());
        postBO.setMatName(line.getMatName());
        postBO.setUomCode(line.getUomCode());
        postBO.setLotNo(line.getLotNo());
        postBO.setRemark(remark);
        return postBO;
    }

    private WmsOutOrderDO requireOrder(Long orderId, String orderNo) {
        WmsOutOrderDO order = orderId != null ? wmsOutOrderMapper.selectById(orderId) : null;
        if (order == null && orderNo != null && !orderNo.isEmpty()) {
            order = wmsOutOrderMapper.selectByOutNo(orderNo);
        }
        if (order == null) {
            throw exception(OUT_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public WmsOutOrderDO getOutOrder(Long id) {
        return wmsOutOrderMapper.selectById(id);
    }

    @Override
    public List<WmsOutOrderLineDO> getOutOrderLineList(Long orderId) {
        return wmsOutOrderLineMapper.selectListByOrderId(orderId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
