package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** RGV 任务类型。与字典 {@code mes_rgv_task_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesRgvTaskTypeEnum implements ArrayValuable<String> {

    /** 工位间流转。 */
    STATION_TRANSFER("工位间流转"),
    /** 缓存区流转。 */
    BUFFER_TRANSFER("缓存区流转"),
    /** 回库。 */
    RETURN("回库");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
