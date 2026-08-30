package com.iboot.pf.module.mdm.api.project;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.project.dto.ProjectTaskRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.project.ProjectDO;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import com.iboot.pf.module.mdm.service.project.ProjectService;
import com.iboot.pf.module.mdm.service.projecttask.ProjectTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
