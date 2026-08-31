package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 外部设备触发方式。 */
@Getter
@AllArgsConstructor
public enum WmsHookModeEnum implements ArrayValuable<String> {

    /** 不触发。 */
    NONE("不触发"),
    /** 立库 WCS。 */
    WCS("立库WCS"),
    /** AGV。 */
    AGV("AGV");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否需要下发设备任务。 */
    public static boolean needDispatch(String hookMode) {
        return hookMode != null && !hookMode.isEmpty() && !ObjUtil.equal(NONE.name(), hookMode);
    }

}
