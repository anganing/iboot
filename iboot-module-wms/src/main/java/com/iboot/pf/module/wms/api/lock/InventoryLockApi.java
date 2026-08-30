package com.iboot.pf.module.wms.api.lock;

import com.iboot.pf.module.wms.api.lock.dto.CreateLockReqDTO;
import com.iboot.pf.module.wms.api.lock.dto.InventoryLockQueryReqDTO;
import com.iboot.pf.module.wms.api.lock.dto.InventoryLockRespDTO;
import com.iboot.pf.module.wms.api.lock.dto.ReleaseLockReqDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 库存冻结 API 门面。
 *
 * <p>向 MES / UI 提供冻结、解冻与冻结记录查询。冻结只占用可用量，不扣减在库量。
 */
public interface InventoryLockApi {

    /**
     * 冻结库存，幂等。
     *
     * @param reqDTO 冻结命令
     * @return 冻结记录编号；幂等命中时返回原编号
     */
    Long lockInventory(CreateLockReqDTO reqDTO);

    /**
     * 解冻库存，幂等。
     *
     * @param reqDTO 解冻命令
     */
    void unlockInventory(ReleaseLockReqDTO reqDTO);

    InventoryLockRespDTO getLock(Long id);

    List<InventoryLockRespDTO> getLockList(Collection<Long> ids);

    Map<Long, InventoryLockRespDTO> getLockMap(Collection<Long> ids);

    /** 按条件查询冻结记录。 */
    List<InventoryLockRespDTO> getLockList(InventoryLockQueryReqDTO query);

}
