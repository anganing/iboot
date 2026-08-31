package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 齐套检查范围。与字典 {@code mes_kit_check_scope} 一致。 */
@Getter
@AllArgsConstructor
public enum MesKitCheckScopeEnum implements ArrayValuable<String> {

    /** 生产工单整单。 */
    PROD_ORDER("生产工单整单"),
    /** 作业工单。 */
    WORK_ORDER("作业工单"),
    /** 工序任务。 */
    OP_TASK("工序任务");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否作业工单级检查：作业工单编号此时必填。 */
    public static boolean isWorkOrderScope(String scope) {
        return ObjUtil.equal(WORK_ORDER.name(), scope);
    }

    /** 是否工序任务级检查：工序任务编号此时必填。 */
    public static boolean isOpTaskScope(String scope) {
        return ObjUtil.equal(OP_TASK.name(), scope);
    }

}
