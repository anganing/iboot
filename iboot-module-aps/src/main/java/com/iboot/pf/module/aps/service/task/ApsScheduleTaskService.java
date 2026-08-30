package com.iboot.pf.module.aps.service.task;

import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import java.util.Collection;
import java.util.List;

/** 工序排程任务服务。 */
public interface ApsScheduleTaskService {
    List<ApsTaskDO> getTaskListByVersionId(Long versionId);
    List<ApsTaskDO> getTaskListByOrderIds(Collection<Long> prodOrderIds);
    void lockTask(LockTaskReqDTO reqDTO);
    void unlockTask(LockTaskReqDTO reqDTO);
}
