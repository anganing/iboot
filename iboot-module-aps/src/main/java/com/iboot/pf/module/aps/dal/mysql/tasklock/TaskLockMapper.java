package com.iboot.pf.module.aps.dal.mysql.tasklock;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.TaskLockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.*;

/**
 * 任务锁定 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TaskLockMapper extends BaseMapperX<TaskLockDO> {

    default PageResult<TaskLockRespVO> selectPage(TaskLockPageReqVO reqVO) {
        IPage<TaskLockRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TaskLockRespVO selectJoinById(@Param("id") Long id);

    IPage<TaskLockRespVO> selectJoinPage(IPage<TaskLockRespVO> page, @Param("req") TaskLockPageReqVO reqVO);

}