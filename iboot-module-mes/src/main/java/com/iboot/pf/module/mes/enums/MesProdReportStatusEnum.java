package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 生产报工状态。与字典 {@code mes_prod_report_status} 一致。
 *
 * <p>报工确认后才把数量推进到工序任务与工单，所以只有新建、已提交允许修改。
 */
@Getter
@AllArgsConstructor
public enum MesProdReportStatusEnum implements ArrayValuable<String> {

    /** 新建。 */
    DRAFT("新建"),
    /** 已提交。 */
    SUBMITTED("已提交"),
    /** 待质检。 */
    QC_PENDING("待质检"),
    /** 已确认。 */
    CONFIRMED("已确认"),
    /** 已取消。 */
    CANCELLED("已取消");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否已确认：确认后数量已推进到工序任务，不允许再改。 */
    public static boolean isConfirmed(String status) {
        return ObjUtil.equal(CONFIRMED.name(), status);
    }

}
