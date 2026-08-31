package com.iboot.pf.module.mdm.api.project;

import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.project.dto.ProjectTaskRespDTO;

import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * 项目 API 接口
 *
 * <p>对 WMS / MES / APS 提供项目与项目任务门面。
 *
 * @author iboot
 */
public interface ProjectApi {

    /**
     * 获得全部项目（含停用）
     *
     * @return 项目列表
     */
    List<ProjectRespDTO> getProjectList();

    /**
     * 按编号批量获得项目，一次 SQL 查完，供其他模块补全展示名称
     *
     * @param ids 项目编号集合
     * @return 编号与项目的映射
     */
    Map<Long, ProjectRespDTO> getProjectMap(Collection<Long> ids);

    /**
     * 按编号批量获得项目任务，一次 SQL 查完，供其他模块补全展示名称
     *
     * @param ids 项目任务编号集合
     * @return 编号与项目任务的映射
     */
    Map<Long, ProjectTaskRespDTO> getProjectTaskMap(Collection<Long> ids);

    /**
     * 按项目编号查询其全部项目任务
     *
     * @param projectId 项目编号
     * @return 项目任务列表；无任务返回空列表
     */
    List<ProjectTaskRespDTO> getProjectTaskList(Long projectId);

}
