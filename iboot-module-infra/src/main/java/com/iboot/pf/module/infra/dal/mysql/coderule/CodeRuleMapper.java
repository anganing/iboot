package com.iboot.pf.module.infra.dal.mysql.coderule;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRulePageReqVO;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeRuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeRuleMapper extends BaseMapperX<CodeRuleDO> {

    default CodeRuleDO selectByRuleCode(String ruleCode) {
        return selectOne(CodeRuleDO::getRuleCode, ruleCode);
    }

    @Select("SELECT * FROM infra_code_rule WHERE rule_code = #{ruleCode} AND deleted = 0 LIMIT 1 FOR UPDATE")
    CodeRuleDO selectByRuleCodeForUpdate(String ruleCode);

    @Select("SELECT * FROM infra_code_rule WHERE id = #{id} AND deleted = 0 LIMIT 1 FOR UPDATE")
    CodeRuleDO selectByIdForUpdate(Long id);

    default PageResult<CodeRuleDO> selectPage(CodeRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CodeRuleDO>()
                .likeIfPresent(CodeRuleDO::getRuleCode, reqVO.getRuleCode())
                .likeIfPresent(CodeRuleDO::getName, reqVO.getName())
                .eqIfPresent(CodeRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CodeRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CodeRuleDO::getId));
    }

}
