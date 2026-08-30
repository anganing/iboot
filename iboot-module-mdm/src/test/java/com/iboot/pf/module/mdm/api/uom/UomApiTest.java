package com.iboot.pf.module.mdm.api.uom;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.uom.dto.UomConvRespDTO;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.dal.mysql.uomconv.UomConvMapper;
import com.iboot.pf.module.mdm.service.uom.UomServiceImpl;
import com.iboot.pf.module.mdm.service.uomconv.UomConvServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({UomApiImpl.class, UomServiceImpl.class, UomConvServiceImpl.class})
class UomApiTest extends BaseDbUnitTest {

    @Resource
    private UomApi uomApi;
    @Resource
    private UomMapper uomMapper;
    @Resource
    private UomConvMapper uomConvMapper;

    @BeforeEach
    void setUp() {
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        uomMapper.insert(new UomDO().setId(2L).setCode("KG").setName("千克").setStatus(0));
        uomConvMapper.insert(UomConvDO.builder().id(1L).matId(100L).fromUomId(1L).toUomId(2L)
                .ratio(new BigDecimal("0.5")).build());
    }

    @Test
    void testGetUomList_returnsAll() {
        List<UomRespDTO> list = uomApi.getUomList();
        assertEquals(2, list.size());
    }

    @Test
    void testGetUomMap_missingId_hasNoKey() {
        Map<Long, UomRespDTO> map = uomApi.getUomMap(Set.of(1L, 999L));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(1L));
    }

    @Test
    void testGetUomConvList_emptyMatIds_returnsEmpty() {
        assertTrue(uomApi.getUomConvList(Set.of()).isEmpty());
    }

    @Test
    void testGetUomConvList_byMatIds_returnsMatch() {
        List<UomConvRespDTO> list = uomApi.getUomConvList(Set.of(100L));
        assertEquals(1, list.size());
        assertEquals(0, new BigDecimal("0.5").compareTo(list.get(0).getRatio()));
    }
}
