package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 设备任务下发状态。 */
@Getter
@AllArgsConstructor
public enum WmsDispatchStatusEnum implements ArrayValuable<String> {

    /** 待下发。 */
    PENDING("待下发"),
    /** 已下发。 */
    SENT("已下发"),
    /** 下发失败。 */
    FAILED("下发失败");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
