package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 派工任务状态。与字典 {@code mes_dispatch_task_status} 一致。 */
@Getter
@AllArgsConstructor
public enum MesDispatchTaskStatusEnum implements ArrayValuable<String> {

    /** 待接单。 */
    PENDING("待接单"),
    /** 已接单。 */
    ACCEPTED("已接单"),
    /** 作业中。 */
    RUNNING("作业中"),
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

    /** 是否待接单：只有待接单状态允许改派人员、班组、资源与计划时间。 */
    public static boolean isPending(String status) {
        return ObjUtil.equal(PENDING.name(), status);
    }

}
