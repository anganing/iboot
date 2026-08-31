package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 来料检验模式。决定入库时质量状态取合格还是待检。 */
@Getter
@AllArgsConstructor
public enum WmsIqcModeEnum implements ArrayValuable<String> {

    /** 先检后入。 */
    PRE_INBOUND("先检后入"),
    /** 先入后检。 */
    POST_INBOUND_PENDING("先入后检");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
