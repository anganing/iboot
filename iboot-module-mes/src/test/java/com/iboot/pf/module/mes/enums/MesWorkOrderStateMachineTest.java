package com.iboot.pf.module.mes.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MesWorkOrderStateMachine} 单元测试。
 *
 * <p>覆盖 04 状态图"作业工单 6 态"的全部 7 条边、2 个终态（DONE / CANCELLED），
 * 以及未定义状态名与未定义动作名。
 */
class MesWorkOrderStateMachineTest {

    /** 全部动作，用于验证终态对任何动作都拒绝。 */
    private static final List<String> ALL_ACTIONS = List.of(
            MesWorkOrderStateMachine.ACTION_READY,
            MesWorkOrderStateMachine.ACTION_START,
            MesWorkOrderStateMachine.ACTION_HOLD,
            MesWorkOrderStateMachine.ACTION_RESUME,
            MesWorkOrderStateMachine.ACTION_DONE,
            MesWorkOrderStateMachine.ACTION_CANCEL);

    @Test
    void shouldTransitFromPending() {
        assertEquals(MesWorkOrderStatusEnum.READY.name(),
                next(MesWorkOrderStatusEnum.PENDING, MesWorkOrderStateMachine.ACTION_READY));
        assertEquals(MesWorkOrderStatusEnum.CANCELLED.name(),
                next(MesWorkOrderStatusEnum.PENDING, MesWorkOrderStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromReady() {
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(),
                next(MesWorkOrderStatusEnum.READY, MesWorkOrderStateMachine.ACTION_START));
        assertEquals(MesWorkOrderStatusEnum.CANCELLED.name(),
                next(MesWorkOrderStatusEnum.READY, MesWorkOrderStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromRunning() {
        assertEquals(MesWorkOrderStatusEnum.HOLD.name(),
                next(MesWorkOrderStatusEnum.RUNNING, MesWorkOrderStateMachine.ACTION_HOLD));
        assertEquals(MesWorkOrderStatusEnum.DONE.name(),
                next(MesWorkOrderStatusEnum.RUNNING, MesWorkOrderStateMachine.ACTION_DONE));
    }

    @Test
    void shouldTransitFromHold() {
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(),
                next(MesWorkOrderStatusEnum.HOLD, MesWorkOrderStateMachine.ACTION_RESUME));
    }

    @Test
    void shouldRejectAnyActionOnDone() {
        assertRejectAllActions(MesWorkOrderStatusEnum.DONE);
    }

    @Test
    void shouldRejectAnyActionOnCancelled() {
        assertRejectAllActions(MesWorkOrderStatusEnum.CANCELLED);
    }

    @Test
    void shouldRejectIllegalCombination() {
        // 待处理不能直接开工，作业中不能重复转待开工，挂起中不能完工
        assertReject(MesWorkOrderStatusEnum.PENDING, MesWorkOrderStateMachine.ACTION_START);
        assertReject(MesWorkOrderStatusEnum.RUNNING, MesWorkOrderStateMachine.ACTION_READY);
        assertReject(MesWorkOrderStatusEnum.HOLD, MesWorkOrderStateMachine.ACTION_DONE);
        // 作业中与挂起中都不允许取消
        assertReject(MesWorkOrderStatusEnum.RUNNING, MesWorkOrderStateMachine.ACTION_CANCEL);
        assertReject(MesWorkOrderStatusEnum.HOLD, MesWorkOrderStateMachine.ACTION_CANCEL);
    }

    @Test
    void shouldRejectUnknownAction() {
        assertServiceException(
                () -> MesWorkOrderStateMachine.next(MesWorkOrderStatusEnum.PENDING.name(), "notAnAction"),
                WORK_ORDER_STATUS_INVALID);
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertServiceException(
                () -> MesWorkOrderStateMachine.next("NOT_A_STATUS", MesWorkOrderStateMachine.ACTION_READY),
                WORK_ORDER_STATUS_INVALID);
        assertServiceException(
                () -> MesWorkOrderStateMachine.next(null, MesWorkOrderStateMachine.ACTION_READY),
                WORK_ORDER_STATUS_INVALID);
    }

    @Test
    void shouldReportTerminalStatus() {
        assertTrue(MesWorkOrderStateMachine.isTerminal(MesWorkOrderStatusEnum.DONE.name()));
        assertTrue(MesWorkOrderStateMachine.isTerminal(MesWorkOrderStatusEnum.CANCELLED.name()));
        assertFalse(MesWorkOrderStateMachine.isTerminal(MesWorkOrderStatusEnum.PENDING.name()));
        assertFalse(MesWorkOrderStateMachine.isTerminal(MesWorkOrderStatusEnum.RUNNING.name()));
        assertFalse(MesWorkOrderStateMachine.isTerminal(MesWorkOrderStatusEnum.HOLD.name()));
        assertFalse(MesWorkOrderStateMachine.isTerminal("NOT_A_STATUS"));
    }

    @Test
    void shouldReportCancellableStatus() {
        assertTrue(MesWorkOrderStateMachine.canCancel(MesWorkOrderStatusEnum.PENDING.name()));
        assertTrue(MesWorkOrderStateMachine.canCancel(MesWorkOrderStatusEnum.READY.name()));
        assertFalse(MesWorkOrderStateMachine.canCancel(MesWorkOrderStatusEnum.RUNNING.name()));
        assertFalse(MesWorkOrderStateMachine.canCancel(MesWorkOrderStatusEnum.HOLD.name()));
        assertFalse(MesWorkOrderStateMachine.canCancel(MesWorkOrderStatusEnum.DONE.name()));
        assertFalse(MesWorkOrderStateMachine.canCancel(MesWorkOrderStatusEnum.CANCELLED.name()));
        assertFalse(MesWorkOrderStateMachine.canCancel(null));
    }

    @Test
    void shouldKeepTerminalConsistentWithTransitionTable() {
        // 终态判定必须与迁移表一致：没有任何合法出边的状态才是终态
        Arrays.stream(MesWorkOrderStatusEnum.values()).forEach(status -> {
            boolean hasLegalAction = ALL_ACTIONS.stream().anyMatch(action -> legal(status, action));
            assertEquals(MesWorkOrderStateMachine.isTerminal(status.name()), !hasLegalAction,
                    "终态判定与迁移表不一致：" + status.name());
        });
    }

    @Test
    void shouldKeepTerminalConsistentWithStatusEnum() {
        // 作业工单的终态口径与枚举 isFinal 一致，两处不允许漂移
        Arrays.stream(MesWorkOrderStatusEnum.values()).forEach(status ->
                assertEquals(MesWorkOrderStatusEnum.isFinal(status.name()),
                        MesWorkOrderStateMachine.isTerminal(status.name()), status.name()));
    }

    private static String next(MesWorkOrderStatusEnum status, String action) {
        return MesWorkOrderStateMachine.next(status.name(), action);
    }

    private static boolean legal(MesWorkOrderStatusEnum status, String action) {
        try {
            MesWorkOrderStateMachine.next(status.name(), action);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static void assertReject(MesWorkOrderStatusEnum status, String action) {
        assertServiceException(() -> MesWorkOrderStateMachine.next(status.name(), action),
                WORK_ORDER_STATUS_INVALID);
    }

    private static void assertRejectAllActions(MesWorkOrderStatusEnum status) {
        ALL_ACTIONS.forEach(action -> assertReject(status, action));
    }

}
