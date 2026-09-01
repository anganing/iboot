package com.iboot.pf.module.aps.solver.calculate;

import com.iboot.pf.module.aps.solver.model.ApsTimeWindow;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 日历推进单元测试：跨班次、跨天、卡边界、产能耗尽。 */
class ApsCalendarCalculatorTest {

    private static final LocalDateTime DAY1_MORNING = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDateTime DAY1_NOON = LocalDateTime.of(2026, 9, 1, 12, 0);
    private static final LocalDateTime DAY1_AFTERNOON = LocalDateTime.of(2026, 9, 1, 13, 0);
    private static final LocalDateTime DAY1_EVENING = LocalDateTime.of(2026, 9, 1, 17, 0);
    private static final LocalDateTime DAY2_MORNING = LocalDateTime.of(2026, 9, 2, 8, 0);
    private static final LocalDateTime DAY2_NOON = LocalDateTime.of(2026, 9, 2, 12, 0);

    /** 两天各两个班次，中间有午休与整夜的空隙。 */
    private List<ApsTimeWindow> twoDayWindows() {
        return List.of(
                new ApsTimeWindow(DAY1_MORNING, DAY1_NOON),
                new ApsTimeWindow(DAY1_AFTERNOON, DAY1_EVENING),
                new ApsTimeWindow(DAY2_MORNING, DAY2_NOON));
    }

    @Test
    void alignShouldKeepTimeInsideWindow() {
        LocalDateTime inside = LocalDateTime.of(2026, 9, 1, 9, 30);
        assertEquals(inside, ApsCalendarCalculator.alignToWorkTime(inside, twoDayWindows()));
    }

    @Test
    void alignShouldSkipGapToNextWindowStart() {
        // 午休时间点顺延到下午班开始
        LocalDateTime inGap = LocalDateTime.of(2026, 9, 1, 12, 30);
        assertEquals(DAY1_AFTERNOON, ApsCalendarCalculator.alignToWorkTime(inGap, twoDayWindows()));
    }

    @Test
    void alignShouldTreatWindowEndAsOutside() {
        // 恰好等于窗口结束时刻已在窗外，顺延到下一个窗口
        assertEquals(DAY1_AFTERNOON, ApsCalendarCalculator.alignToWorkTime(DAY1_NOON, twoDayWindows()));
    }

    @Test
    void alignShouldReturnNullWhenAllWindowsExhausted() {
        LocalDateTime afterAll = LocalDateTime.of(2026, 9, 3, 8, 0);
        assertNull(ApsCalendarCalculator.alignToWorkTime(afterAll, twoDayWindows()));
    }

    @Test
    void addMinutesShouldStayInSingleWindow() {
        LocalDateTime end = ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 120, twoDayWindows());
        assertEquals(LocalDateTime.of(2026, 9, 1, 10, 0), end);
    }

    @Test
    void addMinutesShouldSkipLunchBreak() {
        // 上午剩 4 小时，需要 5 小时：跨过午休，下午再干 1 小时
        LocalDateTime end = ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 300, twoDayWindows());
        assertEquals(LocalDateTime.of(2026, 9, 1, 14, 0), end);
    }

    @Test
    void addMinutesShouldSkipOvernight() {
        // 首日总共 8 小时，需要 9 小时：剩下 1 小时落到次日早班
        LocalDateTime end = ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 540, twoDayWindows());
        assertEquals(LocalDateTime.of(2026, 9, 2, 9, 0), end);
    }

    @Test
    void addMinutesShouldLandExactlyOnWindowEnd() {
        // 正好用完上午班：结束时刻允许等于窗口结束
        LocalDateTime end = ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 240, twoDayWindows());
        assertEquals(DAY1_NOON, end);
    }

    @Test
    void addMinutesShouldReturnNullWhenCapacityRunsOut() {
        // 三个窗口共 12 小时，需要 13 小时：产能不够，交给硬约束惩罚
        assertNull(ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 780, twoDayWindows()));
    }

    @Test
    void zeroMinutesTaskShouldNotConsumeTime() {
        assertEquals(DAY1_MORNING, ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 0, twoDayWindows()));
    }

    @Test
    void emptyWindowsShouldReturnNull() {
        assertNull(ApsCalendarCalculator.alignToWorkTime(DAY1_MORNING, List.of()));
        assertNull(ApsCalendarCalculator.addWorkingMinutes(DAY1_MORNING, 60, List.of()));
    }

}
