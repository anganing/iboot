package com.iboot.pf.module.mes.service.qctask;

import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 抽样数量计算器。
 *
 * <p>四种抽样方式的口径见 {@code dev-docs/业务流程/05-MES质量子域流程.md} 的"抽样与判定"表，
 * 公式集中在这里而不散在各个 Service 里：抽样口径是质量子域的公共语义，IQC、FAI、IPQC、FQC、OQC
 * 五个检验点生成的任务必须用同一把尺子算抽样量。
 *
 * <p>AQL 本轮简化：按 GB/T 2828.1 抽样表查表是独立的一块业务，先按方案
 * {@code acceptance_rule} 里配置的固定值取，未配置退化为全检。
 *
 * @author iboot
 */
public final class MesQcSampleCalculator {

    /** 百分比换算基数。 */
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private MesQcSampleCalculator() {
    }

    /**
     * 按检验方案的抽样方式计算抽样数量。
     *
     * <ul>
     * <li>{@code FULL}：全检，等于送检数量</li>
     * <li>{@code FIXED_QTY}：取 min(抽样值, 送检数量)</li>
     * <li>{@code PERCENT}：送检数量 × 抽样值 ÷ 100，向上取整</li>
     * <li>{@code AQL}：本轮简化，按 acceptance_rule 配置的固定值取；未配置退化为全检</li>
     * </ul>
     *
     * @param plan 检验方案，抽样类型与抽样值从这里取
     * @param qty  送检数量
     * @return 抽样数量，不大于送检数量
     */
    public static BigDecimal calc(QcPlanDO plan, BigDecimal qty) {
        String sampleType = plan.getSampleType();
        if (MesQcSampleTypeEnum.FIXED_QTY.name().equals(sampleType)) {
            BigDecimal sampleValue = plan.getSampleValue();
            return sampleValue.min(qty);
        }
        if (MesQcSampleTypeEnum.PERCENT.name().equals(sampleType)) {
            // 向上取整：按比例抽 10% 抽 10.5 件时，半件没法检，取 11 件
            return qty.multiply(plan.getSampleValue())
                    .divide(HUNDRED, 0, RoundingMode.CEILING);
        }
        if (MesQcSampleTypeEnum.AQL.name().equals(sampleType)) {
            BigDecimal fixed = aqlFixedQty(plan.getAcceptanceRule());
            return fixed == null ? qty : fixed.min(qty);
        }
        // FULL 与未知类型一律全检：抽样类型配置错误时宁可多检不少检
        return qty;
    }

    /**
     * 解析 AQL 接收判定规则里配置的固定抽样量。
     *
     * <p>acceptance_rule 本轮约定存"固定抽样量"数字（如 {@code 8}），其他内容视为未配置
     * 退化为全检。等 GB/T 2828.1 抽样表接入后，这里换成查表实现。
     */
    static BigDecimal aqlFixedQty(String acceptanceRule) {
        if (acceptanceRule == null || acceptanceRule.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(acceptanceRule.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
