package com.iboot.pf.module.mdm.api.partner;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.partner.dto.CustomerRespDTO;
import com.iboot.pf.module.mdm.api.partner.dto.SupplierRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.customer.CustomerDO;
import com.iboot.pf.module.mdm.dal.dataobject.supplier.SupplierDO;
import com.iboot.pf.module.mdm.dal.mysql.customer.CustomerMapper;
import com.iboot.pf.module.mdm.dal.mysql.supplier.SupplierMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.customer.CustomerServiceImpl;
import com.iboot.pf.module.mdm.service.supplier.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({PartnerApiImpl.class, SupplierServiceImpl.class, CustomerServiceImpl.class})
class PartnerApiTest extends BaseDbUnitTest {

    @Resource
    private PartnerApi partnerApi;
    @Resource
    private SupplierMapper supplierMapper;
    @Resource
    private CustomerMapper customerMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        supplierMapper.insert(SupplierDO.builder().id(1L).code("S-001").name("供应商").status(0).build());
        customerMapper.insert(CustomerDO.builder().id(1L).code("C-001").name("客户").status(0).build());
    }

    @Test
    void testGetSupplierList() {
        List<SupplierRespDTO> list = partnerApi.getSupplierList();
        assertEquals(1, list.size());
        assertEquals("S-001", list.get(0).getCode());
    }

    @Test
    void testGetSupplierMap_missingId_hasNoKey() {
        Map<Long, SupplierRespDTO> map = partnerApi.getSupplierMap(Set.of(1L, 999L));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(1L));
    }

    @Test
    void testGetCustomerList() {
        List<CustomerRespDTO> list = partnerApi.getCustomerList();
        assertEquals(1, list.size());
        assertEquals("C-001", list.get(0).getCode());
    }

    @Test
    void testGetCustomerMap_missingId_hasNoKey() {
        Map<Long, CustomerRespDTO> map = partnerApi.getCustomerMap(Set.of(1L, 999L));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(1L));
    }
}
