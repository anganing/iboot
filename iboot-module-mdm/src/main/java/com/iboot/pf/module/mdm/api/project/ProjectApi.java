package com.iboot.pf.module.mdm.api.project;

import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.project.dto.ProjectTaskRespDTO;

import java.util.List;

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
     * 按项目编号查询其全部项目任务
     *
     * @param projectId 项目编号
     * @return 项目任务列表；无任务返回空列表
     */
    List<ProjectTaskRespDTO> getProjectTaskList(Long projectId);

}
