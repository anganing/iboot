package com.iboot.pf.module.wms.service.zone;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.zone.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库区 Service 接口
 *
 * @author iboot
 */
public interface ZoneService {

    /**
     * 创建WMS-库区
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createZone(@Valid ZoneSaveReqVO createReqVO);

    /**
     * 导入WMS-库区
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ZoneImportRespVO importZoneList(List<ZoneImportExcelVO> importList);

    /**
     * 更新WMS-库区
     *
     * @param updateReqVO 更新信息
     */
    void updateZone(@Valid ZoneSaveReqVO updateReqVO);

    /**
     * 删除WMS-库区
     *
     * @param id 编号
     */
    void deleteZone(Long id);

    /**
    * 批量删除WMS-库区
    *
    * @param ids 编号
    */
    void deleteZoneListByIds(List<Long> ids);

    /**
     * 获得WMS-库区
     *
     * @param id 编号
     * @return WMS-库区
     */
    ZoneRespVO getZone(Long id);

    /**
     * 获得WMS-库区分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库区分页
     */
    PageResult<ZoneRespVO> getZonePage(ZonePageReqVO pageReqVO);

}