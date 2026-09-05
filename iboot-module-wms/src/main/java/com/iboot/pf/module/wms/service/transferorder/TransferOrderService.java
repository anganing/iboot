package com.iboot.pf.module.wms.service.transferorder;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;

/**
 * WMS-库存转移调拨单 codegen Service 接口。
 *
 * <p><b>只读</b>：单据的创建、改单、删单、状态推进、过账一律走领域侧
 * {@code service.transfer.WmsTransferOrderService}。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询。
 *
 * @author iboot
 */
public interface TransferOrderService {

    /**
     * 获得WMS-库存转移调拨单
     *
     * @param id 编号
     * @return WMS-库存转移调拨单
     */
    TransferOrderRespVO getTransferOrder(Long id);

    /**
     * 获得WMS-库存转移调拨单分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存转移调拨单分页
     */
    PageResult<TransferOrderRespVO> getTransferOrderPage(TransferOrderPageReqVO pageReqVO);

    // ==================== 子表（WMS-库存转移调拨明细） ====================

    /**
     * 获得WMS-库存转移调拨明细分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存转移调拨明细分页
     */
    PageResult<TransferOrderLineRespVO> getTransferOrderLinePage(TransferOrderLinePageReqVO pageReqVO);

    /**
     * 获得WMS-库存转移调拨明细
     *
     * @param id 编号
     * @return WMS-库存转移调拨明细
     */
    TransferOrderLineRespVO getTransferOrderLine(Long id);

}
