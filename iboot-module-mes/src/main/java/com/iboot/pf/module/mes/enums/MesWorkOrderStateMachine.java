package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.util.Map;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_STATUS_INVALID;

/**
 * 作业工单状态机。作业工单是生产工单在车间的分段执行单位，状态由下属工序任务与生产工单带动，
 * 只能由领域动作经本类推进，不允许表单直填。
 *
 * <p>迁移表逐条对应 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"作业工单 6 态"状态图，共 7 条边：
 * <pre>
 * PENDING -- ready  --&gt; READY      PENDING -- cancel --&gt; CANCELLED
 * READY   -- start  --&gt; RUNNING    READY   -- cancel --&gt; CANCELLED
 * RUNNING -- hold   --&gt; HOLD       RUNNING -- done   --&gt; DONE
 * HOLD    -- resume --&gt; RUNNING
 * </pre>
 *
 * <p>作业工单没有 release 与 close 两个动作：下达是生产工单的动作（下达时本单被展开为 PENDING），
 * 账目收尾也只在生产工单层做。状态取值一律引用 {@link MesWorkOrderStatusEnum}，不写字符串字面量。
 */
public final class MesWorkOrderStateMachine {

    /** 前置作业工单完成或无前置，转入待开工。 */
    public static final String ACTION_READY = "ready";
    /** 本段首道工序开工带动本单开工。 */
    public static final String ACTION_START = "start";
    /** 随生产工单挂起。 */
    public static final String ACTION_HOLD = "hold";
    /** 恢复。 */
    public static final String ACTION_RESUME = "resume";
    /** 本段全部工序完工或跳序。 */
    public static final String ACTION_DONE = "done";
    /** 随生产工单取消：仅开工前允许。 */
    public static final String ACTION_CANCEL = "cancel";

    /** 迁移表：当前状态 -&gt; (动作 -&gt; 目标状态)。表里没有的组合即为非法。 */
    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesWorkOrderStatusEnum.PENDING.name(), Map.of(
                    ACTION_READY, MesWorkOrderStatusEnum.READY.name(),
                    ACTION_CANCEL, MesWorkOrderStatusEnum.CANCELLED.name()),
            MesWorkOrderStatusEnum.READY.name(), Map.of(
                    ACTION_START, MesWorkOrderStatusEnum.RUNNING.name(),
                    ACTION_CANCEL, MesWorkOrderStatusEnum.CANCELLED.name()),
            MesWorkOrderStatusEnum.RUNNING.name(), Map.of(
                    ACTION_HOLD, MesWorkOrderStatusEnum.HOLD.name(),
                    ACTION_DONE, MesWorkOrderStatusEnum.DONE.name()),
            MesWorkOrderStatusEnum.HOLD.name(), Map.of(
                    ACTION_RESUME, MesWorkOrderStatusEnum.RUNNING.name()));

    private MesWorkOrderStateMachine() {
    }

    /**
     * 按动作推进状态，返回目标状态名。
     *
     * @param currentStatus 当前状态，取值见 {@link MesWorkOrderStatusEnum}
     * @param action        动作，取值见本类的 {@code ACTION_*} 常量
     * @return 目标状态名
     * @throws com.iboot.pf.framework.common.exception.ServiceException 当前状态不允许该动作，或状态、动作未定义
     */
    public static String next(String currentStatus, String action) {
        String target = resolve(currentStatus, action);
        if (target == null) {
            throw ServiceExceptionUtil.exception(WORK_ORDER_STATUS_INVALID);
        }
        return target;
    }

    /** 是否终态：已完工与已取消，两者都没有出边，口径与 {@link MesWorkOrderStatusEnum#isFinal} 一致。 */
    public static boolean isTerminal(String status) {
        return ObjUtil.equal(MesWorkOrderStatusEnum.DONE.name(), status)
                || ObjUtil.equal(MesWorkOrderStatusEnum.CANCELLED.name(), status);
    }

    /** 是否允许取消：直接查迁移表有无 cancel 出边，即待处理、待开工两态。 */
    public static boolean canCancel(String status) {
        return resolve(status, ACTION_CANCEL) != null;
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
