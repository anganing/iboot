package com.iboot.pf.module.mes.enums;

/**
 * MES 编码规则码。
 *
 * <p>取值必须与 {@code infra_code_rule.rule_code} 完全一致，否则生成单号时会抛“编码规则不存在”。
 */
public interface MesCodeRuleConstants {

    String PROD_DEMAND = "MES_DEMAND_NO";

    String PROD_ORDER = "MES_PROD_ORDER_NO";

    String WORK_ORDER = "MES_WORK_ORDER_NO";

    String OP_TASK = "MES_OP_TASK_NO";

    String DISPATCH_TASK = "MES_DISPATCH_NO";

    String KIT_CHECK = "MES_KIT_CHECK_NO";

    String MATERIAL_REQ = "MES_MATERIAL_REQ_NO";

    String PROD_REPORT = "MES_PROD_REPORT_NO";

    String REWORK = "MES_REWORK_NO";

    String QC_TASK = "MES_QC_TASK_NO";

    String QC_PLAN = "MES_QC_PLAN_CODE";

    String QC_RULE = "MES_QC_RULE_CODE";

    String DISPOSITION = "MES_DISPOSITION_NO";

    String WIP_TXN = "MES_WIP_TXN_NO";

    String RGV_TASK = "MES_RGV_TASK_NO";

}
