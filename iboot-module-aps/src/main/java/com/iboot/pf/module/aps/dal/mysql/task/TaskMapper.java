package com.iboot.pf.module.aps.dal.mysql.task;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.task.TaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.task.vo.*;

/**
 * APS-工序排程任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TaskMapper extends BaseMapperX<TaskDO> {

    default PageResult<TaskRespVO> selectPage(TaskPageReqVO reqVO) {
        IPage<TaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TaskRespVO selectJoinById(@Param("id") Long id);

    IPage<TaskRespVO> selectJoinPage(IPage<TaskRespVO> page, @Param("req") TaskPageReqVO reqVO);

}