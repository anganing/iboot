package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库位类型。与字典 {@code wms_loc_type} 一致。 */
@Getter
@AllArgsConstructor
public enum WmsLocTypeEnum implements ArrayValuable<String> {

    /** 立库货格。 */
    ASRS_CELL("立库货格"),
    /** 平库区域。 */
    FLAT_AREA("平库区域"),
    /** 货架储位。 */
    RACK_BIN("货架储位"),
    /** 线边储位。 */
    LINE_SIDE("线边储位"),
    /** 黑箱库位。 */
    BLACK_BOX("黑箱库位");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
