package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeMatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeOpDO;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeOpMapper;
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
    @Resource
    private DefectCodeMatCatMapper defectCodeMatCatMapper;
    @Resource
    private DefectCodeOpMapper defectCodeOpMapper;
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

    // ========== 可选缺陷码过滤（T2-06） ==========

    /**
     * 三个代码按适用范围区分：
     * 1 号限分类 100 可用；2 号限工序 200 可用；4 号两张关联表都无记录是通用码；3 号停用不出现。
     */
    @Test
    void testGetAvailableDefectCodeList_filterByMatCatAndOp() {
        defectCodeMapper.insert(DefectCodeDO.builder().id(4L).parentId(0L).code("DC-G").name("通用缺陷")
                .level("MAJOR").status(0).build());
        defectCodeMatCatMapper.insert(DefectCodeMatCatDO.builder().defectCodeId(1L).matCatId(100L).build());
        defectCodeOpMapper.insert(DefectCodeOpDO.builder().defectCodeId(2L).opId(200L).build());

        // 分类 100 + 工序 200：限分类命中的 1、限工序命中的 2、无限制的通用码 4 都可选
        List<DefectCodeRespDTO> available = defectCodeApi.getAvailableDefectCodeList(100L, 200L);
        assertEquals(3, available.size());
        assertTrue(available.stream().allMatch(dto -> dto.getId() != 3L));

        // 换一个分类：限分类 100 的 1 号不可选，通用码仍可选
        List<DefectCodeRespDTO> otherMatCat = defectCodeApi.getAvailableDefectCodeList(999L, null);
        assertTrue(otherMatCat.stream().noneMatch(dto -> dto.getId() == 1L));
        assertTrue(otherMatCat.stream().anyMatch(dto -> dto.getId() == 4L));

        // 换一个工序：限工序 200 的 2 号不可选，通用码仍可选
        List<DefectCodeRespDTO> otherOp = defectCodeApi.getAvailableDefectCodeList(null, 999L);
        assertTrue(otherOp.stream().noneMatch(dto -> dto.getId() == 2L));
        assertTrue(otherOp.stream().anyMatch(dto -> dto.getId() == 4L));

        // 两个维度都没给：受限代码一律不可选，只剩通用码（停用的 3 号始终不出现）
        List<DefectCodeRespDTO> noContext = defectCodeApi.getAvailableDefectCodeList(null, null);
        assertEquals(1, noContext.size());
        assertEquals(4L, noContext.get(0).getId());
    }
}
