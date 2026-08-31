package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库位状态。 */
@Getter
@AllArgsConstructor
public enum WmsLocStatusEnum implements ArrayValuable<String> {

    /** 空闲。 */
    EMPTY("空闲"),
    /** 占用。 */
    OCCUPIED("占用"),
    /** 锁定。 */
    LOCKED("锁定"),
    /** 停用。 */
    DISABLED("停用");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否停用：停用库位不允许收发货。 */
    public static boolean isDisabled(String status) {
        return ObjUtil.equal(DISABLED.name(), status);
    }

}
