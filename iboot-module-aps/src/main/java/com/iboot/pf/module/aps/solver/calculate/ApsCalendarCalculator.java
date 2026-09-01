package com.iboot.pf.module.aps.solver.calculate;

import com.iboot.pf.module.aps.solver.model.ApsTimeWindow;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 按资源可用时间窗推进工时。
 *
 * <p>两件事：把“最早可开工时刻”对齐到下一个可用窗口的起点；把净工时按窗口逐段累加，
 * 跨班次、跨天、跨非工作日时自动跳过空隙。窗口列表必须按开始时间升序且互不重叠，
 * 由 {@code ApsSolverProblemBuilder} 在构建输入时保证。
 *
 * <p>纯静态工具：影子变量监听器每次移动都会调用它，不能带任何可变状态。
 */
public final class ApsCalendarCalculator {

    private ApsCalendarCalculator() {
    }

    /**
     * 把最早可开工时刻对齐到可用窗口内。
     *
     * @param earliestStart 最早可开工时刻
     * @param windows       资源可用时间窗，升序
     * @return 实际可开工时刻；所有窗口都已用尽时返回 {@code null}
     */
    public static LocalDateTime alignToWorkTime(LocalDateTime earliestStart, List<ApsTimeWindow> windows) {
        if (earliestStart == null || windows == null || windows.isEmpty()) {
            return null;
        }
        for (ApsTimeWindow window : windows) {
            // 落在窗内直接用；落在窗前则顺延到窗口起点
            if (window.contains(earliestStart)) {
                return earliestStart;
            }
            if (earliestStart.isBefore(window.start())) {
                return window.start();
            }
        }
        // 计划窗口内已经排不下：交给硬约束惩罚，而不是硬塞到窗外
        return null;
    }

    /**
     * 从给定时刻起累加净工时，得到结束时刻。
     *
     * <p>只有落在可用窗内的分钟才计入工时；窗与窗之间的空隙不计入，但会把结束时刻往后推。
     *
     * @param start       开始时刻，必须已经对齐到窗内
     * @param workMinutes 净工时分钟数
     * @param windows     资源可用时间窗，升序
     * @return 结束时刻；剩余产能不足时返回 {@code null}
     */
    public static LocalDateTime addWorkingMinutes(LocalDateTime start, long workMinutes,
                                                  List<ApsTimeWindow> windows) {
        if (start == null || windows == null || windows.isEmpty()) {
            return null;
        }
        // 零工时任务（例如纯检验节点）不占时间，起止相同
        if (workMinutes <= 0) {
            return start;
        }
        long remaining = workMinutes;
        LocalDateTime cursor = start;
        for (ApsTimeWindow window : windows) {
            if (!window.end().isAfter(cursor)) {
                continue;
            }
            // 光标落在窗前时跳到窗口起点，中间的空隙不消耗工时
            if (cursor.isBefore(window.start())) {
                cursor = window.start();
            }
            long available = java.time.Duration.between(cursor, window.end()).toMinutes();
            if (available >= remaining) {
                return cursor.plusMinutes(remaining);
            }
            remaining -= available;
            cursor = window.end();
        }
        return null;
    }

}
