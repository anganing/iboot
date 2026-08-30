package com.iboot.pf.module.aps.dal.mysql.taskdep;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.TaskDepDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.*;

/**
 * 任务依赖 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TaskDepMapper extends BaseMapperX<TaskDepDO> {

    default PageResult<TaskDepRespVO> selectPage(TaskDepPageReqVO reqVO) {
        IPage<TaskDepRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TaskDepRespVO selectJoinById(@Param("id") Long id);

    IPage<TaskDepRespVO> selectJoinPage(IPage<TaskDepRespVO> page, @Param("req") TaskDepPageReqVO reqVO);

}