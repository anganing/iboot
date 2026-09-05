package com.iboot.pf.module.wms.service.outordermgr;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;

/**
 * WMS-出库单 codegen Service 接口。
 *
 * <p><b>只读</b>：单据的创建、改单、删单、状态推进、过账一律走领域侧
 * {@code service.outorder.WmsOutOrderService}。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询。
 *
 * @author iboot
 */
public interface OutOrderService {

    /**
     * 获得WMS-出库单
     *
     * @param id 编号
     * @return WMS-出库单
     */
    OutOrderRespVO getOutOrder(Long id);

    /**
     * 获得WMS-出库单分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-出库单分页
     */
    PageResult<OutOrderRespVO> getOutOrderPage(OutOrderPageReqVO pageReqVO);

    // ==================== 子表（WMS-出库单明细） ====================

    /**
     * 获得WMS-出库单明细分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-出库单明细分页
     */
    PageResult<OutOrderLineRespVO> getOutOrderLinePage(OutOrderLinePageReqVO pageReqVO);

    /**
     * 获得WMS-出库单明细
     *
     * @param id 编号
     * @return WMS-出库单明细
     */
    OutOrderLineRespVO getOutOrderLine(Long id);

}
