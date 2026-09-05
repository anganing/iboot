package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.util.Map;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;

/**
 * 工序任务状态机。工序任务是执行、报工、检验的最小单位，状态只能由领域动作经本类推进，不允许表单直填。
 *
 * <p>迁移表逐条对应 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"工序任务 9 态"状态图，共 14 条边
 * （其中 PENDING -&gt; READY 有 applySchedule 与 prevDone 两个动作入口，故 15 个状态动作组合）：
 * <pre>
 * PENDING    -- applySchedule / prevDone --&gt; READY
 * PENDING    -- skip            --&gt; SKIPPED      PENDING    -- cancel --&gt; CANCELLED
 * READY      -- start           --&gt; RUNNING
 * READY      -- skip            --&gt; SKIPPED      READY      -- cancel --&gt; CANCELLED
 * RUNNING    -- pause           --&gt; PAUSED       PAUSED     -- resume --&gt; RUNNING
 * RUNNING    -- toQc            --&gt; QC_WAITING
 * RUNNING    -- finish          --&gt; DONE
 * RUNNING    -- hold            --&gt; HOLD         HOLD       -- resume --&gt; RUNNING
 * QC_WAITING -- qcPass          --&gt; RUNNING      QC_WAITING -- qcPassAndFinish --&gt; DONE
 * </pre>
 *
 * <p>PENDING 与 READY 的区别是 APS 闭环的关键：PENDING 还没有计划时间与资源，READY 表示计划已下达可以干。
 * 两者合称未开工态，语义由 {@link MesOpTaskStatusEnum#isNotStarted} 定义，也正是允许 skip 与 cancel 的两个状态。
 *
 * <p>状态取值一律引用 {@link MesOpTaskStatusEnum}，不写字符串字面量。非法组合抛
 * {@link ErrorCodeConstants#OP_TASK_STATUS_INVALID}，不返回 null。
 */
public final class MesOpTaskStateMachine {

    /** APS 下达计划：写入计划时间与资源后转入待开工。 */
    public static final String ACTION_APPLY_SCHEDULE = "applySchedule";
    /** 前序工序完工或跳序，带动本工序转入待开工。 */
    public static final String ACTION_PREV_DONE = "prevDone";
    /** 开工：需通过开工门禁。 */
    public static final String ACTION_START = "start";
    /** 暂停。 */
    public static final String ACTION_PAUSE = "pause";
    /** 恢复：暂停与质量挂起都用它回到加工中。 */
    public static final String ACTION_RESUME = "resume";
    /** 报工后转待检（IPQC 或 FQC）。 */
    public static final String ACTION_TO_QC = "toQc";
    /** 检验合格，继续加工。 */
    public static final String ACTION_QC_PASS = "qcPass";
    /** 末次报工且检验合格，直接完工。 */
    public static final String ACTION_QC_PASS_AND_FINISH = "qcPassAndFinish";
    /** 完工：数量达标且无需检验。 */
    public static final String ACTION_FINISH = "finish";
    /** 质量处置挂起（ADJUST_PROCESS）。 */
    public static final String ACTION_HOLD = "hold";
    /** 跳序：仅未开工态允许，是否放行还要看 skip_policy。 */
    public static final String ACTION_SKIP = "skip";
    /** 随工单取消：仅未开工态允许。 */
    public static final String ACTION_CANCEL = "cancel";

    /** 迁移表：当前状态 -&gt; (动作 -&gt; 目标状态)。表里没有的组合即为非法。 */
    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesOpTaskStatusEnum.PENDING.name(), Map.of(
                    ACTION_APPLY_SCHEDULE, MesOpTaskStatusEnum.READY.name(),
                    ACTION_PREV_DONE, MesOpTaskStatusEnum.READY.name(),
                    ACTION_SKIP, MesOpTaskStatusEnum.SKIPPED.name(),
                    ACTION_CANCEL, MesOpTaskStatusEnum.CANCELLED.name()),
            MesOpTaskStatusEnum.READY.name(), Map.of(
                    ACTION_START, MesOpTaskStatusEnum.RUNNING.name(),
                    ACTION_SKIP, MesOpTaskStatusEnum.SKIPPED.name(),
                    ACTION_CANCEL, MesOpTaskStatusEnum.CANCELLED.name()),
            MesOpTaskStatusEnum.RUNNING.name(), Map.of(
                    ACTION_PAUSE, MesOpTaskStatusEnum.PAUSED.name(),
                    ACTION_TO_QC, MesOpTaskStatusEnum.QC_WAITING.name(),
                    ACTION_FINISH, MesOpTaskStatusEnum.DONE.name(),
                    ACTION_HOLD, MesOpTaskStatusEnum.HOLD.name()),
            MesOpTaskStatusEnum.PAUSED.name(), Map.of(
                    ACTION_RESUME, MesOpTaskStatusEnum.RUNNING.name()),
            MesOpTaskStatusEnum.QC_WAITING.name(), Map.of(
                    ACTION_QC_PASS, MesOpTaskStatusEnum.RUNNING.name(),
                    ACTION_QC_PASS_AND_FINISH, MesOpTaskStatusEnum.DONE.name()),
            MesOpTaskStatusEnum.HOLD.name(), Map.of(
                    ACTION_RESUME, MesOpTaskStatusEnum.RUNNING.name()));

    private MesOpTaskStateMachine() {
    }

    /**
     * 按动作推进状态，返回目标状态名。
     *
     * @param currentStatus 当前状态，取值见 {@link MesOpTaskStatusEnum}
     * @param action        动作，取值见本类的 {@code ACTION_*} 常量
     * @return 目标状态名
     * @throws com.iboot.pf.framework.common.exception.ServiceException 当前状态不允许该动作，或状态、动作未定义
     */
    public static String next(String currentStatus, String action) {
        String target = resolve(currentStatus, action);
        if (target == null) {
            throw ServiceExceptionUtil.exception(OP_TASK_STATUS_INVALID);
        }
        return target;
    }

    /**
     * 是否终态：已完工、已跳过、已取消，三者都没有出边。
     *
     * <p>口径与 {@link MesOpTaskStatusEnum#isFinal} 一致，直接复用避免两处漂移。
     */
    public static boolean isTerminal(String status) {
        return MesOpTaskStatusEnum.isFinal(status);
    }

    /**
     * 是否允许取消：未开工态（待排产、待开工）才允许，语义复用
     * {@link MesOpTaskStatusEnum#isNotStarted}，与迁移表里这两行的 cancel 出边一致。
     */
    public static boolean canCancel(String status) {
        return MesOpTaskStatusEnum.isNotStarted(status);
    }

    /** 查迁移表，未定义的状态或动作返回 null，由调用方决定是否抛错。 */
    private static String resolve(String currentStatus, String action) {
        if (currentStatus == null || action == null) {
            return null;
        }
        Map<String, String> actions = TRANSITIONS.get(currentStatus);
        return actions == null ? null : actions.get(action);
    }

}
