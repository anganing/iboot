package com.iboot.pf.module.mdm.dal.mysql.team;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.team.vo.TeamMemberRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 班组成员 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TeamMemberMapper extends BaseMapperX<TeamMemberDO> {

    default List<TeamMemberDO> selectListByTeamId(Long teamId) {
        return selectList(TeamMemberDO::getTeamId, teamId);
    }

    default TeamMemberDO selectByTeamIdAndUserId(Long teamId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<TeamMemberDO>()
                .eq(TeamMemberDO::getTeamId, teamId)
                .eq(TeamMemberDO::getUserId, userId));
    }

    default int deleteByTeamId(Long teamId) {
        return delete(TeamMemberDO::getTeamId, teamId);
    }

    default PageResult<TeamMemberRespVO> selectJoinPage(PageParam pageParam, Long teamId) {
        IPage<TeamMemberRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, teamId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<TeamMemberRespVO> selectJoinPage(IPage<TeamMemberRespVO> page, @Param("teamId") Long teamId);

    TeamMemberRespVO selectJoinById(@Param("id") Long id);

}
