package com.iboot.pf.module.aps.dal.mysql.projectionresource;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.projectionresource.ProjectionResourceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.*;

/**
 * 资源投影 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProjectionResourceMapper extends BaseMapperX<ProjectionResourceDO> {

    default PageResult<ProjectionResourceRespVO> selectPage(ProjectionResourcePageReqVO reqVO) {
        IPage<ProjectionResourceRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionResourceRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionResourceRespVO> selectJoinPage(IPage<ProjectionResourceRespVO> page, @Param("req") ProjectionResourcePageReqVO reqVO);

}