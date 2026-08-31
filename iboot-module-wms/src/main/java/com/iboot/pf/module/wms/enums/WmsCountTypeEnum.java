package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 盘点类型。 */
@Getter
@AllArgsConstructor
public enum WmsCountTypeEnum implements ArrayValuable<String> {

    /** 全盘。 */
    FULL("全盘"),
    /** 抽盘。 */
    SAMPLE("抽盘"),
    /** ABC 循环盘点。 */
    ABC_CYCLE("ABC循环盘点"),
    /** 动态盘点。 */
    DYNAMIC("动态盘点");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
