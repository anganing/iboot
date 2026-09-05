package com.iboot.pf.module.wms.service.inordermgr;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;

/**
 * WMS-入库单 codegen Service 接口。
 *
 * <p><b>只读</b>：单据的创建、改单、删单、状态推进、过账一律走领域侧
 * {@code service.inorder.WmsInOrderService}。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询，
 * 它们能一次带出仓库名、物料名、批次号这类可读字段，领域侧的 DO 查询做不到。
 *
 * @author iboot
 */
public interface InOrderService {

    /**
     * 获得WMS-入库单
     *
     * @param id 编号
     * @return WMS-入库单
     */
    InOrderRespVO getInOrder(Long id);

    /**
     * 获得WMS-入库单分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-入库单分页
     */
    PageResult<InOrderRespVO> getInOrderPage(InOrderPageReqVO pageReqVO);

    // ==================== 子表（WMS-入库单明细） ====================

    /**
     * 获得WMS-入库单明细分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-入库单明细分页
     */
    PageResult<InOrderLineRespVO> getInOrderLinePage(InOrderLinePageReqVO pageReqVO);

    /**
     * 获得WMS-入库单明细
     *
     * @param id 编号
     * @return WMS-入库单明细
     */
    InOrderLineRespVO getInOrderLine(Long id);

}
