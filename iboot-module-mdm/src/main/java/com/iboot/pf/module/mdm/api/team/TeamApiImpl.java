package com.iboot.pf.module.mdm.api.team;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.team.dto.TeamMemberRespDTO;
import com.iboot.pf.module.mdm.api.team.dto.TeamRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamDO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamMemberDO;
import com.iboot.pf.module.mdm.service.team.TeamService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 班组 API 实现类
 *
 * @author iboot
 */
@Service
public class TeamApiImpl implements TeamApi {

    @Resource
    private TeamService teamService;

    @Override
    @DataPermission(enable = false)
    public List<TeamRespDTO> getTeamListByWorkshopIds(Collection<Long> workshopIds) {
        List<TeamDO> list = teamService.getTeamListByWorkshopIds(workshopIds);
        return BeanUtils.toBean(list, TeamRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<TeamMemberRespDTO> getTeamMemberList(Collection<Long> teamIds) {
        List<TeamMemberDO> list = teamService.getTeamMemberList(teamIds);
        return BeanUtils.toBean(list, TeamMemberRespDTO.class);
    }

}
