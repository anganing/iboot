package com.iboot.pf.module.mes.service;

import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_REPORT_QTY_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QUALITY_QTY_INVALID;

/** MES 领域不变量。规则集中定义，供各领域 Service 复用。 */
public final class MesBusinessRules {

    private MesBusinessRules() {
    }

    /** 返回状态迁移后的状态；支持 PENDING/RUNNING/PAUSED 三类非终态。 */
    public static String nextOpTaskStatus(String currentStatus, String action) {
        String target = switch (currentStatus + ":" + action) {
            case "PENDING:start", "PAUSED:start" -> "RUNNING";
            case "PENDING:skip" -> "SKIPPED";
            case "RUNNING:pause" -> "PAUSED";
            case "RUNNING:finish", "PAUSED:finish" -> "DONE";
            default -> null;
        };
        if (target == null) {
            throw ServiceExceptionUtil.exception(OP_TASK_STATUS_INVALID);
        }
        return target;
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
        return parentQty.multiply(qty).divide(BigDecimal.ONE.subtract(scrapRate), 6, RoundingMode.HALF_UP);
    }
}
