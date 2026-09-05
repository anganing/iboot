package com.iboot.pf.module.wms.service.inordermgr;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * WMS-入库单 codegen Service 实现类。
 *
 * <p>只读：写入方法已由 T0-17 全部删除，见 {@link InOrderService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class InOrderServiceImpl implements InOrderService {

    @Resource
    private WmsInOrderMapper inOrderMapper;
    @Resource
    private WmsInOrderLineMapper inOrderLineMapper;

    @Override
    public InOrderRespVO getInOrder(Long id) {
        return inOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<InOrderRespVO> getInOrderPage(InOrderPageReqVO pageReqVO) {
        return inOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-入库单明细） ====================

    @Override
    public PageResult<InOrderLineRespVO> getInOrderLinePage(InOrderLinePageReqVO pageReqVO) {
        return inOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public InOrderLineRespVO getInOrderLine(Long id) {
        return inOrderLineMapper.selectJoinById(id);
    }

}
