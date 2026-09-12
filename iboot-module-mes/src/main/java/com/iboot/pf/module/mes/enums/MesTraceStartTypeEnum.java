package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 追溯查询起点类型。
 *
 * <p>批次号与销售出库单号从链路中间切入，查询时要把链路双向补全成完整树。
 */
@Getter
@AllArgsConstructor
public enum MesTraceStartTypeEnum implements ArrayValuable<String> {

    /** 批次：原料批次或成品批次，值为批次号（lotNo）或批次编号。 */
    LOT("批次"),
    /** 生产工单：值为生产工单号。 */
    PROD_ORDER("生产工单"),
    /** 销售出库单：值为出库单号。 */
    SALES_OUT("销售出库单");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 按取值解析，不合法返回 null，由调用方决定报错方式。 */
    public static MesTraceStartTypeEnum ofName(String name) {
        for (MesTraceStartTypeEnum type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
