package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 缺陷等级。与字典 {@code mes_defect_level} 一致。
 *
 * <p>取值与 mdm 缺陷代码库 {@code mdm_defect_code.level} 保持同一套，
 * 缺陷记录从缺陷代码带出等级时不需要转换。
 */
@Getter
@AllArgsConstructor
public enum MesDefectLevelEnum implements ArrayValuable<String> {

    /** 致命。 */
    CRITICAL("致命"),
    /** 严重。 */
    MAJOR("严重"),
    /** 轻微。 */
    MINOR("轻微");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
