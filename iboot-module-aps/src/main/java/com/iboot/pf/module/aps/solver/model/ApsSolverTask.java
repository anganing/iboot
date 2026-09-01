package com.iboot.pf.module.aps.solver.model;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.IndexShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariablesInconsistent;
import com.iboot.pf.module.aps.solver.calculate.ApsCalendarCalculator;
import com.iboot.pf.module.aps.solver.calculate.ApsDurationCalculator;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 求解域中的一道工序任务：本模型的被排对象。
 *
 * <p>它是 {@link ApsSolverResource#getTaskList()} 的元素（规划值），同时又被注册成规划实体，
 * 这样才能挂影子变量。所有字段都来自场景投影快照，求解期间不回查数据库。
 *
 * <p>时间不是决策变量：资源与资源内顺序一旦确定，起止时间就唯一确定，用声明式影子变量
 * （{@code @ShadowVariable(supplierName)} + {@code @ShadowSources}）推导。这样搜索空间只有
 * “排到哪台设备、排第几位”，比把时间也当变量小几个数量级。
 *
 * <p>选声明式而不是手写 {@code ListVariableListener}：工序前后序会跨资源（前道在设备 A、后道在设备 B），
 * 手写监听器要自己实现跨实体传播与环路保护，容易漏更新导致增量得分和真实得分不一致
 * （score corruption）——排出来的计划看着可行其实违约。声明式只需在 {@code @ShadowSources} 里
 * 声明依赖路径，传播顺序与环路检测都由求解器负责，中间态出现循环等待时把
 * {@link #inconsistent} 置真，由硬约束罚掉。
 */
@PlanningEntity
@Getter
@Setter
public class ApsSolverTask {

    /** 工序任务编号，来自 {@code aps_projection_order.op_task_id}。 */
    @PlanningId
    private Long id;

    /** 生产工单编号。 */
    private Long prodOrderId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 作业工单编号，0 表示无作业工单。 */
    private Long workOrderId;

    /** 路线工序编号，0 表示无路线工序。 */
    private Long routeOpId;

    /** 标准工序编号，用于匹配资源能力。 */
    private Long opId;

    /** 工序编码快照。 */
    private String opCode;

    /** 工序名称快照。 */
    private String opName;

    /** 工厂编号。 */
    private Long plantId;

    /** 车间编号，0 表示未指定。 */
    private Long workshopId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 生产物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 待加工数量。 */
    private BigDecimal qty;

    /** 交期。 */
    private LocalDateTime dueDate;

    /** 优先级，数值越大越优先。 */
    private Integer priority;

    /** 工序在路线中的顺序号。 */
    private Integer seq;

    /** 准备（换型）工时。 */
    private Integer setupMinutes;

    /** 单件加工工时。 */
    private BigDecimal stdMinutesPerUnit;

    /** 收尾工时。 */
    private Integer teardownMinutes;

    /** 排队等待分钟数，作为前道工序完工后的最小间隔，不占资源。 */
    private Integer queueMinutes;

    /** 转移分钟数，作为本道工序完工后的最小间隔，不占资源。 */
    private Integer moveMinutes;

    /** 工时类型，取值见 {@link com.iboot.pf.module.aps.enums.ApsDurationTypeEnum}。 */
    private String durationType;

    /** 固定工时分钟数。 */
    private Integer stdDuration;

    /** 标准人数。 */
    private Integer manQty;

    /** 物料换型分组，空串表示不参与换型计算。 */
    private String changeoverGroup;

    /**
     * 是否缺料，来自投影阶段冻结的齐套结论。
     *
     * <p>是问题事实而不是影子变量：齐套判定跟排到哪台设备、排第几位无关，
     * 做成影子变量只会让求解器每次移动都白算一遍。
     */
    private boolean materialShortage;

    /**
     * 能做这道工序的资源编号集合，来自资源能力快照。
     *
     * <p>空集合表示主数据没配能力：此时不限制资源，交给软约束优化，而不是直接判为无解。
     * 现场刚上线时能力矩阵往往是空的，硬卡会让所有任务都排不出来。
     */
    private Set<Long> eligibleResIds = new LinkedHashSet<>();

    /**
     * 前道工序任务，同工单内按 {@code seq} 生成。
     *
     * <p>是问题事实，求解期间不得变更：{@code @ShadowSources} 的集合路径要求集合本身稳定，
     * 中途改动会让依赖图与实际结构脱节。
     */
    private List<ApsSolverTask> predecessors = new ArrayList<>();

    /** 是否锁定：已开工或人工锁定的任务不允许重排，资源与时间都保持原值。 */
    private boolean pinned;

    /** 锁定任务的原资源编号。 */
    private Long pinnedResId;

    /** 锁定任务的原计划开始时间。 */
    private LocalDateTime pinnedStartTime;

    /** 锁定任务的原计划结束时间。 */
    private LocalDateTime pinnedEndTime;

    /** 排定的资源，由列表变量反推。 */
    @InverseRelationShadowVariable(sourceVariableName = "taskList")
    private ApsSolverResource resource;

    /** 在资源队列中的下标；未分派时为 {@code null}。 */
    @IndexShadowVariable(sourceVariableName = "taskList")
    private Integer indexInResource;

    /** 同一资源上的前一道任务；队首为 {@code null}。换型损失按它的换型分组计算。 */
    @PreviousElementShadowVariable(sourceVariableName = "taskList")
    private ApsSolverTask previousTask;

    /** 本次换型损失分钟数。 */
    @ShadowVariable(supplierName = "changeoverMinutesSupplier")
    private Integer changeoverMinutes;

    /** 计划占用分钟数（含换型）。 */
    @ShadowVariable(supplierName = "durationMinutesSupplier")
    private Integer durationMinutes;

    /** 计划开始时间。 */
    @ShadowVariable(supplierName = "startTimeSupplier")
    private LocalDateTime startTime;

    /** 计划结束时间。 */
    @ShadowVariable(supplierName = "endTimeSupplier")
    private LocalDateTime endTime;

    /**
     * 影子变量是否处于不一致状态。
     *
     * <p>求解中间态可能出现“前道排在后道之后”的循环等待，求解器检测到环路后把相关影子变量置空并把这里置真。
     * 由硬约束罚掉即可，求解器自己会通过移动任务打破环路。
     */
    @ShadowVariablesInconsistent
    private boolean inconsistent;

    /**
     * 换型损失：同资源上从前一道任务的换型分组切到本道分组。
     *
     * <p>依赖资源（换型矩阵）与队列前一道任务，两者变了都要重算。
     */
    @ShadowSources({"resource", "previousTask"})
    public Integer changeoverMinutesSupplier() {
        if (resource == null) {
            return null;
        }
        return ApsDurationCalculator.calculateChangeoverMinutes(this, resource, previousTask);
    }

    /** 占用分钟数：换型 + 按资源效率折算的净工时。锁定任务用原计划时长。 */
    @ShadowSources({"resource", "changeoverMinutes"})
    public Integer durationMinutesSupplier() {
        if (resource == null) {
            return null;
        }
        if (pinned && pinnedStartTime != null && pinnedEndTime != null) {
            return (int) java.time.Duration.between(pinnedStartTime, pinnedEndTime).toMinutes();
        }
        int changeover = changeoverMinutes == null ? 0 : changeoverMinutes;
        return changeover + ApsDurationCalculator.calculateWorkMinutes(this, resource);
    }

    /**
     * 开始时间：三个下界取最大后对齐到资源可用时间窗。
     *
     * <p>三个下界分别是计划起点、同资源前一道任务的完工时间、所有前置工序完工时间加间隔。
     * {@code predecessors[].endTime} 这条路径就是跨资源传播的声明：前道无论排在哪台设备上，
     * 它的完工时间变了都会触发本任务重算。
     */
    @ShadowSources({"resource", "previousTask.endTime", "predecessors[].endTime"})
    public LocalDateTime startTimeSupplier(ApsSolution solution) {
        if (resource == null) {
            return null;
        }
        // 锁定任务保持原计划：已开工的工序不允许被重排挪走
        if (pinned && pinnedStartTime != null) {
            return pinnedStartTime;
        }
        LocalDateTime earliest = solution.getPlanTime();
        if (previousTask != null) {
            if (previousTask.getEndTime() == null) {
                // 前一道还没算出完工时间，本道也无法确定
                return null;
            }
            if (previousTask.getEndTime().isAfter(earliest)) {
                earliest = previousTask.getEndTime();
            }
        }
        for (ApsSolverTask predecessor : predecessors) {
            if (predecessor.getEndTime() == null) {
                return null;
            }
            LocalDateTime ready = predecessor.getEndTime().plusMinutes(lagMinutesAfter(predecessor));
            if (ready.isAfter(earliest)) {
                earliest = ready;
            }
        }
        return ApsCalendarCalculator.alignToWorkTime(earliest, resource.getWindows());
    }

    /**
     * 结束时间：从开始时间起按资源日历累加占用分钟。
     *
     * <p>返回 {@code null} 表示计划窗口内产能已耗尽，由“任务必须落在可用产能内”硬约束罚掉，
     * 而不是硬塞到窗外排出一份车间做不到的计划。
     */
    @ShadowSources({"startTime", "durationMinutes"})
    public LocalDateTime endTimeSupplier() {
        if (resource == null || startTime == null || durationMinutes == null) {
            return null;
        }
        if (pinned && pinnedEndTime != null) {
            return pinnedEndTime;
        }
        return ApsCalendarCalculator.addWorkingMinutes(startTime, durationMinutes, resource.getWindows());
    }

    /**
     * 前道任务完工后到本道任务开工之间的最小间隔。
     *
     * <p>口径：前道的转移时间 + 本道的排队时间。两者都不占资源，只把开工时间往后推。
     */
    public int lagMinutesAfter(ApsSolverTask predecessor) {
        int move = predecessor == null || predecessor.moveMinutes == null ? 0 : predecessor.moveMinutes;
        return move + (queueMinutes == null ? 0 : queueMinutes);
    }

    /** 资源是否具备做这道工序的能力；能力未配置时不限制。 */
    public boolean isResourceEligible(Long resId) {
        return eligibleResIds.isEmpty() || eligibleResIds.contains(resId);
    }

    @Override
    public String toString() {
        return "ApsSolverTask{" + prodOrderNo + "-" + opCode + "}";
    }

}
