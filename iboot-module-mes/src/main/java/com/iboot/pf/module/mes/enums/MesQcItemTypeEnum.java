package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 检验项类型。与字典 {@code mes_qc_item_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcItemTypeEnum implements ArrayValuable<String> {

    /** 数值型。 */
    NUMERIC("数值型"),
    /** 文本型。 */
    TEXT("文本型"),
    /** 布尔型。 */
    BOOLEAN("布尔型");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否数值型：只有数值型才允许并要求给出上下限。 */
    public static boolean isNumeric(String itemType) {
        return ObjUtil.equal(NUMERIC.name(), itemType);
    }

}
