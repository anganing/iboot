package com.iboot.pf.module.aps.service.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

@Service
public class ApsScheduleTaskServiceImpl implements ApsScheduleTaskService {
    @Resource private ApsTaskMapper apsTaskMapper;
    @Resource private ApsVersionService versionService;
    @Resource private ApsCommandService commandService;

    @Override public List<ApsTaskDO> getTaskListByVersionId(Long versionId) {
        return apsTaskMapper.selectList(new LambdaQueryWrapper<ApsTaskDO>().eq(ApsTaskDO::getVersionId, versionId)
                .orderByAsc(ApsTaskDO::getResId, ApsTaskDO::getStartTime));
    }
    @Override public List<ApsTaskDO> getTaskListByOrderIds(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? List.of() : apsTaskMapper.selectList(new LambdaQueryWrapper<ApsTaskDO>()
                .in(ApsTaskDO::getProdOrderId, ids).orderByAsc(ApsTaskDO::getProdOrderId, ApsTaskDO::getSequenceNo));
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void lockTask(LockTaskReqDTO reqDTO) {
        commandService.executeVoid("LOCK_TASK", reqDTO, () -> changeLock(reqDTO.getTaskId(), true));
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void unlockTask(LockTaskReqDTO reqDTO) {
        commandService.executeVoid("UNLOCK_TASK", reqDTO, () -> changeLock(reqDTO.getTaskId(), false));
    }
    private void changeLock(Long taskId, boolean locked) {
        ApsTaskDO task = apsTaskMapper.selectOneForUpdate(ApsTaskDO::getId, taskId);
        if (task == null) throw exception(TASK_NOT_EXISTS);
        versionService.validateMutable(task.getVersionId());
        if (!locked && "STARTED".equals(task.getStatus())) throw exception(TASK_CANNOT_UNLOCK);
        if (Boolean.valueOf(locked).equals(task.getLockedFlag())) return;
        apsTaskMapper.updateById(ApsTaskDO.builder().id(taskId).lockedFlag(locked)
                .status(locked ? "LOCKED" : "PLANNED").build());
    }
}
