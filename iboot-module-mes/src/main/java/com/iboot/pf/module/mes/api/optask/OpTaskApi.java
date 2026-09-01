package com.iboot.pf.module.mes.api.optask;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.FinishReq;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.StatusReq;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import java.util.Collection;
import java.util.List;

/** 工序任务 API 门面，状态机由 Service 集中维护。 */
public interface OpTaskApi {
    List<OpTaskRespDTO> getOpTaskListByOrderIds(Collection<Long> prodOrderIds);
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

    void startOpTask(StatusReq reqDTO);
    void pauseOpTask(StatusReq reqDTO);
    void finishOpTask(FinishReq reqDTO);
    void skipOpTask(StatusReq reqDTO);
}
