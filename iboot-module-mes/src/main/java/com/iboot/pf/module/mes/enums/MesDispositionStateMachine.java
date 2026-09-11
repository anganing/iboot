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

    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesDispositionStatusEnum.DRAFT.name(), Map.of(
                    ACTION_SUBMIT, MesDispositionStatusEnum.SUBMITTED.name(),
                    ACTION_CANCEL, MesDispositionStatusEnum.CANCELLED.name()),
            MesDispositionStatusEnum.SUBMITTED.name(), Map.of(
                    ACTION_APPROVE, MesDispositionStatusEnum.APPROVED.name(),
                    ACTION_REJECT, MesDispositionStatusEnum.REJECTED.name(),
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
