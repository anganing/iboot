package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 到货明细状态。 */
@Getter
@AllArgsConstructor
public enum WmsArrivalLineStatusEnum implements ArrayValuable<String> {

    /** 待处理。 */
    PENDING("待处理"),
    /** 待质检。 */
    QC_PENDING("待质检"),
    /** 待入库。 */
    READY("待入库"),
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
