package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存转移类型。 */
@Getter
@AllArgsConstructor
public enum WmsTransferTypeEnum implements ArrayValuable<String> {

    /** 库位转移。 */
    LOC_TRANSFER("库位转移"),
    /** 仓库调拨。 */
    WH_TRANSFER("仓库调拨"),
    /** 组织调拨。 */
    INV_ORG_TRANSFER("组织调拨"),
    /** 项目转移。 */
    PROJECT_TRANSFER("项目转移"),
    /** 质量状态转移。 */
    QUALITY_TRANSFER("质量状态转移");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
