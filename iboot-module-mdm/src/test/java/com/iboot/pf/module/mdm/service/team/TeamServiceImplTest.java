package com.iboot.pf.module.mdm.service.team;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.team.vo.TeamMemberSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.team.vo.TeamSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.module.mdm.dal.mysql.workshop.WorkshopMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.TEAM_MEMBER_USER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Import(TeamServiceImpl.class)
class TeamServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TeamService teamService;
    @Resource
    private WorkshopMapper workshopMapper;

    @MockitoBean
    private MdmCodeService mdmCodeService;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @BeforeEach
    void createReferences() {
        when(mdmCodeService.resolveCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        workshopMapper.insert(WorkshopDO.builder().id(1L).plantId(1L).code("WS-1")
                .name("装配车间").status(0).build());
    }

    @Test
    void testCreateTeamMember_invalidUserRejected() {
        Long teamId = teamService.createTeam(buildTeamReqVO());

        // system 用户校验失败时异常直接冒泡，班组成员必须引用存在的 user_id
        doThrow(exception(TEAM_MEMBER_USER_NOT_EXISTS)).when(adminUserApi).validateUser(999L);
        TeamMemberSaveReqVO reqVO = new TeamMemberSaveReqVO();
        reqVO.setTeamId(teamId);
        reqVO.setUserId(999L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        assertServiceException(() -> teamService.createTeamMember(reqVO), TEAM_MEMBER_USER_NOT_EXISTS);
    }

    @Test
    void testCreateTeam_validReferences() {
        Long teamId = teamService.createTeam(buildTeamReqVO());
        assertNotNull(teamService.getTeam(teamId));
    }

    private TeamSaveReqVO buildTeamReqVO() {
        TeamSaveReqVO reqVO = new TeamSaveReqVO();
        reqVO.setCode("TEAM-001");
        reqVO.setName("装配一班");
        reqVO.setWorkshopId(1L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
