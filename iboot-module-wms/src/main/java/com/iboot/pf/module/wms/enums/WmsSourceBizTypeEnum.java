package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 来源业务类型。冻结、预留、流水、设备任务共用一套来源标识，
 * 用于把库存动作追回到发起它的业务单据。
 */
@Getter
@AllArgsConstructor
public enum WmsSourceBizTypeEnum implements ArrayValuable<String> {

    /** 入库单。 */
    IN_ORDER("入库单"),
    /** 出库单。 */
    OUT_ORDER("出库单"),
    /** 调拨转移单。 */
    TRANSFER_ORDER("调拨转移单"),
    /** 盘点单。 */
    COUNT_ORDER("盘点单"),
    /** 出库波次。 */
    WAVE("出库波次"),
    /** 到货登记。 */
    ARRIVAL("到货登记"),
    /** 生产领料。 */
    PROD_ISSUE("生产领料"),
    /** 生产预留。 */
    PROD_RESERVE("生产预留"),
    /** 销售发货。 */
    SALES_DELIVERY("销售发货"),
    /** 项目专用预留。 */
    PROJECT_RESERVE("项目专用预留"),
    /** 质检冻结。 */
    QC_HOLD("质检冻结"),
    /** 盘点冻结。 */
    COUNT_FREEZE("盘点冻结"),
    /** 设备搬运任务。 */
    DEVICE_TASK("设备搬运任务"),
    /** 人工处理。 */
    MANUAL("人工处理"),
    /** 其他业务。 */
    OTHER("其他业务");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
