package com.iboot.pf.module.mdm.dal.mysql.team;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.team.vo.TeamPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.team.vo.TeamRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 班组 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TeamMapper extends BaseMapperX<TeamDO> {

    default PageResult<TeamRespVO> selectJoinPage(TeamPageReqVO reqVO) {
        IPage<TeamRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TeamRespVO selectJoinById(@Param("id") Long id);

    IPage<TeamRespVO> selectJoinPage(IPage<TeamRespVO> page, @Param("req") TeamPageReqVO reqVO);

    default TeamDO selectByCode(String code) {
        return selectOne(TeamDO::getCode, code);
    }

}
