package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 设备类型。 */
@Getter
@AllArgsConstructor
public enum WmsDeviceTypeEnum implements ArrayValuable<String> {

    /** 立库 WCS。 */
    WCS("立库WCS"),
    /** AGV。 */
    AGV("AGV"),
    /** RGV。 */
    RGV("RGV");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
