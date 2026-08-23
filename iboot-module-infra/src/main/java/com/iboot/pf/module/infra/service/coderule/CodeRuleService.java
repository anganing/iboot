package com.iboot.pf.module.infra.service.coderule;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRulePageReqVO;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleSaveReqVO;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeRuleDO;
import jakarta.validation.Valid;

public interface CodeRuleService {

    Long createCodeRule(@Valid CodeRuleSaveReqVO createReqVO);

    void updateCodeRule(@Valid CodeRuleSaveReqVO updateReqVO);

    void deleteCodeRule(Long id);

    CodeRuleDO getCodeRule(Long id);

    PageResult<CodeRuleDO> getCodeRulePage(CodeRulePageReqVO pageReqVO);

    String previewCode(String ruleCode);

    String generateCode(String ruleCode);

}
