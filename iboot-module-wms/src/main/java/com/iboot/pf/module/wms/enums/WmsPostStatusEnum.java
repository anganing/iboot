package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存过账状态。 */
@Getter
@AllArgsConstructor
public enum WmsPostStatusEnum implements ArrayValuable<String> {

    /** 待过账。 */
    PENDING("待过账"),
    /** 已过账。 */
    POSTED("已过账"),
    /** 过账失败。 */
    FAILED("过账失败");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static boolean isPosted(String status) {
        return ObjUtil.equal(POSTED.name(), status);
    }

}
