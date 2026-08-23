package com.iboot.pf.module.mdm.service.coderule;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** MDM 业务编码分配服务。 */
@Service
public class MdmCodeService {

    @Resource
    private CodeRuleApi codeRuleApi;

    /**
     * 保留用户手工输入的编码；为空时按指定规则分配。
     */
    public String resolveCode(String code, String ruleCode) {
        return StrUtil.isBlank(code) ? codeRuleApi.generate(ruleCode) : code;
    }

}
