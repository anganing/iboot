package com.iboot.pf.module.wms.service.reservation;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.module.wms.dal.mysql.reservation.WmsInvReservationMapper;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReserveBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_AVAILABLE_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_NOT_ACTIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_REMAIN_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.RESERVATION;

/** 库存预留 Service 实现。 */
@Service
public class WmsInventoryReservationServiceImpl implements WmsInventoryReservationService {

    /** 过账幂等键后缀，预留命令与它触发的流水各用一个键。 */
    private static final String POST_KEY_SUFFIX = ":POST";

    @Resource
    private WmsInvReservationMapper wmsInvReservationMapper;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsCodeService codeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reserve(WmsReserveBO bo) {
        WmsInvReservationDO existed = wmsInvReservationMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (bo.getQty() == null || bo.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        String qualityStatus = bo.getQualityStatus() == null || bo.getQualityStatus().isEmpty()
                ? WmsQualityStatusEnum.QUALIFIED.name() : bo.getQualityStatus();

        // 预留必须校验可用量充足
        BigDecimal available = inventoryService.getAvailableQty(buildQuery(bo, qualityStatus));
        if (available.compareTo(bo.getQty()) < 0) {
            throw exception(INV_AVAILABLE_QTY_NOT_ENOUGH);
        }

        WmsInvReservationDO reservation = new WmsInvReservationDO();
        reservation.setId(null);
        reservation.setReservationNo(codeService.generate(RESERVATION, null));
        reservation.setBizType(bo.getBizType());
        reservation.setBizNo(bo.getBizNo());
        reservation.setBizLineNo(bo.getBizLineNo());
        reservation.setInvOrgId(bo.getInvOrgId());
        reservation.setWhId(bo.getWhId());
        reservation.setLocId(bo.getLocId());
        reservation.setMatId(bo.getMatId());
        reservation.setBatchId(bo.getBatchId());
        reservation.setContainerId(bo.getContainerId());
        reservation.setProjectId(bo.getProjectId());
        reservation.setProjectTaskId(bo.getProjectTaskId());
        reservation.setQualityStatus(qualityStatus);
        reservation.setQty(bo.getQty());
        reservation.setReleasedQty(BigDecimal.ZERO);
        reservation.setStatus(WmsReservationStatusEnum.ACTIVE.name());
        reservation.setExpireTime(bo.getExpireTime());
        reservation.setIdempotencyKey(bo.getIdempotencyKey());
        reservation.setRemark(bo.getRemark());
        wmsInvReservationMapper.insert(reservation);

        inventoryService.post(buildPostBO(reservation, WmsTxnTypeEnum.RESERVE, bo.getQty(),
                bo.getIdempotencyKey() + POST_KEY_SUFFIX, bo.getRemark()));
        return reservation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(WmsReservationHandleBO bo) {
        handle(bo, WmsTxnTypeEnum.RELEASE, WmsReservationStatusEnum.RELEASED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(WmsReservationHandleBO bo) {
        handle(bo, WmsTxnTypeEnum.RELEASE, WmsReservationStatusEnum.CONSUMED);
    }

    private void handle(WmsReservationHandleBO bo, WmsTxnTypeEnum txnType, WmsReservationStatusEnum targetStatus) {
        WmsInvReservationDO reservation = requireReservation(bo);
        // 幂等：该命令生成的流水已存在，说明已处理过
        if (inventoryService.posted(bo.getIdempotencyKey() + POST_KEY_SUFFIX)) {
            return;
        }
        String status = reservation.getStatus();
        if (WmsReservationStatusEnum.RELEASED.name().equals(status)
                || WmsReservationStatusEnum.CONSUMED.name().equals(status)
                || WmsReservationStatusEnum.CANCELLED.name().equals(status)) {
            throw exception(RESERVATION_NOT_ACTIVE);
        }
        BigDecimal remaining = reservation.getQty().subtract(nvl(reservation.getReleasedQty()));
        BigDecimal qty = bo.getQty() == null ? remaining : bo.getQty();
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        if (qty.compareTo(remaining) > 0) {
            throw exception(RESERVATION_REMAIN_NOT_ENOUGH);
        }

        reservation.setReleasedQty(nvl(reservation.getReleasedQty()).add(qty));
        reservation.setStatus(reservation.getReleasedQty().compareTo(reservation.getQty()) >= 0
                ? targetStatus.name() : WmsReservationStatusEnum.PARTIAL.name());
        reservation.setRemark(bo.getRemark());
        wmsInvReservationMapper.updateById(reservation);

        inventoryService.post(buildPostBO(reservation, txnType, qty,
                bo.getIdempotencyKey() + POST_KEY_SUFFIX, bo.getRemark()));
    }

    private WmsInvReservationDO requireReservation(WmsReservationHandleBO bo) {
        WmsInvReservationDO reservation = bo.getReservationId() != null
                ? wmsInvReservationMapper.selectById(bo.getReservationId()) : null;
        if (reservation == null && bo.getReservationNo() != null && !bo.getReservationNo().isEmpty()) {
            reservation = wmsInvReservationMapper.selectByReservationNo(bo.getReservationNo());
        }
        if (reservation == null) {
            throw exception(RESERVATION_NOT_EXISTS);
        }
        return reservation;
    }

    private WmsInventoryQueryBO buildQuery(WmsReserveBO bo, String qualityStatus) {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(bo.getInvOrgId());
        query.setWhId(bo.getWhId());
        query.setLocId(bo.getLocId());
        query.setMatId(bo.getMatId());
        query.setBatchId(bo.getBatchId());
        query.setContainerId(bo.getContainerId());
        query.setProjectId(bo.getProjectId());
        query.setProjectTaskId(bo.getProjectTaskId());
        query.setQualityStatus(qualityStatus);
        return query;
    }

    private WmsInvPostBO buildPostBO(WmsInvReservationDO reservation, WmsTxnTypeEnum txnType, BigDecimal qty,
                                     String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(txnType.name());
        postBO.setBizType(reservation.getBizType());
        postBO.setBizNo(reservation.getBizNo());
        postBO.setBizLineNo(reservation.getBizLineNo());
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setFromLocId(reservation.getLocId());
        postBO.setQualityStatusFrom(reservation.getQualityStatus());
        postBO.setQualityStatusTo(reservation.getQualityStatus());
        postBO.setProjectIdFrom(reservation.getProjectId());
        postBO.setProjectTaskIdFrom(reservation.getProjectTaskId());
        postBO.setInvOrgId(reservation.getInvOrgId());
        postBO.setWhId(reservation.getWhId());
        postBO.setMatId(reservation.getMatId());
        postBO.setBatchId(reservation.getBatchId());
        postBO.setContainerId(reservation.getContainerId());
        postBO.setRemark(remark);
        return postBO;
    }

    @Override
    public WmsInvReservationDO getReservation(Long id) {
        return wmsInvReservationMapper.selectById(id);
    }

    @Override
    public List<WmsInvReservationDO> getReservationList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsInvReservationMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsInvReservationDO> getReservationMap(Collection<Long> ids) {
        return convertMap(getReservationList(ids), WmsInvReservationDO::getId);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
