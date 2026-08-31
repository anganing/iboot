package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库位尺寸校验模式。 */
@Getter
@AllArgsConstructor
public enum WmsDimensionCheckModeEnum implements ArrayValuable<String> {

    /** 不校验。 */
    NONE("不校验"),
    /** 仅校验高度。 */
    HEIGHT_ONLY("仅校验高度"),
    /** 全尺寸校验。 */
    FULL_DIM("全尺寸校验");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
