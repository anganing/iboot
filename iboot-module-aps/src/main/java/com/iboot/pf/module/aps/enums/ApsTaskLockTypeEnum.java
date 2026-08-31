package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 排程任务锁定类型。与字典 {@code aps_task_lock_type} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsTaskLockTypeEnum implements ArrayValuable<String> {

    /** 已开工锁定。 */
    STARTED("已开工锁定"),
    /** 人工锁定。 */
    MANUAL("人工锁定"),
    /** 物料受限锁定。 */
    MATERIAL("物料受限锁定"),
    /** 资源受限锁定。 */
    RESOURCE("资源受限锁定");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
