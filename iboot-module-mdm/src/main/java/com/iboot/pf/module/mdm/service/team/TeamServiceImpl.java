package com.iboot.pf.module.mdm.service.team;

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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
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
        return teamMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TeamRespVO> getTeamPage(TeamPageReqVO pageReqVO) {
        return teamMapper.selectJoinPage(pageReqVO);
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
    public void deleteTeamMember(Long id) {
        if (teamMemberMapper.selectById(id) == null) {
            throw exception(TEAM_MEMBER_NOT_EXISTS);
        }
        teamMemberMapper.deleteById(id);
    }

    private void validateMemberUnique(Long teamId, Long userId, Long excludeId) {
        TeamMemberDO existing = teamMemberMapper.selectByTeamIdAndUserId(teamId, userId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(TEAM_MEMBER_DUPLICATE);
        }
    }

    @Override
    public PageResult<TeamMemberRespVO> getTeamMemberPage(PageParam pageParam, Long teamId) {
        return teamMemberMapper.selectJoinPage(pageParam, teamId);
    }

    @Override
    public List<TeamMemberDO> getTeamMemberListByTeamId(Long teamId) {
        return teamMemberMapper.selectListByTeamId(teamId);
    }

}
