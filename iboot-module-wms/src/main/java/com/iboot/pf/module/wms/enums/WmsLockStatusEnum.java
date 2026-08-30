package com.iboot.pf.module.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 库存冻结状态。 */
@Getter
@AllArgsConstructor
public enum WmsLockStatusEnum {

    /** 生效：全额冻结。 */
    ACTIVE("生效"),
    /** 部分解冻。 */
    PARTIAL("部分解冻"),
    /** 已解冻。 */
    RELEASED("已解冻"),
    /** 已取消。 */
    CANCELLED("已取消");

    private final String name;

}
