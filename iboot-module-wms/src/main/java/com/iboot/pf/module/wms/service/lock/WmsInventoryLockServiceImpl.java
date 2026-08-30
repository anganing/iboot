package com.iboot.pf.module.wms.service.lock;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.wms.dal.dataobject.lock.WmsInvLockDO;
import com.iboot.pf.module.wms.dal.mysql.lock.WmsInvLockMapper;
import com.iboot.pf.module.wms.enums.WmsLockStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockQueryBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsUnlockBO;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_NOT_ACTIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_REASON_BLANK;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_REMAIN_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.wms.enums.WmsCodeRuleConstants.LOCK;

/** 库存冻结 Service 实现。 */
@Service
public class WmsInventoryLockServiceImpl implements WmsInventoryLockService {

    private static final String POST_KEY_SUFFIX = ":POST";

    @Resource
    private WmsInvLockMapper wmsInvLockMapper;
    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsCodeService codeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long lock(WmsLockBO bo) {
        WmsInvLockDO existed = wmsInvLockMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        if (bo.getQty() == null || bo.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        if (bo.getReason() == null || bo.getReason().isEmpty()) {
            throw exception(LOCK_REASON_BLANK);
        }

        WmsInvLockDO lock = new WmsInvLockDO();
        lock.setId(null);
        lock.setLockNo(codeService.generate(LOCK, null));
        lock.setBizType(bo.getBizType());
        lock.setBizNo(bo.getBizNo());
        lock.setInvOrgId(bo.getInvOrgId());
        lock.setWhId(bo.getWhId());
        lock.setLocId(bo.getLocId());
        lock.setMatId(bo.getMatId());
        lock.setBatchId(bo.getBatchId());
        lock.setContainerId(bo.getContainerId());
        lock.setProjectId(bo.getProjectId());
        lock.setProjectTaskId(bo.getProjectTaskId());
        lock.setQualityStatus(bo.getQualityStatus() == null || bo.getQualityStatus().isEmpty()
                ? WmsQualityStatusEnum.QUALIFIED.name() : bo.getQualityStatus());
        lock.setQty(bo.getQty());
        lock.setReleasedQty(BigDecimal.ZERO);
        lock.setReason(bo.getReason());
        lock.setStatus(WmsLockStatusEnum.ACTIVE.name());
        lock.setIdempotencyKey(bo.getIdempotencyKey());
        lock.setRemark(bo.getRemark());
        wmsInvLockMapper.insert(lock);

        inventoryService.post(buildPostBO(lock, WmsTxnTypeEnum.FREEZE, bo.getQty(),
                bo.getIdempotencyKey() + POST_KEY_SUFFIX, bo.getRemark()));
        return lock.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlock(WmsUnlockBO bo) {
        WmsInvLockDO lock = bo.getLockId() != null ? wmsInvLockMapper.selectById(bo.getLockId()) : null;
        if (lock == null && bo.getLockNo() != null && !bo.getLockNo().isEmpty()) {
            lock = wmsInvLockMapper.selectByLockNo(bo.getLockNo());
        }
        if (lock == null) {
            throw exception(LOCK_NOT_EXISTS);
        }
        if (inventoryService.posted(bo.getIdempotencyKey() + POST_KEY_SUFFIX)) {
            return;
        }
        String status = lock.getStatus();
        if (WmsLockStatusEnum.RELEASED.name().equals(status)
                || WmsLockStatusEnum.CANCELLED.name().equals(status)) {
            throw exception(LOCK_NOT_ACTIVE);
        }
        BigDecimal remaining = lock.getQty().subtract(nvl(lock.getReleasedQty()));
        BigDecimal qty = bo.getQty() == null ? remaining : bo.getQty();
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        if (qty.compareTo(remaining) > 0) {
            throw exception(LOCK_REMAIN_NOT_ENOUGH);
        }

        lock.setReleasedQty(nvl(lock.getReleasedQty()).add(qty));
        lock.setStatus(lock.getReleasedQty().compareTo(lock.getQty()) >= 0
                ? WmsLockStatusEnum.RELEASED.name() : WmsLockStatusEnum.PARTIAL.name());
        lock.setRemark(bo.getRemark());
        wmsInvLockMapper.updateById(lock);

        inventoryService.post(buildPostBO(lock, WmsTxnTypeEnum.UNFREEZE, qty,
                bo.getIdempotencyKey() + POST_KEY_SUFFIX, bo.getRemark()));
    }

    private WmsInvPostBO buildPostBO(WmsInvLockDO lock, WmsTxnTypeEnum txnType, BigDecimal qty,
                                     String postKey, String remark) {
        WmsInvPostBO postBO = new WmsInvPostBO();
        postBO.setTxnType(txnType.name());
        postBO.setBizType(lock.getBizType());
        postBO.setBizNo(lock.getBizNo());
        postBO.setIdempotencyKey(postKey);
        postBO.setQty(qty);
        postBO.setFromLocId(lock.getLocId());
        postBO.setQualityStatusFrom(lock.getQualityStatus());
        postBO.setQualityStatusTo(lock.getQualityStatus());
        postBO.setProjectIdFrom(lock.getProjectId());
        postBO.setProjectTaskIdFrom(lock.getProjectTaskId());
        postBO.setInvOrgId(lock.getInvOrgId());
        postBO.setWhId(lock.getWhId());
        postBO.setMatId(lock.getMatId());
        postBO.setBatchId(lock.getBatchId());
        postBO.setContainerId(lock.getContainerId());
        postBO.setRemark(remark);
        return postBO;
    }

    @Override
    public WmsInvLockDO getLock(Long id) {
        return wmsInvLockMapper.selectById(id);
    }

    @Override
    public List<WmsInvLockDO> getLockList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsInvLockMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsInvLockDO> getLockMap(Collection<Long> ids) {
        return convertMap(getLockList(ids), WmsInvLockDO::getId);
    }

    @Override
    public List<WmsInvLockDO> getLockList(WmsLockQueryBO query) {
        return wmsInvLockMapper.selectLockList(query);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
