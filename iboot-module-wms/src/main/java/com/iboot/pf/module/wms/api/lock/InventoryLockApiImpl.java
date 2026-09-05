package com.iboot.pf.module.wms.api.lock;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.lock.dto.CreateLockReqDTO;
import com.iboot.pf.module.wms.api.lock.dto.InventoryLockQueryReqDTO;
import com.iboot.pf.module.wms.api.lock.dto.InventoryLockRespDTO;
import com.iboot.pf.module.wms.api.lock.dto.ReleaseLockReqDTO;
import com.iboot.pf.module.wms.service.lock.WmsInventoryLockService;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockQueryBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsUnlockBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 库存冻结 API 实现类。
 *
 * <p>命令方法由 {@link WmsInventoryLockService} 承载事务与幂等，本类只做参数转换。
 */
@Service
@Validated
@DataPermission(enable = false)
public class InventoryLockApiImpl implements InventoryLockApi {

    @Resource
    private WmsInventoryLockService lockService;

    @Override
    public Long lockInventory(CreateLockReqDTO reqDTO) {
        WmsLockBO bo = BeanUtils.toBean(reqDTO, WmsLockBO.class);
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setBizNo(reqDTO.getSourceBizNo());
        return lockService.lock(bo);
    }

    @Override
    public void unlockInventory(ReleaseLockReqDTO reqDTO) {
        WmsUnlockBO bo = new WmsUnlockBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setLockId(reqDTO.getLockId());
        bo.setLockNo(reqDTO.getLockNo());
        bo.setQty(reqDTO.getQty());
        bo.setRemark(reqDTO.getRemark());
        lockService.unlock(bo);
    }

    @Override
    public InventoryLockRespDTO getLock(Long id) {
        return BeanUtils.toBean(lockService.getLock(id), InventoryLockRespDTO.class);
    }

    @Override
    public List<InventoryLockRespDTO> getLockList(Collection<Long> ids) {
        return BeanUtils.toBean(lockService.getLockList(ids), InventoryLockRespDTO.class);
    }

    @Override
    public Map<Long, InventoryLockRespDTO> getLockMap(Collection<Long> ids) {
        return convertMap(BeanUtils.toBean(lockService.getLockList(ids), InventoryLockRespDTO.class),
                InventoryLockRespDTO::getId);
    }

    @Override
    public List<InventoryLockRespDTO> getLockList(InventoryLockQueryReqDTO query) {
        WmsLockQueryBO bo = new WmsLockQueryBO();
        bo.setInvOrgId(query.getInvOrgId());
        bo.setWhId(query.getWhId());
        bo.setLocId(query.getLocId());
        bo.setMatId(query.getMatId());
        bo.setBatchId(query.getBatchId());
        bo.setContainerId(query.getContainerId());
        bo.setStatus(query.getStatus());
        return BeanUtils.toBean(lockService.getLockList(bo), InventoryLockRespDTO.class);
    }

}
