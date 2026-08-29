package com.iboot.pf.module.mdm.dal.mysql.skill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.SkillPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.SkillRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.SkillDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 技能 Mapper
 *
 * @author iboot
 */
@Mapper
public interface SkillMapper extends BaseMapperX<SkillDO> {

    default PageResult<SkillDO> selectPage(SkillPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SkillDO>()
                .eqIfPresent(SkillDO::getCode, reqVO.getCode())
                .likeIfPresent(SkillDO::getName, reqVO.getName())
                .eqIfPresent(SkillDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SkillDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SkillDO::getId));
    }

    default SkillDO selectByCode(String code) {
        return selectOne(SkillDO::getCode, code);
    }

}
