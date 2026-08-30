package com.iboot.pf.module.aps.dal.mysql.projectionorder;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.projectionorder.ProjectionOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.*;

/**
 * 工单投影 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProjectionOrderMapper extends BaseMapperX<ProjectionOrderDO> {

    default PageResult<ProjectionOrderRespVO> selectPage(ProjectionOrderPageReqVO reqVO) {
        IPage<ProjectionOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionOrderRespVO> selectJoinPage(IPage<ProjectionOrderRespVO> page, @Param("req") ProjectionOrderPageReqVO reqVO);

}