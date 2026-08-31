package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 领退补料申请类型。与字典 {@code mes_material_req_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesMaterialReqTypeEnum implements ArrayValuable<String> {

    /** 领料。 */
    ISSUE("领料"),
    /** 退料。 */
    RETURN("退料"),
    /** 补料。 */
    SUPPLEMENT("补料");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否退料：退料必须指定批次，否则退回去的库存对不上原批次。 */
    public static boolean isReturn(String reqType) {
        return ObjUtil.equal(RETURN.name(), reqType);
    }

}
