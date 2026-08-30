package com.iboot.pf.module.mdm.service.team;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.team.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamDO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamMemberDO;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;
import com.iboot.pf.module.mdm.dal.mysql.team.TeamMapper;
import com.iboot.pf.module.mdm.dal.mysql.team.TeamMemberMapper;
import com.iboot.pf.module.mdm.dal.mysql.workshop.WorkshopMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import com.iboot.pf.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.framework.common.util.collection.MapUtils.findAndThen;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.TEAM;

/**
 * 班组 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class TeamServiceImpl implements TeamService {

    @Resource
    private TeamMapper teamMapper;
    @Resource
    private TeamMemberMapper teamMemberMapper;
    @Resource
    private WorkshopMapper workshopMapper;
    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private MdmCodeService mdmCodeService;

    // ==================== 班组 ====================

    @Override
    public Long createTeam(TeamSaveReqVO createReqVO) {
        validateReferences(createReqVO.getWorkshopId(), createReqVO.getShiftId(), createReqVO.getLeaderUserId());
        TeamDO team = BeanUtils.toBean(createReqVO, TeamDO.class);
        team.setId(null);
        team.setCode(mdmCodeService.resolveCode(team.getCode(), TEAM));
        teamMapper.insert(team);
        return team.getId();
    }

    @Override
    public void updateTeam(TeamSaveReqVO updateReqVO) {
        validateTeamExists(updateReqVO.getId());
        validateReferences(updateReqVO.getWorkshopId(), updateReqVO.getShiftId(), updateReqVO.getLeaderUserId());
        TeamDO updateObj = BeanUtils.toBean(updateReqVO, TeamDO.class);
        teamMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeam(Long id) {
        validateTeamExists(id);
        teamMemberMapper.deleteByTeamId(id);
        teamMapper.deleteById(id);
    }

    @Override
    public void deleteTeamListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteTeam(id);
        }
    }

    private TeamDO validateTeamExists(Long id) {
        TeamDO team = teamMapper.selectById(id);
        if (team == null) {
            throw exception(TEAM_NOT_EXISTS);
        }
        return team;
    }

    private void validateReferences(Long workshopId, Long shiftId, Long leaderUserId) {
        WorkshopDO workshop = workshopMapper.selectById(workshopId);
        if (workshop == null) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
        if (shiftId != null && shiftMapper.selectById(shiftId) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }
        if (leaderUserId != null) {
            adminUserApi.validateUser(leaderUserId);
        }
    }

    @Override
    public TeamRespVO getTeam(Long id) {
        TeamRespVO team = teamMapper.selectJoinById(id);
        if (team != null) {
            fillLeaderUserName(Collections.singletonList(team));
        }
        return team;
    }

    @Override
    public PageResult<TeamRespVO> getTeamPage(TeamPageReqVO pageReqVO) {
        PageResult<TeamRespVO> pageResult = teamMapper.selectJoinPage(pageReqVO);
        fillLeaderUserName(pageResult.getList());
        return pageResult;
    }

    /**
     * 批量补全班组长姓名。system_users 属 system 模块，禁止跨模块 JOIN，
     * 统一走 AdminUserApi 门面按 id 批量查询，保证将来微服务拆分时无需改动。
     */
    private void fillLeaderUserName(List<TeamRespVO> list) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, TeamRespVO::getLeaderUserId));
        list.forEach(vo -> findAndThen(userMap, vo.getLeaderUserId(),
                user -> vo.setLeaderUserName(user.getNickname())));
    }

    /**
     * 批量补全班组成员姓名，约束同 {@link #fillLeaderUserName(List)}
     */
    private void fillMemberUserName(List<TeamMemberRespVO> list) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, TeamMemberRespVO::getUserId));
        list.forEach(vo -> findAndThen(userMap, vo.getUserId(),
                user -> vo.setUserName(user.getNickname())));
    }

    // ==================== 班组成员 ====================

    @Override
    public Long createTeamMember(TeamMemberSaveReqVO createReqVO) {
        validateTeamExists(createReqVO.getTeamId());
        adminUserApi.validateUser(createReqVO.getUserId());
        validateMemberUnique(createReqVO.getTeamId(), createReqVO.getUserId(), null);
        TeamMemberDO member = BeanUtils.toBean(createReqVO, TeamMemberDO.class);
        member.setId(null);
        teamMemberMapper.insert(member);
        return member.getId();
    }

    @Override
    public void updateTeamMember(TeamMemberSaveReqVO updateReqVO) {
        validateTeamMemberExists(updateReqVO.getId());
        validateTeamExists(updateReqVO.getTeamId());
        adminUserApi.validateUser(updateReqVO.getUserId());
        validateMemberUnique(updateReqVO.getTeamId(), updateReqVO.getUserId(), updateReqVO.getId());
        teamMemberMapper.updateById(BeanUtils.toBean(updateReqVO, TeamMemberDO.class));
    }

    @Override
    public void deleteTeamMember(Long id) {
        validateTeamMemberExists(id);
        teamMemberMapper.deleteById(id);
    }

    @Override
    public void deleteTeamMemberListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteTeamMember(id);
        }
    }

    private void validateTeamMemberExists(Long id) {
        if (teamMemberMapper.selectById(id) == null) {
            throw exception(TEAM_MEMBER_NOT_EXISTS);
        }
    }

    private void validateMemberUnique(Long teamId, Long userId, Long excludeId) {
        TeamMemberDO existing = teamMemberMapper.selectByTeamIdAndUserId(teamId, userId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(TEAM_MEMBER_DUPLICATE);
        }
    }

    @Override
    public TeamMemberRespVO getTeamMember(Long id) {
        TeamMemberRespVO member = teamMemberMapper.selectJoinById(id);
        if (member != null) {
            fillMemberUserName(Collections.singletonList(member));
        }
        return member;
    }

    @Override
    public PageResult<TeamMemberRespVO> getTeamMemberPage(PageParam pageParam, Long teamId) {
        PageResult<TeamMemberRespVO> pageResult = teamMemberMapper.selectJoinPage(pageParam, teamId);
        fillMemberUserName(pageResult.getList());
        return pageResult;
    }

    @Override
    public List<TeamMemberDO> getTeamMemberListByTeamId(Long teamId) {
        return teamMemberMapper.selectListByTeamId(teamId);
    }

    @Override
    public List<TeamDO> getTeamListByWorkshopIds(Collection<Long> workshopIds) {
        if (CollUtil.isEmpty(workshopIds)) {
            return List.of();
        }
        return teamMapper.selectList(TeamDO::getWorkshopId, workshopIds);
    }

    @Override
    public List<TeamMemberDO> getTeamMemberList(Collection<Long> teamIds) {
        if (CollUtil.isEmpty(teamIds)) {
            return List.of();
        }
        return teamMemberMapper.selectList(TeamMemberDO::getTeamId, teamIds);
    }

}
