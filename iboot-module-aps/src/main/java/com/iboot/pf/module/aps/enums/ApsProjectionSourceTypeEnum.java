package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 工单投影来源类型。与字典 {@code aps_projection_source_type} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsProjectionSourceTypeEnum implements ArrayValuable<String> {

    /** 生产需求。 */
    DEMAND("生产需求"),
    /** 生产工单。 */
    PROD_ORDER("生产工单"),
    /** 工序任务。 */
    OP_TASK("工序任务");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
