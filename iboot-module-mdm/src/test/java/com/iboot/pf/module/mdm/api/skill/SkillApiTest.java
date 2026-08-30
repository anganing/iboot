package com.iboot.pf.module.mdm.api.skill;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.skill.dto.MemberSkillRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.OpSkillReqRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.SkillRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.MemberSkillDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.OpSkillReqDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.SkillDO;
import com.iboot.pf.module.mdm.dal.mysql.skill.MemberSkillMapper;
import com.iboot.pf.module.mdm.dal.mysql.skill.OpSkillReqMapper;
import com.iboot.pf.module.mdm.dal.mysql.skill.SkillMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.skill.SkillServiceImpl;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({SkillApiImpl.class, SkillServiceImpl.class})
class SkillApiTest extends BaseDbUnitTest {

    @Resource
    private SkillApi skillApi;
    @Resource
    private SkillMapper skillMapper;
    @Resource
    private OpSkillReqMapper opSkillReqMapper;
    @Resource
    private MemberSkillMapper memberSkillMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @BeforeEach
    void setUp() {
        skillMapper.insert(SkillDO.builder().id(1L).code("SK-001").name("焊接").status(0).build());
        opSkillReqMapper.insert(OpSkillReqDO.builder().id(1L).opId(10L).skillId(1L).minLevel(2).status(0).build());
        memberSkillMapper.insert(MemberSkillDO.builder().id(1L).userId(100L).skillId(1L).level(3)
                .effectiveDate(LocalDate.of(2026, 1, 1)).expiryDate(LocalDate.of(2026, 12, 31)).status(0).build());
    }

    @Test
    void testGetSkillList() {
        List<SkillRespDTO> list = skillApi.getSkillList();
        assertEquals(1, list.size());
        assertEquals("SK-001", list.get(0).getCode());
    }

    @Test
    void testGetOpSkillReqList_byOpIds() {
        List<OpSkillReqRespDTO> list = skillApi.getOpSkillReqList(Set.of(10L));
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getMinLevel());
    }

    @Test
    void testGetOpSkillReqList_empty_returnsEmpty() {
        assertTrue(skillApi.getOpSkillReqList(Set.of()).isEmpty());
    }

    @Test
    void testGetMemberSkillList_byUserIds() {
        List<MemberSkillRespDTO> list = skillApi.getMemberSkillList(Set.of(100L));
        assertEquals(1, list.size());
        assertEquals(3, list.get(0).getLevel());
    }

    @Test
    void testGetMemberSkillList_empty_returnsEmpty() {
        assertTrue(skillApi.getMemberSkillList(Set.of()).isEmpty());
    }
}
