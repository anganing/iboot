package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 排程任务状态。与字典 {@code aps_task_status} 一致。
 *
 * <p>流转链路：已排定 -> 已锁定 -> 已下达 -> 已开工 -> 已完工，未开工前都可取消。
 * 已开工的任务不允许解锁，否则重排会把车间正在做的工序挪走。
 */
@Getter
@AllArgsConstructor
public enum ApsTaskStatusEnum implements ArrayValuable<String> {

    /** 已排定。 */
    PLANNED("已排定"),
    /** 已锁定。 */
    LOCKED("已锁定"),
    /** 已下达。 */
    RELEASED("已下达"),
    /** 已开工。 */
    STARTED("已开工"),
    /** 已完工。 */
    DONE("已完工"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已开工：已开工任务不允许解锁，也不允许调整计划时间。 */
    public static boolean isStarted(String status) {
        return ObjUtil.equal(STARTED.name(), status);
    }

}
