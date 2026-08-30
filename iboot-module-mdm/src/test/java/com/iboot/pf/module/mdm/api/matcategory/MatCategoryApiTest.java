package com.iboot.pf.module.mdm.api.matcategory;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.matcategory.dto.MatCategoryRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.matcat.MatCatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({MatCategoryApiImpl.class, MatCatServiceImpl.class})
class MatCategoryApiTest extends BaseDbUnitTest {

    @Resource
    private MatCategoryApi matCategoryApi;
    @Resource
    private MatCatMapper matCatMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        matCatMapper.insert(MatCatDO.builder().id(1L).parentId(0L).code("CAT-A").name("顶级A").status(0).build());
        matCatMapper.insert(MatCatDO.builder().id(2L).parentId(1L).code("CAT-A1").name("子级A1").status(0).build());
        matCatMapper.insert(MatCatDO.builder().id(3L).parentId(0L).code("CAT-D").name("停用顶级").status(1).build());
    }

    @Test
    void testGetMatCategoryTree_onlyEnabled_andNested() {
        List<MatCategoryRespDTO> tree = matCategoryApi.getMatCategoryTree();
        // 顶级仅 1 个启用节点，停用节点不出现
        assertEquals(1, tree.size());
        assertEquals("CAT-A", tree.get(0).getCode());
        // 启用子级挂到父级下
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("CAT-A1", tree.get(0).getChildren().get(0).getCode());
    }

    @Test
    void testGetMatCategoryList_containsDisabled() {
        List<MatCategoryRespDTO> list = matCategoryApi.getMatCategoryList();
        assertEquals(3, list.size());
    }

    @Test
    void testGetMatCategoryTree_empty_returnsEmpty() {
        matCatMapper.deleteById(1L);
        matCatMapper.deleteById(2L);
        matCatMapper.deleteById(3L);
        assertTrue(matCategoryApi.getMatCategoryTree().isEmpty());
    }
}
