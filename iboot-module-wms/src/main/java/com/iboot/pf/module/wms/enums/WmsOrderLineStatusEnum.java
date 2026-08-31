package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 单据明细状态。入库、出库、转移明细共用：明细只关心“新建 / 作业中 / 已过账 / 已取消”，
 * 主单的质检、审批、冻结账面等状态不下沉到明细。
 */
@Getter
@AllArgsConstructor
public enum WmsOrderLineStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 作业中。 */
    IN_PROGRESS("作业中"),
    /** 已过账。 */
    DONE("已过账"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
