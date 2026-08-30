package com.iboot.pf.module.mes.dal.mysql.dispatchtask;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.*;

/**
 * MES-派工任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DispatchTaskMapper extends BaseMapperX<DispatchTaskDO> {

    default PageResult<DispatchTaskRespVO> selectPage(DispatchTaskPageReqVO reqVO) {
        IPage<DispatchTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DispatchTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<DispatchTaskRespVO> selectJoinPage(IPage<DispatchTaskRespVO> page, @Param("req") DispatchTaskPageReqVO reqVO);

}