package com.iboot.pf.module.mdm.service.bom;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.bom.BomMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.LocalDate;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.BOM_PUBLISH_WITHOUT_LINE;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.BOM_PUBLISHED_LOCKED;

@Import(BomServiceImpl.class)
class BomServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BomService bomService;
    @Resource
    private BomMapper bomMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private UomMapper uomMapper;

    @Test
    void testPublishBom_withoutLine() {
        BomDO bom = createDraftBom();
        BomSaveReqVO reqVO = buildBomReqVO(bom, "PUBLISHED");

        assertServiceException(() -> bomService.updateBom(reqVO), BOM_PUBLISH_WITHOUT_LINE);
    }

    @Test
    void testPublishedBom_cannotUpdateOrDelete() {
        BomDO bom = createDraftBom();
        BomSaveReqVO publishReqVO = buildBomReqVO(bom, "DRAFT");
        bomService.createBomLine(com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO.builder()
                .bomId(bom.getId()).lineNo(10).matId(bom.getMatId()).uomId(1L)
                .qty(java.math.BigDecimal.ONE).build());
        publishReqVO.setStatus("PUBLISHED");
        bomService.updateBom(publishReqVO);

        assertServiceException(() -> bomService.deleteBom(bom.getId()), BOM_PUBLISHED_LOCKED);
    }

    private BomDO createDraftBom() {
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).code("MAT-001").name("物料")
                .baseUomId(1L).status(0).build());
        Long id = bomService.createBom(buildBomReqVO(null, "DRAFT"));
        return bomMapper.selectById(id);
    }

    private BomSaveReqVO buildBomReqVO(BomDO bom, String status) {
        BomSaveReqVO reqVO = new BomSaveReqVO();
        if (bom != null) {
            reqVO.setId(bom.getId());
        }
        reqVO.setMatId(1L);
        reqVO.setVersion("V1");
        reqVO.setStatus(status);
        reqVO.setEffectiveDate(LocalDate.of(2026, 8, 23));
        return reqVO;
    }
}
