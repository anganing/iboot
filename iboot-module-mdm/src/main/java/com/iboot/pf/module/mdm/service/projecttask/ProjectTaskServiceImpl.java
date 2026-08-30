package com.iboot.pf.module.mdm.service.projecttask;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.ProjectTaskPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.ProjectTaskRespVO;
import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.ProjectTaskSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import com.iboot.pf.module.mdm.dal.mysql.project.ProjectMapper;
import com.iboot.pf.module.mdm.dal.mysql.projecttask.ProjectTaskMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.PROJECT_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.PROJECT_TASK;

/**
 * 项目任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProjectTaskServiceImpl implements ProjectTaskService {

    @Resource
    private ProjectTaskMapper projectTaskMapper;
    @Resource
    private MdmCodeService mdmCodeService;
    @Resource
    private ProjectMapper projectMapper;

    @Override
    public Long createProjectTask(ProjectTaskSaveReqVO createReqVO) {
        validateProjectExists(createReqVO.getProjectId());
        // 插入
        ProjectTaskDO projectTask = BeanUtils.toBean(createReqVO, ProjectTaskDO.class);
        projectTask.setId(null);
        projectTask.setCode(mdmCodeService.resolveCode(projectTask.getCode(), PROJECT_TASK));
        projectTaskMapper.insert(projectTask);

        // 返回
        return projectTask.getId();
    }

    @Override
    public void updateProjectTask(ProjectTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectTaskExists(updateReqVO.getId());
        validateProjectExists(updateReqVO.getProjectId());
        // 更新
        ProjectTaskDO updateObj = BeanUtils.toBean(updateReqVO, ProjectTaskDO.class);
        projectTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteProjectTask(Long id) {
        // 校验存在
        validateProjectTaskExists(id);
        // 删除
        projectTaskMapper.deleteById(id);
    }

    @Override
        public void deleteProjectTaskListByIds(List<Long> ids) {
        // 删除
        projectTaskMapper.deleteByIds(ids);
        }


    private void validateProjectTaskExists(Long id) {
        if (projectTaskMapper.selectById(id) == null) {
            throw exception(PROJECT_TASK_NOT_EXISTS);
        }
    }

    private void validateProjectExists(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
    }

    @Override
    public ProjectTaskRespVO getProjectTask(Long id) {
        return projectTaskMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProjectTaskRespVO> getProjectTaskPage(ProjectTaskPageReqVO pageReqVO) {
        return projectTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProjectTaskDO> getProjectTaskList(Long projectId) {
        return projectTaskMapper.selectList(ProjectTaskDO::getProjectId, projectId);
    }

}
