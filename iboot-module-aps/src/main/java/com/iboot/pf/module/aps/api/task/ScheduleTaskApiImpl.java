package com.iboot.pf.module.aps.api.task;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.api.task.dto.ScheduleTaskRespDTO;
import com.iboot.pf.module.aps.service.task.ApsTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 排程任务 API 实现。 */
@Service
@Validated
@DataPermission(enable = false)
public class ScheduleTaskApiImpl implements ScheduleTaskApi {

    @Resource
    private ApsTaskService taskService;

    @Override
    public List<ScheduleTaskRespDTO> getTaskListByVersionId(Long versionId) {
        return BeanUtils.toBean(taskService.getTaskDOListByVersionId(versionId), ScheduleTaskRespDTO.class);
    }

    @Override
    public List<ScheduleTaskRespDTO> getTaskListByOrderIds(Collection<Long> ids) {
        return BeanUtils.toBean(taskService.getTaskDOListByProdOrderIds(ids), ScheduleTaskRespDTO.class);
    }

    @Override
    public void lockTask(LockTaskReqDTO reqDTO) {
        taskService.lockTask(reqDTO);
    }

    @Override
    public void unlockTask(LockTaskReqDTO reqDTO) {
        taskService.unlockTask(reqDTO);
    }

}
