package com.iboot.pf.module.mdm.service.defectcode;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeMatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_MAT_CAT_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_OP_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import(DefectCodeServiceImpl.class)
class DefectCodeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DefectCodeService defectCodeService;
    @Resource
    private DefectCodeOpMapper defectCodeOpMapper;
    @Resource
    private DefectCodeMatCatMapper defectCodeMatCatMapper;
    @Resource
    private OpMapper opMapper;
    @Resource
    private MatCatMapper matCatMapper;

    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void createReferences() {
        when(mdmCodeService.resolveCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        opMapper.insert(OpDO.builder().id(1L).code("OP-001").name("打磨")
                .stdDuration(1).durationType("FIXED").status(0).build());
        opMapper.insert(OpDO.builder().id(2L).code("OP-002").name("喷涂")
                .stdDuration(1).durationType("FIXED").status(0).build());
        opMapper.insert(OpDO.builder().id(3L).code("OP-003").name("总装")
                .stdDuration(1).durationType("FIXED").status(0).build());
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT-01").name("钣金件").status(0).build());
        matCatMapper.insert(MatCatDO.builder().id(2L).code("CAT-02").name("成品").status(0).build());
    }

    @Test
    void testCreate_savesMultipleScopeRelations() {
        DefectCodeSaveReqVO reqVO = buildReqVO();
        reqVO.setOpIds(List.of(1L, 2L, 3L));
        reqVO.setMatCatIds(List.of(1L, 2L));

        Long id = defectCodeService.createDefectCode(reqVO);

        assertEquals(List.of(1L, 2L, 3L), selectOpIds(id));
        assertEquals(List.of(1L, 2L), selectMatCatIds(id));
    }

    @Test
    void testUpdate_replacesScopeRelations() {
        DefectCodeSaveReqVO createReqVO = buildReqVO();
        createReqVO.setOpIds(List.of(1L, 2L));
        createReqVO.setMatCatIds(List.of(1L));
        Long id = defectCodeService.createDefectCode(createReqVO);

        DefectCodeSaveReqVO updateReqVO = buildReqVO();
        updateReqVO.setId(id);
        updateReqVO.setOpIds(List.of(3L));
        updateReqVO.setMatCatIds(List.of(2L));
        defectCodeService.updateDefectCode(updateReqVO);

        assertEquals(List.of(3L), selectOpIds(id));
        assertEquals(List.of(2L), selectMatCatIds(id));
    }

    @Test
    void testCreate_emptyScopeMeansUnlimited() {
        Long id = defectCodeService.createDefectCode(buildReqVO());

        assertTrue(selectOpIds(id).isEmpty());
        assertTrue(selectMatCatIds(id).isEmpty());
    }

    @Test
    void testDelete_cascadesScopeRelations() {
        DefectCodeSaveReqVO reqVO = buildReqVO();
        reqVO.setOpIds(List.of(1L));
        reqVO.setMatCatIds(List.of(1L));
        Long id = defectCodeService.createDefectCode(reqVO);

        defectCodeService.deleteDefectCode(id);

        assertTrue(selectOpIds(id).isEmpty());
        assertTrue(selectMatCatIds(id).isEmpty());
    }

    @Test
    void testCreate_rejectsUnknownScopeReference() {
        DefectCodeSaveReqVO unknownOpReqVO = buildReqVO();
        unknownOpReqVO.setOpIds(List.of(999L));
        assertServiceException(() -> defectCodeService.createDefectCode(unknownOpReqVO), DEFECT_CODE_OP_NOT_EXISTS);

        DefectCodeSaveReqVO unknownCatReqVO = buildReqVO();
        unknownCatReqVO.setMatCatIds(List.of(999L));
        assertServiceException(() -> defectCodeService.createDefectCode(unknownCatReqVO),
                DEFECT_CODE_MAT_CAT_NOT_EXISTS);
    }

    private List<Long> selectOpIds(Long defectCodeId) {
        return defectCodeOpMapper.selectListByDefectCodeId(defectCodeId).stream()
                .map(DefectCodeOpDO::getOpId).sorted().toList();
    }

    private List<Long> selectMatCatIds(Long defectCodeId) {
        return defectCodeMatCatMapper.selectListByDefectCodeId(defectCodeId).stream()
                .map(DefectCodeMatCatDO::getMatCatId).sorted().toList();
    }

    private DefectCodeSaveReqVO buildReqVO() {
        DefectCodeSaveReqVO reqVO = new DefectCodeSaveReqVO();
        reqVO.setCode("DC-001");
        reqVO.setName("外观划伤");
        reqVO.setLevel("MINOR");
        reqVO.setParentId(0L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
