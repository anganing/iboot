package com.iboot.pf.module.aps.dal.mysql.tasklock;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockPageReqVO;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockRespVO;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.ApsTaskLockDO;
import com.iboot.pf.module.aps.enums.ApsTaskLockStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** APS-排程任务锁定记录 Mapper。 */
@Mapper
public interface ApsTaskLockMapper extends BaseMapperX<ApsTaskLockDO> {

    default PageResult<TaskLockRespVO> selectPage(TaskLockPageReqVO reqVO) {
        IPage<TaskLockRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TaskLockRespVO selectJoinById(@Param("id") Long id);

    IPage<TaskLockRespVO> selectJoinPage(IPage<TaskLockRespVO> page, @Param("req") TaskLockPageReqVO reqVO);

    /**
     * 任务当前锁定中的记录。
     *
     * <p>表上没有唯一索引（历史锁定记录要留痕），所以“同一任务只能有一条锁定中”这条不变量由应用层保证，
     * 取最新一条避免历史脏数据导致 selectOne 报错。
     */
    default ApsTaskLockDO selectLockedByTaskId(Long taskId) {
        return selectFirstOne(ApsTaskLockDO::getTaskId, taskId,
                ApsTaskLockDO::getStatus, ApsTaskLockStatusEnum.LOCKED.name());
    }

    /** 任务是否存在锁定记录，用于任务删除前的占用校验。 */
    default Long selectCountByTaskId(Long taskId) {
        return selectCount(ApsTaskLockDO::getTaskId, taskId);
    }

}
