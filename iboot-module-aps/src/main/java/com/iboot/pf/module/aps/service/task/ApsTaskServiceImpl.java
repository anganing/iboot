package com.iboot.pf.module.aps.service.task;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskAdjustReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskPageReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskRespVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.ApsTaskLockDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.dal.mysql.tasklock.ApsTaskLockMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockStatusEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.snapshot.ApsSnapshotService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_CANNOT_UNLOCK;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_RESOURCE_OVERLAP;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_TIME_INVALID;

/**
 * APS-工序排程任务 Service 实现类。
 *
 * <p>三条不变量：已发布版本的任务不允许改；同一资源上的任务时间不允许重叠；已开工的任务不允许解锁。
 * 锁定与解锁同时维护 {@code aps_task.locked_flag} 与 {@code aps_task_lock} 留痕记录，两者在一个事务里改。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsTaskServiceImpl implements ApsTaskService {

    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsTaskLockMapper taskLockMapper;
    @Resource
    private ApsVersionService versionService;
    @Resource
    private ApsSnapshotService snapshotService;
    @Resource
    private ApsRelationFiller relationFiller;
    @Resource
    private ApsCommandService commandService;

    @Override
    public TaskRespVO getTask(Long id) {
        TaskRespVO respVO = taskMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<TaskRespVO> getTaskPage(TaskPageReqVO pageReqVO) {
        PageResult<TaskRespVO> pageResult = taskMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustTask(TaskAdjustReqVO adjustReqVO) {
        ApsTaskDO task = validateTaskExists(adjustReqVO.getId());
        versionService.validateMutable(task.getVersionId());
        // 锁定或已开工的任务不允许挪动，否则车间在做的工序会被计划甩到别处
        if (Boolean.TRUE.equals(task.getLockedFlag()) || ApsTaskStatusEnum.isStarted(task.getStatus())) {
            throw exception(TASK_CANNOT_UNLOCK);
        }
        validateTimeRange(adjustReqVO.getStartTime(), adjustReqVO.getEndTime());
        // 资源换了也要校验：目标资源必须真实存在，并带出编码快照
        String resCode = snapshotService.requireResource(adjustReqVO.getResId()).getCode();
        validateNoOverlap(task.getVersionId(), adjustReqVO.getResId(), adjustReqVO.getStartTime(),
                adjustReqVO.getEndTime(), task.getId());
        taskMapper.updateById(ApsTaskDO.builder()
                .id(task.getId())
                .resId(adjustReqVO.getResId())
                .resCode(resCode)
                .startTime(adjustReqVO.getStartTime())
                .endTime(adjustReqVO.getEndTime())
                // 占用分钟数由起止时间推导，不让前端自己填，避免和时间轴对不上
                .durationMinutes(Math.toIntExact(
                        Duration.between(adjustReqVO.getStartTime(), adjustReqVO.getEndTime()).toMinutes()))
                .sequenceNo(adjustReqVO.getSequenceNo())
                .remark(adjustReqVO.getRemark())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockTask(LockTaskReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.LOCK_TASK, reqDTO, () -> {
            ApsTaskDO task = requireForUpdate(reqDTO.getTaskId());
            versionService.validateMutable(task.getVersionId());
            // 幂等：已锁定直接返回
            if (Boolean.TRUE.equals(task.getLockedFlag())) {
                return;
            }
            taskMapper.updateById(ApsTaskDO.builder()
                    .id(task.getId())
                    .lockedFlag(true)
                    .status(ApsTaskStatusEnum.LOCKED.name())
                    .build());
            taskLockMapper.insert(ApsTaskLockDO.builder()
                    .taskId(task.getId())
                    .lockType(reqDTO.getLockType() == null
                            ? ApsTaskLockTypeEnum.MANUAL.name() : reqDTO.getLockType())
                    .reason(reqDTO.getReason())
                    .lockedBy(reqDTO.getOperatorId())
                    .lockedTime(LocalDateTime.now())
                    .status(ApsTaskLockStatusEnum.LOCKED.name())
                    .build());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockTask(LockTaskReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.UNLOCK_TASK, reqDTO, () -> {
            ApsTaskDO task = requireForUpdate(reqDTO.getTaskId());
            versionService.validateMutable(task.getVersionId());
            // 已开工的任务解锁等于允许重排把它挪走，直接拒绝
            if (ApsTaskStatusEnum.isStarted(task.getStatus())) {
                throw exception(TASK_CANNOT_UNLOCK);
            }
            // 幂等：本来就没锁直接返回
            if (!Boolean.TRUE.equals(task.getLockedFlag())) {
                return;
            }
            taskMapper.updateById(ApsTaskDO.builder()
                    .id(task.getId())
                    .lockedFlag(false)
                    .status(ApsTaskStatusEnum.PLANNED.name())
                    .build());
            // 锁定记录不删，改成已解锁并写解锁时间，保留锁定历史
            ApsTaskLockDO locked = taskLockMapper.selectLockedByTaskId(task.getId());
            if (locked != null) {
                taskLockMapper.updateById(ApsTaskLockDO.builder()
                        .id(locked.getId())
                        .status(ApsTaskLockStatusEnum.UNLOCKED.name())
                        .unlockTime(LocalDateTime.now())
                        .build());
            }
        });
    }

    @Override
    public List<ApsTaskDO> getTaskDOListByVersionId(Long versionId) {
        return taskMapper.selectListByVersionId(versionId);
    }

    @Override
    public List<ApsTaskDO> getTaskDOListByProdOrderIds(Collection<Long> prodOrderIds) {
        return taskMapper.selectListByProdOrderIds(prodOrderIds);
    }

    @Override
    public ApsTaskDO validateTaskExists(Long id) {
        ApsTaskDO task = id == null ? null : taskMapper.selectById(id);
        if (task == null) {
            throw exception(TASK_NOT_EXISTS);
        }
        return task;
    }

    private ApsTaskDO requireForUpdate(Long id) {
        ApsTaskDO task = id == null ? null : taskMapper.selectOneForUpdate(ApsTaskDO::getId, id);
        if (task == null) {
            throw exception(TASK_NOT_EXISTS);
        }
        return task;
    }

    /** 计划时间必须是正区间。 */
    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw exception(TASK_TIME_INVALID);
        }
    }

    /** 同一资源同一时间只能做一道工序，重叠判定见 Mapper 注释。 */
    private void validateNoOverlap(Long versionId, Long resId, LocalDateTime startTime, LocalDateTime endTime,
                                   Long excludeId) {
        if (taskMapper.selectOverlapCount(versionId, resId, startTime, endTime, excludeId) > 0) {
            throw exception(TASK_RESOURCE_OVERLAP);
        }
    }

    /**
     * 跨模块可读字段补全。
     *
     * <p>放在 Service 而非 Controller：Excel 导出复用这里的分页结果，写在 Controller 会丢字段。
     */
    private void fillRelationNames(List<TaskRespVO> list) {
        relationFiller.fillPlantName(list, TaskRespVO::getPlantId, TaskRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, TaskRespVO::getWorkshopId, TaskRespVO::setWorkshopName);
        relationFiller.fillProjectName(list, TaskRespVO::getProjectId, TaskRespVO::setProjectName);
        relationFiller.fillProjectTaskName(list, TaskRespVO::getProjectTaskId, TaskRespVO::setProjectTaskName);
        relationFiller.fillResName(list, TaskRespVO::getResId, TaskRespVO::setResName);
    }

}
