package com.iboot.pf.module.wms.service.lock;

import com.iboot.pf.module.wms.dal.dataobject.lock.WmsInvLockDO;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockQueryBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsUnlockBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 库存冻结 Service。冻结只占用可用量，不扣减在库量。 */
public interface WmsInventoryLockService {

    /**
     * 冻结库存，幂等。
     *
     * @param bo 冻结参数
     * @return 冻结记录编号；幂等命中时返回原记录编号
     */
    Long lock(WmsLockBO bo);

    /**
     * 解冻库存，幂等。
     *
     * @param bo 解冻参数
     */
    void unlock(WmsUnlockBO bo);

    WmsInvLockDO getLock(Long id);

    List<WmsInvLockDO> getLockList(Collection<Long> ids);

    Map<Long, WmsInvLockDO> getLockMap(Collection<Long> ids);

    List<WmsInvLockDO> getLockList(WmsLockQueryBO query);

}
