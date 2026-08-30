package com.iboot.pf.module.aps.dal.mysql.projectionmat;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.projectionmat.ProjectionMatDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.*;

/**
 * APS-MDM物料投影 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProjectionMatMapper extends BaseMapperX<ProjectionMatDO> {

    default PageResult<ProjectionMatRespVO> selectPage(ProjectionMatPageReqVO reqVO) {
        IPage<ProjectionMatRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionMatRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionMatRespVO> selectJoinPage(IPage<ProjectionMatRespVO> page, @Param("req") ProjectionMatPageReqVO reqVO);

}