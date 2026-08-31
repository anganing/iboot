package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 仓储策略类型。 */
@Getter
@AllArgsConstructor
public enum WmsStrategyTypeEnum implements ArrayValuable<String> {

    /** 上架策略。 */
    PUTAWAY("上架策略"),
    /** 拣选策略。 */
    PICK("拣选策略"),
    /** 补货策略。 */
    REPLENISH("补货策略");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
