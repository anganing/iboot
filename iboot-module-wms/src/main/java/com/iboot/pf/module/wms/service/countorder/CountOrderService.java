package com.iboot.pf.module.wms.service.countorder;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;

/**
 * WMS-盘点单 codegen Service 接口。
 *
 * <p><b>只读</b>：盘点单的创建、改单、删单、冻结、录实盘、差异确认一律走领域侧
 * {@code service.count.WmsCountOrderService}。盘点明细尤其不许从这里写——{@code book_qty}
 * 是账实对比的基准，只能由领域侧按余额快照生成。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询。
 *
 * @author iboot
 */
public interface CountOrderService {

    /**
     * 获得WMS-盘点单
     *
     * @param id 编号
     * @return WMS-盘点单
     */
    CountOrderRespVO getCountOrder(Long id);

    /**
     * 获得WMS-盘点单分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-盘点单分页
     */
    PageResult<CountOrderRespVO> getCountOrderPage(CountOrderPageReqVO pageReqVO);

    // ==================== 子表（WMS-盘点明细） ====================

    /**
     * 获得WMS-盘点明细分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-盘点明细分页
     */
    PageResult<CountOrderLineRespVO> getCountOrderLinePage(CountOrderLinePageReqVO pageReqVO);

    /**
     * 获得WMS-盘点明细
     *
     * @param id 编号
     * @return WMS-盘点明细
     */
    CountOrderLineRespVO getCountOrderLine(Long id);

}
