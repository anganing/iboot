package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 工序执行模式。与字典 {@code mes_op_exec_mode} 一致。 */
@Getter
@AllArgsConstructor
public enum MesOpExecModeEnum implements ArrayValuable<String> {

    /** 顺序执行。 */
    SEQUENTIAL("顺序执行"),
    /** 并行执行。 */
    PARALLEL("并行执行"),
    /** 可选工序。 */
    OPTIONAL("可选工序");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
