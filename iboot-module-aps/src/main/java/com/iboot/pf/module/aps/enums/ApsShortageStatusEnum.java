package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;

/** 缺料状态。与字典 {@code aps_shortage_status} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsShortageStatusEnum implements ArrayValuable<String> {

    /** 未解决。 */
    OPEN("未解决"),
    /** 部分满足。 */
    PARTIAL("部分满足"),
    /** 已解决。 */
    RESOLVED("已解决"),
    /** 已忽略。 */
    IGNORED("已忽略");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /**
     * 依据短缺数量与可用数量推导状态。
     *
     * <p>不缺料即已解决；仍缺料但有可用库存算部分满足；一点都没有算未解决。
     * 已忽略是人工判断，不由这里推导。
     */
    public static ApsShortageStatusEnum of(BigDecimal shortQty, BigDecimal availQty) {
        if (shortQty.signum() == 0) {
            return RESOLVED;
        }
        return availQty.signum() > 0 ? PARTIAL : OPEN;
    }

}
