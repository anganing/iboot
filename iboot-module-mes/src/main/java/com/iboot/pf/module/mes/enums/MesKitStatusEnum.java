package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 齐套状态。与字典 {@code mes_kit_status} 一致。
 *
 * <p>生产工单、作业工单的齐套状态字段和齐套快照的检查结果共用这套取值，
 * 快照结果只用后三个，UNCHECKED 表示还没查过。
 */
@Getter
@AllArgsConstructor
public enum MesKitStatusEnum implements ArrayValuable<String> {

    /** 未检查。 */
    UNCHECKED("未检查"),
    /** 齐套。 */
    FULL_KIT("齐套"),
    /** 部分齐套。 */
    PARTIAL_KIT("部分齐套"),
    /** 短缺。 */
    SHORTAGE("短缺");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
