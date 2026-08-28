package com.iboot.pf.module.mdm.service.project;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.project.vo.ProjectPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.project.vo.ProjectSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.project.ProjectDO;
import com.iboot.pf.module.mdm.dal.mysql.project.ProjectMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.PROJECT;

/**
 * 项目 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    @Override
    public Long createProject(ProjectSaveReqVO createReqVO) {
        // 插入
        ProjectDO project = BeanUtils.toBean(createReqVO, ProjectDO.class);
        project.setId(null);
        project.setCode(mdmCodeService.resolveCode(project.getCode(), PROJECT));
        projectMapper.insert(project);

        // 返回
        return project.getId();
    }

    @Override
    public void updateProject(ProjectSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectExists(updateReqVO.getId());
        // 更新
        ProjectDO updateObj = BeanUtils.toBean(updateReqVO, ProjectDO.class);
        projectMapper.updateById(updateObj);
    }

    @Override
    public void deleteProject(Long id) {
        // 校验存在
        validateProjectExists(id);
        // 删除
        projectMapper.deleteById(id);
    }

    @Override
        public void deleteProjectListByIds(List<Long> ids) {
        // 删除
        projectMapper.deleteByIds(ids);
        }


    private void validateProjectExists(Long id) {
        if (projectMapper.selectById(id) == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
    }

    @Override
    public ProjectDO getProject(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectDO> getProjectPage(ProjectPageReqVO pageReqVO) {
        return projectMapper.selectPage(pageReqVO);
    }

}
