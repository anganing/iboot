package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** RGV 任务下发状态。与字典 {@code mes_rgv_dispatch_status} 一致。 */
@Getter
@AllArgsConstructor
public enum MesRgvDispatchStatusEnum implements ArrayValuable<String> {

    /** 待下发。 */
    PENDING("待下发"),
    /** 已下发。 */
    SENT("已下发"),
    /** 下发失败。 */
    FAILED("下发失败");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已下发：下发后不允许改动搬运起终点，否则和外部调度系统不一致。 */
    public static boolean isSent(String dispatchStatus) {
        return ObjUtil.equal(SENT.name(), dispatchStatus);
    }

}
