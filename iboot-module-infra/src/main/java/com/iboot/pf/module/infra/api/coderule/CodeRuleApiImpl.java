package com.iboot.pf.module.infra.api.coderule;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.infra.service.coderule.CodeRuleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 编码规则 API 实现类
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：门面是给别的模块调的，
 * 取号动作与各业务模块登录用户的数据范围毫无关系，跨模块调用天生是系统身份而不是用户身份。
 * 打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class CodeRuleApiImpl implements CodeRuleApi {

    @Resource
    private CodeRuleService codeRuleService;

    @Override
    public String generate(String ruleCode) {
        return codeRuleService.generateCode(ruleCode);
    }

}
