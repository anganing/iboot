package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.util.Map;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_STATUS_INVALID;

/**
 * 返工任务状态机。
 *
 * <p>返工任务自身只记录质量处置到复检的生命周期；返工工序任务仍沿用既有工序任务状态机。
 */
public final class MesReworkStateMachine {

    public static final String ACTION_RELEASE = "release";
    public static final String ACTION_START = "start";
    public static final String ACTION_FINISH = "finish";
    public static final String ACTION_QC_PASS = "qcPass";
    public static final String ACTION_QC_REJECT = "qcReject";
    public static final String ACTION_CANCEL = "cancel";

    private static final Map<String, Map<String, String>> TRANSITIONS = Map.of(
            MesReworkStatusEnum.DRAFT.name(), Map.of(
                    ACTION_RELEASE, MesReworkStatusEnum.RELEASED.name(),
                    ACTION_CANCEL, MesReworkStatusEnum.CANCELLED.name()),
            MesReworkStatusEnum.RELEASED.name(), Map.of(
                    ACTION_START, MesReworkStatusEnum.RUNNING.name()),
            MesReworkStatusEnum.RUNNING.name(), Map.of(
                    ACTION_FINISH, MesReworkStatusEnum.QC_PENDING.name()),
            MesReworkStatusEnum.QC_PENDING.name(), Map.of(
                    ACTION_QC_PASS, MesReworkStatusEnum.DONE.name(),
                    ACTION_QC_REJECT, MesReworkStatusEnum.RUNNING.name()));

    private MesReworkStateMachine() {
    }

    public static String next(String currentStatus, String action) {
        Map<String, String> actions = currentStatus == null ? null : TRANSITIONS.get(currentStatus);
        String target = actions == null || action == null ? null : actions.get(action);
        if (target == null) {
            throw ServiceExceptionUtil.exception(REWORK_STATUS_INVALID);
        }
        return target;
    }
}
