package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 业务单据类型。与字典 {@code mes_biz_type} 一致。
 *
 * <p>质检任务的来源业务、在制流水的来源业务、处置单的联动目标共用这套取值，
 * 配合 {@code bizNo} 定位到具体单据。
 */
@Getter
@AllArgsConstructor
public enum MesBizTypeEnum implements ArrayValuable<String> {

    /** 生产需求。 */
    PROD_DEMAND("生产需求"),
    /** 生产工单。 */
    PROD_ORDER("生产工单"),
    /** 作业工单。 */
    WORK_ORDER("作业工单"),
    /** 工序任务。 */
    OP_TASK("工序任务"),
    /** 生产报工。 */
    PROD_REPORT("生产报工"),
    /** 返工任务。 */
    REWORK("返工任务"),
    /** 领退补料申请。 */
    MATERIAL_REQ("领退补料申请"),
    /** 质检任务。 */
    QC_TASK("质检任务"),
    /** 不合格品处置单。 */
    DISPOSITION("不合格品处置单"),
    /** WMS入库单。 */
    IN_ORDER("WMS入库单"),
    /** WMS出库单。 */
    OUT_ORDER("WMS出库单"),
    /** 到货登记。 */
    ARRIVAL("到货登记");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
