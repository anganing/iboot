package com.iboot.pf.module.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 库存预留状态。 */
@Getter
@AllArgsConstructor
public enum WmsReservationStatusEnum {

    /** 生效：全额预留。 */
    ACTIVE("生效"),
    /** 部分处理：已释放或核销一部分。 */
    PARTIAL("部分处理"),
    /** 已释放。 */
    RELEASED("已释放"),
    /** 已核销：库存实际出库后消掉预留。 */
    CONSUMED("已核销"),
    /** 已取消。 */
    CANCELLED("已取消");

    private final String name;

}
