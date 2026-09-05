package com.iboot.pf.module.mdm.api.team;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.team.dto.TeamMemberRespDTO;
import com.iboot.pf.module.mdm.api.team.dto.TeamRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamDO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamMemberDO;
import com.iboot.pf.module.mdm.dal.mysql.team.TeamMapper;
import com.iboot.pf.module.mdm.service.team.TeamService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 班组 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class TeamApiImpl implements TeamApi {

    @Resource
    private TeamService teamService;
    @Resource
    private TeamMapper teamMapper;

    @Override
    public List<TeamRespDTO> getTeamListByWorkshopIds(Collection<Long> workshopIds) {
        List<TeamDO> list = teamService.getTeamListByWorkshopIds(workshopIds);
        return BeanUtils.toBean(list, TeamRespDTO.class);
    }

    @Override
    public Map<Long, TeamRespDTO> getTeamMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<TeamDO> list = teamMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, TeamRespDTO.class), TeamRespDTO::getId);
    }

    @Override
    public List<TeamMemberRespDTO> getTeamMemberList(Collection<Long> teamIds) {
        List<TeamMemberDO> list = teamService.getTeamMemberList(teamIds);
        return BeanUtils.toBean(list, TeamMemberRespDTO.class);
    }

}
