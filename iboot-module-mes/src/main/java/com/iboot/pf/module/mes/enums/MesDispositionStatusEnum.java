package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 不合格品处置单状态。与字典 {@code mes_disposition_status} 一致。
 *
 * <p>审批通过后才允许联动返工单、报废或 WMS 冻结，因此审批人与审批时间只在 APPROVED、REJECTED 时有值。
 */
@Getter
@AllArgsConstructor
public enum MesDispositionStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 已提交。 */
    SUBMITTED("已提交"),
    /** 已批准。 */
    APPROVED("已批准"),
    /** 执行中。 */
    EXECUTING("执行中"),
    /** 已完成。 */
    DONE("已完成"),
    /** 已驳回。 */
    REJECTED("已驳回"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否新建：只有新建状态允许改动处置单主体字段。 */
    public static boolean isDraft(String status) {
        return ObjUtil.equal(DRAFT.name(), status);
    }

    /** 是否已审批：审批人与审批时间此时必填。 */
    public static boolean isApprovalDecided(String status) {
        return ObjUtil.equal(APPROVED.name(), status) || ObjUtil.equal(REJECTED.name(), status);
    }

}
