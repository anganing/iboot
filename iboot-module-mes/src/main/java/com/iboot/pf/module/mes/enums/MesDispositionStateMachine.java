package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.util.Map;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_STATUS_INVALID;

/** 不合格品处置单状态机。 */
public final class MesDispositionStateMachine {

    public static final String ACTION_SUBMIT = "submit";
    public static final String ACTION_APPROVE = "approve";
    public static final String ACTION_EXECUTE = "execute";
    public static final String ACTION_DONE = "done";
    public static final String ACTION_REJECT = "reject";
    public static final String ACTION_REVISE = "revise";
    public static final String ACTION_CANCEL = "cancel";
    /**
     * 审批回退：BPM 流程被驳回或撤销，处置单从已提交退回新建，允许改完重新提交。
     *
     * <p>这条边与 {@link #ACTION_REJECT} 的区别是目标状态不同：{@code reject} 是业务侧审批人直接判
     * 不合格结论（停在 {@code REJECTED}，要走 {@code revise} 才能回 {@code DRAFT}），而审批回退是
     * 流程没通过，单据本身还没被任何人判定，语义上等于"这次提交不算"，所以任务书要求直接回
     * {@code DRAFT}。补这条边而不是在 Service 里裸写状态值，是为了让状态机仍是唯一的迁移权威。
     */
    public static final String ACTION_APPROVAL_RETURN = "approvalReturn";

    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesDispositionStatusEnum.DRAFT.name(), Map.of(
                    ACTION_SUBMIT, MesDispositionStatusEnum.SUBMITTED.name(),
                    ACTION_CANCEL, MesDispositionStatusEnum.CANCELLED.name()),
            MesDispositionStatusEnum.SUBMITTED.name(), Map.of(
                    ACTION_APPROVE, MesDispositionStatusEnum.APPROVED.name(),
                    ACTION_REJECT, MesDispositionStatusEnum.REJECTED.name(),
                    ACTION_APPROVAL_RETURN, MesDispositionStatusEnum.DRAFT.name(),
                    ACTION_CANCEL, MesDispositionStatusEnum.CANCELLED.name()),
            MesDispositionStatusEnum.APPROVED.name(), Map.of(
                    ACTION_EXECUTE, MesDispositionStatusEnum.EXECUTING.name()),
            MesDispositionStatusEnum.EXECUTING.name(), Map.of(
                    ACTION_DONE, MesDispositionStatusEnum.DONE.name()),
            MesDispositionStatusEnum.REJECTED.name(), Map.of(
                    ACTION_REVISE, MesDispositionStatusEnum.DRAFT.name()));

    private MesDispositionStateMachine() {
    }

    public static String next(String status, String action) {
        String target = TRANSITIONS.getOrDefault(status, Map.of()).get(action);
        if (target == null) {
            throw ServiceExceptionUtil.exception(DISPOSITION_STATUS_INVALID);
        }
        return target;
    }
}
