package com.iboot.pf.module.mes.dal.mysql.optask;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * MES-工序任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OpTaskMapper extends BaseMapperX<OpTaskDO> {

    default PageResult<OpTaskRespVO> selectPage(OpTaskPageReqVO reqVO) {
        IPage<OpTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OpTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<OpTaskRespVO> selectJoinPage(IPage<OpTaskRespVO> page, @Param("req") OpTaskPageReqVO reqVO);

    /** 按任务号查询，唯一索引 uk_mes_op_task_no_deleted 的应用层前置校验。 */
    default OpTaskDO selectByTaskNo(String taskNo) {
        return selectOne(OpTaskDO::getTaskNo, taskNo);
    }

    /** 按作业工单 + 路线工序查询，唯一索引 uk_mes_op_task_route_deleted 的应用层前置校验。 */
    default OpTaskDO selectByWorkOrderIdAndRouteOpId(Long workOrderId, Long routeOpId) {
        return selectOne(OpTaskDO::getWorkOrderId, workOrderId, OpTaskDO::getRouteOpId, routeOpId);
    }

    /** 统计某作业工单下的工序任务数，作业工单删除前校验占用。 */
    default Long selectCountByWorkOrderId(Long workOrderId) {
        return selectCount(OpTaskDO::getWorkOrderId, workOrderId);
    }

    default List<OpTaskDO> selectListByWorkOrderIds(Collection<Long> workOrderIds) {
        return selectList(OpTaskDO::getWorkOrderId, workOrderIds);
    }

    default List<OpTaskDO> selectListByRouteOpIds(Collection<Long> routeOpIds) {
        return selectList(OpTaskDO::getRouteOpId, routeOpIds);
    }

}
