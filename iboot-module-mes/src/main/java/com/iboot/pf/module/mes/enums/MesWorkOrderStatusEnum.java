package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 作业工单状态。与字典 {@code mes_work_order_status} 一致。
 *
 * <p>作业工单是生产工单在车间的执行单位，状态由下属工序任务推进。
 */
@Getter
@AllArgsConstructor
public enum MesWorkOrderStatusEnum implements ArrayValuable<String> {

    /** 待处理。 */
    PENDING("待处理"),
    /** 待开工。 */
    READY("待开工"),
    /** 作业中。 */
    RUNNING("作业中"),
    /** 已挂起。 */
    HOLD("已挂起"),
    /** 已完工。 */
    DONE("已完工"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否待处理：只有待处理状态允许改动工单主体字段与删除。 */
    public static boolean isPending(String status) {
        return ObjUtil.equal(PENDING.name(), status);
    }

    /** 是否终态。 */
    public static boolean isFinal(String status) {
        return ObjUtil.equal(DONE.name(), status) || ObjUtil.equal(CANCELLED.name(), status);
    }

}
