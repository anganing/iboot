package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 出库业务类型。与字典 {@code wms_out_biz_type} 一致。 */
@Getter
@AllArgsConstructor
public enum WmsOutBizTypeEnum implements ArrayValuable<String> {

    /** 生产领料出库。 */
    PROD_ISSUE_OUT("生产领料出库"),
    /** 销售发货出库。 */
    SALES_DELIVERY_OUT("销售发货出库"),
    /** 采购退货出库。 */
    PO_RETURN_OUT("采购退货出库"),
    /** 报废出库。 */
    SCRAP_OUT("报废出库"),
    /** 调拨出库。 */
    TRANSFER_OUT("调拨出库"),
    /** 盘亏出库。 */
    COUNT_LOSS_OUT("盘亏出库"),
    /** 其他出库。 */
    OTHER_OUT("其他出库");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
