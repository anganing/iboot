package com.iboot.pf.module.wms.service.arrival;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;

/**
 * WMS-到货登记 codegen Service 接口。
 *
 * <p><b>只读</b>：到货单的创建、改单、删单、确认到货（含批次注册）、生成入库单一律走领域侧
 * {@code service.arrivalorder.WmsArrivalOrderService}。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询。
 *
 * @author iboot
 */
public interface ArrivalService {

    /**
     * 获得WMS-到货登记
     *
     * @param id 编号
     * @return WMS-到货登记
     */
    ArrivalRespVO getArrival(Long id);

    /**
     * 获得WMS-到货登记分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-到货登记分页
     */
    PageResult<ArrivalRespVO> getArrivalPage(ArrivalPageReqVO pageReqVO);

    // ==================== 子表（WMS-到货登记明细） ====================

    /**
     * 获得WMS-到货登记明细分页
     *
     * @param pageReqVO 分页查询，包含到货登记编号
     * @return WMS-到货登记明细分页
     */
    PageResult<ArrivalLineRespVO> getArrivalLinePage(ArrivalLinePageReqVO pageReqVO);

    /**
     * 获得WMS-到货登记明细
     *
     * @param id 编号
     * @return WMS-到货登记明细
     */
    ArrivalLineRespVO getArrivalLine(Long id);

}
