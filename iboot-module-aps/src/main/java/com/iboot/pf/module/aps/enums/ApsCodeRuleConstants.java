package com.iboot.pf.module.aps.enums;

/**
 * APS 编码规则码。
 *
 * <p>取值必须与 {@code infra_code_rule.rule_code} 完全一致，否则生成编号时会抛“编码规则不存在”。
 */
public interface ApsCodeRuleConstants {

    String SCENARIO = "APS_SCENARIO_CODE";

    String STRATEGY = "APS_STRATEGY_CODE";

    String TASK = "APS_TASK_NO";

}
