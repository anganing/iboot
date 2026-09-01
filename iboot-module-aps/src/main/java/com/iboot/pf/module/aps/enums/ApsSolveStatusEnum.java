package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 求解进度状态。
 *
 * <p>只用于进度推送与前端展示，不入库；版本自身的状态机仍然是
 * {@link ApsVersionStatusEnum}。分开是因为进度粒度比版本状态细得多
 * （组装输入、求解中、落库中都属于版本的 SOLVING 状态）。
 */
@Getter
@AllArgsConstructor
public enum ApsSolveStatusEnum {

    /** 组装求解输入。 */
    PREPARING("准备输入"),
    /** 求解计算中。 */
    SOLVING("求解中"),
    /** 保存求解结果。 */
    SAVING("保存结果"),
    /** 求解完成。 */
    SUCCESS("求解完成"),
    /** 求解失败或无可行解。 */
    FAILED("求解失败"),
    /** 被人工取消。 */
    CANCELLED("已取消");

    private final String name;

    private static final Set<ApsSolveStatusEnum> FINISHED = Set.of(SUCCESS, FAILED, CANCELLED);

    /** 是否终态：前端收到终态后停止等待并刷新列表。 */
    public boolean isFinished() {
        return FINISHED.contains(this);
    }

    /** 是否终态。 */
    public static boolean isFinished(String status) {
        return FINISHED.stream().anyMatch(item -> ObjUtil.equal(item.name(), status));
    }

}
