package com.iboot.pf.module.aps.api.task;

import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.api.task.dto.ScheduleTaskRespDTO;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/** 工序排程任务 API 门面。 */
public interface ScheduleTaskApi {
    List<ScheduleTaskRespDTO> getTaskListByVersionId(Long versionId);
    List<ScheduleTaskRespDTO> getTaskListByOrderIds(Collection<Long> prodOrderIds);
    void lockTask(@Valid LockTaskReqDTO reqDTO);
    void unlockTask(@Valid LockTaskReqDTO reqDTO);
}
