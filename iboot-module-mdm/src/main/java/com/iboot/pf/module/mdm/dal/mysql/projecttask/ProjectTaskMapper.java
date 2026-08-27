package com.iboot.pf.module.mdm.dal.mysql.projecttask;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.*;

/**
 * 项目任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProjectTaskMapper extends BaseMapperX<ProjectTaskDO> {

    default PageResult<ProjectTaskRespVO> selectPage(ProjectTaskPageReqVO reqVO) {
        IPage<ProjectTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectTaskRespVO> selectJoinPage(IPage<ProjectTaskRespVO> page, @Param("req") ProjectTaskPageReqVO reqVO);

}
