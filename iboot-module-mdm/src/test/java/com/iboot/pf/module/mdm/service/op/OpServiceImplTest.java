package com.iboot.pf.module.mdm.service.op;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.OP_DELETE_REFERENCED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.OP_TIME_MODEL_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import({OpServiceImpl.class, com.iboot.pf.module.mdm.service.route.RouteServiceImpl.class})
class OpServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OpService opService;
    @Resource
    private RouteService routeService;
    @Resource
    private OpMapper opMapper;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private UomMapper uomMapper;
    @Resource
    private WcMapper wcMapper;

    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void createReferences() {
        when(mdmCodeService.resolveCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        matCatMapper.insert(MatCatDO.builder().id(1L).code("CAT").name("分类").status(0).build());
        uomMapper.insert(new UomDO().setId(1L).setCode("EA").setName("件").setStatus(0));
        matMapper.insert(MatDO.builder().id(1L).catId(1L).code("MAT-001").name("螺栓")
                .spec("M8").baseUomId(1L).matType("RAW").status(0).build());
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-001")
                .name("装配中心").status(0).build());
    }

    @Test
    void testCreateOp_perUnitRequiresStdMinutesPerUnit() {
        OpSaveReqVO reqVO = buildOpReqVO("PER_UNIT");
        reqVO.setStdMinutesPerUnit(null);
        assertServiceException(() -> opService.createOp(reqVO), OP_TIME_MODEL_INVALID);
    }

    @Test
    void testCreateOp_fixedWithoutPerUnitMinutes() {
        OpSaveReqVO reqVO = buildOpReqVO("FIXED");
        reqVO.setStdMinutesPerUnit(null);
        Long id = opService.createOp(reqVO);
        assertEquals("FIXED", opService.getOp(id).getDurationType());
    }

    @Test
    void testDeleteOp_referencedByRouteOp() {
        Long opId = opService.createOp(buildOpReqVO("FIXED"));
        Long routeId = routeService.createRoute(buildRouteReqVO());
        createRouteOp(routeId, opId);

        assertServiceException(() -> opService.deleteOp(opId), OP_DELETE_REFERENCED);
    }

    private OpSaveReqVO buildOpReqVO(String durationType) {
        OpSaveReqVO reqVO = new OpSaveReqVO();
        reqVO.setCode("OP-001");
        reqVO.setName("装配");
        reqVO.setStdDuration(30);
        reqVO.setStdMinutesPerUnit(new BigDecimal("2.5"));
        reqVO.setDurationType(durationType);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private RouteSaveReqVO buildRouteReqVO() {
        RouteSaveReqVO reqVO = new RouteSaveReqVO();
        reqVO.setMatId(1L);
        reqVO.setVersion("V1");
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        reqVO.setEffectiveDate(LocalDate.of(2026, 8, 23));
        return reqVO;
    }

    private void createRouteOp(Long routeId, Long opId) {
        RouteOpSaveReqVO reqVO = new RouteOpSaveReqVO();
        reqVO.setRouteId(routeId);
        reqVO.setOpId(opId);
        reqVO.setWcId(1L);
        reqVO.setKeyFlag(false);
        reqVO.setFaiFlag(false);
        routeService.createRouteOp(reqVO);
    }

}
