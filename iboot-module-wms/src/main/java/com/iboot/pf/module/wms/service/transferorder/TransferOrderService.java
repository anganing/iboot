package com.iboot.pf.module.wms.service.transferorder;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.transferorder.TransferOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库存转移调拨单 Service 接口
 *
 * @author iboot
 */
public interface TransferOrderService {

    /**
     * 创建WMS-库存转移调拨单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTransferOrder(@Valid TransferOrderSaveReqVO createReqVO);

    /**
     * 导入WMS-库存转移调拨单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    TransferOrderImportRespVO importTransferOrderList(List<TransferOrderImportExcelVO> importList);

    /**
     * 更新WMS-库存转移调拨单
     *
     * @param updateReqVO 更新信息
     */
    void updateTransferOrder(@Valid TransferOrderSaveReqVO updateReqVO);

    /**
     * 删除WMS-库存转移调拨单
     *
     * @param id 编号
     */
    void deleteTransferOrder(Long id);

    /**
    * 批量删除WMS-库存转移调拨单
    *
    * @param ids 编号
    */
    void deleteTransferOrderListByIds(List<Long> ids);

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
     * @param orderId 转移调拨单编号
     * @return WMS-库存转移调拨明细分页
     */
    PageResult<TransferOrderLineDO> getTransferOrderLinePage(PageParam pageReqVO, Long orderId);

    /**
     * 创建WMS-库存转移调拨明细
     *
     * @param transferOrderLine 创建信息
     * @return 编号
     */
    Long createTransferOrderLine(@Valid TransferOrderLineDO transferOrderLine);

    /**
     * 更新WMS-库存转移调拨明细
     *
     * @param transferOrderLine 更新信息
     */
    void updateTransferOrderLine(@Valid TransferOrderLineDO transferOrderLine);

    /**
     * 删除WMS-库存转移调拨明细
     *
     * @param id 编号
     */
    void deleteTransferOrderLine(Long id);

    /**
    * 批量删除WMS-库存转移调拨明细
    *
    * @param ids 编号
    */
    void deleteTransferOrderLineListByIds(List<Long> ids);

	/**
	 * 获得WMS-库存转移调拨明细
	 *
	 * @param id 编号
     * @return WMS-库存转移调拨明细
	 */
    TransferOrderLineDO getTransferOrderLine(Long id);

}