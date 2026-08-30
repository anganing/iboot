package com.iboot.pf.module.aps.dal.mysql.projectioninv;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.projectioninv.ProjectionInvDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.*;

/**
 * 库存投影 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProjectionInvMapper extends BaseMapperX<ProjectionInvDO> {

    default PageResult<ProjectionInvRespVO> selectPage(ProjectionInvPageReqVO reqVO) {
        IPage<ProjectionInvRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionInvRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionInvRespVO> selectJoinPage(IPage<ProjectionInvRespVO> page, @Param("req") ProjectionInvPageReqVO reqVO);

}