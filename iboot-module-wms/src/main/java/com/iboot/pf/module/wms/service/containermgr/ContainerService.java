package com.iboot.pf.module.wms.service.containermgr;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.containermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.containermgr.ContainerDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-容器主档 Service 接口
 *
 * @author iboot
 */
public interface ContainerService {

    /**
     * 创建WMS-容器主档
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContainer(@Valid ContainerSaveReqVO createReqVO);

    /**
     * 导入WMS-容器主档
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ContainerImportRespVO importContainerList(List<ContainerImportExcelVO> importList);

    /**
     * 更新WMS-容器主档
     *
     * @param updateReqVO 更新信息
     */
    void updateContainer(@Valid ContainerSaveReqVO updateReqVO);

    /**
     * 删除WMS-容器主档
     *
     * @param id 编号
     */
    void deleteContainer(Long id);

    /**
    * 批量删除WMS-容器主档
    *
    * @param ids 编号
    */
    void deleteContainerListByIds(List<Long> ids);

    /**
     * 获得WMS-容器主档
     *
     * @param id 编号
     * @return WMS-容器主档
     */
    ContainerRespVO getContainer(Long id);

    /**
     * 获得WMS-容器主档分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-容器主档分页
     */
    PageResult<ContainerRespVO> getContainerPage(ContainerPageReqVO pageReqVO);

}