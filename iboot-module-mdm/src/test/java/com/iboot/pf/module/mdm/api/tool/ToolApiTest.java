package com.iboot.pf.module.mdm.api.tool;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.tool.dto.RouteOpToolRespDTO;
import com.iboot.pf.module.mdm.api.tool.dto.ToolRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.RouteOpToolDO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.ToolDO;
import com.iboot.pf.module.mdm.dal.mysql.tool.RouteOpToolMapper;
import com.iboot.pf.module.mdm.dal.mysql.tool.ToolMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.tool.ToolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ToolApiImpl.class, ToolServiceImpl.class})
class ToolApiTest extends BaseDbUnitTest {

    @Resource
    private ToolApi toolApi;
    @Resource
    private ToolMapper toolMapper;
    @Resource
    private RouteOpToolMapper routeOpToolMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        toolMapper.insert(ToolDO.builder().id(1L).code("TL-001").name("夹具").toolType("FIXTURE").status(0).build());
        routeOpToolMapper.insert(RouteOpToolDO.builder().id(1L).routeOpId(10L).toolId(1L).qty(2).requiredFlag(true).build());
    }

    @Test
    void testGetToolList() {
        List<ToolRespDTO> list = toolApi.getToolList();
        assertEquals(1, list.size());
        assertEquals("TL-001", list.get(0).getCode());
    }

    @Test
    void testGetRouteOpToolList_byRouteOpIds() {
        List<RouteOpToolRespDTO> list = toolApi.getRouteOpToolList(Set.of(10L));
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getQty());
        assertEquals(true, list.get(0).getRequiredFlag());
    }

    @Test
    void testGetRouteOpToolList_empty_returnsEmpty() {
        assertTrue(toolApi.getRouteOpToolList(Set.of()).isEmpty());
    }
}
