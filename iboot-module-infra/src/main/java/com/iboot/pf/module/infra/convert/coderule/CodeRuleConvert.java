package com.iboot.pf.module.infra.convert.coderule;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleRespVO;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleSaveReqVO;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeRuleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CodeRuleConvert {

    CodeRuleConvert INSTANCE = Mappers.getMapper(CodeRuleConvert.class);

    CodeRuleDO convert(CodeRuleSaveReqVO bean);

    CodeRuleRespVO convert(CodeRuleDO bean);

    PageResult<CodeRuleRespVO> convertPage(PageResult<CodeRuleDO> page);

}
