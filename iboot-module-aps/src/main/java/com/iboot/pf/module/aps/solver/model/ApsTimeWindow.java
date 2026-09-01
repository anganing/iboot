package com.iboot.pf.module.aps.solver.model;

import java.time.LocalDateTime;

/**
 * 资源可用时间窗。
 *
 * <p>由资源投影的“日期 + 班次起止”展开而来，是求解时唯一的产能边界：任务只能落在窗内，
 * 窗与窗之间的空隙（换班、非工作日）不计入工时。
 *
 * <p>跨零点班次（例如 22:00-06:00）在展开时已经拆成 {@code start < end} 的绝对时间区间，
 * 这里不再处理跨天判断。
 *
 * @param start 窗口开始时刻（含）
 * @param end   窗口结束时刻（不含）
 */
public record ApsTimeWindow(LocalDateTime start, LocalDateTime end) {

    /** 窗口内可用分钟数。 */
    public long minutes() {
        return java.time.Duration.between(start, end).toMinutes();
    }

    /** 给定时刻是否落在窗内；右端点开区间，正好等于 end 视为已在窗外。 */
    public boolean contains(LocalDateTime time) {
        return !time.isBefore(start) && time.isBefore(end);
    }

}
