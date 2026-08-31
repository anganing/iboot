package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 返工任务状态。与字典 {@code mes_rework_status} 一致。 */
@Getter
@AllArgsConstructor
public enum MesReworkStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 已下达。 */
    RELEASED("已下达"),
    /** 返工中。 */
    RUNNING("返工中"),
    /** 待质检。 */
    QC_PENDING("待质检"),
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

    /** 是否新建：只有新建状态允许改动返工任务主体字段。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

}
