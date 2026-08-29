package com.iboot.pf.module.mdm.dal.mysql.skill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.MemberSkillRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.MemberSkillDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 人员技能 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MemberSkillMapper extends BaseMapperX<MemberSkillDO> {

    default MemberSkillDO selectByUserIdAndSkillId(Long userId, Long skillId) {
        return selectOne(new LambdaQueryWrapperX<MemberSkillDO>()
                .eq(MemberSkillDO::getUserId, userId)
                .eq(MemberSkillDO::getSkillId, skillId));
    }

    default PageResult<MemberSkillRespVO> selectJoinPage(PageParam pageParam, Long userId, Long skillId) {
        IPage<MemberSkillRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, userId, skillId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<MemberSkillRespVO> selectJoinPage(IPage<MemberSkillRespVO> page,
                                            @Param("userId") Long userId,
                                            @Param("skillId") Long skillId);

    MemberSkillRespVO selectJoinById(@Param("id") Long id);

}
