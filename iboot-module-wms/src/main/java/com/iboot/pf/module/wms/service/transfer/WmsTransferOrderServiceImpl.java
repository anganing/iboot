package com.iboot.pf.module.wms.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderLineDO;
import com.iboot.pf.module.wms.dal.mysql.transfer.WmsTransferOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.transfer.WmsTransferOrderMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandService;
import com.iboot.pf.module.wms.service.command.bo.WmsCommandBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.transfer.bo.WmsCreateTransferOrderBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_SUBMIT;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_LOC_REQUIRED;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_SAME_TARGET;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.TRANSFER_ORDER;

/** 转移调拨单 Service 实现。 */
@Service
public class WmsTransferOrderServiceImpl implements WmsTransferOrderService {

    @Resource
    private WmsTransferOrderMapper wmsTransferOrderMapper;
    @Resource
    private WmsTransferOrderLineMapper wmsTransferOrderLineMapper;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsCommandService commandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransferOrder(WmsCreateTransferOrderBO bo) {
        WmsTransferOrderDO existed = wmsTransferOrderMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(TRANSFER_ORDER_LINE_EMPTY);
        }

        WmsTransferOrderDO order = BeanUtils.toBean(bo, WmsTransferOrderDO.class);
        order.setId(null);
        order.setTransferNo(codeService.generate(TRANSFER_ORDER, bo.getTransferNo()));
        order.setSourceType(bo.getSourceModule());
        order.setSourceBizNo(bo.getSourceBizNo());
        order.setStatus(WmsOrderStatusEnum.DRAFT.name());
        wmsTransferOrderMapper.insert(order);

        insertLines(order, bo);
        return order.getId();
    }

    /** 按参数建转移行。创建与修改共用：修改走行的全量替换，建行规则与创建时必须一致。 */
    private void insertLines(WmsTransferOrderDO order, WmsCreateTransferOrderBO bo) {
        for (WmsCreateTransferOrderBO.Line line : bo.getLines()) {
            if (line.getPlanQty() == null || line.getPlanQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(QTY_MUST_BE_POSITIVE);
            }
            WmsTransferOrderLineDO lineDO = BeanUtils.toBean(line, WmsTransferOrderLineDO.class);
            lineDO.setId(null);
            lineDO.setOrderId(order.getId());
            lineDO.setQualityStatusFrom(line.getQualityStatusFrom() == null || line.getQualityStatusFrom().isEmpty()
                    ? WmsQualityStatusEnum.QUALIFIED.name() : line.getQualityStatusFrom());
            lineDO.setQualityStatusTo(line.getQualityStatusTo() == null || line.getQualityStatusTo().isEmpty()
                    ? lineDO.getQualityStatusFrom() : line.getQualityStatusTo());
            lineDO.setMovedQty(BigDecimal.ZERO);
            lineDO.setPostedQty(BigDecimal.ZERO);
            lineDO.setStatus(WmsOrderLineStatusEnum.DRAFT.name());
            wmsTransferOrderLineMapper.insert(lineDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransferOrder(Long orderId, WmsCreateTransferOrderBO bo) {
        WmsTransferOrderDO order = requireDraft(orderId);
        if (CollUtil.isEmpty(bo.getLines())) {
            throw exception(TRANSFER_ORDER_LINE_EMPTY);
        }
        // 单号、状态、创建幂等键保持原值：它们是单据身份，不随内容改
        WmsTransferOrderDO update = BeanUtils.toBean(bo, WmsTransferOrderDO.class);
        update.setId(order.getId());
        update.setTransferNo(order.getTransferNo());
        update.setStatus(order.getStatus());
        update.setIdempotencyKey(order.getIdempotencyKey());
        update.setSourceType(bo.getSourceModule());
        update.setSourceBizNo(bo.getSourceBizNo());
        wmsTransferOrderMapper.updateById(update);

        for (WmsTransferOrderLineDO old : wmsTransferOrderLineMapper.selectListByOrderId(order.getId())) {
            wmsTransferOrderLineMapper.deleteById(old.getId());
        }
        insertLines(update, bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransferOrder(Long orderId) {
        requireDraft(orderId);
        for (WmsTransferOrderLineDO line : wmsTransferOrderLineMapper.selectListByOrderId(orderId)) {
            wmsTransferOrderLineMapper.deleteById(line.getId());
        }
        wmsTransferOrderMapper.deleteById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTransferOrder(WmsSubmitOrderBO bo) {
        commandService.executeVoid(WmsCommandTypeEnum.SUBMIT_TRANSFER,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsTransferOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
                        throw exception(TRANSFER_ORDER_CANNOT_SUBMIT);
                    }
                    if (CollUtil.isEmpty(wmsTransferOrderLineMapper.selectListByOrderId(order.getId()))) {
                        throw exception(TRANSFER_ORDER_LINE_EMPTY);
                    }
                    order.setStatus(WmsOrderStatusEnum.SUBMITTED.name());
                    if (bo.getRemark() != null) {
                        order.setRemark(bo.getRemark());
                    }
                    wmsTransferOrderMapper.updateById(order);
                });
    }

    /** 取单据并校验处于新建状态。改单与删单的共同前置：已提交的单据不许改行、改数量、改库位。 */
    private WmsTransferOrderDO requireDraft(Long orderId) {
        WmsTransferOrderDO order = requireOrder(orderId, null);
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(TRANSFER_ORDER_CANNOT_MODIFY);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransferOrder(WmsCancelOrderBO bo) {
        WmsTransferOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
        if (!WmsOrderStatusEnum.isDraft(order.getStatus())) {
            throw exception(TRANSFER_ORDER_CANNOT_CANCEL);
        }
        order.setStatus(WmsOrderStatusEnum.CANCELLED.name());
        order.setRemark(bo.getReason());
        wmsTransferOrderMapper.updateById(order);
        for (WmsTransferOrderLineDO line : wmsTransferOrderLineMapper.selectListByOrderId(order.getId())) {
            line.setStatus(WmsOrderLineStatusEnum.CANCELLED.name());
            wmsTransferOrderLineMapper.updateById(line);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmTransferOrder(WmsConfirmOrderBO bo) {
        // 单据级命令幂等：同键重复确认原样返回，不再把 postedQty / movedQty 加一遍；
        // 行级流水幂等键（:POST:{行号}）由过账层继续兜底。状态校验放幂等壳内，
        // 重复提交原样返回而非报非法状态，换键重试才真正走到状态机。
        commandService.executeVoid(WmsCommandTypeEnum.CONFIRM_TRANSFER,
                WmsCommandBO.of(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo(), null),
                () -> {
                    WmsTransferOrderDO order = requireOrder(bo.getOrderId(), bo.getOrderNo());
                    if (!WmsOrderStatusEnum.canPost(order.getStatus())) {
                        throw exception(TRANSFER_ORDER_CANNOT_CONFIRM);
                    }
                    List<WmsTransferOrderLineDO> lines = wmsTransferOrderLineMapper.selectListByOrderId(order.getId());
                    if (CollUtil.isEmpty(lines)) {
                        throw exception(TRANSFER_ORDER_LINE_EMPTY);
                    }

                    for (WmsTransferOrderLineDO line : lines) {
                        if (line.getFromLocId() == null || line.getToLocId() == null) {
                            throw exception(TRANSFER_ORDER_LOC_REQUIRED);
                        }
                        if (sameTarget(order, line)) {
                            throw exception(TRANSFER_ORDER_SAME_TARGET);
                        }
                        BigDecimal qty = line.getPlanQty().subtract(nvl(line.getPostedQty()));
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            continue;
                        }

                        String postKey = bo.getIdempotencyKey() + ":POST:" + line.getLineNo();
                        inventoryService.post(buildPostBO(order, line, qty, postKey, bo.getRemark()));

                        line.setMovedQty(nvl(line.getMovedQty()).add(qty));
                        line.setPostedQty(nvl(line.getPostedQty()).add(qty));
                        line.setStatus(WmsOrderLineStatusEnum.DONE.name());
                        wmsTransferOrderLineMapper.updateById(line);
                    }

                    order.setStatus(WmsOrderStatusEnum.DONE.name());
                    wmsTransferOrderMapper.updateById(order);
                });
    }

    /** 来源与目标维度完全一致时没有转移意义，直接拒绝，避免产生无意义流水。 */
    private boolean sameTarget(WmsTransferOrderDO order, WmsTransferOrderLineDO line) {
        return ObjUtil.equal(order.getFromInvOrgId(), order.getToInvOrgId())
                && ObjUtil.equal(order.getFromWhId(), order.getToWhId())
                && ObjUtil.equal(line.getFromLocId(), line.getToLocId())
                && ObjUtil.equal(nvlId(line.getFromProjectId()), nvlId(line.getToProjectId()))
                && ObjUtil.equal(line.getQualityStatusFrom(), line.getQualityStatusTo());
    }

    private WmsInvPostBO buildPostBO(WmsTransferOrderDO order, WmsTransferOrderLineDO line, BigDecimal qty,
                                     String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(WmsTxnTypeEnum.TRANSFER.name());
        postBO.setBizType(order.getTransferType());
        postBO.setBizNo(order.getTransferNo());
        postBO.setBizLineNo(String.valueOf(line.getLineNo()));
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setFromInvOrgId(order.getFromInvOrgId());
        postBO.setToInvOrgId(order.getToInvOrgId());
        postBO.setFromWhId(order.getFromWhId());
        postBO.setToWhId(order.getToWhId());
        postBO.setFromLocId(line.getFromLocId());
        postBO.setToLocId(line.getToLocId());
        postBO.setQualityStatusFrom(line.getQualityStatusFrom());
        postBO.setQualityStatusTo(line.getQualityStatusTo());
        postBO.setFromStockScope(line.getFromStockScope());
        postBO.setToStockScope(line.getToStockScope());
        postBO.setProjectIdFrom(line.getFromProjectId());
        postBO.setProjectTaskIdFrom(line.getFromProjectTaskId());
        postBO.setProjectIdTo(line.getToProjectId());
        postBO.setProjectTaskIdTo(line.getToProjectTaskId());
        postBO.setInvOrgId(order.getFromInvOrgId());
        postBO.setWhId(order.getFromWhId());
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

    private WmsTransferOrderDO requireOrder(Long orderId, String orderNo) {
        WmsTransferOrderDO order = orderId != null ? wmsTransferOrderMapper.selectById(orderId) : null;
        if (order == null && orderNo != null && !orderNo.isEmpty()) {
            order = wmsTransferOrderMapper.selectByTransferNo(orderNo);
        }
        if (order == null) {
            throw exception(TRANSFER_ORDER_NOT_EXISTS);
        }
        return order;
    }

    @Override
    public WmsTransferOrderDO getTransferOrder(Long id) {
        return wmsTransferOrderMapper.selectById(id);
    }

    @Override
    public List<WmsTransferOrderLineDO> getTransferOrderLineList(Long orderId) {
        return wmsTransferOrderLineMapper.selectListByOrderId(orderId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static Long nvlId(Long value) {
        return value == null ? WmsConstants.NONE_ID : value;
    }

}
