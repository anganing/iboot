package com.iboot.pf.module.aps.solver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceCapability;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsOrderProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsResourceProjectionMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import com.iboot.pf.module.aps.solver.model.ApsTimeWindow;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResChangeoverRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_NO_RESOURCE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_NO_TASK;

/**
 * 把场景投影快照组装成求解输入。
 *
 * <p>输入只来自四张投影表与 mdm 换型矩阵：投影是场景冻结的副本，同一场景重复求解得到同一份输入，
 * 结果可复现、可追溯。唯一的例外是换型矩阵，它是配置型主数据、量小且不随场景变化，
 * 单独建一张投影表的收益不抵维护成本。
 */
@Component
public class ApsSolverProblemBuilder {

    @Resource
    private ApsResourceProjectionMapper resourceProjectionMapper;
    @Resource
    private ApsOrderProjectionMapper orderProjectionMapper;
    @Resource
    private ResourceApi resourceApi;

    /**
     * 组装一次求解的输入。
     *
     * @param scenario 排程场景
     * @param versionId 排程版本编号，同时作为 problemId
     * @param config 策略配置
     * @return 求解输入
     */
    public ApsSolution build(ApsScenarioDO scenario, Long versionId, ApsStrategyConfig config) {
        List<ApsResourceProjectionDO> resourceRows =
                resourceProjectionMapper.selectListByScenarioId(scenario.getId());
        if (CollUtil.isEmpty(resourceRows)) {
            throw exception(SOLVE_NO_RESOURCE);
        }
        List<ApsOrderProjectionDO> orderRows = orderProjectionMapper
                .selectListByScenario(scenario.getId(), null, null, null, null);
        if (CollUtil.isEmpty(orderRows)) {
            throw exception(SOLVE_NO_TASK);
        }
        LocalDateTime planTime = resolvePlanTime(scenario);
        List<ApsSolverResource> resources = buildResources(resourceRows, planTime);
        if (CollUtil.isEmpty(resources)) {
            throw exception(SOLVE_NO_RESOURCE);
        }
        List<ApsSolverTask> tasks = buildTasks(orderRows, resources);
        if (CollUtil.isEmpty(tasks)) {
            throw exception(SOLVE_NO_TASK);
        }
        linkPredecessors(tasks);
        assignPinnedTasks(resources, tasks);

        ApsSolution solution = new ApsSolution();
        solution.setVersionId(versionId);
        solution.setScenarioId(scenario.getId());
        solution.setPlanTime(planTime);
        solution.setStrategyConfig(config);
        solution.setResources(resources);
        solution.setTasks(tasks);
        solution.setConstraintWeightOverrides(ApsConstraintWeightResolver.resolve(config));
        return solution;
    }

    /**
     * 计划起点：场景窗口开始时间与当前时刻取较晚者。
     *
     * <p>场景窗口起点已经过去时用当前时刻，否则会把工序排到过去，车间没法执行。
     */
    private LocalDateTime resolvePlanTime(ApsScenarioDO scenario) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = scenario.getPlanStartTime();
        return windowStart == null || windowStart.isBefore(now) ? now : windowStart;
    }

    /**
     * 资源与可用时间窗。
     *
     * <p>时间窗优先用班次明细（shiftId != 0）：只有班次才知道一天里哪几段能干活。
     * 某个资源某天完全没有班次明细时，回落到日汇总记录，按“从当天 0 点起连续 availableMinutes 分钟”近似，
     * 这是没有班次数据时唯一能做的推断，比整天视为可用要保守。
     */
    private List<ApsSolverResource> buildResources(List<ApsResourceProjectionDO> rows, LocalDateTime planTime) {
        Map<Long, List<ApsResourceProjectionDO>> byResource = new LinkedHashMap<>();
        for (ApsResourceProjectionDO row : rows) {
            byResource.computeIfAbsent(row.getSourceResId(), key -> new ArrayList<>()).add(row);
        }
        Map<Long, ResourceRespDTO> resourceMap = resourceApi.getResourceMap(byResource.keySet());
        Map<Long, Map<String, Integer>> changeoverMap = buildChangeoverMap(byResource.keySet(), resourceMap);
        List<ApsSolverResource> result = new ArrayList<>(byResource.size());
        byResource.forEach((resId, resourceRows) -> {
            ApsResourceProjectionDO first = resourceRows.get(0);
            ResourceRespDTO master = resourceMap.get(resId);
            ApsSolverResource resource = new ApsSolverResource();
            resource.setId(resId);
            resource.setCode(first.getResCode());
            resource.setName(first.getResName());
            resource.setEfficiency(master == null ? BigDecimal.ONE : master.getEfficiency());
            resource.setResGroup(master == null ? null : master.getResGroup());
            resource.setWindows(buildWindows(resourceRows, planTime));
            resource.setCapabilityEfficiencies(parseCapabilities(first.getCapabilityJson()));
            resource.setChangeoverMinutes(changeoverMap.getOrDefault(resId, Map.of()));
            // 没有任何可用产能的资源不参与排程，留着只会让求解器白试
            if (!resource.getWindows().isEmpty()) {
                result.add(resource);
            }
        });
        return result;
    }

    /** 按“日期 + 班次起止”展开时间窗，过滤掉计划起点之前的部分并按开始时间排序。 */
    private List<ApsTimeWindow> buildWindows(List<ApsResourceProjectionDO> rows, LocalDateTime planTime) {
        List<ApsTimeWindow> windows = new ArrayList<>();
        Set<LocalDate> shiftCoveredDates = new LinkedHashSet<>();
        for (ApsResourceProjectionDO row : rows) {
            if (ApsConstants.NONE_ID.equals(row.getShiftId())
                    || row.getShiftStartTime() == null || row.getShiftEndTime() == null) {
                continue;
            }
            shiftCoveredDates.add(row.getWorkDate());
            windows.add(toShiftWindow(row));
        }
        for (ApsResourceProjectionDO row : rows) {
            // 只对没有班次明细的日期回落到日汇总，避免同一天既算班次又算汇总，产能翻倍
            if (!ApsConstants.NONE_ID.equals(row.getShiftId()) || shiftCoveredDates.contains(row.getWorkDate())) {
                continue;
            }
            int minutes = row.getAvailableMinutes() == null ? 0 : row.getAvailableMinutes();
            if (minutes <= 0) {
                continue;
            }
            LocalDateTime start = row.getWorkDate().atStartOfDay();
            windows.add(new ApsTimeWindow(start, start.plusMinutes(minutes)));
        }
        return windows.stream()
                .map(window -> clipToPlanTime(window, planTime))
                .filter(window -> window != null && window.minutes() > 0)
                .sorted(Comparator.comparing(ApsTimeWindow::start))
                .toList();
    }

    /** 班次窗口；结束时间不晚于开始时间的按跨零点处理，结束落到次日。 */
    private ApsTimeWindow toShiftWindow(ApsResourceProjectionDO row) {
        LocalTime startTime = row.getShiftStartTime();
        LocalTime endTime = row.getShiftEndTime();
        LocalDateTime start = row.getWorkDate().atTime(startTime);
        LocalDateTime end = endTime.isAfter(startTime)
                ? row.getWorkDate().atTime(endTime)
                : row.getWorkDate().plusDays(1).atTime(endTime);
        return new ApsTimeWindow(start, end);
    }

    /** 裁掉计划起点之前的产能：已经过去的班次不能再用来排产。 */
    private ApsTimeWindow clipToPlanTime(ApsTimeWindow window, LocalDateTime planTime) {
        if (!window.end().isAfter(planTime)) {
            return null;
        }
        return window.start().isBefore(planTime) ? new ApsTimeWindow(planTime, window.end()) : window;
    }

    /** 解析能力快照 JSON：标准工序编号 -> 能力效率。 */
    private Map<Long, BigDecimal> parseCapabilities(String capabilityJson) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        if (StrUtil.isBlank(capabilityJson)) {
            return result;
        }
        List<ApsResourceCapability> capabilities = JsonUtils.parseObjectQuietly(capabilityJson,
                new TypeReference<List<ApsResourceCapability>>() {
                });
        if (capabilities == null) {
            return result;
        }
        for (ApsResourceCapability capability : capabilities) {
            if (capability.opId() != null) {
                result.put(capability.opId(), capability.efficiencyOrDefault());
            }
        }
        return result;
    }

    /**
     * 换型矩阵：资源编号 -> ({@code 换出分组->换入分组} -> 分钟数)。
     *
     * <p>{@code mdm_res_changeover} 支持按资源或按资源组配置，按资源的更具体、优先生效。
     */
    private Map<Long, Map<String, Integer>> buildChangeoverMap(Set<Long> resIds,
                                                              Map<Long, ResourceRespDTO> resourceMap) {
        Set<String> resGroups = new LinkedHashSet<>();
        resourceMap.values().forEach(item -> {
            if (StrUtil.isNotBlank(item.getResGroup())) {
                resGroups.add(item.getResGroup());
            }
        });
        List<ResChangeoverRespDTO> changeovers = resourceApi.getChangeoverList(resIds, resGroups);
        Map<Long, Map<String, Integer>> result = new LinkedHashMap<>();
        // 先铺按资源组的规则，再让按资源的规则覆盖它
        for (ResChangeoverRespDTO changeover : changeovers) {
            if (!ApsConstants.NONE_ID.equals(changeover.getResId())) {
                continue;
            }
            resIds.stream()
                    .filter(resId -> matchesGroup(resourceMap.get(resId), changeover.getResGroup()))
                    .forEach(resId -> putChangeover(result, resId, changeover));
        }
        for (ResChangeoverRespDTO changeover : changeovers) {
            if (ApsConstants.NONE_ID.equals(changeover.getResId())) {
                continue;
            }
            putChangeover(result, changeover.getResId(), changeover);
        }
        return result;
    }

    private boolean matchesGroup(ResourceRespDTO resource, String resGroup) {
        return resource != null && StrUtil.isNotBlank(resGroup) && resGroup.equals(resource.getResGroup());
    }

    private void putChangeover(Map<Long, Map<String, Integer>> target, Long resId,
                               ResChangeoverRespDTO changeover) {
        if (StrUtil.isBlank(changeover.getFromGroup()) || StrUtil.isBlank(changeover.getToGroup())) {
            return;
        }
        String key = changeover.getFromGroup() + "->" + changeover.getToGroup();
        target.computeIfAbsent(resId, id -> new LinkedHashMap<>())
                .put(key, changeover.getChangeoverMinutes() == null ? 0 : changeover.getChangeoverMinutes());
    }

    /** 工单投影转求解任务；只取工序级投影，需求与整单级别没有工序，无法排到具体资源上。 */
    private List<ApsSolverTask> buildTasks(List<ApsOrderProjectionDO> rows, List<ApsSolverResource> resources) {
        // 标准工序 -> 具备该能力的资源集合，由资源能力快照反向索引得到
        Map<Long, Set<Long>> opToResources = new LinkedHashMap<>();
        for (ApsSolverResource resource : resources) {
            resource.getCapabilityEfficiencies().keySet().forEach(opId ->
                    opToResources.computeIfAbsent(opId, key -> new LinkedHashSet<>()).add(resource.getId()));
        }
        List<ApsSolverTask> tasks = new ArrayList<>(rows.size());
        for (ApsOrderProjectionDO row : rows) {
            if (ApsConstants.NONE_ID.equals(row.getOpTaskId())) {
                continue;
            }
            tasks.add(toTask(row, opToResources));
        }
        return tasks;
    }

    private ApsSolverTask toTask(ApsOrderProjectionDO row, Map<Long, Set<Long>> opToResources) {
        ApsSolverTask task = new ApsSolverTask();
        task.setId(row.getOpTaskId());
        task.setProdOrderId(row.getProdOrderId());
        task.setProdOrderNo(row.getSourceNo());
        task.setWorkOrderId(row.getWorkOrderId());
        task.setRouteOpId(row.getRouteOpId());
        task.setOpId(row.getOpId());
        task.setOpCode(row.getOpCode());
        task.setOpName(row.getOpName());
        task.setPlantId(row.getPlantId());
        task.setWorkshopId(row.getWorkshopId());
        task.setProjectId(row.getProjectId());
        task.setProjectTaskId(row.getProjectTaskId());
        task.setMatId(row.getMatId());
        task.setMatCode(row.getMatCode());
        task.setQty(remainingQty(row));
        task.setDueDate(row.getDueDate());
        task.setPriority(row.getPriority());
        task.setSeq(row.getSeq());
        task.setSetupMinutes(row.getSetupMinutes());
        task.setStdMinutesPerUnit(row.getStdMinutesPerUnit());
        task.setTeardownMinutes(row.getTeardownMinutes());
        task.setQueueMinutes(row.getQueueMinutes());
        task.setMoveMinutes(row.getMoveMinutes());
        task.setDurationType(row.getDurationType());
        task.setStdDuration(row.getStdDuration());
        task.setManQty(row.getManQty());
        task.setChangeoverGroup(row.getChangeoverGroup());
        task.setPinned(Boolean.TRUE.equals(row.getLockedFlag()));
        task.setPinnedResId(row.getResId());
        task.setPinnedStartTime(row.getActualStartTime());
        // 能力候选资源按标准工序编号反查；主数据没配能力时留空集合，表示不限制
        task.setEligibleResIds(new LinkedHashSet<>(
                opToResources.getOrDefault(row.getOpId(), Set.of())));
        return task;
    }

    /** 待加工数量 = 计划数量 - 已完成数量，负数归零。 */
    private BigDecimal remainingQty(ApsOrderProjectionDO row) {
        BigDecimal qty = row.getQty() == null ? BigDecimal.ZERO : row.getQty();
        BigDecimal completed = row.getCompletedQty() == null ? BigDecimal.ZERO : row.getCompletedQty();
        BigDecimal remaining = qty.subtract(completed);
        return remaining.signum() < 0 ? BigDecimal.ZERO : remaining;
    }

    /**
     * 同工单内按 {@code seq} 串成前后序链。
     *
     * <p>只连相邻的一道：{@code seq} 10/20/30 时 20 依赖 10、30 依赖 20，不让 30 直接依赖 10。
     * 传递依赖由链式关系自然满足，全连会让约束流的匹配数量按平方增长。
     */
    private void linkPredecessors(List<ApsSolverTask> tasks) {
        Map<Long, List<ApsSolverTask>> byOrder = new LinkedHashMap<>();
        for (ApsSolverTask task : tasks) {
            byOrder.computeIfAbsent(task.getProdOrderId(), key -> new ArrayList<>()).add(task);
        }
        byOrder.values().forEach(orderTasks -> {
            orderTasks.sort(Comparator.comparing(task -> task.getSeq() == null ? 0 : task.getSeq()));
            for (int index = 1; index < orderTasks.size(); index++) {
                ApsSolverTask current = orderTasks.get(index);
                ApsSolverTask previous = orderTasks.get(index - 1);
                // 同一 seq 属于并行工序，彼此没有先后关系
                if (java.util.Objects.equals(current.getSeq(), previous.getSeq())) {
                    continue;
                }
                current.getPredecessors().add(previous);
            }
        });
    }

    /**
     * 把锁定任务预先放进原资源队列，并用 {@code firstUnpinnedIndex} 钉住。
     *
     * <p>锁定任务已经在车间开工，必须留在原资源、原时间上；求解器只能在它们之后安排剩下的工序。
     * 原资源不在本次范围内（例如资源被停用）时不钉，交给硬约束报出来，不能悄悄挪走。
     */
    private void assignPinnedTasks(List<ApsSolverResource> resources, List<ApsSolverTask> tasks) {
        Map<Long, ApsSolverResource> resourceMap = new LinkedHashMap<>();
        resources.forEach(resource -> resourceMap.put(resource.getId(), resource));
        List<ApsSolverTask> pinnedTasks = tasks.stream()
                .filter(ApsSolverTask::isPinned)
                .filter(task -> task.getPinnedStartTime() != null)
                .filter(task -> resourceMap.containsKey(task.getPinnedResId()))
                .sorted(Comparator.comparing(ApsSolverTask::getPinnedStartTime))
                .toList();
        for (ApsSolverTask task : pinnedTasks) {
            ApsSolverResource resource = resourceMap.get(task.getPinnedResId());
            resource.getTaskList().add(task);
            resource.setFirstUnpinnedIndex(resource.getTaskList().size());
        }
    }

}
