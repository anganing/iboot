package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 原始来源类型：把 WMS 单据追溯到最初的业务单据。 */
@Getter
@AllArgsConstructor
public enum WmsOriginTypeEnum implements ArrayValuable<String> {

    /** 采购订单。 */
    PO("采购订单"),
    /** 销售订单。 */
    SO("销售订单"),
    /** 生产工单。 */
    MO("生产工单"),
    /** 调拨单。 */
    TRANSFER("调拨单"),
    /** 盘点单。 */
    COUNT("盘点单"),
    /** 其他来源。 */
    OTHER("其他来源");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
