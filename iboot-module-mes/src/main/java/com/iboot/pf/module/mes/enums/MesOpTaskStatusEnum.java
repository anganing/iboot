package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 工序任务状态。与字典 {@code mes_op_task_status} 一致。
 *
 * <p>状态迁移由 {@link com.iboot.pf.module.mes.service.MesBusinessRules} 统一裁决，
 * 这里只声明合法取值与几个语义判定，避免各处散落字符串比较。
 */
@Getter
@AllArgsConstructor
public enum MesOpTaskStatusEnum implements ArrayValuable<String> {

    /** 待排产。 */
    PENDING("待排产"),
    /** 待开工。 */
    READY("待开工"),
    /** 加工中。 */
    RUNNING("加工中"),
    /** 已暂停。 */
    PAUSED("已暂停"),
    /** 待质检。 */
    QC_WAITING("待质检"),
    /** 已挂起。 */
    HOLD("已挂起"),
    /** 已完工。 */
    DONE("已完工"),
    /** 已跳过。 */
    SKIPPED("已跳过"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否未开工：待排产或待开工，允许改动任务主体字段与删除。 */
    public static boolean isNotStarted(String status) {
        return ObjUtil.equal(PENDING.name(), status) || ObjUtil.equal(READY.name(), status);
    }

    /** 是否终态：已完工、已跳过、已取消都不允许再流转。 */
    public static boolean isFinal(String status) {
        return ObjUtil.equal(DONE.name(), status)
                || ObjUtil.equal(SKIPPED.name(), status)
                || ObjUtil.equal(CANCELLED.name(), status);
    }

}
