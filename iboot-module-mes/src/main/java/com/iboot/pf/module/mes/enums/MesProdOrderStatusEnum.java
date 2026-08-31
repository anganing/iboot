package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 生产工单状态。与字典 {@code mes_prod_order_status} 一致。
 *
 * <p>流转链路：新建 -> 已下达 -> 待开工 -> 生产中 -> 已完工 -> 已关闭；
 * 生产中可挂起为 HOLD，未开工前可取消。
 */
@Getter
@AllArgsConstructor
public enum MesProdOrderStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 已下达。 */
    RELEASED("已下达"),
    /** 待开工。 */
    READY("待开工"),
    /** 生产中。 */
    RUNNING("生产中"),
    /** 已挂起。 */
    HOLD("已挂起"),
    /** 已完工。 */
    DONE("已完工"),
    /** 已关闭。 */
    CLOSED("已关闭"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否新建：只有新建状态允许改动工单主体字段与删除。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

    /** 是否已下达：下达后才允许生成作业工单与工序任务。 */
    public static boolean isReleased(String status) {
        return ObjUtil.equal(RELEASED.name(), status);
    }

    /** 是否终态：已完工、已关闭、已取消都不允许再推进。 */
    public static boolean isFinal(String status) {
        return ObjUtil.equal(DONE.name(), status)
                || ObjUtil.equal(CLOSED.name(), status)
                || ObjUtil.equal(CANCELLED.name(), status);
    }

}
