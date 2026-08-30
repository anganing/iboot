package com.iboot.pf.module.aps.service.projectionresource;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionresource.ProjectionResourceDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 资源投影 Service 接口
 *
 * @author iboot
 */
public interface ProjectionResourceService {

    /**
     * 创建资源投影
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectionResource(@Valid ProjectionResourceSaveReqVO createReqVO);

    /**
     * 导入资源投影
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProjectionResourceImportRespVO importProjectionResourceList(List<ProjectionResourceImportExcelVO> importList);

    /**
     * 更新资源投影
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectionResource(@Valid ProjectionResourceSaveReqVO updateReqVO);

    /**
     * 删除资源投影
     *
     * @param id 编号
     */
    void deleteProjectionResource(Long id);

    /**
    * 批量删除资源投影
    *
    * @param ids 编号
    */
    void deleteProjectionResourceListByIds(List<Long> ids);

    /**
     * 获得资源投影
     *
     * @param id 编号
     * @return 资源投影
     */
    ProjectionResourceRespVO getProjectionResource(Long id);

    /**
     * 获得资源投影分页
     *
     * @param pageReqVO 分页查询
     * @return 资源投影分页
     */
    PageResult<ProjectionResourceRespVO> getProjectionResourcePage(ProjectionResourcePageReqVO pageReqVO);

}