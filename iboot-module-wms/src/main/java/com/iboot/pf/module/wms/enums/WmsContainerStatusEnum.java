package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 容器状态。 */
@Getter
@AllArgsConstructor
public enum WmsContainerStatusEnum implements ArrayValuable<String> {

    /** 空载。 */
    EMPTY("空载"),
    /** 已装载。 */
    LOADED("已装载"),
    /** 在途。 */
    IN_TRANSIT("在途"),
    /** 已入库。 */
    STORED("已入库"),
    /** 已关闭。 */
    CLOSED("已关闭");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
