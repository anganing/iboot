package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 到货来源类型。 */
@Getter
@AllArgsConstructor
public enum WmsArrivalSourceTypeEnum implements ArrayValuable<String> {

    /** 采购订单。 */
    PURCHASE_ORDER("采购订单"),
    /** 发货通知。 */
    ASN("发货通知"),
    /** 销售退货。 */
    SALES_RETURN("销售退货"),
    /** 其他来源。 */
    OTHER("其他来源");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
