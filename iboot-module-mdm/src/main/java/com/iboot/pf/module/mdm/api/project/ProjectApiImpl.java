package com.iboot.pf.module.mdm.api.project;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.project.dto.ProjectTaskRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.project.ProjectDO;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import com.iboot.pf.module.mdm.dal.mysql.project.ProjectMapper;
import com.iboot.pf.module.mdm.dal.mysql.projecttask.ProjectTaskMapper;
import com.iboot.pf.module.mdm.service.project.ProjectService;
import com.iboot.pf.module.mdm.service.projecttask.ProjectTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 项目 API 实现类
 *
 * @author iboot
 */
@Service
public class ProjectApiImpl implements ProjectApi {

    @Resource
    private ProjectService projectService;
    @Resource
    private ProjectTaskService projectTaskService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectTaskMapper projectTaskMapper;

    @Override
    @DataPermission(enable = false)
    public List<ProjectRespDTO> getProjectList() {
        List<ProjectDO> list = projectService.getProjectList();
        return BeanUtils.toBean(list, ProjectRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ProjectTaskRespDTO> getProjectTaskList(Long projectId) {
        List<ProjectTaskDO> list = projectTaskService.getProjectTaskList(projectId);
        return BeanUtils.toBean(list, ProjectTaskRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, ProjectRespDTO> getProjectMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<ProjectDO> list = projectMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, ProjectRespDTO.class), ProjectRespDTO::getId);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, ProjectTaskRespDTO> getProjectTaskMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProjectTaskDO> list = projectTaskMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, ProjectTaskRespDTO.class), ProjectTaskRespDTO::getId);
    }

}
