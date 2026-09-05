package com.iboot.pf.module.wms.service.invlock;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;

/**
 * WMS-库存冻结记录 codegen Service 接口。
 *
 * <p><b>只读</b>：冻结与解冻一律走领域侧 {@code service.lock.WmsInventoryLockService}。原因是
 * codegen 侧那套 CRUD 只往 {@code wms_inv_lock} 插记录、不改 {@code wms_inv_balance.qty_frozen}，
 * 该动不了的货照样被领走。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询与物料名批量补全。
 *
 * @author iboot
 */
public interface InvLockService {

    /**
     * 获得WMS-库存冻结记录
     *
     * @param id 编号
     * @return WMS-库存冻结记录
     */
    InvLockRespVO getInvLock(Long id);

    /**
     * 获得WMS-库存冻结记录分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存冻结记录分页
     */
    PageResult<InvLockRespVO> getInvLockPage(InvLockPageReqVO pageReqVO);

}
