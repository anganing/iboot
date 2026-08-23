package com.iboot.pf.module.infra.api.coderule;

/**
 * 编码规则 API。
 */
public interface CodeRuleApi {

    /**
     * 按规则编码生成一个编号。
     *
     * @param ruleCode 规则编码
     * @return 编号
     */
    String generate(String ruleCode);

}
