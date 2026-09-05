package com.iboot.pf.module.wms.service.outordermgr;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * WMS-出库单 codegen Service 实现类。
 *
 * <p>只读：写入方法已由 T0-17 全部删除，见 {@link OutOrderService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class OutOrderServiceImpl implements OutOrderService {

    @Resource
    private WmsOutOrderMapper outOrderMapper;
    @Resource
    private WmsOutOrderLineMapper outOrderLineMapper;

    @Override
    public OutOrderRespVO getOutOrder(Long id) {
        return outOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<OutOrderRespVO> getOutOrderPage(OutOrderPageReqVO pageReqVO) {
        return outOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-出库单明细） ====================

    @Override
    public PageResult<OutOrderLineRespVO> getOutOrderLinePage(OutOrderLinePageReqVO pageReqVO) {
        return outOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public OutOrderLineRespVO getOutOrderLine(Long id) {
        return outOrderLineMapper.selectJoinById(id);
    }

}
