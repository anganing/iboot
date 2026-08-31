package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 盘点明细状态。 */
@Getter
@AllArgsConstructor
public enum WmsCountLineStatusEnum implements ArrayValuable<String> {

    /** 待盘点。 */
    PENDING("待盘点"),
    /** 已实盘且无差异。 */
    COUNTED("已盘点"),
    /** 已实盘且存在差异。 */
    DIFF("有差异"),
    /** 差异已过账。 */
    ADJUSTED("差异已过账");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
