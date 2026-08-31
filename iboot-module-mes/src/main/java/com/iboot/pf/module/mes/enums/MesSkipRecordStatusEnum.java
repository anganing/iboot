package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 工序跳过记录状态。与字典 {@code mes_skip_record_status} 一致。
 *
 * <p>只有生效的跳过记录才真正把工序任务置为已跳过；撤销后工序回到原状态。
 */
@Getter
@AllArgsConstructor
public enum MesSkipRecordStatusEnum implements ArrayValuable<String> {

    /** 待生效。 */
    PENDING("待生效"),
    /** 已生效。 */
    EFFECTIVE("已生效"),
    /** 已撤销。 */
    REVOKED("已撤销");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已生效。 */
    public static boolean isEffective(String status) {
        return ObjUtil.equal(EFFECTIVE.name(), status);
    }

}
