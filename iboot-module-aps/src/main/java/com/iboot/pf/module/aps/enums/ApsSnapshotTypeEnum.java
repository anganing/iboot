package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** Timefold 求解快照类型。与字典 {@code aps_snapshot_type} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsSnapshotTypeEnum implements ArrayValuable<String> {

    /** 求解输入。 */
    INPUT("求解输入"),
    /** 求解结果。 */
    SOLUTION("求解结果"),
    /** 约束命中。 */
    CONSTRAINT_MATCH("约束命中"),
    /** 求解异常。 */
    ERROR("求解异常");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
