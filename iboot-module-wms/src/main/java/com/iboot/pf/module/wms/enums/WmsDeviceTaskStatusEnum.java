package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 设备任务执行状态。 */
@Getter
@AllArgsConstructor
public enum WmsDeviceTaskStatusEnum implements ArrayValuable<String> {

    /** 已创建。 */
    CREATED("已创建"),
    /** 执行中。 */
    RUNNING("执行中"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消"),
    /** 异常。 */
    EXCEPTION("异常");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
