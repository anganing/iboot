package com.iboot.pf.module.wms.service.wh;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.wh.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-仓库 Service 接口
 *
 * @author iboot
 */
public interface WhService {

    /**
     * 创建WMS-仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWh(@Valid WhSaveReqVO createReqVO);

    /**
     * 导入WMS-仓库
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    WhImportRespVO importWhList(List<WhImportExcelVO> importList);

    /**
     * 更新WMS-仓库
     *
     * @param updateReqVO 更新信息
     */
    void updateWh(@Valid WhSaveReqVO updateReqVO);

    /**
     * 删除WMS-仓库
     *
     * @param id 编号
     */
    void deleteWh(Long id);

    /**
    * 批量删除WMS-仓库
    *
    * @param ids 编号
    */
    void deleteWhListByIds(List<Long> ids);

    /**
     * 获得WMS-仓库
     *
     * @param id 编号
     * @return WMS-仓库
     */
    WhRespVO getWh(Long id);

    /**
     * 获得WMS-仓库分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-仓库分页
     */
    PageResult<WhRespVO> getWhPage(WhPageReqVO pageReqVO);

}