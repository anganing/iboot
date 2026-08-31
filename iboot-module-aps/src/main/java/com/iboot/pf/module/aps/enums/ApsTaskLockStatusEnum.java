package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 排程任务锁定记录状态。与字典 {@code aps_task_lock_status} 一致。 */
@Getter
@AllArgsConstructor
public enum ApsTaskLockStatusEnum implements ArrayValuable<String> {

    /** 锁定中。 */
    LOCKED("锁定中"),
    /** 已解锁。 */
    UNLOCKED("已解锁");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否锁定中：同一任务只允许存在一条锁定中的记录。 */
    public static boolean isLocked(String status) {
        return ObjUtil.equal(LOCKED.name(), status);
    }

}
