package com.iboot.pf.module.mdm.service.route;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteDetailRespVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSortReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_SORT_INVALID;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_PUBLISHED_LOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Import(RouteServiceImpl.class)
class RouteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private RouteService routeService;
    @Resource
    private RouteOpMatMapper routeOpMatMapper;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private UomMapper uomMapper;
    @Resource
    private OpMapper opMapper;
    @Resource
    private WcMapper wcMapper;

    @BeforeEach
    void createReferences() {
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).code("MAT-001").name("螺栓")
                .spec("M8").baseUomId(1L).matType("RAW").status(0).build());
        opMapper.insert(OpDO.builder().id(1L).code("OP-001").name("装配")
                .stdDuration(1).status(0).build());
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-001")
                .name("装配中心").status(0).build());
    }

    @Test
    void testCreateRouteOp_appendsDecimalSequence() {
        Long routeId = createRoute("V1");

        Long firstId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        Long secondId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        Long thirdId = routeService.createRouteOp(buildRouteOpReqVO(routeId));

        assertEquals(List.of(10, 20, 30), routeService.getRouteDetail(routeId).getOperations()
                .stream().map(item -> item.getSeq()).toList());
        assertEquals(List.of(firstId, secondId, thirdId), routeService.getRouteDetail(routeId).getOperations()
                .stream().map(item -> item.getId()).toList());
    }

    @Test
    void testUpdateRouteOpSort_swapsSafelyUnderUniqueIndex() {
        Long routeId = createRoute("V1");
        Long firstId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        Long secondId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        Long thirdId = routeService.createRouteOp(buildRouteOpReqVO(routeId));

        routeService.updateRouteOpSort(buildSortReqVO(routeId, List.of(secondId, thirdId, firstId)));

        RouteDetailRespVO detail = routeService.getRouteDetail(routeId);
        assertEquals(List.of(secondId, thirdId, firstId), detail.getOperations()
                .stream().map(item -> item.getId()).toList());
        assertEquals(List.of(10, 20, 30), detail.getOperations()
                .stream().map(item -> item.getSeq()).toList());
    }

    @Test
    void testUpdateRouteOpSort_rejectsMissingId() {
        Long routeId = createRoute("V1");
        Long firstId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        routeService.createRouteOp(buildRouteOpReqVO(routeId));

        assertServiceException(
                () -> routeService.updateRouteOpSort(buildSortReqVO(routeId, List.of(firstId))),
                ROUTE_OP_SORT_INVALID);
    }

    @Test
    void testUpdateRouteOpSort_rejectsCrossRouteId() {
        Long firstRouteId = createRoute("V1");
        Long secondRouteId = createRoute("V2");
        Long firstId = routeService.createRouteOp(buildRouteOpReqVO(firstRouteId));
        routeService.createRouteOp(buildRouteOpReqVO(firstRouteId));
        Long otherRouteOpId = routeService.createRouteOp(buildRouteOpReqVO(secondRouteId));

        assertServiceException(
                () -> routeService.updateRouteOpSort(
                        buildSortReqVO(firstRouteId, List.of(firstId, otherRouteOpId))),
                ROUTE_OP_SORT_INVALID);
    }

    @Test
    void testPublishedRoute_rejectsArrangementWrites() {
        Long routeId = createRoute("V1");
        Long routeOpId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        RouteSaveReqVO publishReqVO = buildRouteReqVO("V1");
        publishReqVO.setId(routeId);
        publishReqVO.setStatus("PUBLISHED");
        routeService.updateRoute(publishReqVO);

        assertServiceException(
                () -> routeService.createRouteOp(buildRouteOpReqVO(routeId)),
                ROUTE_PUBLISHED_LOCKED);
        assertServiceException(
                () -> routeService.updateRouteOpSort(buildSortReqVO(routeId, List.of(routeOpId))),
                ROUTE_PUBLISHED_LOCKED);
        assertServiceException(() -> routeService.deleteRouteOp(routeOpId), ROUTE_PUBLISHED_LOCKED);
    }

    @Test
    void testGetRouteDetail_returnsDisplayFieldsAndMaterialCount() {
        Long routeId = createRoute("V1");
        Long routeOpId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        RouteOpMatDO material = RouteOpMatDO.builder().routeOpId(routeOpId).matId(1L)
                .uomId(1L).qty(new BigDecimal("2.500000")).remark("工位配送").build();
        routeOpMatMapper.insert(material);

        RouteDetailRespVO detail = routeService.getRouteDetail(routeId);

        assertEquals("MAT-001", detail.getMatCode());
        assertEquals("螺栓", detail.getMatName());
        assertEquals("OP-001", detail.getOperations().get(0).getOpCode());
        assertEquals("装配", detail.getOperations().get(0).getOpName());
        assertEquals("WC-001", detail.getOperations().get(0).getWcCode());
        assertEquals("装配中心", detail.getOperations().get(0).getWcName());
        assertEquals(1, detail.getOperations().get(0).getMaterialCount());
        assertEquals("M8", detail.getOperations().get(0).getMaterials().get(0).getMatSpec());
        assertEquals("EA", detail.getOperations().get(0).getMaterials().get(0).getUomCode());
        assertEquals("件", detail.getOperations().get(0).getMaterials().get(0).getUomName());
    }

    @Test
    void testDeleteRoute_cascadesRouteOpMaterials() {
        Long routeId = createRoute("V1");
        Long routeOpId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        RouteOpMatDO material = insertRouteOpMaterial(routeOpId);

        routeService.deleteRoute(routeId);

        assertNull(routeService.getRouteOp(routeOpId));
        assertNull(routeOpMatMapper.selectById(material.getId()));
    }

    @Test
    void testDeleteRouteOp_cascadesRouteOpMaterials() {
        Long routeId = createRoute("V1");
        Long routeOpId = routeService.createRouteOp(buildRouteOpReqVO(routeId));
        RouteOpMatDO material = insertRouteOpMaterial(routeOpId);

        routeService.deleteRouteOp(routeOpId);

        assertNull(routeOpMatMapper.selectById(material.getId()));
    }

    private Long createRoute(String version) {
        return routeService.createRoute(buildRouteReqVO(version));
    }

    private RouteSaveReqVO buildRouteReqVO(String version) {
        RouteSaveReqVO reqVO = new RouteSaveReqVO();
        reqVO.setMatId(1L);
        reqVO.setVersion(version);
        reqVO.setStatus("DRAFT");
        reqVO.setEffectiveDate(LocalDate.of(2026, 8, 23));
        return reqVO;
    }

    private RouteOpSaveReqVO buildRouteOpReqVO(Long routeId) {
        RouteOpSaveReqVO reqVO = new RouteOpSaveReqVO();
        reqVO.setRouteId(routeId);
        reqVO.setOpId(1L);
        reqVO.setWcId(1L);
        reqVO.setKeyFlag(false);
        reqVO.setFaiFlag(false);
        return reqVO;
    }

    private RouteOpSortReqVO buildSortReqVO(Long routeId, List<Long> orderedIds) {
        RouteOpSortReqVO reqVO = new RouteOpSortReqVO();
        reqVO.setRouteId(routeId);
        reqVO.setOrderedIds(orderedIds);
        return reqVO;
    }

    private RouteOpMatDO insertRouteOpMaterial(Long routeOpId) {
        RouteOpMatDO material = RouteOpMatDO.builder().routeOpId(routeOpId).matId(1L)
                .uomId(1L).qty(BigDecimal.ONE).build();
        routeOpMatMapper.insert(material);
        return material;
    }
}
