package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** RGV 任务执行状态。与字典 {@code mes_rgv_task_status} 一致。 */
@Getter
@AllArgsConstructor
public enum MesRgvTaskStatusEnum implements ArrayValuable<String> {

    /** 已创建。 */
    CREATED("已创建"),
    /** 搬运中。 */
    RUNNING("搬运中"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消"),
    /** 异常。 */
    EXCEPTION("异常");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已完成：完成时间此时必填。 */
    public static boolean isDone(String status) {
        return ObjUtil.equal(DONE.name(), status);
    }

}
