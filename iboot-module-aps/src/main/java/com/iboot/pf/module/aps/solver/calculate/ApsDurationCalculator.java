package com.iboot.pf.module.aps.solver.calculate;

import com.iboot.pf.module.aps.enums.ApsDurationTypeEnum;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 工序占用时长计算。
 *
 * <p>口径（与 {@code dev-docs/MDM基础数据补齐方案.md} 3.1 节一致）：
 * <pre>
 * duration = changeover + setup + qty × perUnit ÷ (资源效率 × 能力效率) + teardown
 * </pre>
 * 只有加工工时按效率折算，准备与收尾是固定动作，不随效率变化。
 *
 * <p>{@code queueMinutes} 与 {@code moveMinutes} 不在这里：它们是工序之间的等待与转移，
 * 只把后道开工时间往后推，不占用资源产能。混进 duration 会让资源负荷虚高。
 */
public final class ApsDurationCalculator {

    /** 折算加工工时的中间精度，最终结果向上取整到分钟。 */
    private static final int SCALE = 6;

    private ApsDurationCalculator() {
    }

    /**
     * 计算净加工工时（不含换型）。
     *
     * @param task     工序任务
     * @param resource 排定资源
     * @return 净工时分钟数，向上取整；不足 1 分钟按 1 分钟计
     */
    public static int calculateWorkMinutes(ApsSolverTask task, ApsSolverResource resource) {
        if (task == null || resource == null) {
            return 0;
        }
        int setup = orZero(task.getSetupMinutes());
        int teardown = orZero(task.getTeardownMinutes());
        if (ApsDurationTypeEnum.isFixed(task.getDurationType())) {
            // 固定工时：数量不影响时长，准备与收尾已经包含在标准时长里
            return Math.max(orZero(task.getStdDuration()), 0);
        }
        BigDecimal qty = task.getQty() == null ? BigDecimal.ZERO : task.getQty();
        BigDecimal perUnit = task.getStdMinutesPerUnit() == null ? BigDecimal.ZERO : task.getStdMinutesPerUnit();
        BigDecimal efficiency = resource.efficiencyOrDefault()
                .multiply(resource.capabilityEfficiencyOf(task.getOpId()));
        BigDecimal processing = qty.multiply(perUnit).divide(efficiency, SCALE, RoundingMode.HALF_UP);
        // 向上取整：宁可多留一分钟产能，也不让计划比实际乐观
        int processingMinutes = processing.setScale(0, RoundingMode.CEILING).intValueExact();
        return setup + processingMinutes + teardown;
    }

    /**
     * 计算换型损失分钟数。
     *
     * <p>按“同资源上前一道任务的换型分组 -> 本道任务的换型分组”查资源换型矩阵。
     * 队首任务没有前道，不产生换型。
     */
    public static int calculateChangeoverMinutes(ApsSolverTask task, ApsSolverResource resource,
                                                ApsSolverTask previousTask) {
        if (task == null || resource == null || previousTask == null) {
            return 0;
        }
        return resource.changeoverMinutesBetween(previousTask.getChangeoverGroup(), task.getChangeoverGroup());
    }

    /** 总占用分钟数：换型 + 净工时。落库到 {@code aps_task.duration_minutes}。 */
    public static int calculateTotalMinutes(ApsSolverTask task, ApsSolverResource resource,
                                           ApsSolverTask previousTask) {
        return calculateChangeoverMinutes(task, resource, previousTask)
                + calculateWorkMinutes(task, resource);
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

}
