package com.iboot.pf.module.wms.service.loc;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.loc.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.loc.LocDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库位 Service 接口
 *
 * @author iboot
 */
public interface LocService {

    /**
     * 创建WMS-库位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLoc(@Valid LocSaveReqVO createReqVO);

    /**
     * 导入WMS-库位
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    LocImportRespVO importLocList(List<LocImportExcelVO> importList);

    /**
     * 更新WMS-库位
     *
     * @param updateReqVO 更新信息
     */
    void updateLoc(@Valid LocSaveReqVO updateReqVO);

    /**
     * 删除WMS-库位
     *
     * @param id 编号
     */
    void deleteLoc(Long id);

    /**
    * 批量删除WMS-库位
    *
    * @param ids 编号
    */
    void deleteLocListByIds(List<Long> ids);

    /**
     * 获得WMS-库位
     *
     * @param id 编号
     * @return WMS-库位
     */
    LocRespVO getLoc(Long id);

    /**
     * 获得WMS-库位分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库位分页
     */
    PageResult<LocRespVO> getLocPage(LocPageReqVO pageReqVO);

}