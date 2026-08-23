package com.iboot.pf.module.infra.enums.coderule;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 编码规则的流水重置周期。
 */
@Getter
@AllArgsConstructor
public enum CodeResetCycleEnum implements ArrayValuable<Integer> {

    NEVER(0, "不重置"),
    YEAR(1, "按年"),
    MONTH(2, "按月"),
    DAY(3, "按日");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(CodeResetCycleEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
