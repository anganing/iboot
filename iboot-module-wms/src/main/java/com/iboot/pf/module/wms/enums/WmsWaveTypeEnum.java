package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 波次组波方式。 */
@Getter
@AllArgsConstructor
public enum WmsWaveTypeEnum implements ArrayValuable<String> {

    /** 按订单组波。 */
    BY_ORDER("按订单组波"),
    /** 按库区组波。 */
    BY_ZONE("按库区组波"),
    /** 按客户组波。 */
    BY_CUSTOMER("按客户组波"),
    /** 按拣货路线组波。 */
    BY_ROUTE("按拣货路线组波"),
    /** 人工组波。 */
    MANUAL("人工组波");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
