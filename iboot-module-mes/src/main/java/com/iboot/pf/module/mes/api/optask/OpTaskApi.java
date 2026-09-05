package com.iboot.pf.module.mes.api.optask;

import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskFinishReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskSkipReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO;

import java.util.Collection;
import java.util.List;

/**
 * 工序任务 API 门面。
 *
 * <p>状态迁移由工序任务状态机集中判定，门面只负责把动作意图传进来。
 */
public interface OpTaskApi {

    /**
     * 按生产工单批量查询工序任务。
     *
     * @param prodOrderIds 生产工单编号集合
     * @return 工序任务事实列表
     */
    List<OpTaskRespDTO> getOpTaskListByOrderIds(Collection<Long> prodOrderIds);

    /**
     * 按路线工序批量查询工序任务。
     *
     * @param routeOpIds 路线工序编号集合
     * @return 工序任务事实列表
     */
    List<OpTaskRespDTO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds);

    /**
     * 把排程结果回写到工序任务的计划字段。
     *
     * <p>只写资源与计划起止时间，已开工或已终态的工序会被跳过：车间已经在做的工序不能被新计划挪走。
     *
     * @param reqDTO 回写命令
     * @return 实际生效的工序条数
     */
    int applySchedule(ApplyScheduleReqDTO reqDTO);

    /**
     * 工序开工，过开工门禁校验链。
     *
     * @param reqDTO 状态动作命令
     */
    void startOpTask(OpTaskStatusReqDTO reqDTO);

    /**
     * 工序暂停。
     *
     * @param reqDTO 状态动作命令
     */
    void pauseOpTask(OpTaskStatusReqDTO reqDTO);

    /**
     * 工序完工，校验合格数与报废数之和等于计划数，并推进后序工序。
     *
     * @param reqDTO 完工命令
     */
    void finishOpTask(OpTaskFinishReqDTO reqDTO);

    /**
     * 工序跳序，是否放行由工序任务的跳序策略决定。
     *
     * @param reqDTO 跳序命令
     */
    void skipOpTask(OpTaskSkipReqDTO reqDTO);

}
