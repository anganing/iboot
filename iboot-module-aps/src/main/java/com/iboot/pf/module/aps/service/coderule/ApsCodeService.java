package com.iboot.pf.module.aps.service.coderule;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** APS 编号生成服务，统一走 infra 编码规则。 */
@Service
public class ApsCodeService {

    @Resource
    private CodeRuleApi codeRuleApi;

    /**
     * 生成编号；调用方已指定编号时直接沿用。
     *
     * @param ruleCode 编码规则码，取值见 {@link com.iboot.pf.module.aps.enums.ApsCodeRuleConstants}
     * @param existed  调用方传入的编号，可为空
     * @return 编号
     */
    public String generate(String ruleCode, String existed) {
        return StrUtil.isNotBlank(existed) ? existed : codeRuleApi.generate(ruleCode);
    }

}
