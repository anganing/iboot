package com.iboot.pf.module.mdm.service.mat;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.MAT_SAFETY_STOCK_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import(MatServiceImpl.class)
class MatServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MatService matService;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private UomMapper uomMapper;

    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void createReferences() {
        when(mdmCodeService.resolveCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
    }

    @Test
    void testCreateMat_mtsRequiresSafetyStock() {
        MatSaveReqVO reqVO = buildMatReqVO();
        reqVO.setPlanStrategy("MTS");
        reqVO.setSafetyStock(null);
        assertServiceException(() -> matService.createMat(reqVO), MAT_SAFETY_STOCK_REQUIRED);

        reqVO.setSafetyStock(new BigDecimal("100.000000"));
        matService.createMat(reqVO);
    }

    @Test
    void testCreateMat_transportDimensionsSaved() {
        MatSaveReqVO reqVO = buildMatReqVO();
        reqVO.setLength(new BigDecimal("100"));
        reqVO.setWidth(new BigDecimal("50"));
        reqVO.setHeight(new BigDecimal("30"));
        reqVO.setVolume(new BigDecimal("150000"));
        Long id = matService.createMat(reqVO);

        assertEquals(0, reqVO.getLength().compareTo(matService.getMat(id).getLength()));
        assertEquals(0, reqVO.getHeight().compareTo(matService.getMat(id).getHeight()));
    }

    private MatSaveReqVO buildMatReqVO() {
        MatSaveReqVO reqVO = new MatSaveReqVO();
        reqVO.setCatId(1L);
        reqVO.setCode("MAT-001");
        reqVO.setName("螺栓");
        reqVO.setSpec("M8");
        reqVO.setMatType("RAW");
        reqVO.setBaseUomId(1L);
        reqVO.setBatchFlag(false);
        reqVO.setSnFlag(false);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
