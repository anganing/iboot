package com.iboot.pf.module.aps.service.task;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskAdjustReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskGanttRespVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskPageReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskRespVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.ApsTaskLockDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.dal.mysql.taskdep.ApsTaskDepMapper;
import com.iboot.pf.module.aps.dal.mysql.tasklock.ApsTaskLockMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockStatusEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.snapshot.ApsSnapshotService;
import com.iboot.pf.module.aps.service.task.bo.ApsGanttOrderGroupBO;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_CANNOT_UNLOCK;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_RESOURCE_OVERLAP;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_TIME_INVALID;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;

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
    private ApsTaskDepMapper taskDepMapper;
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

    /**
     * 甘特图视图。
     *
     * <p>一次查完整版本而不是分页：连线要靠两端任务同时在结果集里才画得出来，时间轴左右边界也必须
     * 由全量任务的最早开始与最晚结束推出，分页片段两者都做不到。
     *
     * <p>依赖连线只保留两端任务都在本版本内的边：{@code aps_task_dep} 与 {@code aps_task} 每轮求解
     * 一起物理重建，理论上不会有悬空引用，但人工维护依赖时可能删掉任务，前端拿到悬空边会画出错位的线。
     *
     * <p>任务平铺返回，另给资源与工单两套分组元数据，让前端在同一份数据上切换维度，不必为换维度多打一次请求。
     */
    @Override
    public TaskGanttRespVO getTaskGantt(Long versionId) {
        VersionRespVO version = versionService.getVersion(versionId);
        if (version == null) {
            throw exception(VERSION_NOT_EXISTS);
        }
        TaskGanttRespVO respVO = new TaskGanttRespVO();
        respVO.setVersionId(versionId);
        respVO.setVersionNo(version.getVersionNo());
        respVO.setScenarioCode(version.getScenarioCode());
        respVO.setScenarioName(version.getScenarioName());
        respVO.setVersionStatus(version.getStatus());
        // Mapper 已按资源、开始时间排序，这里不再二次排序
        List<ApsTaskDO> tasks = taskMapper.selectListByVersionId(versionId);
        respVO.setTaskCount(tasks.size());
        if (tasks.isEmpty()) {
            return respVO;
        }
        respVO.setTasks(buildGanttTasks(tasks));
        respVO.setResourceGroups(buildResourceGroups(tasks));
        respVO.setOrderGroups(buildOrderGroups(versionId));
        respVO.setMinTime(tasks.stream().map(ApsTaskDO::getStartTime).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null));
        respVO.setMaxTime(tasks.stream().map(ApsTaskDO::getEndTime).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null));
        respVO.setLinks(buildLinks(versionId, tasks));
        return respVO;
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

    /**
     * 任务条平铺，资源名称与项目名称走门面批量补全，不做 N+1。
     *
     * <p>资源名称补在每条任务上而不是只补在分组上：工单维度下父行是工单，任务条自己得说明"在哪台设备做"。
     */
    private List<TaskGanttRespVO.Task> buildGanttTasks(List<ApsTaskDO> tasks) {
        List<TaskGanttRespVO.Task> bars = new ArrayList<>(tasks.size());
        Map<Long, Long> taskIdToProjectId = new LinkedHashMap<>();
        for (ApsTaskDO task : tasks) {
            bars.add(toGanttTask(task));
            taskIdToProjectId.put(task.getId(), task.getProjectId());
        }
        relationFiller.fillResName(bars, TaskGanttRespVO.Task::getResId, TaskGanttRespVO.Task::setResName);
        fillGanttProjectNames(bars, taskIdToProjectId);
        return bars;
    }

    /** 资源维度的分组元数据：任务条数、负荷合计与该资源上的计划时间跨度。 */
    private List<TaskGanttRespVO.ResourceGroup> buildResourceGroups(List<ApsTaskDO> tasks) {
        Map<Long, TaskGanttRespVO.ResourceGroup> groupMap = new LinkedHashMap<>();
        for (ApsTaskDO task : tasks) {
            TaskGanttRespVO.ResourceGroup group = groupMap.computeIfAbsent(task.getResId(), resId -> {
                TaskGanttRespVO.ResourceGroup row = new TaskGanttRespVO.ResourceGroup();
                row.setResId(resId);
                row.setResCode(task.getResCode());
                row.setTaskCount(0);
                row.setLoadMinutes(0);
                return row;
            });
            group.setTaskCount(group.getTaskCount() + 1);
            group.setLoadMinutes(group.getLoadMinutes()
                    + (task.getDurationMinutes() == null ? 0 : task.getDurationMinutes()));
            group.setPlanStartTime(minTime(group.getPlanStartTime(), task.getStartTime()));
            group.setPlanEndTime(maxTime(group.getPlanEndTime(), task.getEndTime()));
        }
        List<TaskGanttRespVO.ResourceGroup> groups = new ArrayList<>(groupMap.values());
        relationFiller.fillResName(groups, TaskGanttRespVO.ResourceGroup::getResId,
                TaskGanttRespVO.ResourceGroup::setResName);
        return groups;
    }

    /**
     * 工单维度的分组元数据。
     *
     * <p>交期由 Mapper 一次 JOIN 出来，物料名称跨模块调 mdm 门面批量补全。超期判定放在这里而不是前端：
     * "计划完工晚于交期"是业务口径，两个视图与将来的导出都要一致，各写一份必然走偏。
     */
    private List<TaskGanttRespVO.OrderGroup> buildOrderGroups(Long versionId) {
        List<ApsGanttOrderGroupBO> aggregates = taskMapper.selectGanttOrderGroups(versionId);
        relationFiller.fillMatName(aggregates, ApsGanttOrderGroupBO::getMatId, ApsGanttOrderGroupBO::setMatName);
        List<TaskGanttRespVO.OrderGroup> groups = new ArrayList<>(aggregates.size());
        for (ApsGanttOrderGroupBO aggregate : aggregates) {
            TaskGanttRespVO.OrderGroup group = new TaskGanttRespVO.OrderGroup();
            group.setProdOrderId(aggregate.getProdOrderId());
            group.setProdOrderNo(aggregate.getProdOrderNo());
            group.setMatCode(aggregate.getMatCode());
            group.setMatName(aggregate.getMatName());
            group.setDueDate(aggregate.getDueDate());
            group.setTaskCount(aggregate.getTaskCount());
            group.setLoadMinutes(aggregate.getLoadMinutes());
            group.setPlanStartTime(aggregate.getPlanStartTime());
            group.setPlanEndTime(aggregate.getPlanEndTime());
            group.setDelayMinutes(calcDelayMinutes(aggregate.getPlanEndTime(), aggregate.getDueDate()));
            group.setDelayFlag(group.getDelayMinutes() > 0);
            groups.add(group);
        }
        return groups;
    }

    /** 超期分钟数：交期缺失（工单投影里没这张单）时按不超期处理，不能拿"没交期"当逾期报警。 */
    private Integer calcDelayMinutes(LocalDateTime planEndTime, LocalDateTime dueDate) {
        if (planEndTime == null || dueDate == null || !planEndTime.isAfter(dueDate)) {
            return 0;
        }
        return Math.toIntExact(Duration.between(dueDate, planEndTime).toMinutes());
    }

    private LocalDateTime minTime(LocalDateTime current, LocalDateTime candidate) {
        if (candidate == null) {
            return current;
        }
        return current == null || candidate.isBefore(current) ? candidate : current;
    }

    private LocalDateTime maxTime(LocalDateTime current, LocalDateTime candidate) {
        if (candidate == null) {
            return current;
        }
        return current == null || candidate.isAfter(current) ? candidate : current;
    }

    /**
     * 补全任务条上的项目名称。
     *
     * <p>项目编号不在 {@code TaskGanttRespVO.Task} 里（前端不需要裸 ID），所以借一个临时载体过一遍
     * 批量补全，仍然只打一次 mdm 门面。
     */
    private void fillGanttProjectNames(List<TaskGanttRespVO.Task> bars, Map<Long, Long> taskIdToProjectId) {
        List<ProjectNameCarrier> carriers = bars.stream()
                .map(bar -> new ProjectNameCarrier(bar, taskIdToProjectId.get(bar.getId())))
                .toList();
        relationFiller.fillProjectName(carriers, ProjectNameCarrier::projectId,
                (carrier, name) -> carrier.bar().setProjectName(name));
    }

    /** 项目名称补全的临时载体。 */
    private record ProjectNameCarrier(TaskGanttRespVO.Task bar, Long projectId) {
    }

    private TaskGanttRespVO.Task toGanttTask(ApsTaskDO task) {
        TaskGanttRespVO.Task bar = new TaskGanttRespVO.Task();
        bar.setId(task.getId());
        bar.setTaskNo(task.getTaskNo());
        bar.setProdOrderId(task.getProdOrderId());
        bar.setProdOrderNo(task.getProdOrderNo());
        bar.setResId(task.getResId());
        bar.setResCode(task.getResCode());
        bar.setOpCode(task.getOpCode());
        bar.setOpName(task.getOpName());
        bar.setStartTime(task.getStartTime());
        bar.setEndTime(task.getEndTime());
        bar.setDurationMinutes(task.getDurationMinutes());
        bar.setChangeoverMinutes(task.getChangeoverMinutes());
        bar.setSequenceNo(task.getSequenceNo());
        bar.setLockedFlag(task.getLockedFlag());
        bar.setStatus(task.getStatus());
        return bar;
    }

    /** 依赖连线，丢弃两端任务不全在本版本内的悬空边。 */
    private List<TaskGanttRespVO.Link> buildLinks(Long versionId, List<ApsTaskDO> tasks) {
        Set<Long> taskIds = new HashSet<>(tasks.stream().map(ApsTaskDO::getId).toList());
        List<TaskGanttRespVO.Link> links = new ArrayList<>();
        for (ApsTaskDepDO dep : taskDepMapper.selectListByVersionId(versionId)) {
            if (!taskIds.contains(dep.getFromTaskId()) || !taskIds.contains(dep.getToTaskId())) {
                continue;
            }
            TaskGanttRespVO.Link link = new TaskGanttRespVO.Link();
            link.setFromTaskId(dep.getFromTaskId());
            link.setToTaskId(dep.getToTaskId());
            link.setDepType(dep.getDepType());
            link.setLagMinutes(dep.getLagMinutes());
            links.add(link);
        }
        return links;
    }

}
