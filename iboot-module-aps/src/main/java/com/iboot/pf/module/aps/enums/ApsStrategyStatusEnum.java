package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 排程策略状态。与字典 {@code aps_strategy_status} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsStrategyStatusEnum implements ArrayValuable<String> {

    /** 草稿。 */
    DRAFT("草稿"),
    /** 已启用。 */
    ENABLED("已启用"),
    /** 已停用。 */
    DISABLED("已停用");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已启用：只有启用中的策略才允许被场景引用。 */
    public static boolean isEnabled(String status) {
        return ObjUtil.equal(ENABLED.name(), status);
    }

}
