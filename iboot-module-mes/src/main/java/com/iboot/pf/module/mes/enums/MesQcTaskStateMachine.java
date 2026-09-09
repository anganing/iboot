package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.util.Map;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_STATUS_INVALID;

/**
 * 质检任务状态机。质检任务的状态只能由领域动作经本类推进，不允许表单直填。
 *
 * <p>迁移表逐条对应 {@code dev-docs/业务流程/05-MES质量子域流程.md} 的"检验任务生命周期"状态图，
 * 共 6 条边：
 * <pre>
 * CREATED        -- assign            --&gt; ASSIGNED        CREATED        -- cancel --&gt; CANCELLED
 * ASSIGNED       -- startInspect      --&gt; INSPECTING      ASSIGNED       -- cancel --&gt; CANCELLED
 * INSPECTING     -- postResult        --&gt; RESULT_POSTING
 * RESULT_POSTING -- submitConclusion  --&gt; DONE
 * </pre>
 *
 * <p>{@code RESULT_POSTING} 是结果录完但结论未定的中间态：多样本多项目的检验录入可能跨班次，
 * 需要一个"录完待判"的稳定状态。{@code INSPECTING -&gt; RESULT_POSTING} 由结果录入动作在全部
 * 必检项录完时自动推进（T2-05），本状态机只备好这条边。
 *
 * <p>取消只对未开检的任务（{@code CREATED} / {@code ASSIGNED}）开放：检验已开始则料已检、数已录，
 * 来源业务取消时不能销毁检验记录，不合格走处置流程而不是取消。
 *
 * <p>状态取值一律引用 {@link MesQcTaskStatusEnum}，不写字符串字面量。非法组合抛
 * {@link ErrorCodeConstants#QC_TASK_STATUS_INVALID}，不返回 null。
 */
public final class MesQcTaskStateMachine {

    /** 指派检验员：写入 assigned_inspector_id。 */
    public static final String ACTION_ASSIGN = "assign";
    /** 开始检验：写 inspect_start_time，检验员开始录入实测值。 */
    public static final String ACTION_START_INSPECT = "startInspect";
    /** 结果录入完成：全部必检项录完时由结果录入动作（T2-05）推进。 */
    public static final String ACTION_POST_RESULT = "postResult";
    /** 提交结论：写结论与合格、不合格数量，任务终审落定。 */
    public static final String ACTION_SUBMIT_CONCLUSION = "submitConclusion";
    /** 取消：仅未开检的任务允许，来源业务取消时调用。 */
    public static final String ACTION_CANCEL = "cancel";

    /** 迁移表：当前状态 -&gt; (动作 -&gt; 目标状态)。表里没有的组合即为非法。 */
    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesQcTaskStatusEnum.CREATED.name(), Map.of(
                    ACTION_ASSIGN, MesQcTaskStatusEnum.ASSIGNED.name(),
                    ACTION_CANCEL, MesQcTaskStatusEnum.CANCELLED.name()),
            MesQcTaskStatusEnum.ASSIGNED.name(), Map.of(
                    ACTION_START_INSPECT, MesQcTaskStatusEnum.INSPECTING.name(),
                    ACTION_CANCEL, MesQcTaskStatusEnum.CANCELLED.name()),
            MesQcTaskStatusEnum.INSPECTING.name(), Map.of(
                    ACTION_POST_RESULT, MesQcTaskStatusEnum.RESULT_POSTING.name()),
            MesQcTaskStatusEnum.RESULT_POSTING.name(), Map.of(
                    ACTION_SUBMIT_CONCLUSION, MesQcTaskStatusEnum.DONE.name()));

    private MesQcTaskStateMachine() {
    }

    /**
     * 按动作推进状态，返回目标状态名。
     *
     * @param currentStatus 当前状态，取值见 {@link MesQcTaskStatusEnum}
     * @param action        动作，取值见本类的 {@code ACTION_*} 常量
     * @return 目标状态名
     * @throws com.iboot.pf.framework.common.exception.ServiceException 当前状态不允许该动作，或状态、动作未定义
     */
    public static String next(String currentStatus, String action) {
        String target = resolve(currentStatus, action);
        if (target == null) {
            throw ServiceExceptionUtil.exception(QC_TASK_STATUS_INVALID);
        }
        return target;
    }

    /**
     * 是否终态：已完成与已取消，两者都没有出边。
     *
     * <p>与 {@link MesQcTaskStatusEnum#isFinal} 口径一致：质检任务没有可撤销的中间态
     * （对比工序任务的 SKIPPED 有 revokeSkip 出边），做完的和取消的都回不来。
     */
    public static boolean isTerminal(String status) {
        return ObjUtil.equal(MesQcTaskStatusEnum.DONE.name(), status)
                || ObjUtil.equal(MesQcTaskStatusEnum.CANCELLED.name(), status);
    }

    /**
     * 是否允许取消：未开检的任务（已创建、已指派）才允许，与迁移表里这两行的 cancel 出边一致。
     */
    public static boolean canCancel(String status) {
        return ObjUtil.equal(MesQcTaskStatusEnum.CREATED.name(), status)
                || ObjUtil.equal(MesQcTaskStatusEnum.ASSIGNED.name(), status);
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
