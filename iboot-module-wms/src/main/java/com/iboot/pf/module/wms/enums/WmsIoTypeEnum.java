package com.iboot.pf.module.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 库存流水收发方向。 */
@Getter
@AllArgsConstructor
public enum WmsIoTypeEnum {

    /** 入：现存数量增加。 */
    IN("入"),
    /** 出：现存数量减少。 */
    OUT("出"),
    /** 转移：来源减少、目标增加。 */
    MOVE("转移"),
    /** 状态变更：现存数量不变。 */
    STATUS("状态变更");

    private final String name;

}
