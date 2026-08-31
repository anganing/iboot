package com.iboot.pf.module.aps.dal.mysql.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskPageReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskRespVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/** APS-工序排程任务 Mapper。 */
@Mapper
public interface ApsTaskMapper extends BaseMapperX<ApsTaskDO> {

    default PageResult<TaskRespVO> selectPage(TaskPageReqVO reqVO) {
        IPage<TaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TaskRespVO selectJoinById(@Param("id") Long id);

    IPage<TaskRespVO> selectJoinPage(IPage<TaskRespVO> page, @Param("req") TaskPageReqVO reqVO);

    /** 唯一索引 uk_aps_task_no_deleted 的应用层前置校验。 */
    default ApsTaskDO selectByTaskNo(String taskNo) {
        return selectOne(ApsTaskDO::getTaskNo, taskNo);
    }

    /** 唯一索引 uk_aps_task_version_op_deleted 的应用层前置校验：同一版本内一道工序只排一次。 */
    default ApsTaskDO selectByVersionIdAndOpTaskId(Long versionId, Long opTaskId) {
        return selectOne(ApsTaskDO::getVersionId, versionId, ApsTaskDO::getOpTaskId, opTaskId);
    }

    /** 按版本列出任务，按资源与开始时间排序，供甘特与负荷计算使用。 */
    default List<ApsTaskDO> selectListByVersionId(Long versionId) {
        return selectList(new LambdaQueryWrapper<ApsTaskDO>()
                .eq(ApsTaskDO::getVersionId, versionId)
                .orderByAsc(ApsTaskDO::getResId, ApsTaskDO::getStartTime));
    }

    /** 按生产工单列出任务，按工单与顺序号排序。 */
    default List<ApsTaskDO> selectListByProdOrderIds(Collection<Long> prodOrderIds) {
        if (prodOrderIds == null || prodOrderIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<ApsTaskDO>()
                .in(ApsTaskDO::getProdOrderId, prodOrderIds)
                .orderByAsc(ApsTaskDO::getProdOrderId, ApsTaskDO::getSequenceNo));
    }

    /**
     * 同一资源上时间重叠的任务数量，用于排程任务的时间冲突校验。
     *
     * <p>重叠判定用半开区间：新任务开始时间 &lt; 已有结束时间 且 新任务结束时间 &gt; 已有开始时间，
     * 首尾相接（前一道 10:00 结束、后一道 10:00 开始）不算冲突。
     */
    default Long selectOverlapCount(Long versionId, Long resId, java.time.LocalDateTime startTime,
                                    java.time.LocalDateTime endTime, Long excludeId) {
        return selectCount(new LambdaQueryWrapper<ApsTaskDO>()
                .eq(ApsTaskDO::getVersionId, versionId)
                .eq(ApsTaskDO::getResId, resId)
                .lt(ApsTaskDO::getStartTime, endTime)
                .gt(ApsTaskDO::getEndTime, startTime)
                .ne(excludeId != null, ApsTaskDO::getId, excludeId));
    }

}
