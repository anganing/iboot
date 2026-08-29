package com.iboot.pf.module.mdm.service.team;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.team.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamDO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamMemberDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 班组 Service 接口
 *
 * @author iboot
 */
public interface TeamService {

    // ==================== 班组 ====================

    /**
     * 创建班组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTeam(@Valid TeamSaveReqVO createReqVO);

    /**
     * 更新班组
     *
     * @param updateReqVO 更新信息
     */
    void updateTeam(@Valid TeamSaveReqVO updateReqVO);

    /**
     * 删除班组（级联删除成员）
     *
     * @param id 编号
     */
    void deleteTeam(Long id);

    /**
     * 批量删除班组
     *
     * @param ids 编号列表
     */
    void deleteTeamListByIds(List<Long> ids);

    /**
     * 获得班组
     *
     * @param id 编号
     * @return 班组
     */
    TeamRespVO getTeam(Long id);

    /**
     * 获得班组分页
     *
     * @param pageReqVO 分页查询
     * @return 班组分页
     */
    PageResult<TeamRespVO> getTeamPage(TeamPageReqVO pageReqVO);

    // ==================== 班组成员 ====================

    /**
     * 创建班组成员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTeamMember(@Valid TeamMemberSaveReqVO createReqVO);

    /**
     * 删除班组成员
     *
     * @param id 编号
     */
    void deleteTeamMember(Long id);

    /**
     * 获得班组成员分页
     *
     * @param pageParam 分页参数
     * @param teamId 班组编号
     * @return 班组成员分页
     */
    PageResult<TeamMemberRespVO> getTeamMemberPage(PageParam pageParam, Long teamId);

    /**
     * 获得班组成员列表
     *
     * @param teamId 班组编号
     * @return 成员列表
     */
    List<TeamMemberDO> getTeamMemberListByTeamId(Long teamId);

}
