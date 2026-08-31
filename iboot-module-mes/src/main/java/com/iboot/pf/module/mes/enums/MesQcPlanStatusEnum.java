package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 检验方案状态。与字典 {@code mes_qc_plan_status} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcPlanStatusEnum implements ArrayValuable<String> {

    /** 草稿。 */
    DRAFT("草稿"),
    /** 已发布。 */
    PUBLISHED("已发布"),
    /** 已停用。 */
    DISABLED("已停用");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否草稿：只有草稿允许改动方案与检验项，已发布的方案被质检任务锁定引用。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

    /** 是否已发布：只有已发布方案能被质检规则引用。 */
    public static boolean isPublished(String status) {
        return ObjUtil.equal(PUBLISHED.name(), status);
    }

}
