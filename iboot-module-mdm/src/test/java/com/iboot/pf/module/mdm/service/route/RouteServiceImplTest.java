package com.iboot.pf.module.mdm.service.route;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
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
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Test
    void testDeleteRoute_cascadesRouteOpMaterials() {
        createReferences();
        Long routeId = routeService.createRoute(buildRouteReqVO());
        Long routeOpId = routeService.createRouteOp(RouteOpDO.builder()
                .routeId(routeId).seq(10).opId(1L).wcId(1L).keyFlag(false).faiFlag(false).build());
        RouteOpMatDO routeOpMat = RouteOpMatDO.builder().routeOpId(routeOpId).matId(1L)
                .uomId(1L).qty(BigDecimal.ONE).build();
        routeOpMatMapper.insert(routeOpMat);

        routeService.deleteRoute(routeId);

        assertNull(routeService.getRouteOp(routeOpId));
        assertNull(routeOpMatMapper.selectById(routeOpMat.getId()));
    }

    @Test
    void testDeleteRouteOp_cascadesRouteOpMaterials() {
        createReferences();
        Long routeId = routeService.createRoute(buildRouteReqVO());
        Long routeOpId = routeService.createRouteOp(RouteOpDO.builder()
                .routeId(routeId).seq(10).opId(1L).wcId(1L).keyFlag(false).faiFlag(false).build());
        RouteOpMatDO routeOpMat = RouteOpMatDO.builder().routeOpId(routeOpId).matId(1L)
                .uomId(1L).qty(BigDecimal.ONE).build();
        routeOpMatMapper.insert(routeOpMat);

        routeService.deleteRouteOp(routeOpId);

        assertNull(routeOpMatMapper.selectById(routeOpMat.getId()));
    }

    private void createReferences() {
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).code("MAT-001").name("物料")
                .baseUomId(1L).matType("RAW").status(0).build());
        opMapper.insert(OpDO.builder().id(1L).code("OP-001").name("工序").stdDuration(1).status(0).build());
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-001").name("工作中心").status(0).build());
    }

    private RouteSaveReqVO buildRouteReqVO() {
        RouteSaveReqVO reqVO = new RouteSaveReqVO();
        reqVO.setMatId(1L);
        reqVO.setVersion("V1");
        reqVO.setStatus("DRAFT");
        reqVO.setEffectiveDate(LocalDate.of(2026, 8, 23));
        return reqVO;
    }
}
