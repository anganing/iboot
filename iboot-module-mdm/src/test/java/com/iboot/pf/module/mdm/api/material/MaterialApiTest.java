package com.iboot.pf.module.mdm.api.material;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.mat.MatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.MATERIAL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({MaterialApiImpl.class, MatServiceImpl.class})
class MaterialApiTest extends BaseDbUnitTest {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private MatMapper matMapper;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private UomMapper uomMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        matCatMapper.insert(MatCatDO.builder().id(1L).parentId(0L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).baseUomId(1L).code("M-001").name("物料A").status(0).build());
        matMapper.insert(MatDO.builder().id(2L).catId(1L).baseUomId(1L).code("M-002").name("物料B").status(0).build());
    }

    @Test
    void testGetMaterial_notExists_returnsNull() {
        assertNull(materialApi.getMaterial(999L));
    }

    @Test
    void testGetMaterial_exists_returnsDto() {
        MaterialRespDTO dto = materialApi.getMaterial(1L);
        assertEquals("M-001", dto.getCode());
    }

    @Test
    void testGetMaterialList_emptyCollection_returnsEmpty() {
        assertTrue(materialApi.getMaterialList(List.of()).isEmpty());
    }

    @Test
    void testGetMaterialList_byIds_returnsEnabled() {
        List<MaterialRespDTO> list = materialApi.getMaterialList(Set.of(1L, 2L));
        assertEquals(2, list.size());
    }

    @Test
    void testGetMaterialMap_missingId_hasNoKey() {
        Map<Long, MaterialRespDTO> map = materialApi.getMaterialMap(Set.of(1L, 999L));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(1L));
    }

    @Test
    void testGetMaterialListByCodes_returnsMatch() {
        List<MaterialRespDTO> list = materialApi.getMaterialListByCodes(Set.of("M-001"));
        assertEquals(1, list.size());
        assertEquals("物料A", list.get(0).getName());
    }

    @Test
    void testValidateMaterialList_missing_throws() {
        assertServiceException(() -> materialApi.validateMaterialList(Set.of(1L, 999L)), MATERIAL_NOT_EXISTS);
    }

    @Test
    void testValidateMaterialList_disabled_throws() {
        matMapper.insert(MatDO.builder().id(3L).catId(1L).baseUomId(1L).code("M-003").name("停用").status(1).build());
        assertServiceException(() -> materialApi.validateMaterialList(Set.of(1L, 3L)), MATERIAL_NOT_EXISTS);
    }
}
