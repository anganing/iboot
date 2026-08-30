package com.iboot.pf.module.mdm.api.route;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.file.FileApi;
import com.iboot.pf.module.mdm.api.material.MaterialApiImpl;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpEsopRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopDO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopFileDO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopFileMapper;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.esop.RouteOpEsopServiceImpl;
import com.iboot.pf.module.mdm.service.mat.MatServiceImpl;
import com.iboot.pf.module.mdm.service.route.RouteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import({RouteApiImpl.class, RouteServiceImpl.class, RouteOpEsopServiceImpl.class,
        MaterialApiImpl.class, MatServiceImpl.class})
class RouteApiTest extends BaseDbUnitTest {

    @Resource
    private RouteApi routeApi;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private UomMapper uomMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private RouteMapper routeMapper;
    @Resource
    private RouteOpMapper routeOpMapper;
    @Resource
    private RouteOpMatMapper routeOpMatMapper;
    @Resource
    private RouteOpEsopMapper routeOpEsopMapper;
    @Resource
    private RouteOpEsopFileMapper routeOpEsopFileMapper;
    @MockitoBean
    private FileApi fileApi;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).code("M-001").name("成品").baseUomId(1L).status(0).build());
        when(fileApi.presignGetUrl(anyString(), org.mockito.ArgumentMatchers.isNull()))
                .thenAnswer(inv -> "presigned-" + inv.getArgument(0));
    }

    @Test
    void testGetRoute_notExists_returnsNull() {
        assertNull(routeApi.getRoute(999L));
    }

    @Test
    void testGetEffectiveRoute_takesLatest() {
        routeMapper.insert(RouteDO.builder().id(1L).matId(1L).version("R1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1)).expiryDate(LocalDate.of(2026, 8, 31)).build());
        routeMapper.insert(RouteDO.builder().id(2L).matId(1L).version("R2")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 15)).expiryDate(LocalDate.of(2026, 9, 30)).build());
        RouteRespDTO dto = routeApi.getEffectiveRoute(1L, LocalDate.of(2026, 8, 20));
        assertEquals("R2", dto.getVersion());
    }

    @Test
    void testGetRouteOpList_orderedBySeq() {
        routeMapper.insert(RouteDO.builder().id(1L).matId(1L).version("R1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .effectiveDate(LocalDate.of(2026, 8, 1)).build());
        routeOpMapper.insert(RouteOpDO.builder().id(1L).routeId(1L).seq(20).opId(1L).wcId(1L).keyFlag(false).faiFlag(false).build());
        routeOpMapper.insert(RouteOpDO.builder().id(2L).routeId(1L).seq(10).opId(1L).wcId(1L).keyFlag(false).faiFlag(false).build());
        List<RouteOpRespDTO> ops = routeApi.getRouteOpList(1L);
        assertEquals(2, ops.size());
        assertEquals(10, ops.get(0).getSeq());
        assertEquals(20, ops.get(1).getSeq());
    }

    @Test
    void testGetRouteOpMatList_multipleOps_once() {
        routeOpMatMapper.insert(RouteOpMatDO.builder().id(1L).routeOpId(1L).matId(1L).qty(new BigDecimal("1")).uomId(1L).build());
        routeOpMatMapper.insert(RouteOpMatDO.builder().id(2L).routeOpId(2L).matId(1L).qty(new BigDecimal("2")).uomId(1L).build());
        List<RouteOpMatRespDTO> list = routeApi.getRouteOpMatList(Set.of(1L, 2L));
        assertEquals(2, list.size());
    }

    @Test
    void testGetRouteOpMatList_empty_returnsEmpty() {
        assertTrue(routeApi.getRouteOpMatList(Set.of()).isEmpty());
    }

    @Test
    void testGetRouteOpEsopList_presignsFileUrl() {
        routeOpEsopMapper.insert(RouteOpEsopDO.builder().id(1L).routeOpId(1L).seq(10).title("装配")
                .status(CommonStatusEnum.ENABLE.getStatus()).build());
        routeOpEsopFileMapper.insert(RouteOpEsopFileDO.builder().id(1L).esopId(1L).seq(1)
                .fileType("IMAGE").fileName("图.png").fileUrl("http://obj/raw/图.png").build());

        List<RouteOpEsopRespDTO> list = routeApi.getRouteOpEsopList(Set.of(1L));
        assertEquals(1, list.size());
        assertEquals("装配", list.get(0).getTitle());
        // 附件 fileUrl 已被预签名替换，且保留 fileName/fileType/fileId
        assertEquals("presigned-http://obj/raw/图.png", list.get(0).getFiles().get(0).getFileUrl());
        assertEquals("图.png", list.get(0).getFiles().get(0).getFileName());
        assertEquals("IMAGE", list.get(0).getFiles().get(0).getFileType());
    }

    @Test
    void testGetRouteOpEsopList_empty_returnsEmpty() {
        assertTrue(routeApi.getRouteOpEsopList(Set.of()).isEmpty());
    }
}
