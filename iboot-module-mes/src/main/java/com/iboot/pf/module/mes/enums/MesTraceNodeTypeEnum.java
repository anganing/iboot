package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 追溯树节点类型。与 08 文档批次追溯链路的六类节点一一对应。
 */
@Getter
@AllArgsConstructor
public enum MesTraceNodeTypeEnum implements ArrayValuable<String> {

    /** 批次节点：原料批次或成品批次。 */
    BATCH("批次"),
    /** WMS 出库单节点：生产领料出库或销售发货出库。 */
    OUT_ORDER("出库单"),
    /** 生产工单节点。 */
    PROD_ORDER("生产工单"),
    /** 工序任务节点。 */
    OP_TASK("工序任务"),
    /** WMS 入库单节点：完工入库。 */
    IN_ORDER("入库单"),
    /** 客户节点：链路终点。 */
    CUSTOMER("客户");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
