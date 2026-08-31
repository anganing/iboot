package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 生产需求状态。与字典 {@code mes_demand_status} 一致。
 *
 * <p>流转链路：新建 -> 已确认 -> 已排产 -> 已下达 -> 已完成，任一非终态都可取消。
 */
@Getter
@AllArgsConstructor
public enum MesDemandStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 已确认。 */
    CONFIRMED("已确认"),
    /** 已排产。 */
    PLANNED("已排产"),
    /** 已下达。 */
    RELEASED("已下达"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否新建：只有新建状态允许改动需求主体字段。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

    /** 是否终态：已完成或已取消，不允许再流转。 */
    public static boolean isFinal(String status) {
        return ObjUtil.equal(DONE.name(), status) || ObjUtil.equal(CANCELLED.name(), status);
    }

}
