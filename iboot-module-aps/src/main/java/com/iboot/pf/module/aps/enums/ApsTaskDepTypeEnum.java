package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 排程任务依赖类型。与字典 {@code aps_task_dep_type} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsTaskDepTypeEnum implements ArrayValuable<String> {

    /** 完工到开工。 */
    FINISH_START("完工到开工"),
    /** 开工到开工。 */
    START_START("开工到开工"),
    /** 完工到完工。 */
    FINISH_FINISH("完工到完工");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
