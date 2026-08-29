package com.iboot.pf.module.mdm.dal.mysql.skill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.OpSkillReqRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.OpSkillReqDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工序技能要求 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OpSkillReqMapper extends BaseMapperX<OpSkillReqDO> {

    default List<OpSkillReqDO> selectListByOpId(Long opId) {
        return selectList(OpSkillReqDO::getOpId, opId);
    }

    default OpSkillReqDO selectByOpIdAndSkillId(Long opId, Long skillId) {
        return selectOne(new LambdaQueryWrapperX<OpSkillReqDO>()
                .eq(OpSkillReqDO::getOpId, opId)
                .eq(OpSkillReqDO::getSkillId, skillId));
    }

    default int deleteByOpId(Long opId) {
        return delete(OpSkillReqDO::getOpId, opId);
    }

    default PageResult<OpSkillReqRespVO> selectJoinPage(PageParam pageParam, Long opId) {
        IPage<OpSkillReqRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, opId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<OpSkillReqRespVO> selectJoinPage(IPage<OpSkillReqRespVO> page, @Param("opId") Long opId);

    OpSkillReqRespVO selectJoinById(@Param("id") Long id);

}
