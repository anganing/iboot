package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 仓库管理方式。决定作业是否需要下发设备任务。 */
@Getter
@AllArgsConstructor
public enum WmsManagedByEnum implements ArrayValuable<String> {

    /** 人工管理。 */
    MANUAL("人工管理"),
    /** 立库 WCS。 */
    WCS("立库WCS"),
    /** AGV。 */
    AGV("AGV"),
    /** 混合。 */
    MIXED("混合");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static boolean isManual(String managedBy) {
        return ObjUtil.equal(MANUAL.name(), managedBy);
    }

}
