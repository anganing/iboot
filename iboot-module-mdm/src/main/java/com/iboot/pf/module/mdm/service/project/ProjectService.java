package com.iboot.pf.module.mdm.service.project;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.project.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.project.ProjectDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 项目 Service 接口
 *
 * @author iboot
 */
public interface ProjectService {

    /**
     * 创建项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProject(@Valid ProjectSaveReqVO createReqVO);

    /**
     * 更新项目
     *
     * @param updateReqVO 更新信息
     */
    void updateProject(@Valid ProjectSaveReqVO updateReqVO);

    /**
     * 删除项目
     *
     * @param id 编号
     */
    void deleteProject(Long id);

    /**
    * 批量删除项目
    *
    * @param ids 编号
    */
    void deleteProjectListByIds(List<Long> ids);

    /**
     * 获得项目
     *
     * @param id 编号
     * @return 项目
     */
    ProjectDO getProject(Long id);

    /**
     * 获得项目分页
     *
     * @param pageReqVO 分页查询
     * @return 项目分页
     */
    PageResult<ProjectDO> getProjectPage(ProjectPageReqVO pageReqVO);

}