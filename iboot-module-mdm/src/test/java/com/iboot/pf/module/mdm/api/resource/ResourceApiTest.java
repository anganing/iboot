package com.iboot.pf.module.mdm.api.resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCalendarRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCapabilityRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescapability.ResCapabilityDO;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.rescalendar.ResCalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.rescapability.ResCapabilityMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.changeover.ResChangeoverServiceImpl;
import com.iboot.pf.module.mdm.service.res.ResServiceImpl;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarServiceImpl;
import com.iboot.pf.module.mdm.service.rescapability.ResCapabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ResourceApiImpl.class, ResServiceImpl.class, ResCalendarServiceImpl.class,
        ResCapabilityServiceImpl.class, ResChangeoverServiceImpl.class})
class ResourceApiTest extends BaseDbUnitTest {

    @Resource
    private ResourceApi resourceApi;
    @Resource
    private ResMapper resMapper;
    @Resource
    private ResCalendarMapper resCalendarMapper;
    @Resource
    private ResCapabilityMapper resCapabilityMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        resMapper.insert(ResDO.builder().id(1L).wcId(1L).resType("EQUIP").code("R-001").name("设备1").status(0).build());
        resMapper.insert(ResDO.builder().id(2L).wcId(1L).resType("EQUIP").code("R-002").name("设备2").status(0).build());
    }

    @Test
    void testGetResource_notExists_returnsNull() {
        assertNull(resourceApi.getResource(999L));
    }

    @Test
    void testGetResourceMap_missingId_hasNoKey() {
        Map<Long, ResourceRespDTO> map = resourceApi.getResourceMap(Set.of(1L, 999L));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(1L));
    }

    @Test
    void testGetResourceCalendarList_closedRange() {
        resCalendarMapper.insert(ResCalendarDO.builder().id(1L).resId(1L)
                .workDate(LocalDate.of(2026, 8, 20)).shiftId(1L).availableMinutes(480).build());
        resCalendarMapper.insert(ResCalendarDO.builder().id(2L).resId(1L)
                .workDate(LocalDate.of(2026, 8, 22)).shiftId(1L).availableMinutes(0).build());
        // 闭区间：含 20 与 22，不含 19 与 23
        List<ResourceCalendarRespDTO> list = resourceApi.getResourceCalendarList(
                Set.of(1L), LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 22));
        assertEquals(2, list.size());
    }

    @Test
    void testGetResourceCapabilityList_onlyEnabled() {
        resCapabilityMapper.insert(ResCapabilityDO.builder().id(1L).resId(1L).opId(10L).preferredFlag(true).status(0).build());
        resCapabilityMapper.insert(ResCapabilityDO.builder().id(2L).resId(2L).opId(10L).preferredFlag(false).status(1).build());
        List<ResourceCapabilityRespDTO> list = resourceApi.getResourceCapabilityList(Set.of(10L));
        // 停用能力被过滤，仅返回启用
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getResId());
    }
}
