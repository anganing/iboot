package com.iboot.pf.module.mdm.service.tool;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.RouteOpToolSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.ToolSaveReqVO;
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

import java.time.LocalDate;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.TOOL_DELETE_REFERENCED;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import({ToolServiceImpl.class, com.iboot.pf.module.mdm.service.route.RouteServiceImpl.class})
class ToolServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ToolService toolService;
    @Resource
    private RouteService routeService;
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
        opMapper.insert(OpDO.builder().id(1L).code("OP-001").name("装配")
                .stdDuration(1).durationType("FIXED").status(0).build());
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-001")
                .name("装配中心").status(0).build());
    }

    @Test
    void testDeleteTool_referencedByRouteOp() {
        Long toolId = toolService.createTool(buildToolReqVO());
        Long routeId = routeService.createRoute(buildRouteReqVO());
        Long routeOpId = createRouteOp(routeId);

        RouteOpToolSaveReqVO reqVO = new RouteOpToolSaveReqVO();
        reqVO.setRouteOpId(routeOpId);
        reqVO.setToolId(toolId);
        reqVO.setQty(1);
        reqVO.setRequiredFlag(true);
        toolService.createRouteOpTool(reqVO);

        assertServiceException(() -> toolService.deleteTool(toolId), TOOL_DELETE_REFERENCED);
    }

    private ToolSaveReqVO buildToolReqVO() {
        ToolSaveReqVO reqVO = new ToolSaveReqVO();
        reqVO.setCode("TOOL-001");
        reqVO.setName("夹具A");
        reqVO.setToolType("FIXTURE");
        reqVO.setWcId(1L);
        reqVO.setQtyTotal(2);
        reqVO.setLifeType("COUNT");
        reqVO.setLifeLimit(5000);
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

    private Long createRouteOp(Long routeId) {
        RouteOpSaveReqVO reqVO = new RouteOpSaveReqVO();
        reqVO.setRouteId(routeId);
        reqVO.setOpId(1L);
        reqVO.setWcId(1L);
        reqVO.setKeyFlag(false);
        reqVO.setFaiFlag(false);
        return routeService.createRouteOp(reqVO);
    }

}
