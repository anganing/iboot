package com.iboot.pf.module.mdm.api.team;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.team.dto.TeamMemberRespDTO;
import com.iboot.pf.module.mdm.api.team.dto.TeamRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamDO;
import com.iboot.pf.module.mdm.dal.dataobject.team.TeamMemberDO;
import com.iboot.pf.module.mdm.dal.mysql.team.TeamMapper;
import com.iboot.pf.module.mdm.dal.mysql.team.TeamMemberMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.team.TeamServiceImpl;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({TeamApiImpl.class, TeamServiceImpl.class})
class TeamApiTest extends BaseDbUnitTest {

    @Resource
    private TeamApi teamApi;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private TeamMemberMapper teamMemberMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @BeforeEach
    void setUp() {
        teamMapper.insert(TeamDO.builder().id(1L).code("TM-001").name("班组").workshopId(10L).status(0).build());
        teamMemberMapper.insert(TeamMemberDO.builder().id(1L).teamId(1L).userId(100L).status(0).build());
    }

    @Test
    void testGetTeamListByWorkshopIds_returnsMatch() {
        List<TeamRespDTO> list = teamApi.getTeamListByWorkshopIds(Set.of(10L));
        assertEquals(1, list.size());
        assertEquals("TM-001", list.get(0).getCode());
    }

    @Test
    void testGetTeamListByWorkshopIds_empty_returnsEmpty() {
        assertTrue(teamApi.getTeamListByWorkshopIds(Set.of()).isEmpty());
    }

    @Test
    void testGetTeamMemberList_returnsMatch() {
        List<TeamMemberRespDTO> list = teamApi.getTeamMemberList(Set.of(1L));
        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).getUserId());
    }

    @Test
    void testGetTeamMemberList_empty_returnsEmpty() {
        assertTrue(teamApi.getTeamMemberList(Set.of()).isEmpty());
    }
}
