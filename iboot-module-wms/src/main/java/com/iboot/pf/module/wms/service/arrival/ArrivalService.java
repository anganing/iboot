package com.iboot.pf.module.wms.service.arrival;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-到货登记 Service 接口
 *
 * @author iboot
 */
public interface ArrivalService {

    /**
     * 创建WMS-到货登记
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArrival(@Valid ArrivalSaveReqVO createReqVO);

    /**
     * 导入WMS-到货登记
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ArrivalImportRespVO importArrivalList(List<ArrivalImportExcelVO> importList);

    /**
     * 更新WMS-到货登记
     *
     * @param updateReqVO 更新信息
     */
    void updateArrival(@Valid ArrivalSaveReqVO updateReqVO);

    /**
     * 删除WMS-到货登记
     *
     * @param id 编号
     */
    void deleteArrival(Long id);

    /**
    * 批量删除WMS-到货登记
    *
    * @param ids 编号
    */
    void deleteArrivalListByIds(List<Long> ids);

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
     * 创建WMS-到货登记明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArrivalLine(@Valid ArrivalLineSaveReqVO createReqVO);

    /**
     * 更新WMS-到货登记明细
     *
     * @param updateReqVO 更新信息
     */
    void updateArrivalLine(@Valid ArrivalLineSaveReqVO updateReqVO);

    /**
     * 删除WMS-到货登记明细
     *
     * @param id 编号
     */
    void deleteArrivalLine(Long id);

    /**
    * 批量删除WMS-到货登记明细
    *
    * @param ids 编号
    */
    void deleteArrivalLineListByIds(List<Long> ids);

	/**
	 * 获得WMS-到货登记明细
	 *
	 * @param id 编号
     * @return WMS-到货登记明细
	 */
    ArrivalLineRespVO getArrivalLine(Long id);

}
