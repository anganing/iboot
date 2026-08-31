package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 排程场景状态。与字典 {@code aps_scenario_status} 一致。
 *
 * <p>流转链路：新建 -> 快照中 -> 待求解 -> 求解中 -> 已求解，任一状态都可关闭；关闭后不再允许生成版本。
 */
@Getter
@AllArgsConstructor
public enum ApsScenarioStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 快照中。 */
    SNAPSHOTTING("快照中"),
    /** 待求解。 */
    READY("待求解"),
    /** 求解中。 */
    SOLVING("求解中"),
    /** 已求解。 */
    SOLVED("已求解"),
    /** 已关闭。 */
    CLOSED("已关闭");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已关闭：关闭后不允许再生成排程版本，也不允许刷新投影。 */
    public static boolean isClosed(String status) {
        return ObjUtil.equal(CLOSED.name(), status);
    }

}
