package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 跳序原因类型。与字典 {@code mes_skip_reason_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesSkipReasonTypeEnum implements ArrayValuable<String> {

    /** 工序不适用。 */
    NOT_APPLICABLE("工序不适用"),
    /** 设备故障。 */
    EQUIPMENT_FAULT("设备故障"),
    /** 缺料。 */
    MATERIAL_SHORTAGE("缺料"),
    /** 客户特批。 */
    CUSTOMER_WAIVER("客户特批"),
    /** 工艺变更。 */
    PROCESS_CHANGE("工艺变更"),
    /** 其他。 */
    OTHER("其他");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
