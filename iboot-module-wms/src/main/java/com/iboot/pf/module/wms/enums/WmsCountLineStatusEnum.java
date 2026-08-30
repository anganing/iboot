package com.iboot.pf.module.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 盘点明细状态。 */
@Getter
@AllArgsConstructor
public enum WmsCountLineStatusEnum {

    /** 待盘点。 */
    PENDING("待盘点"),
    /** 已实盘且无差异。 */
    COUNTED("已盘点"),
    /** 已实盘且存在差异。 */
    DIFF("有差异"),
    /** 差异已过账。 */
    ADJUSTED("差异已过账");

    private final String name;

}
