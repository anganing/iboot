package com.iboot.pf.module.mdm.service.projecttask;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 项目任务 Service 接口
 *
 * @author iboot
 */
public interface ProjectTaskService {

    /**
     * 创建项目任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectTask(@Valid ProjectTaskSaveReqVO createReqVO);

    /**
     * 更新项目任务
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectTask(@Valid ProjectTaskSaveReqVO updateReqVO);

    /**
     * 删除项目任务
     *
     * @param id 编号
     */
    void deleteProjectTask(Long id);

    /**
    * 批量删除项目任务
    *
    * @param ids 编号
    */
    void deleteProjectTaskListByIds(List<Long> ids);

    /**
     * 获得项目任务
     *
     * @param id 编号
     * @return 项目任务
     */
    ProjectTaskRespVO getProjectTask(Long id);

    /**
     * 获得项目任务分页
     *
     * @param pageReqVO 分页查询
     * @return 项目任务分页
     */
    PageResult<ProjectTaskRespVO> getProjectTaskPage(ProjectTaskPageReqVO pageReqVO);

    /**
     * 按项目编号查询其全部项目任务
     *
     * @param projectId 项目编号
     * @return 项目任务列表；无任务返回空列表
     */
    List<ProjectTaskDO> getProjectTaskList(Long projectId);

}