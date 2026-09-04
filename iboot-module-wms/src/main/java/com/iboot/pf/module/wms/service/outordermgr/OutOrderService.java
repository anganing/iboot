package com.iboot.pf.module.wms.service.outordermgr;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-出库单 Service 接口
 *
 * @author iboot
 */
public interface OutOrderService {

    /**
     * 创建WMS-出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutOrder(@Valid OutOrderSaveReqVO createReqVO);

    /**
     * 导入WMS-出库单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    OutOrderImportRespVO importOutOrderList(List<OutOrderImportExcelVO> importList);

    /**
     * 更新WMS-出库单
     *
     * @param updateReqVO 更新信息
     */
    void updateOutOrder(@Valid OutOrderSaveReqVO updateReqVO);

    /**
     * 删除WMS-出库单
     *
     * @param id 编号
     */
    void deleteOutOrder(Long id);

    /**
    * 批量删除WMS-出库单
    *
    * @param ids 编号
    */
    void deleteOutOrderListByIds(List<Long> ids);

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
     * @param orderId 出库单编号
     * @return WMS-出库单明细分页
     */
    PageResult<OutOrderLineRespVO> getOutOrderLinePage(OutOrderLinePageReqVO pageReqVO);

    /**
     * 创建WMS-出库单明细
     *
     * @param outOrderLine 创建信息
     * @return 编号
     */
    Long createOutOrderLine(@Valid OutOrderLineSaveReqVO createReqVO);

    /**
     * 更新WMS-出库单明细
     *
     * @param outOrderLine 更新信息
     */
    void updateOutOrderLine(@Valid OutOrderLineSaveReqVO updateReqVO);

    /**
     * 删除WMS-出库单明细
     *
     * @param id 编号
     */
    void deleteOutOrderLine(Long id);

    /**
    * 批量删除WMS-出库单明细
    *
    * @param ids 编号
    */
    void deleteOutOrderLineListByIds(List<Long> ids);

	/**
	 * 获得WMS-出库单明细
	 *
	 * @param id 编号
     * @return WMS-出库单明细
	 */
    OutOrderLineRespVO getOutOrderLine(Long id);

}
