package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 库存质量状态。对外只暴露 code 字符串，不暴露枚举。 */
@Getter
@AllArgsConstructor
public enum WmsQualityStatusEnum {

    /** 合格。 */
    QUALIFIED("合格"),
    /** 待检。 */
    QC_PENDING("待检"),
    /** 不合格。 */
    REJECTED("不合格"),
    /** 冻结。 */
    FROZEN("冻结");

    private final String name;

    public static boolean isQualified(String status) {
        return ObjUtil.equal(QUALIFIED.name(), status);
    }

}
