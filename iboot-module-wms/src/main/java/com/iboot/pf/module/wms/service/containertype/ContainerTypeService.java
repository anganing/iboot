package com.iboot.pf.module.wms.service.containertype;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.containertype.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerTypeDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-容器类型 Service 接口
 *
 * @author iboot
 */
public interface ContainerTypeService {

    /**
     * 创建WMS-容器类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContainerType(@Valid ContainerTypeSaveReqVO createReqVO);

    /**
     * 导入WMS-容器类型
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ContainerTypeImportRespVO importContainerTypeList(List<ContainerTypeImportExcelVO> importList);

    /**
     * 更新WMS-容器类型
     *
     * @param updateReqVO 更新信息
     */
    void updateContainerType(@Valid ContainerTypeSaveReqVO updateReqVO);

    /**
     * 删除WMS-容器类型
     *
     * @param id 编号
     */
    void deleteContainerType(Long id);

    /**
    * 批量删除WMS-容器类型
    *
    * @param ids 编号
    */
    void deleteContainerTypeListByIds(List<Long> ids);

    /**
     * 获得WMS-容器类型
     *
     * @param id 编号
     * @return WMS-容器类型
     */
    ContainerTypeRespVO getContainerType(Long id);

    /**
     * 获得WMS-容器类型分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-容器类型分页
     */
    PageResult<ContainerTypeRespVO> getContainerTypePage(ContainerTypePageReqVO pageReqVO);

}