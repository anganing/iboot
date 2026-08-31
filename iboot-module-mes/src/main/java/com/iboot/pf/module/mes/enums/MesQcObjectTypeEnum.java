package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 检验对象类型。与字典 {@code mes_qc_object_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcObjectTypeEnum implements ArrayValuable<String> {

    /** 物料。 */
    MATERIAL("物料"),
    /** 批次。 */
    BATCH("批次"),
    /** 工序任务。 */
    OP_TASK("工序任务"),
    /** 生产工单。 */
    PROD_ORDER("生产工单"),
    /** 出库单。 */
    OUT_ORDER("出库单");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
