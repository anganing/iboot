package com.iboot.pf.module.mes.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MesQcTaskStateMachine} 单元测试。
 *
 * <p>覆盖 05 文档"检验任务生命周期"状态图的全部 6 条边（7 个状态动作组合）、2 个终态
 * （DONE / CANCELLED），以及未定义状态名与未定义动作名。INSPECTING -&gt; RESULT_POSTING 的
 * 推进由 T2-05 的结果录入触发，状态机只备边，边本身在这里验证。
 */
class MesQcTaskStateMachineTest {

    /** 全部动作，用于验证终态对任何动作都拒绝。 */
    private static final List<String> ALL_ACTIONS = List.of(
            MesQcTaskStateMachine.ACTION_ASSIGN,
            MesQcTaskStateMachine.ACTION_START_INSPECT,
            MesQcTaskStateMachine.ACTION_POST_RESULT,
            MesQcTaskStateMachine.ACTION_SUBMIT_CONCLUSION,
            MesQcTaskStateMachine.ACTION_CANCEL);

    @Test
    void shouldTransitFromCreatedByAssign() {
        assertEquals(MesQcTaskStatusEnum.ASSIGNED.name(),
                next(MesQcTaskStatusEnum.CREATED, MesQcTaskStateMachine.ACTION_ASSIGN));
    }

    @Test
    void shouldTransitFromCreatedByCancel() {
        assertEquals(MesQcTaskStatusEnum.CANCELLED.name(),
                next(MesQcTaskStatusEnum.CREATED, MesQcTaskStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromAssignedByStartInspect() {
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(),
                next(MesQcTaskStatusEnum.ASSIGNED, MesQcTaskStateMachine.ACTION_START_INSPECT));
    }

    @Test
    void shouldTransitFromAssignedByCancel() {
        assertEquals(MesQcTaskStatusEnum.CANCELLED.name(),
                next(MesQcTaskStatusEnum.ASSIGNED, MesQcTaskStateMachine.ACTION_CANCEL));
    }

    @Test
    void shouldTransitFromInspectingByPostResult() {
        // 全部必检项录完时由结果录入（T2-05）推进，状态机只备这条边
        assertEquals(MesQcTaskStatusEnum.RESULT_POSTING.name(),
                next(MesQcTaskStatusEnum.INSPECTING, MesQcTaskStateMachine.ACTION_POST_RESULT));
    }

    @Test
    void shouldTransitFromResultPostingBySubmitConclusion() {
        assertEquals(MesQcTaskStatusEnum.DONE.name(),
                next(MesQcTaskStatusEnum.RESULT_POSTING, MesQcTaskStateMachine.ACTION_SUBMIT_CONCLUSION));
    }

    @Test
    void shouldRejectAnyActionOnDone() {
        assertRejectAllActions(MesQcTaskStatusEnum.DONE);
    }

    @Test
    void shouldRejectAnyActionOnCancelled() {
        assertRejectAllActions(MesQcTaskStatusEnum.CANCELLED);
    }

    @Test
    void shouldRejectIllegalCombination() {
        // 未创建不能直接开检，必须先指派
        assertReject(MesQcTaskStatusEnum.CREATED, MesQcTaskStateMachine.ACTION_START_INSPECT);
        // 已指派不能重复指派
        assertReject(MesQcTaskStatusEnum.ASSIGNED, MesQcTaskStateMachine.ACTION_ASSIGN);
        // 检验中不允许取消：料已检数已录，不合格走处置而不是销毁记录
        assertReject(MesQcTaskStatusEnum.INSPECTING, MesQcTaskStateMachine.ACTION_CANCEL);
        // 结果录入完成不能再录
        assertReject(MesQcTaskStatusEnum.RESULT_POSTING, MesQcTaskStateMachine.ACTION_POST_RESULT);
        // 结论没提交不能落 DONE，未到结果录入阶段也不能提交结论
        assertReject(MesQcTaskStatusEnum.INSPECTING, MesQcTaskStateMachine.ACTION_SUBMIT_CONCLUSION);
        assertReject(MesQcTaskStatusEnum.CREATED, MesQcTaskStateMachine.ACTION_SUBMIT_CONCLUSION);
    }

    @Test
    void shouldRejectUnknownAction() {
        assertServiceException(
                () -> MesQcTaskStateMachine.next(MesQcTaskStatusEnum.ASSIGNED.name(), "notAnAction"),
                QC_TASK_STATUS_INVALID);
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertServiceException(
                () -> MesQcTaskStateMachine.next("NOT_A_STATUS", MesQcTaskStateMachine.ACTION_ASSIGN),
                QC_TASK_STATUS_INVALID);
        assertServiceException(
                () -> MesQcTaskStateMachine.next(null, MesQcTaskStateMachine.ACTION_ASSIGN),
                QC_TASK_STATUS_INVALID);
    }

    @Test
    void shouldReportTerminalStatus() {
        assertTrue(MesQcTaskStateMachine.isTerminal(MesQcTaskStatusEnum.DONE.name()));
        assertTrue(MesQcTaskStateMachine.isTerminal(MesQcTaskStatusEnum.CANCELLED.name()));
        assertFalse(MesQcTaskStateMachine.isTerminal(MesQcTaskStatusEnum.CREATED.name()));
        assertFalse(MesQcTaskStateMachine.isTerminal(MesQcTaskStatusEnum.ASSIGNED.name()));
        assertFalse(MesQcTaskStateMachine.isTerminal(MesQcTaskStatusEnum.INSPECTING.name()));
        assertFalse(MesQcTaskStateMachine.isTerminal(MesQcTaskStatusEnum.RESULT_POSTING.name()));
        assertFalse(MesQcTaskStateMachine.isTerminal("NOT_A_STATUS"));
    }

    @Test
    void shouldReportCancellableStatus() {
        // 只有未开检的任务允许取消，与迁移表里两行 cancel 出边一致
        assertTrue(MesQcTaskStateMachine.canCancel(MesQcTaskStatusEnum.CREATED.name()));
        assertTrue(MesQcTaskStateMachine.canCancel(MesQcTaskStatusEnum.ASSIGNED.name()));
        assertFalse(MesQcTaskStateMachine.canCancel(MesQcTaskStatusEnum.INSPECTING.name()));
        assertFalse(MesQcTaskStateMachine.canCancel(MesQcTaskStatusEnum.RESULT_POSTING.name()));
        assertFalse(MesQcTaskStateMachine.canCancel(MesQcTaskStatusEnum.DONE.name()));
        assertFalse(MesQcTaskStateMachine.canCancel(null));
    }

    @Test
    void shouldKeepTerminalConsistentWithTransitionTable() {
        // 终态判定必须与迁移表一致：没有任何合法出边的状态才是终态
        Arrays.stream(MesQcTaskStatusEnum.values()).forEach(status -> {
            boolean hasLegalAction = ALL_ACTIONS.stream().anyMatch(action -> legal(status, action));
            assertEquals(MesQcTaskStateMachine.isTerminal(status.name()), !hasLegalAction,
                    "终态判定与迁移表不一致：" + status.name());
        });
    }

    @Test
    void shouldKeepCancellableConsistentWithTransitionTable() {
        // canCancel 必须与迁移表里的 cancel 出边一致
        Arrays.stream(MesQcTaskStatusEnum.values()).forEach(status -> {
            boolean cancelInTable = legal(status, MesQcTaskStateMachine.ACTION_CANCEL);
            assertEquals(cancelInTable, MesQcTaskStateMachine.canCancel(status.name()), status.name());
        });
    }

    private static String next(MesQcTaskStatusEnum status, String action) {
        return MesQcTaskStateMachine.next(status.name(), action);
    }

    private static boolean legal(MesQcTaskStatusEnum status, String action) {
        try {
            MesQcTaskStateMachine.next(status.name(), action);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static void assertReject(MesQcTaskStatusEnum status, String action) {
        assertServiceException(() -> MesQcTaskStateMachine.next(status.name(), action), QC_TASK_STATUS_INVALID);
    }

    private static void assertRejectAllActions(MesQcTaskStatusEnum status) {
        ALL_ACTIONS.forEach(action -> assertReject(status, action));
    }

}
