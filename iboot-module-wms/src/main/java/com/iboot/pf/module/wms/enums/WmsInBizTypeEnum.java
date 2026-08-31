package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 入库业务类型。与字典 {@code wms_in_biz_type} 一致。 */
@Getter
@AllArgsConstructor
public enum WmsInBizTypeEnum implements ArrayValuable<String> {

    /** 采购入库。 */
    PO_IN("采购入库"),
    /** 生产完工入库。 */
    PROD_FINISH_IN("生产完工入库"),
    /** 返工完工入库。 */
    REWORK_FINISH_IN("返工完工入库"),
    /** 销售退货入库。 */
    SALES_RETURN_IN("销售退货入库"),
    /** 生产退料入库。 */
    PROD_RETURN_IN("生产退料入库"),
    /** 调拨入库。 */
    TRANSFER_IN("调拨入库"),
    /** 盘盈入库。 */
    COUNT_GAIN_IN("盘盈入库"),
    /** 其他入库。 */
    OTHER_IN("其他入库");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
