package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 出库波次状态，波次明细复用同一套状态。 */
@Getter
@AllArgsConstructor
public enum WmsWaveStatusEnum implements ArrayValuable<String> {

    /** 草稿。 */
    DRAFT("草稿"),
    /** 已下发。 */
    RELEASED("已下发"),
    /** 拣货中。 */
    PICKING("拣货中"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
