package com.iboot.pf.module.mes.service;

import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.iboot.pf.module.mes.enums.MesConstants.QTY_SCALE;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_REPORT_QTY_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QUALITY_QTY_INVALID;

/**
 * MES 领域不变量。数量类规则集中定义，供各领域 Service 复用。
 *
 * <p>数量精度统一取库表的 decimal(20,6)。
 *
 * <p>状态迁移不在本类，三个状态机各自独立：
 * {@link com.iboot.pf.module.mes.enums.MesProdOrderStateMachine}、
 * {@link com.iboot.pf.module.mes.enums.MesWorkOrderStateMachine}、
 * {@link com.iboot.pf.module.mes.enums.MesOpTaskStateMachine}。
 */
public final class MesBusinessRules {

    private MesBusinessRules() {
    }

    /** 校验工序任务完工数量：合格数 + 报废数 = 计划数。 */
    public static void validateFinishQty(BigDecimal planQty, BigDecimal qualifiedQty, BigDecimal scrapQty) {
        if (planQty == null || qualifiedQty == null || scrapQty == null
                || qualifiedQty.signum() < 0 || scrapQty.signum() < 0
                || qualifiedQty.add(scrapQty).compareTo(planQty) != 0) {
            throw ServiceExceptionUtil.exception(OP_TASK_REPORT_QTY_INVALID);
        }
    }

    /** 校验质量抽检数量：合格数 + 不合格数 = 抽检数。 */
    public static void validateQualityQty(BigDecimal sampleQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty) {
        if (sampleQty == null || qualifiedQty == null || unqualifiedQty == null
                || qualifiedQty.signum() < 0 || unqualifiedQty.signum() < 0
                || qualifiedQty.add(unqualifiedQty).compareTo(sampleQty) != 0) {
            throw ServiceExceptionUtil.exception(QUALITY_QTY_INVALID);
        }
    }

    /** 按 BOM 损耗率计算需求量：parentQty * qty / (1 - scrapRate)。 */
    public static BigDecimal requiredQty(BigDecimal parentQty, BigDecimal qty, BigDecimal scrapRate) {
        if (parentQty == null || qty == null || scrapRate == null || scrapRate.signum() < 0
                || scrapRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("数量和损耗率参数不合法");
        }
        return parentQty.multiply(qty).divide(BigDecimal.ONE.subtract(scrapRate), QTY_SCALE, RoundingMode.HALF_UP);
    }
}
