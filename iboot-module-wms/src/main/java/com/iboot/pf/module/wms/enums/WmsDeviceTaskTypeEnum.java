package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 设备搬运任务类型。 */
@Getter
@AllArgsConstructor
public enum WmsDeviceTaskTypeEnum implements ArrayValuable<String> {

    /** 上架。 */
    PUTAWAY("上架"),
    /** 拣货。 */
    PICK("拣货"),
    /** 库内搬运。 */
    TRANSFER("库内搬运"),
    /** 送料。 */
    DELIVERY("送料"),
    /** 回库。 */
    RETURN("回库");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
