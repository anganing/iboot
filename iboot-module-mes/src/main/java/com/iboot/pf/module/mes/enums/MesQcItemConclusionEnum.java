package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 单项检验结论。与字典 {@code mes_qc_item_conclusion} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcItemConclusionEnum implements ArrayValuable<String> {

    /** 合格。 */
    PASS("合格"),
    /** 不合格。 */
    FAIL("不合格");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 结论与是否合格标记必须一致，避免两个字段互相矛盾。 */
    public static boolean matches(String conclusion, Boolean passFlag) {
        if (conclusion == null || passFlag == null) {
            return false;
        }
        return ObjUtil.equal(PASS.name(), conclusion) == passFlag;
    }

}
