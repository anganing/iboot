package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存预留状态。 */
@Getter
@AllArgsConstructor
public enum WmsReservationStatusEnum implements ArrayValuable<String> {

    /** 生效：全额预留。 */
    ACTIVE("生效"),
    /** 部分处理：已释放或核销一部分。 */
    PARTIAL("部分处理"),
    /** 已释放。 */
    RELEASED("已释放"),
    /** 已核销：库存实际出库后消掉预留。 */
    CONSUMED("已核销"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
