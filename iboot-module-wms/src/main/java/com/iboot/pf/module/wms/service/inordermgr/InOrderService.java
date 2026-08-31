package com.iboot.pf.module.wms.service.inordermgr;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.inordermgr.InOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-入库单 Service 接口
 *
 * @author iboot
 */
public interface InOrderService {

    /**
     * 创建WMS-入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInOrder(@Valid InOrderSaveReqVO createReqVO);

    /**
     * 导入WMS-入库单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    InOrderImportRespVO importInOrderList(List<InOrderImportExcelVO> importList);

    /**
     * 更新WMS-入库单
     *
     * @param updateReqVO 更新信息
     */
    void updateInOrder(@Valid InOrderSaveReqVO updateReqVO);

    /**
     * 删除WMS-入库单
     *
     * @param id 编号
     */
    void deleteInOrder(Long id);

    /**
    * 批量删除WMS-入库单
    *
    * @param ids 编号
    */
    void deleteInOrderListByIds(List<Long> ids);

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
     * @param orderId 入库单编号
     * @return WMS-入库单明细分页
     */
    PageResult<InOrderLineRespVO> getInOrderLinePage(InOrderLinePageReqVO pageReqVO);

    /**
     * 创建WMS-入库单明细
     *
     * @param inOrderLine 创建信息
     * @return 编号
     */
    Long createInOrderLine(@Valid InOrderLineSaveReqVO createReqVO);

    /**
     * 更新WMS-入库单明细
     *
     * @param inOrderLine 更新信息
     */
    void updateInOrderLine(@Valid InOrderLineSaveReqVO updateReqVO);

    /**
     * 删除WMS-入库单明细
     *
     * @param id 编号
     */
    void deleteInOrderLine(Long id);

    /**
    * 批量删除WMS-入库单明细
    *
    * @param ids 编号
    */
    void deleteInOrderLineListByIds(List<Long> ids);

	/**
	 * 获得WMS-入库单明细
	 *
	 * @param id 编号
     * @return WMS-入库单明细
	 */
    InOrderLineRespVO getInOrderLine(Long id);

}
