package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库区类型。与字典 {@code wms_zone_type} 一致。 */
@Getter
@AllArgsConstructor
public enum WmsZoneTypeEnum implements ArrayValuable<String> {

    /** 原料区。 */
    RAW("原料区"),
    /** 在制区。 */
    WIP("在制区"),
    /** 成品区。 */
    FG("成品区"),
    /** 质检区。 */
    QC("质检区"),
    /** 退货区。 */
    RETURN("退货区"),
    /** 报废区。 */
    SCRAP("报废区"),
    /** 收货区。 */
    RECEIVING("收货区"),
    /** 发货区。 */
    SHIPPING("发货区");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
