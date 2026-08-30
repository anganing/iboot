package com.iboot.pf.module.mdm.api.bom;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.bom.dto.BomDetailRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApiImpl;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.bom.BomMapper;
import com.iboot.pf.module.mdm.dal.mysql.bomline.BomLineMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.service.bom.BomServiceImpl;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.mat.MatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({BomApiImpl.class, BomServiceImpl.class, MaterialApiImpl.class, MatServiceImpl.class})
class BomApiTest extends BaseDbUnitTest {

    @Resource
    private BomApi bomApi;
    @Resource
    private BomMapper bomMapper;
    @Resource
    private BomLineMapper bomLineMapper;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private UomMapper uomMapper;
    @Resource
    private MatMapper matMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).code("M-001").name("成品").baseUomId(1L).status(0).build());
        matMapper.insert(MatDO.builder().id(2L).catId(1L).code("M-002").name("子件").baseUomId(1L).status(0).build());
    }

    @Test
    void testGetBom_notExists_returnsNull() {
        assertNull(bomApi.getBom(999L));
    }

    @Test
    void testGetEffectiveBom_dateRule_selectsLatest() {
        // V1: 8/1-8/31；V2: 8/15-9/30，8/20 时应取 V2（effectiveDate 最新）
        bomMapper.insert(BomDO.builder().id(1L).matId(1L).version("V1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1))
                .expiryDate(LocalDate.of(2026, 8, 31)).build());
        bomMapper.insert(BomDO.builder().id(2L).matId(1L).version("V2")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 15))
                .expiryDate(LocalDate.of(2026, 9, 30)).build());

        BomRespDTO dto = bomApi.getEffectiveBom(1L, LocalDate.of(2026, 8, 20));
        assertEquals("V2", dto.getVersion());
    }

    @Test
    void testGetEffectiveBom_outOfRange_returnsNull() {
        bomMapper.insert(BomDO.builder().id(1L).matId(1L).version("V1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1))
                .expiryDate(LocalDate.of(2026, 8, 31)).build());
        assertNull(bomApi.getEffectiveBom(1L, LocalDate.of(2026, 9, 10)));
    }

    @Test
    void testGetEffectiveBom_disabledIgnored() {
        bomMapper.insert(BomDO.builder().id(1L).matId(1L).version("V1")
                .status(CommonStatusEnum.DISABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1)).build());
        assertNull(bomApi.getEffectiveBom(1L, LocalDate.of(2026, 8, 20)));
    }

    @Test
    void testGetBomDetail_containsLinesAndMat() {
        bomMapper.insert(BomDO.builder().id(1L).matId(1L).version("V1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1)).build());
        bomLineMapper.insert(BomLineDO.builder().id(1L).bomId(1L).lineNo(10).matId(2L)
                .matCode("M-002").matName("子件").qty(new BigDecimal("2")).uomId(1L).build());

        BomDetailRespDTO detail = bomApi.getBomDetail(1L);
        assertEquals("V1", detail.getVersion());
        assertEquals(1, detail.getLines().size());
        assertEquals("M-001", detail.getMat().getCode());
        assertEquals(2, detail.getLines().get(0).getQty().intValue());
    }

    @Test
    void testGetBomLineList_empty_returnsEmpty() {
        bomMapper.insert(BomDO.builder().id(1L).matId(1L).version("V1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1)).build());
        assertTrue(bomApi.getBomLineList(1L).isEmpty());
    }
}
