package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存质量状态。对外只暴露 code 字符串，不暴露枚举。 */
@Getter
@AllArgsConstructor
public enum WmsQualityStatusEnum implements ArrayValuable<String> {

    /** 合格。 */
    QUALIFIED("合格"),
    /** 待检。 */
    QC_PENDING("待检"),
    /** 不合格。 */
    REJECTED("不合格"),
    /** 冻结。 */
    FROZEN("冻结");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static boolean isQualified(String status) {
        return ObjUtil.equal(QUALIFIED.name(), status);
    }

}
