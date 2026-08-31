package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 质检任务状态。与字典 {@code mes_qc_task_status} 一致。
 *
 * <p>结论与合格、不合格数量在 DONE 时才落定，之前的状态只表示检验推进到哪一步。
 */
@Getter
@AllArgsConstructor
public enum MesQcTaskStatusEnum implements ArrayValuable<String> {

    /** 已创建。 */
    CREATED("已创建"),
    /** 已指派。 */
    ASSIGNED("已指派"),
    /** 检验中。 */
    INSPECTING("检验中"),
    /** 结果录入中。 */
    RESULT_POSTING("结果录入中"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已完成：完成后不允许再改任务与检验结果。 */
    public static boolean isDone(String status) {
        return ObjUtil.equal(DONE.name(), status);
    }

    /** 是否终态。 */
    public static boolean isFinal(String status) {
        return isDone(status) || ObjUtil.equal(CANCELLED.name(), status);
    }

}
