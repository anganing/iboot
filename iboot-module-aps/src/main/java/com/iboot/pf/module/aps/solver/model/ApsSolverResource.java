package com.iboot.pf.module.aps.solver.model;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPinToIndex;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 求解域中的一台生产资源：本模型唯一的真实规划实体。
 *
 * <p>决策变量就是 {@link #taskList}：哪些工序排到这台资源上、按什么顺序。用列表变量而不是
 * “每个任务选一台资源 + 各自的开始时间”，是因为列表天然表达了资源上的独占排队，
 * 同一资源不可能出现两道工序时间重叠，这条硬约束由数据结构保证，不必再写约束流去罚它。
 *
 * <p>{@code parallelCapacity > 1} 的资源本轮按 1 处理：多工位并行需要把一台资源拆成多个工位实体，
 * 属于后续能力。
 */
@PlanningEntity
@Getter
@Setter
public class ApsSolverResource {

    /** 生产资源编号。 */
    @PlanningId
    private Long id;

    /** 资源编码快照。 */
    private String code;

    /** 资源名称快照。 */
    private String name;

    /** 资源整体效率系数，与工序能力效率相乘后折算工时。 */
    private BigDecimal efficiency;

    /** 资源组，换型矩阵按资源或资源组匹配。 */
    private String resGroup;

    /** 可用时间窗，按开始时间升序且互不重叠。 */
    private List<ApsTimeWindow> windows = new ArrayList<>();

    /** 工序能力：标准工序编号 -> 能力效率系数。 */
    private Map<Long, BigDecimal> capabilityEfficiencies = new LinkedHashMap<>();

    /** 换型矩阵：{@code 换出分组 + "->" + 换入分组} -> 换型分钟数。 */
    private Map<String, Integer> changeoverMinutes = new LinkedHashMap<>();

    /**
     * 排定在这台资源上的工序队列。
     *
     * <p>{@code allowsUnassignedValues = false}：每道待排工序都必须落到某台资源上。
     * 允许悬空会让求解器用“干脆不排”来规避惩罚，排出一份空计划却拿到满分。
     */
    @PlanningListVariable(valueRangeProviderRefs = ApsSolverConstants.TASK_RANGE_ID)
    private List<ApsSolverTask> taskList = new ArrayList<>();

    /**
     * 队列中已锁定的任务个数，下标小于它的元素不参与移动。
     *
     * <p>用 {@code @PlanningPinToIndex} 而不是在任务上打 {@code @PlanningPin}：列表变量模型里
     * “钉住某个值”的官方机制是按下标切分资源队列。把锁定任务从值域里剔除再手动塞进列表，
     * 会让值域与列表内容不一致，求解器直接报错。
     */
    @PlanningPinToIndex
    private int firstUnpinnedIndex;

    /** 这台资源做该工序的能力效率；未配置时按 1 处理。 */
    public BigDecimal capabilityEfficiencyOf(Long opId) {
        BigDecimal value = opId == null ? null : capabilityEfficiencies.get(opId);
        return value == null || value.signum() <= 0 ? BigDecimal.ONE : value;
    }

    /** 资源整体效率；未配置或非正数时按 1 处理，避免除零把工时算成无穷。 */
    public BigDecimal efficiencyOrDefault() {
        return efficiency == null || efficiency.signum() <= 0 ? BigDecimal.ONE : efficiency;
    }

    /**
     * 从上一道工序的换型分组切到当前分组的换型损失。
     *
     * <p>分组相同或任一为空时不产生换型：现场没配换型矩阵时按 0 处理，不能凭空造损失。
     */
    public int changeoverMinutesBetween(String fromGroup, String toGroup) {
        if (fromGroup == null || toGroup == null || fromGroup.isEmpty() || toGroup.isEmpty()
                || fromGroup.equals(toGroup)) {
            return 0;
        }
        Integer minutes = changeoverMinutes.get(fromGroup + ApsSolverConstants.CHANGEOVER_KEY_SEPARATOR + toGroup);
        return minutes == null ? 0 : minutes;
    }

    @Override
    public String toString() {
        return "ApsSolverResource{" + code + "}";
    }

}
