package com.iboot.pf.module.wms.service.countorder;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * WMS-盘点单 codegen Service 实现类。
 *
 * <p>只读：写入方法已由 T0-17 全部删除，见 {@link CountOrderService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class CountOrderServiceImpl implements CountOrderService {

    @Resource
    private WmsCountOrderMapper countOrderMapper;
    @Resource
    private WmsCountOrderLineMapper countOrderLineMapper;

    @Override
    public CountOrderRespVO getCountOrder(Long id) {
        return countOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<CountOrderRespVO> getCountOrderPage(CountOrderPageReqVO pageReqVO) {
        return countOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-盘点明细） ====================

    @Override
    public PageResult<CountOrderLineRespVO> getCountOrderLinePage(CountOrderLinePageReqVO pageReqVO) {
        return countOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public CountOrderLineRespVO getCountOrderLine(Long id) {
        return countOrderLineMapper.selectJoinById(id);
    }

}
