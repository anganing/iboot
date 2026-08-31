package com.iboot.pf.module.aps.dal.mysql.taskdep;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepPageReqVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepRespVO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** APS-排程任务依赖 Mapper。 */
@Mapper
public interface ApsTaskDepMapper extends BaseMapperX<ApsTaskDepDO> {

    default PageResult<TaskDepRespVO> selectPage(TaskDepPageReqVO reqVO) {
        IPage<TaskDepRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TaskDepRespVO selectJoinById(@Param("id") Long id);

    IPage<TaskDepRespVO> selectJoinPage(IPage<TaskDepRespVO> page, @Param("req") TaskDepPageReqVO reqVO);

    /** 唯一索引 uk_aps_task_dep_deleted 的应用层前置校验。 */
    default ApsTaskDepDO selectByTasksAndType(Long versionId, Long fromTaskId, Long toTaskId, String depType) {
        return selectOne(new LambdaQueryWrapper<ApsTaskDepDO>()
                .eq(ApsTaskDepDO::getVersionId, versionId)
                .eq(ApsTaskDepDO::getFromTaskId, fromTaskId)
                .eq(ApsTaskDepDO::getToTaskId, toTaskId)
                .eq(ApsTaskDepDO::getDepType, depType));
    }

    /** 任务是否被依赖引用，用于任务删除前的占用校验。 */
    default Long selectCountByTaskId(Long taskId) {
        return selectCount(new LambdaQueryWrapper<ApsTaskDepDO>()
                .eq(ApsTaskDepDO::getFromTaskId, taskId)
                .or()
                .eq(ApsTaskDepDO::getToTaskId, taskId));
    }

}
