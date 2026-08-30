package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.defectcode.DefectCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({DefectCodeApiImpl.class, DefectCodeServiceImpl.class})
class DefectCodeApiTest extends BaseDbUnitTest {

    @Resource
    private DefectCodeApi defectCodeApi;
    @Resource
    private DefectCodeMapper defectCodeMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        defectCodeMapper.insert(DefectCodeDO.builder().id(1L).parentId(0L).code("DC-01").name("外观")
                .level("MAJOR").status(0).build());
        defectCodeMapper.insert(DefectCodeDO.builder().id(2L).parentId(1L).code("DC-0101").name("划痕")
                .level("MINOR").status(0).build());
        defectCodeMapper.insert(DefectCodeDO.builder().id(3L).parentId(0L).code("DC-D").name("停用缺陷")
                .level("CRITICAL").status(1).build());
    }

    @Test
    void testGetDefectCodeList_containsDisabled() {
        List<DefectCodeRespDTO> list = defectCodeApi.getDefectCodeList();
        assertEquals(3, list.size());
    }

    @Test
    void testGetDefectCodeTree_onlyEnabled_andNested() {
        List<DefectCodeRespDTO> tree = defectCodeApi.getDefectCodeTree();
        // 顶级仅 1 个启用，停用顶级不出现
        assertEquals(1, tree.size());
        assertEquals("DC-01", tree.get(0).getCode());
        // 启用子级挂到父级下
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("DC-0101", tree.get(0).getChildren().get(0).getCode());
    }

    @Test
    void testGetDefectCodeTree_empty_returnsEmpty() {
        defectCodeMapper.deleteById(1L);
        defectCodeMapper.deleteById(2L);
        defectCodeMapper.deleteById(3L);
        assertTrue(defectCodeApi.getDefectCodeTree().isEmpty());
    }
}
