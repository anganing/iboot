package com.iboot.pf.module.wms.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 到货登记状态。 */
@Getter
@AllArgsConstructor
public enum WmsArrivalStatusEnum implements ArrayValuable<String> {

    /** 草稿。 */
    DRAFT("草稿"),
    /** 已到货。 */
    ARRIVED("已到货"),
    /** 待质检。 */
    QC_PENDING("待质检"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否草稿：只有草稿允许改动明细。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

}
