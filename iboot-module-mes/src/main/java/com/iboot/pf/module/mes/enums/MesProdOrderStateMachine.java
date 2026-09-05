package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.util.Map;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;

/**
 * 生产工单状态机。生产工单状态只能由领域动作经本类推进，不允许表单直填。
 *
 * <p>迁移表逐条对应 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"生产工单 8 态"状态图，共 10 条边：
 * <pre>
 * DRAFT    -- release --&gt; RELEASED     DRAFT    -- cancel --&gt; CANCELLED
 * RELEASED -- ready   --&gt; READY        RELEASED -- cancel --&gt; CANCELLED
 * READY    -- start   --&gt; RUNNING      READY    -- cancel --&gt; CANCELLED
 * RUNNING  -- hold    --&gt; HOLD         RUNNING  -- done   --&gt; DONE
 * HOLD     -- resume  --&gt; RUNNING      DONE     -- close  --&gt; CLOSED
 * </pre>
 *
 * <p>状态取值一律引用 {@link MesProdOrderStatusEnum}，不写字符串字面量。非法组合抛
 * {@link ErrorCodeConstants#PROD_ORDER_STATUS_INVALID}，不返回 null，避免调用方拿到空值继续往下写库。
 */
public final class MesProdOrderStateMachine {

    /** 下达：锁 BOM 与工艺版本并展开工序。 */
    public static final String ACTION_RELEASE = "release";
    /** 齐套通过或已发料，转入待开工。 */
    public static final String ACTION_READY = "ready";
    /** 首道工序开工带动整单开工。 */
    public static final String ACTION_START = "start";
    /** 挂起。 */
    public static final String ACTION_HOLD = "hold";
    /** 恢复。 */
    public static final String ACTION_RESUME = "resume";
    /** 末道工序完工且数量达标。 */
    public static final String ACTION_DONE = "done";
    /** 关闭：释放剩余预留、倒冲扣料、账目收尾。 */
    public static final String ACTION_CLOSE = "close";
    /** 取消：仅开工前允许。 */
    public static final String ACTION_CANCEL = "cancel";

    /** 迁移表：当前状态 -&gt; (动作 -&gt; 目标状态)。表里没有的组合即为非法。 */
    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesProdOrderStatusEnum.DRAFT.name(), Map.of(
                    ACTION_RELEASE, MesProdOrderStatusEnum.RELEASED.name(),
                    ACTION_CANCEL, MesProdOrderStatusEnum.CANCELLED.name()),
            MesProdOrderStatusEnum.RELEASED.name(), Map.of(
                    ACTION_READY, MesProdOrderStatusEnum.READY.name(),
                    ACTION_CANCEL, MesProdOrderStatusEnum.CANCELLED.name()),
            MesProdOrderStatusEnum.READY.name(), Map.of(
                    ACTION_START, MesProdOrderStatusEnum.RUNNING.name(),
                    ACTION_CANCEL, MesProdOrderStatusEnum.CANCELLED.name()),
            MesProdOrderStatusEnum.RUNNING.name(), Map.of(
                    ACTION_HOLD, MesProdOrderStatusEnum.HOLD.name(),
                    ACTION_DONE, MesProdOrderStatusEnum.DONE.name()),
            MesProdOrderStatusEnum.HOLD.name(), Map.of(
                    ACTION_RESUME, MesProdOrderStatusEnum.RUNNING.name()),
            MesProdOrderStatusEnum.DONE.name(), Map.of(
                    ACTION_CLOSE, MesProdOrderStatusEnum.CLOSED.name()));

    private MesProdOrderStateMachine() {
    }

    /**
     * 按动作推进状态，返回目标状态名。
     *
     * @param currentStatus 当前状态，取值见 {@link MesProdOrderStatusEnum}
     * @param action        动作，取值见本类的 {@code ACTION_*} 常量
     * @return 目标状态名
     * @throws com.iboot.pf.framework.common.exception.ServiceException 当前状态不允许该动作，或状态、动作未定义
     */
    public static String next(String currentStatus, String action) {
        String target = resolve(currentStatus, action);
        if (target == null) {
            throw ServiceExceptionUtil.exception(PROD_ORDER_STATUS_INVALID);
        }
        return target;
    }

    /**
     * 是否终态：已关闭与已取消，两者都没有出边。
     *
     * <p>注意与 {@link MesProdOrderStatusEnum#isFinal} 的差别：那里把 DONE 也算终态（表单不可再改），
     * 而状态机里 DONE 还有 close 一条出边，因此 DONE 不是终态。
     */
    public static boolean isTerminal(String status) {
        return ObjUtil.equal(MesProdOrderStatusEnum.CLOSED.name(), status)
                || ObjUtil.equal(MesProdOrderStatusEnum.CANCELLED.name(), status);
    }

    /** 是否允许取消：直接查迁移表有无 cancel 出边，即新建、已下达、待开工三态。 */
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
