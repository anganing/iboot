package com.iboot.pf.module.infra.api.coderule;

import com.iboot.pf.module.infra.service.coderule.CodeRuleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class CodeRuleApiImpl implements CodeRuleApi {

    @Resource
    private CodeRuleService codeRuleService;

    @Override
    public String generate(String ruleCode) {
        return codeRuleService.generateCode(ruleCode);
    }

}
