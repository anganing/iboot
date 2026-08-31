package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存冻结状态。 */
@Getter
@AllArgsConstructor
public enum WmsLockStatusEnum implements ArrayValuable<String> {

    /** 生效：全额冻结。 */
    ACTIVE("生效"),
    /** 部分解冻。 */
    PARTIAL("部分解冻"),
    /** 已解冻。 */
    RELEASED("已解冻"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
