package com.iboot.pf.module.aps.service.solve;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.dal.mysql.taskdep.ApsTaskDepMapper;
import com.iboot.pf.module.aps.enums.ApsCodeRuleConstants;
import com.iboot.pf.module.aps.enums.ApsTaskDepTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.service.coderule.ApsCodeService;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把求解结果落成排程任务与任务依赖。
 *
 * <p>整表重建而不是增量更新：求解产出的是一份完整的新计划，逐条比对更新既复杂又容易留下上一轮的残留。
 * 删除用物理删除，因为唯一索引带 deleted 列，逻辑删除后重算会撞唯一键。
 *
 * <p>单独成类并带独立事务：求解跑在异步线程里、可能几分钟，不能把数据库事务从头开到尾占着连接。
 * 事务只覆盖“删旧数据 + 写新数据”这一小段。
 */
@Service
public class ApsSolveResultWriter {

    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsTaskDepMapper taskDepMapper;
    @Resource
    private ApsCodeService codeService;

    /**
     * 保存求解结果。
     *
     * @param solution 求解结果
     * @return 落库的任务条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int save(ApsSolution solution) {
        Long versionId = solution.getVersionId();
        // 先删依赖再删任务：依赖引用任务编号，反过来会留下悬空引用
        taskDepMapper.deletePhysicalByVersionId(versionId);
        taskMapper.deletePhysicalByVersionId(versionId);
        List<ApsTaskDO> tasks = buildTasks(solution);
        if (CollUtil.isEmpty(tasks)) {
            return 0;
        }
        taskMapper.insertBatch(tasks);
        List<ApsTaskDepDO> deps = buildDeps(solution, tasks);
        if (CollUtil.isNotEmpty(deps)) {
            taskDepMapper.insertBatch(deps);
        }
        return tasks.size();
    }

    private List<ApsTaskDO> buildTasks(ApsSolution solution) {
        List<ApsTaskDO> rows = new ArrayList<>();
        for (ApsSolverResource resource : solution.getResources()) {
            int sequenceNo = 0;
            for (ApsSolverTask task : resource.getTaskList()) {
                rows.add(toTaskDO(solution.getVersionId(), resource, task, ++sequenceNo));
            }
        }
        return rows;
    }

    private ApsTaskDO toTaskDO(Long versionId, ApsSolverResource resource, ApsSolverTask task, int sequenceNo) {
        return ApsTaskDO.builder()
                .versionId(versionId)
                .taskNo(codeService.generate(ApsCodeRuleConstants.TASK, null))
                .prodOrderId(task.getProdOrderId())
                .prodOrderNo(task.getProdOrderNo())
                .workOrderId(task.getWorkOrderId())
                .opTaskId(task.getId())
                .routeOpId(task.getRouteOpId())
                .opCode(task.getOpCode())
                .opName(task.getOpName())
                .plantId(task.getPlantId())
                .workshopId(task.getWorkshopId())
                .projectId(task.getProjectId())
                .projectTaskId(task.getProjectTaskId())
                .resId(resource.getId())
                .resCode(resource.getCode())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .durationMinutes(task.getDurationMinutes())
                .changeoverMinutes(task.getChangeoverMinutes())
                .sequenceNo(sequenceNo)
                .lockedFlag(task.isPinned())
                // 锁定任务是已开工的在制工序，落回 LOCKED 状态；其余是新排定的计划
                .status(task.isPinned() ? ApsTaskStatusEnum.LOCKED.name() : ApsTaskStatusEnum.PLANNED.name())
                .build();
    }

    /**
     * 依赖边：把求解域里的工序前后序落成 {@code aps_task_dep}。
     *
     * <p>类型固定 {@code FINISH_START}，间隔取“前道转移 + 本道排队”，与求解时用的口径一致。
     * 不一致会导致人工调整时的校验和求解结论互相矛盾。
     */
    private List<ApsTaskDepDO> buildDeps(ApsSolution solution, List<ApsTaskDO> savedTasks) {
        Map<Long, Long> opTaskIdToTaskId = new LinkedHashMap<>();
        savedTasks.forEach(task -> opTaskIdToTaskId.put(task.getOpTaskId(), task.getId()));
        List<ApsTaskDepDO> deps = new ArrayList<>();
        for (ApsSolverTask task : solution.getTasks()) {
            Long toTaskId = opTaskIdToTaskId.get(task.getId());
            if (toTaskId == null) {
                continue;
            }
            for (ApsSolverTask predecessor : task.getPredecessors()) {
                Long fromTaskId = opTaskIdToTaskId.get(predecessor.getId());
                if (fromTaskId == null) {
                    continue;
                }
                deps.add(ApsTaskDepDO.builder()
                        .versionId(solution.getVersionId())
                        .fromTaskId(fromTaskId)
                        .toTaskId(toTaskId)
                        .depType(ApsTaskDepTypeEnum.FINISH_START.name())
                        .lagMinutes(task.lagMinutesAfter(predecessor))
                        .build());
            }
        }
        return deps;
    }

}
