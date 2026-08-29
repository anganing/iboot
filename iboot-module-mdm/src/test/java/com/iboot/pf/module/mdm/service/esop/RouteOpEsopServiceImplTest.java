package com.iboot.pf.module.mdm.service.esop;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.file.FileApi;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopFileReqVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteCopyReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopDO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopFileDO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopFileMapper;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.service.route.RouteService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_ENABLED_LOCKED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_ESOP_CONTENT_REQUIRED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_ESOP_SEQ_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({RouteOpEsopServiceImpl.class, com.iboot.pf.module.mdm.service.route.RouteServiceImpl.class})
class RouteOpEsopServiceImplTest extends BaseDbUnitTest {

    @Resource
    private RouteOpEsopService routeOpEsopService;
    @Resource
    private RouteService routeService;
    @Resource
    private RouteOpEsopMapper routeOpEsopMapper;
    @Resource
    private RouteOpEsopFileMapper routeOpEsopFileMapper;
    @Resource
    private RouteOpMapper routeOpMapper;
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
    private FileApi fileApi;

    @BeforeEach
    void createReferences() {
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
    void testEnabledRoute_rejectsEsopWrites() {
        Long routeId = createRoute("V1");
        Long routeOpId = createRouteOp(routeId);
        // 停用期先创建一条 ESOP，随后启用路线
        Long esopId = routeOpEsopService.createRouteOpEsop(buildEsopReqVO(routeOpId));
        enableRoute(routeId, "V1");

        // 启用后新增、修改、删除均被锁
        assertServiceException(
                () -> routeOpEsopService.createRouteOpEsop(buildEsopReqVO(routeOpId)), ROUTE_ENABLED_LOCKED);
        assertServiceException(() -> routeOpEsopService.updateRouteOpEsop(buildEsopReqVO(routeOpId, esopId)),
                ROUTE_ENABLED_LOCKED);
        assertServiceException(() -> routeOpEsopService.deleteRouteOpEsop(esopId), ROUTE_ENABLED_LOCKED);
    }

    @Test
    void testCopyRoute_copiesEsopWithFiles() {
        Long sourceRouteId = createRoute("V1");
        Long routeOpId = createRouteOp(sourceRouteId);
        RouteOpEsopSaveReqVO reqVO = buildEsopReqVO(routeOpId);
        reqVO.setFiles(List.of(buildFileReqVO("绕线示意图.png"), buildFileReqVO("装配图纸.dwg")));
        Long esopId = routeOpEsopService.createRouteOpEsop(reqVO);

        Long newRouteId = routeService.copyRoute(buildCopyReqVO(sourceRouteId, "V2"));

        // 新版本 ESOP 明细已复制且 route_op_id 重指向新版本工序
        Long newRouteOpId = routeOpMapper.selectListByRouteId(newRouteId).get(0).getId();
        List<RouteOpEsopDO> newEsops = routeOpEsopMapper.selectListByRouteOpId(newRouteOpId);
        assertEquals(1, newEsops.size());
        assertEquals(newRouteOpId, newEsops.get(0).getRouteOpId());
        assertEquals("装配要点", newEsops.get(0).getTitle());
        assertNotNull(newEsops.get(0).getId());
        assertEquals(esopId, routeOpEsopMapper.selectById(esopId).getId());

        // 附件一并复制且 esop_id 重指向新建的作业步骤
        List<RouteOpEsopFileDO> newFiles = routeOpEsopFileMapper.selectListByEsopId(newEsops.get(0).getId());
        assertEquals(2, newFiles.size());
        assertEquals(newEsops.get(0).getId(), newFiles.get(0).getEsopId());
        assertEquals("IMAGE", newFiles.get(0).getFileType());
        assertEquals("DRAWING", newFiles.get(1).getFileType());
        // 源步骤附件不受影响
        assertEquals(2, routeOpEsopFileMapper.selectListByEsopId(esopId).size());
    }

    @Test
    void testEsopFiles_replacedOnUpdateAndTypeResolvedByExtension() {
        Long routeId = createRoute("V1");
        Long routeOpId = createRouteOp(routeId);
        RouteOpEsopSaveReqVO createReqVO = buildEsopReqVO(routeOpId);
        createReqVO.setFiles(List.of(buildFileReqVO("步骤一.png"), buildFileReqVO("操作视频.mp4"),
                buildFileReqVO("参数表.xlsx"), buildFileReqVO("说明.pdf"), buildFileReqVO("readme")));
        Long esopId = routeOpEsopService.createRouteOpEsop(createReqVO);

        List<RouteOpEsopFileDO> files = routeOpEsopFileMapper.selectListByEsopId(esopId);
        assertEquals(5, files.size());
        assertEquals(List.of("IMAGE", "VIDEO", "OFFICE", "PDF", "OTHER"),
                files.stream().map(RouteOpEsopFileDO::getFileType).toList());
        assertEquals(List.of(1, 2, 3, 4, 5), files.stream().map(RouteOpEsopFileDO::getSeq).toList());

        // 更新为单个附件：旧附件行全量替换
        RouteOpEsopSaveReqVO updateReqVO = buildEsopReqVO(routeOpId, esopId);
        updateReqVO.setFiles(List.of(buildFileReqVO("新图纸.dxf")));
        routeOpEsopService.updateRouteOpEsop(updateReqVO);

        List<RouteOpEsopFileDO> updatedFiles = routeOpEsopFileMapper.selectListByEsopId(esopId);
        assertEquals(1, updatedFiles.size());
        assertEquals("DRAWING", updatedFiles.get(0).getFileType());
        assertEquals("新图纸.dxf", updatedFiles.get(0).getFileName());
    }

    @Test
    void testEsop_rejectsEmptyContentAndDuplicateSeq() {
        Long routeId = createRoute("V1");
        Long routeOpId = createRouteOp(routeId);

        // 作业要点、工艺参数、附件三者同时为空被拒
        RouteOpEsopSaveReqVO emptyReqVO = buildEsopReqVO(routeOpId);
        emptyReqVO.setContent("  ");
        assertServiceException(() -> routeOpEsopService.createRouteOpEsop(emptyReqVO),
                ROUTE_OP_ESOP_CONTENT_REQUIRED);

        // 仅有工艺参数即可通过
        RouteOpEsopSaveReqVO paramOnlyReqVO = buildEsopReqVO(routeOpId);
        paramOnlyReqVO.setContent(null);
        paramOnlyReqVO.setParamJson("{\"扭矩\":\"12N·m\"}");
        Long esopId = routeOpEsopService.createRouteOpEsop(paramOnlyReqVO);
        assertNotNull(esopId);

        // 同一工序下 seq 重复被拒
        assertServiceException(() -> routeOpEsopService.createRouteOpEsop(buildEsopReqVO(routeOpId)),
                ROUTE_OP_ESOP_SEQ_DUPLICATE);
    }

    @Test
    void testDeleteEsop_cascadesFiles() {
        Long routeId = createRoute("V1");
        Long routeOpId = createRouteOp(routeId);
        RouteOpEsopSaveReqVO reqVO = buildEsopReqVO(routeOpId);
        reqVO.setFiles(List.of(buildFileReqVO("步骤一.png")));
        Long esopId = routeOpEsopService.createRouteOpEsop(reqVO);

        routeOpEsopService.deleteRouteOpEsop(esopId);

        assertEquals(0, routeOpEsopFileMapper.selectListByEsopId(esopId).size());
    }

    private RouteOpEsopFileReqVO buildFileReqVO(String fileName) {
        RouteOpEsopFileReqVO file = new RouteOpEsopFileReqVO();
        file.setFileName(fileName);
        file.setFileUrl("http://localhost/infra/file/" + fileName);
        return file;
    }

    private RouteOpEsopSaveReqVO buildEsopReqVO(Long routeOpId) {
        RouteOpEsopSaveReqVO reqVO = new RouteOpEsopSaveReqVO();
        reqVO.setRouteOpId(routeOpId);
        reqVO.setSeq(10);
        reqVO.setTitle("装配要点");
        reqVO.setContent("先装法兰再装轴承");
        reqVO.setRequiredConfirmFlag(true);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private RouteOpEsopSaveReqVO buildEsopReqVO(Long routeOpId, Long id) {
        RouteOpEsopSaveReqVO reqVO = buildEsopReqVO(routeOpId);
        reqVO.setId(id);
        return reqVO;
    }

    private Long createRoute(String version) {
        return routeService.createRoute(buildRouteReqVO(version, null, CommonStatusEnum.DISABLE.getStatus()));
    }

    private RouteSaveReqVO buildRouteReqVO(String version, Long id, Integer status) {
        RouteSaveReqVO reqVO = new RouteSaveReqVO();
        reqVO.setId(id);
        reqVO.setMatId(1L);
        reqVO.setVersion(version);
        reqVO.setStatus(status);
        reqVO.setEffectiveDate(LocalDate.of(2026, 8, 23));
        return reqVO;
    }

    private void enableRoute(Long routeId, String version) {
        routeService.updateRoute(buildRouteReqVO(version, routeId, CommonStatusEnum.ENABLE.getStatus()));
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

    private RouteCopyReqVO buildCopyReqVO(Long id, String version) {
        RouteCopyReqVO reqVO = new RouteCopyReqVO();
        reqVO.setId(id);
        reqVO.setVersion(version);
        return reqVO;
    }

}
