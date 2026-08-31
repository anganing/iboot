package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存范围。项目专用库存不能被公共需求占用，是多维余额的一个维度。 */
@Getter
@AllArgsConstructor
public enum WmsStockScopeEnum implements ArrayValuable<String> {

    /** 公共库存。 */
    COMMON("公共库存"),
    /** 项目专用。 */
    PROJECT_DEDICATED("项目专用");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 为空时按公共库存兜底，避免余额维度出现 null 导致查不到。 */
    public static String defaultIfEmpty(String scope) {
        return scope == null || scope.isEmpty() ? COMMON.name() : scope;
    }

    public static boolean isCommon(String scope) {
        return ObjUtil.equal(COMMON.name(), scope);
    }

}
