package com.iboot.pf.module.mdm.dal.mysql.projecttask;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.*;

/**
 * 项目任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProjectTaskMapper extends BaseMapperX<ProjectTaskDO> {

    default PageResult<ProjectTaskDO> selectPage(ProjectTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProjectTaskDO>()
                .eqIfPresent(ProjectTaskDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(ProjectTaskDO::getCode, reqVO.getCode())
                .likeIfPresent(ProjectTaskDO::getName, reqVO.getName())
                .eqIfPresent(ProjectTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProjectTaskDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ProjectTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProjectTaskDO::getId));
    }

}