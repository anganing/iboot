package com.iboot.pf.module.wms.service.countorder;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.countorder.CountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.countorderline.CountOrderLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-盘点单 Service 接口
 *
 * @author iboot
 */
public interface CountOrderService {

    /**
     * 创建WMS-盘点单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCountOrder(@Valid CountOrderSaveReqVO createReqVO);

    /**
     * 导入WMS-盘点单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    CountOrderImportRespVO importCountOrderList(List<CountOrderImportExcelVO> importList);

    /**
     * 更新WMS-盘点单
     *
     * @param updateReqVO 更新信息
     */
    void updateCountOrder(@Valid CountOrderSaveReqVO updateReqVO);

    /**
     * 删除WMS-盘点单
     *
     * @param id 编号
     */
    void deleteCountOrder(Long id);

    /**
    * 批量删除WMS-盘点单
    *
    * @param ids 编号
    */
    void deleteCountOrderListByIds(List<Long> ids);

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
     * @param orderId 盘点单编号
     * @return WMS-盘点明细分页
     */
    PageResult<CountOrderLineDO> getCountOrderLinePage(PageParam pageReqVO, Long orderId);

    /**
     * 创建WMS-盘点明细
     *
     * @param countOrderLine 创建信息
     * @return 编号
     */
    Long createCountOrderLine(@Valid CountOrderLineDO countOrderLine);

    /**
     * 更新WMS-盘点明细
     *
     * @param countOrderLine 更新信息
     */
    void updateCountOrderLine(@Valid CountOrderLineDO countOrderLine);

    /**
     * 删除WMS-盘点明细
     *
     * @param id 编号
     */
    void deleteCountOrderLine(Long id);

    /**
    * 批量删除WMS-盘点明细
    *
    * @param ids 编号
    */
    void deleteCountOrderLineListByIds(List<Long> ids);

	/**
	 * 获得WMS-盘点明细
	 *
	 * @param id 编号
     * @return WMS-盘点明细
	 */
    CountOrderLineDO getCountOrderLine(Long id);

}