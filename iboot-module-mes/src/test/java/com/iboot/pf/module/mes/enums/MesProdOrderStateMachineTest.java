package com.iboot.pf.module.mes.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MesProdOrderStateMachine} 单元测试。
 *
 * <p>覆盖 04 状态图"生产工单 8 态"的全部 10 条边、2 个终态（CLOSED / CANCELLED），
 * 以及未定义状态名与未定义动作名。
 */
class MesProdOrderStateMachineTest {

    /** 全部动作，用于验证终态对任何动作都拒绝。 */
    private static final List<String> ALL_ACTIONS = List.of(
            MesProdOrderStateMachine.ACTION_RELEASE,
            MesProdOrderStateMachine.ACTION_READY,
            MesProdOrderStateMachine.ACTION_START,
            MesProdOrderStateMachine.ACTION_HOLD,
            MesProdOrderStateMachine.ACTION_RESUME,
            MesProdOrderStateMachine.ACTION_DONE,
            MesProdOrderStateMachine.ACTION_CLOSE,
            MesProdOrderStateMachine.ACTION_CANCEL);

    @Test
    void shouldTransitFromDraft() {
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(),
                next(MesProdOrderStatusEnum.DRAFT, MesProdOrderStateMachine.ACTION_RELEASE));
        assertEquals(MesProdOrderStatusEnum.CANCELLED.name(),
                next(MesProdOrderStatusEnum.DRAFT, MesProdOrderStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromReleased() {
        assertEquals(MesProdOrderStatusEnum.READY.name(),
                next(MesProdOrderStatusEnum.RELEASED, MesProdOrderStateMachine.ACTION_READY));
        assertEquals(MesProdOrderStatusEnum.CANCELLED.name(),
                next(MesProdOrderStatusEnum.RELEASED, MesProdOrderStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromReady() {
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                next(MesProdOrderStatusEnum.READY, MesProdOrderStateMachine.ACTION_START));
        assertEquals(MesProdOrderStatusEnum.CANCELLED.name(),
                next(MesProdOrderStatusEnum.READY, MesProdOrderStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromRunning() {
        assertEquals(MesProdOrderStatusEnum.HOLD.name(),
                next(MesProdOrderStatusEnum.RUNNING, MesProdOrderStateMachine.ACTION_HOLD));
        assertEquals(MesProdOrderStatusEnum.DONE.name(),
                next(MesProdOrderStatusEnum.RUNNING, MesProdOrderStateMachine.ACTION_DONE));
    }

    @Test
    void shouldTransitFromHold() {
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                next(MesProdOrderStatusEnum.HOLD, MesProdOrderStateMachine.ACTION_RESUME));
    }

    @Test
    void shouldTransitFromDone() {
        assertEquals(MesProdOrderStatusEnum.CLOSED.name(),
                next(MesProdOrderStatusEnum.DONE, MesProdOrderStateMachine.ACTION_CLOSE));
    }

    @Test
    void shouldRejectAnyActionOnClosed() {
        assertRejectAllActions(MesProdOrderStatusEnum.CLOSED);
    }

    @Test
    void shouldRejectAnyActionOnCancelled() {
        assertRejectAllActions(MesProdOrderStatusEnum.CANCELLED);
    }

    @Test
    void shouldRejectIllegalCombination() {
        // 未下达不能开工，已开工不能重复下达，挂起中不能完工，待开工不能完工
        assertReject(MesProdOrderStatusEnum.DRAFT, MesProdOrderStateMachine.ACTION_START);
        assertReject(MesProdOrderStatusEnum.RUNNING, MesProdOrderStateMachine.ACTION_RELEASE);
        assertReject(MesProdOrderStatusEnum.HOLD, MesProdOrderStateMachine.ACTION_DONE);
        assertReject(MesProdOrderStatusEnum.READY, MesProdOrderStateMachine.ACTION_DONE);
        // 挂起中与已完工都不允许取消
        assertReject(MesProdOrderStatusEnum.HOLD, MesProdOrderStateMachine.ACTION_CANCEL);
        assertReject(MesProdOrderStatusEnum.DONE, MesProdOrderStateMachine.ACTION_CANCEL);
    }

    @Test
    void shouldRejectUnknownAction() {
        assertServiceException(
                () -> MesProdOrderStateMachine.next(MesProdOrderStatusEnum.DRAFT.name(), "notAnAction"),
                PROD_ORDER_STATUS_INVALID);
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertServiceException(
                () -> MesProdOrderStateMachine.next("NOT_A_STATUS", MesProdOrderStateMachine.ACTION_RELEASE),
                PROD_ORDER_STATUS_INVALID);
        assertServiceException(
                () -> MesProdOrderStateMachine.next(null, MesProdOrderStateMachine.ACTION_RELEASE),
                PROD_ORDER_STATUS_INVALID);
    }

    @Test
    void shouldReportTerminalStatus() {
        assertTrue(MesProdOrderStateMachine.isTerminal(MesProdOrderStatusEnum.CLOSED.name()));
        assertTrue(MesProdOrderStateMachine.isTerminal(MesProdOrderStatusEnum.CANCELLED.name()));
        // DONE 还有 close 出边，不是终态，与枚举的 isFinal 口径不同
        assertFalse(MesProdOrderStateMachine.isTerminal(MesProdOrderStatusEnum.DONE.name()));
        assertFalse(MesProdOrderStateMachine.isTerminal(MesProdOrderStatusEnum.DRAFT.name()));
        assertFalse(MesProdOrderStateMachine.isTerminal(MesProdOrderStatusEnum.RUNNING.name()));
        assertFalse(MesProdOrderStateMachine.isTerminal("NOT_A_STATUS"));
    }

    @Test
    void shouldReportCancellableStatus() {
        assertTrue(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.DRAFT.name()));
        assertTrue(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.RELEASED.name()));
        assertTrue(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.READY.name()));
        assertFalse(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.RUNNING.name()));
        assertFalse(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.HOLD.name()));
        assertFalse(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.DONE.name()));
        assertFalse(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.CLOSED.name()));
        assertFalse(MesProdOrderStateMachine.canCancel(MesProdOrderStatusEnum.CANCELLED.name()));
        assertFalse(MesProdOrderStateMachine.canCancel(null));
    }

    @Test
    void shouldKeepTerminalConsistentWithTransitionTable() {
        // 终态判定必须与迁移表一致：没有任何合法出边的状态才是终态
        Arrays.stream(MesProdOrderStatusEnum.values()).forEach(status -> {
            boolean hasLegalAction = ALL_ACTIONS.stream().anyMatch(action -> legal(status, action));
            assertEquals(MesProdOrderStateMachine.isTerminal(status.name()), !hasLegalAction,
                    "终态判定与迁移表不一致：" + status.name());
        });
    }

    private static String next(MesProdOrderStatusEnum status, String action) {
        return MesProdOrderStateMachine.next(status.name(), action);
    }

    private static boolean legal(MesProdOrderStatusEnum status, String action) {
        try {
            MesProdOrderStateMachine.next(status.name(), action);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static void assertReject(MesProdOrderStatusEnum status, String action) {
        assertServiceException(() -> MesProdOrderStateMachine.next(status.name(), action),
                PROD_ORDER_STATUS_INVALID);
    }

    private static void assertRejectAllActions(MesProdOrderStatusEnum status) {
        ALL_ACTIONS.forEach(action -> assertReject(status, action));
    }

}
