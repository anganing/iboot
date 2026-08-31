package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 仓库类型。与字典 {@code wms_wh_type} 一致。 */
@Getter
@AllArgsConstructor
public enum WmsWhTypeEnum implements ArrayValuable<String> {

    /** 立库。 */
    ASRS("立库"),
    /** 平库。 */
    FLAT("平库"),
    /** 线边仓。 */
    LINE_SIDE("线边仓"),
    /** 虚拟仓。 */
    VIRTUAL("虚拟仓"),
    /** 质检仓。 */
    QC("质检仓"),
    /** 在途仓。 */
    IN_TRANSIT("在途仓");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
