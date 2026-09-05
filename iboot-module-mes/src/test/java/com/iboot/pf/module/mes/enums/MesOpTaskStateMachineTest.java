package com.iboot.pf.module.mes.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MesOpTaskStateMachine} 单元测试。
 *
 * <p>覆盖 04 状态图"工序任务 9 态"的全部 14 条边（15 个状态动作组合）、3 个终态
 * （DONE / SKIPPED / CANCELLED），以及未定义状态名与未定义动作名。
 */
class MesOpTaskStateMachineTest {

    /** 全部动作，用于验证终态对任何动作都拒绝。 */
    private static final List<String> ALL_ACTIONS = List.of(
            MesOpTaskStateMachine.ACTION_APPLY_SCHEDULE,
            MesOpTaskStateMachine.ACTION_PREV_DONE,
            MesOpTaskStateMachine.ACTION_START,
            MesOpTaskStateMachine.ACTION_PAUSE,
            MesOpTaskStateMachine.ACTION_RESUME,
            MesOpTaskStateMachine.ACTION_TO_QC,
            MesOpTaskStateMachine.ACTION_QC_PASS,
            MesOpTaskStateMachine.ACTION_QC_PASS_AND_FINISH,
            MesOpTaskStateMachine.ACTION_FINISH,
            MesOpTaskStateMachine.ACTION_HOLD,
            MesOpTaskStateMachine.ACTION_SKIP,
            MesOpTaskStateMachine.ACTION_CANCEL);

    @Test
    void shouldTransitFromPendingByApplySchedule() {
        assertEquals(MesOpTaskStatusEnum.READY.name(),
                next(MesOpTaskStatusEnum.PENDING, MesOpTaskStateMachine.ACTION_APPLY_SCHEDULE));
    }

    @Test
    void shouldTransitFromPendingByPrevDone() {
        assertEquals(MesOpTaskStatusEnum.READY.name(),
                next(MesOpTaskStatusEnum.PENDING, MesOpTaskStateMachine.ACTION_PREV_DONE));
    }

    @Test
    void shouldTransitFromPendingBySkip() {
        assertEquals(MesOpTaskStatusEnum.SKIPPED.name(),
                next(MesOpTaskStatusEnum.PENDING, MesOpTaskStateMachine.ACTION_SKIP));
    }

    @Test
    void shouldTransitFromPendingByCancel() {
        assertEquals(MesOpTaskStatusEnum.CANCELLED.name(),
                next(MesOpTaskStatusEnum.PENDING, MesOpTaskStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromReadyByStart() {
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(),
                next(MesOpTaskStatusEnum.READY, MesOpTaskStateMachine.ACTION_START));
    }

    @Test
    void shouldTransitFromReadyBySkip() {
        assertEquals(MesOpTaskStatusEnum.SKIPPED.name(),
                next(MesOpTaskStatusEnum.READY, MesOpTaskStateMachine.ACTION_SKIP));
    }

    @Test
    void shouldTransitFromReadyByCancel() {
        assertEquals(MesOpTaskStatusEnum.CANCELLED.name(),
                next(MesOpTaskStatusEnum.READY, MesOpTaskStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromRunningByPause() {
        assertEquals(MesOpTaskStatusEnum.PAUSED.name(),
                next(MesOpTaskStatusEnum.RUNNING, MesOpTaskStateMachine.ACTION_PAUSE));
    }

    @Test
    void shouldTransitFromRunningByToQc() {
        assertEquals(MesOpTaskStatusEnum.QC_WAITING.name(),
                next(MesOpTaskStatusEnum.RUNNING, MesOpTaskStateMachine.ACTION_TO_QC));
    }

    @Test
    void shouldTransitFromRunningByFinish() {
        assertEquals(MesOpTaskStatusEnum.DONE.name(),
                next(MesOpTaskStatusEnum.RUNNING, MesOpTaskStateMachine.ACTION_FINISH));
    }

    @Test
    void shouldTransitFromRunningByHold() {
        assertEquals(MesOpTaskStatusEnum.HOLD.name(),
                next(MesOpTaskStatusEnum.RUNNING, MesOpTaskStateMachine.ACTION_HOLD));
    }

    @Test
    void shouldTransitFromPausedByResume() {
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(),
                next(MesOpTaskStatusEnum.PAUSED, MesOpTaskStateMachine.ACTION_RESUME));
    }

    @Test
    void shouldTransitFromQcWaitingByQcPass() {
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(),
                next(MesOpTaskStatusEnum.QC_WAITING, MesOpTaskStateMachine.ACTION_QC_PASS));
    }

    @Test
    void shouldTransitFromQcWaitingByQcPassAndFinish() {
        assertEquals(MesOpTaskStatusEnum.DONE.name(),
                next(MesOpTaskStatusEnum.QC_WAITING, MesOpTaskStateMachine.ACTION_QC_PASS_AND_FINISH));
    }

    @Test
    void shouldTransitFromHoldByResume() {
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(),
                next(MesOpTaskStatusEnum.HOLD, MesOpTaskStateMachine.ACTION_RESUME));
    }

    @Test
    void shouldRejectAnyActionOnDone() {
        assertRejectAllActions(MesOpTaskStatusEnum.DONE);
    }

    @Test
    void shouldRejectAnyActionOnSkipped() {
        assertRejectAllActions(MesOpTaskStatusEnum.SKIPPED);
    }

    @Test
    void shouldRejectAnyActionOnCancelled() {
        assertRejectAllActions(MesOpTaskStatusEnum.CANCELLED);
    }

    @Test
    void shouldRejectIllegalCombination() {
        // 待排产不能直接开工，必须先经 applySchedule 或 prevDone 转待开工
        assertReject(MesOpTaskStatusEnum.PENDING, MesOpTaskStateMachine.ACTION_START);
        // 已暂停只能 resume 回加工中，不能直接开工或完工
        assertReject(MesOpTaskStatusEnum.PAUSED, MesOpTaskStateMachine.ACTION_START);
        assertReject(MesOpTaskStatusEnum.PAUSED, MesOpTaskStateMachine.ACTION_FINISH);
        // 加工中不能跳序、不能取消
        assertReject(MesOpTaskStatusEnum.RUNNING, MesOpTaskStateMachine.ACTION_SKIP);
        assertReject(MesOpTaskStatusEnum.RUNNING, MesOpTaskStateMachine.ACTION_CANCEL);
        // 待质检只能走 qcPass 或 qcPassAndFinish
        assertReject(MesOpTaskStatusEnum.QC_WAITING, MesOpTaskStateMachine.ACTION_FINISH);
        // 待开工不能重复下达计划
        assertReject(MesOpTaskStatusEnum.READY, MesOpTaskStateMachine.ACTION_APPLY_SCHEDULE);
    }

    @Test
    void shouldRejectUnknownAction() {
        assertServiceException(
                () -> MesOpTaskStateMachine.next(MesOpTaskStatusEnum.READY.name(), "notAnAction"),
                OP_TASK_STATUS_INVALID);
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertServiceException(
                () -> MesOpTaskStateMachine.next("NOT_A_STATUS", MesOpTaskStateMachine.ACTION_START),
                OP_TASK_STATUS_INVALID);
        assertServiceException(
                () -> MesOpTaskStateMachine.next(null, MesOpTaskStateMachine.ACTION_START),
                OP_TASK_STATUS_INVALID);
    }

    @Test
    void shouldReportTerminalStatus() {
        assertTrue(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.DONE.name()));
        assertTrue(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.SKIPPED.name()));
        assertTrue(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.CANCELLED.name()));
        assertFalse(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.PENDING.name()));
        assertFalse(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.RUNNING.name()));
        assertFalse(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.QC_WAITING.name()));
        assertFalse(MesOpTaskStateMachine.isTerminal(MesOpTaskStatusEnum.HOLD.name()));
        assertFalse(MesOpTaskStateMachine.isTerminal("NOT_A_STATUS"));
    }

    @Test
    void shouldReportCancellableStatus() {
        assertTrue(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.PENDING.name()));
        assertTrue(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.READY.name()));
        assertFalse(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.RUNNING.name()));
        assertFalse(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.PAUSED.name()));
        assertFalse(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.QC_WAITING.name()));
        assertFalse(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.HOLD.name()));
        assertFalse(MesOpTaskStateMachine.canCancel(MesOpTaskStatusEnum.DONE.name()));
        assertFalse(MesOpTaskStateMachine.canCancel(null));
    }

    @Test
    void shouldKeepTerminalConsistentWithTransitionTable() {
        // 终态判定必须与迁移表一致：没有任何合法出边的状态才是终态
        Arrays.stream(MesOpTaskStatusEnum.values()).forEach(status -> {
            boolean hasLegalAction = ALL_ACTIONS.stream().anyMatch(action -> legal(status, action));
            assertEquals(MesOpTaskStateMachine.isTerminal(status.name()), !hasLegalAction,
                    "终态判定与迁移表不一致：" + status.name());
        });
    }

    @Test
    void shouldKeepCancellableConsistentWithNotStartedSemantic() {
        // canCancel 复用 isNotStarted 语义，必须与迁移表里的 cancel 出边一致
        Arrays.stream(MesOpTaskStatusEnum.values()).forEach(status -> {
            boolean cancelInTable = legal(status, MesOpTaskStateMachine.ACTION_CANCEL);
            assertEquals(cancelInTable, MesOpTaskStateMachine.canCancel(status.name()), status.name());
            assertEquals(cancelInTable, MesOpTaskStatusEnum.isNotStarted(status.name()), status.name());
        });
    }

    private static String next(MesOpTaskStatusEnum status, String action) {
        return MesOpTaskStateMachine.next(status.name(), action);
    }

    private static boolean legal(MesOpTaskStatusEnum status, String action) {
        try {
            MesOpTaskStateMachine.next(status.name(), action);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static void assertReject(MesOpTaskStatusEnum status, String action) {
        assertServiceException(() -> MesOpTaskStateMachine.next(status.name(), action), OP_TASK_STATUS_INVALID);
    }

    private static void assertRejectAllActions(MesOpTaskStatusEnum status) {
        ALL_ACTIONS.forEach(action -> assertReject(status, action));
    }

}
