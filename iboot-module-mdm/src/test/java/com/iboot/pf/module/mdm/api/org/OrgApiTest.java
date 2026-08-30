package com.iboot.pf.module.mdm.api.org;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WcRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WorkshopRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.module.mdm.dal.mysql.invorg.InvOrgMapper;
import com.iboot.pf.module.mdm.dal.mysql.plant.PlantMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.dal.mysql.workshop.WorkshopMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.invorg.InvOrgServiceImpl;
import com.iboot.pf.module.mdm.service.plant.PlantServiceImpl;
import com.iboot.pf.module.mdm.service.wc.WcServiceImpl;
import com.iboot.pf.module.mdm.service.workshop.WorkshopServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({OrgApiImpl.class, InvOrgServiceImpl.class, PlantServiceImpl.class,
        WorkshopServiceImpl.class, WcServiceImpl.class})
class OrgApiTest extends BaseDbUnitTest {

    @Resource
    private OrgApi orgApi;
    @Resource
    private InvOrgMapper invOrgMapper;
    @Resource
    private PlantMapper plantMapper;
    @Resource
    private WorkshopMapper workshopMapper;
    @Resource
    private WcMapper wcMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        invOrgMapper.insert(InvOrgDO.builder().id(1L).code("IO-001").name("库存组织").status(0).build());
        plantMapper.insert(PlantDO.builder().id(1L).invOrgId(1L).code("PL-001").name("工厂").status(0).build());
        workshopMapper.insert(WorkshopDO.builder().id(1L).plantId(1L).code("WS-001").name("车间").status(0).build());
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-001").name("工作中心").status(0).build());
    }

    @Test
    void testGetInvOrgList() {
        List<InvOrgRespDTO> list = orgApi.getInvOrgList();
        assertEquals(1, list.size());
        assertEquals("IO-001", list.get(0).getCode());
    }

    @Test
    void testGetPlantList() {
        List<PlantRespDTO> list = orgApi.getPlantList();
        assertEquals(1, list.size());
        assertEquals("PL-001", list.get(0).getCode());
    }

    @Test
    void testGetWorkshopList() {
        List<WorkshopRespDTO> list = orgApi.getWorkshopList();
        assertEquals(1, list.size());
        assertEquals("WS-001", list.get(0).getCode());
    }

    @Test
    void testGetWcList() {
        List<WcRespDTO> list = orgApi.getWcList();
        assertEquals(1, list.size());
        assertEquals("WC-001", list.get(0).getCode());
    }
}
