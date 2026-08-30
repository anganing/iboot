package com.iboot.pf.module.mes.api.optask;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.FinishReq;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.StatusReq;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import java.util.Collection;
import java.util.List;

/** 工序任务 API 门面，状态机由 Service 集中维护。 */
public interface OpTaskApi {
    List<OpTaskRespDTO> getOpTaskListByOrderIds(Collection<Long> prodOrderIds);
    List<OpTaskRespDTO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds);
    void startOpTask(StatusReq reqDTO);
    void pauseOpTask(StatusReq reqDTO);
    void finishOpTask(FinishReq reqDTO);
    void skipOpTask(StatusReq reqDTO);
}
