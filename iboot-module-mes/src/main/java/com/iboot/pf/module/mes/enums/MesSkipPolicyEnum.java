package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 工序跳序策略。与字典 {@code mes_skip_policy} 一致。 */
@Getter
@AllArgsConstructor
public enum MesSkipPolicyEnum implements ArrayValuable<String> {

    /** 不允许跳序。 */
    NOT_ALLOWED("不允许跳序"),
    /** 填原因即可跳序。 */
    ALLOW_WITH_REASON("填原因即可跳序"),
    /** 需审批后跳序。 */
    APPROVAL_REQUIRED("需审批后跳序");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否禁止跳序。 */
    public static boolean isNotAllowed(String policy) {
        return ObjUtil.equal(NOT_ALLOWED.name(), policy);
    }

    /** 是否需要审批才能生效。 */
    public static boolean isApprovalRequired(String policy) {
        return ObjUtil.equal(APPROVAL_REQUIRED.name(), policy);
    }

}
