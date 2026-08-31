package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 跳序审批状态。与字典 {@code mes_skip_approve_status} 一致。 */
@Getter
@AllArgsConstructor
public enum MesSkipApproveStatusEnum implements ArrayValuable<String> {

    /** 无需审批。 */
    NOT_REQUIRED("无需审批"),
    /** 待审批。 */
    PENDING("待审批"),
    /** 已批准。 */
    APPROVED("已批准"),
    /** 已驳回。 */
    REJECTED("已驳回");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已作出审批决定：审批人此时必填。 */
    public static boolean isDecided(String approveStatus) {
        return ObjUtil.equal(APPROVED.name(), approveStatus) || ObjUtil.equal(REJECTED.name(), approveStatus);
    }

    /** 是否已批准。 */
    public static boolean isApproved(String approveStatus) {
        return ObjUtil.equal(APPROVED.name(), approveStatus);
    }

}
