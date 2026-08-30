package com.iboot.pf.module.wms.service.coderule;

import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** WMS 单号生成服务，统一走 infra 编码规则。 */
@Service
public class WmsCodeService {

    @Resource
    private CodeRuleApi codeRuleApi;

    /**
     * 生成单号；调用方已指定编号时直接沿用。
     *
     * @param ruleCode 编码规则码
     * @param existed  调用方传入的编号，可为空
     * @return 单号
     */
    public String generate(String ruleCode, String existed) {
        return existed != null && !existed.isEmpty() ? existed : codeRuleApi.generate(ruleCode);
    }

}
