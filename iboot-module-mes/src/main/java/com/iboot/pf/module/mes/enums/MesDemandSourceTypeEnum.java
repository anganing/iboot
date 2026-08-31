package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 生产需求来源类型。与字典 {@code mes_demand_source_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesDemandSourceTypeEnum implements ArrayValuable<String> {

    /** 手工录入。 */
    MANUAL("手工录入"),
    /** Excel 导入。 */
    EXCEL("Excel导入"),
    /** ERP 下发。 */
    ERP("ERP下发"),
    /** APS 排程下发。 */
    APS("APS下发");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
