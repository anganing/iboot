package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 仓储策略状态。 */
@Getter
@AllArgsConstructor
public enum WmsStrategyStatusEnum implements ArrayValuable<String> {

    /** 草稿。 */
    DRAFT("草稿"),
    /** 启用。 */
    ENABLED("启用"),
    /** 停用。 */
    DISABLED("停用");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否启用：只有启用的策略参与选库位与拣选。 */
    public static boolean isEnabled(String status) {
        return ObjUtil.equal(ENABLED.name(), status);
    }

}
