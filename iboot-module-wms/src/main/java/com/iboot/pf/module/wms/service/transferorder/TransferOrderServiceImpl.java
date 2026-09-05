package com.iboot.pf.module.wms.service.transferorder;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.module.wms.dal.mysql.transfer.WmsTransferOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.transfer.WmsTransferOrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * WMS-库存转移调拨单 codegen Service 实现类。
 *
 * <p>只读：写入方法已由 T0-17 全部删除，见 {@link TransferOrderService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class TransferOrderServiceImpl implements TransferOrderService {

    @Resource
    private WmsTransferOrderMapper transferOrderMapper;
    @Resource
    private WmsTransferOrderLineMapper transferOrderLineMapper;

    @Override
    public TransferOrderRespVO getTransferOrder(Long id) {
        return transferOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TransferOrderRespVO> getTransferOrderPage(TransferOrderPageReqVO pageReqVO) {
        return transferOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-库存转移调拨明细） ====================

    @Override
    public PageResult<TransferOrderLineRespVO> getTransferOrderLinePage(TransferOrderLinePageReqVO pageReqVO) {
        return transferOrderLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public TransferOrderLineRespVO getTransferOrderLine(Long id) {
        return transferOrderLineMapper.selectJoinById(id);
    }

}
