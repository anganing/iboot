package com.iboot.pf.module.mdm.api.team;

import com.iboot.pf.module.mdm.api.team.dto.TeamMemberRespDTO;
import com.iboot.pf.module.mdm.api.team.dto.TeamRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 班组 API 接口
 *
 * <p>对 MES 提供班组与成员门面。
 *
 * @author iboot
 */
public interface TeamApi {

    /**
     * 按车间编号集合批量查询班组
     *
     * @param workshopIds 车间编号集合
     * @return 班组列表；空集合返回空列表
     */
    List<TeamRespDTO> getTeamListByWorkshopIds(Collection<Long> workshopIds);

    /**
     * 按编号批量查询班组 Map，供批量补全
     *
     * @param ids 班组编号集合
     * @return 班组编号 -> 班组；缺失编号不产生键
     */
    Map<Long, TeamRespDTO> getTeamMap(Collection<Long> ids);

    /**
     * 按班组编号集合批量查询班组成员
     *
     * @param teamIds 班组编号集合
     * @return 成员列表；空集合返回空列表
     */
    List<TeamMemberRespDTO> getTeamMemberList(Collection<Long> teamIds);

}
