package com.iboot.pf.module.wms.service.invreservation;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库存预留 Service 接口
 *
 * @author iboot
 */
public interface InvReservationService {

    /**
     * 创建WMS-库存预留
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInvReservation(@Valid InvReservationSaveReqVO createReqVO);

    /**
     * 导入WMS-库存预留
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    InvReservationImportRespVO importInvReservationList(List<InvReservationImportExcelVO> importList);

    /**
     * 更新WMS-库存预留
     *
     * @param updateReqVO 更新信息
     */
    void updateInvReservation(@Valid InvReservationSaveReqVO updateReqVO);

    /**
     * 删除WMS-库存预留
     *
     * @param id 编号
     */
    void deleteInvReservation(Long id);

    /**
    * 批量删除WMS-库存预留
    *
    * @param ids 编号
    */
    void deleteInvReservationListByIds(List<Long> ids);

    /**
     * 获得WMS-库存预留
     *
     * @param id 编号
     * @return WMS-库存预留
     */
    InvReservationRespVO getInvReservation(Long id);

    /**
     * 获得WMS-库存预留分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存预留分页
     */
    PageResult<InvReservationRespVO> getInvReservationPage(InvReservationPageReqVO pageReqVO);

}