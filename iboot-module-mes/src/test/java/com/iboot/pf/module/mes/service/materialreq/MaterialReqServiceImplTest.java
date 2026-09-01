package com.iboot.pf.module.mes.service.materialreq;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLinePageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqPageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_RETURN_BATCH_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 领退补料申请 Service 测试：主单从生产工单继承维度、交给 WMS 后禁改、主单删除级联清明细、
 * 明细行号唯一、退料必须指定批次、快照由后端补全。
 */
@Import(MaterialReqServiceImpl.class)
class MaterialReqServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MaterialReqService materialReqService;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long prodOrderId;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "MR0001" : invocation.getArgument(1));
        when(snapshotService.requireMaterial(any())).thenReturn(MesTestFixtures.material());
        when(snapshotService.requireUomCode(any())).thenReturn(MesTestFixtures.uomCode());
        when(snapshotService.lotNoOrNull(any())).thenAnswer(invocation -> {
            Long batchId = invocation.getArgument(0);
            return batchId == null || MesConstants.NONE_ID.equals(batchId) ? null : "LOT-001";
        });
        ProdOrderDO prodOrder = new ProdOrderDO()
                .setOrderNo("PO-0001")
                .setProjectId(7L)
                .setProjectTaskId(8L);
        prodOrderMapper.insert(prodOrder);
        prodOrderId = prodOrder.getId();
    }

    private MaterialReqSaveReqVO buildReqVO(String reqNo, String reqType) {
        MaterialReqSaveReqVO reqVO = new MaterialReqSaveReqVO();
        reqVO.setReqNo(reqNo);
        reqVO.setProdOrderId(prodOrderId);
        reqVO.setWorkOrderId(MesConstants.NONE_ID);
        reqVO.setOpTaskId(MesConstants.NONE_ID);
        reqVO.setReqType(reqType);
        reqVO.setInvOrgId(1L);
        reqVO.setWhId(2L);
        reqVO.setStatus(MesMaterialReqStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    private MaterialReqLineSaveReqVO buildLineReqVO(Long reqId, Integer lineNo, Long batchId) {
        MaterialReqLineSaveReqVO reqVO = new MaterialReqLineSaveReqVO();
        reqVO.setReqId(reqId);
        reqVO.setLineNo(lineNo);
        reqVO.setRouteOpMatId(MesConstants.NONE_ID);
        reqVO.setMatId(1L);
        reqVO.setQty(new BigDecimal("10"));
        reqVO.setUomId(1L);
        reqVO.setBatchId(batchId);
        reqVO.setMustIssueBeforeStart(Boolean.TRUE);
        reqVO.setStatus(MesMaterialReqLineStatusEnum.DRAFT.name());
        reqVO.setRemark("单测明细");
        return reqVO;
    }

    @Test
    void testCreateMaterialReq_inheritFromProdOrder() {
        Long id = materialReqService.createMaterialReq(buildReqVO(null, MesMaterialReqTypeEnum.ISSUE.name()));

        MaterialReqDO materialReq = materialReqMapper.selectById(id);
        assertNotNull(materialReq);
        assertEquals("MR0001", materialReq.getReqNo());
        // 单号快照与项目维度从生产工单继承
        assertEquals("PO-0001", materialReq.getProdOrderNo());
        assertEquals(7L, materialReq.getProjectId());
        assertEquals(8L, materialReq.getProjectTaskId());
    }

    @Test
    void testCreateMaterialReq_prodOrderNotExists() {
        MaterialReqSaveReqVO reqVO = buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name());
        reqVO.setProdOrderId(randomLongId());

        assertServiceException(() -> materialReqService.createMaterialReq(reqVO), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    void testUpdateMaterialReq_handedToWmsRejected() {
        MaterialReqSaveReqVO createReqVO = buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name());
        createReqVO.setStatus(MesMaterialReqStatusEnum.WMS_PROCESSING.name());
        Long id = materialReqService.createMaterialReq(createReqVO);

        MaterialReqSaveReqVO updateReqVO = buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name());
        updateReqVO.setId(id);
        // 已交给 WMS 的申请不允许再改，两侧数量口径必须一致
        assertServiceException(() -> materialReqService.updateMaterialReq(updateReqVO),
                MATERIAL_REQ_STATUS_INVALID);
    }

    @Test
    void testUpdateMaterialReq_notExists() {
        MaterialReqSaveReqVO updateReqVO = buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name());
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> materialReqService.updateMaterialReq(updateReqVO), MATERIAL_REQ_NOT_EXISTS);
    }

    @Test
    void testDeleteMaterialReq_cascadeLines() {
        Long id = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        Long lineId = materialReqService.createMaterialReqLine(buildLineReqVO(id, 1, MesConstants.NONE_ID));

        materialReqService.deleteMaterialReq(id);

        assertNull(materialReqMapper.selectById(id));
        // 主单删除必须级联清理明细，否则留下孤儿数据
        assertNull(materialReqLineMapper.selectById(lineId));
    }

    @Test
    void testDeleteMaterialReqList_cascadeLines() {
        Long first = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        Long second = materialReqService.createMaterialReq(buildReqVO("MR-B", MesMaterialReqTypeEnum.ISSUE.name()));
        Long lineId = materialReqService.createMaterialReqLine(buildLineReqVO(second, 1, MesConstants.NONE_ID));

        materialReqService.deleteMaterialReqListByIds(List.of(first, second));

        assertNull(materialReqMapper.selectById(first));
        assertNull(materialReqLineMapper.selectById(lineId));
    }

    @Test
    void testCreateMaterialReqLine_snapshotFilledAndProcessedZeroed() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));

        Long lineId = materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID));

        MaterialReqLineDO line = materialReqLineMapper.selectById(lineId);
        assertNotNull(line);
        assertEquals("MAT-001", line.getMatCode());
        assertEquals("PCS", line.getUomCode());
        // WMS 已处理数量由 WMS 回写，创建时归零
        assertEquals(0, line.getProcessedQty().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testCreateMaterialReqLine_duplicateLineNoRejected() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID));

        assertServiceException(
                () -> materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID)),
                MATERIAL_REQ_LINE_NO_DUPLICATE);
    }

    @Test
    void testCreateMaterialReqLine_returnBatchRequired() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.RETURN.name()));

        // 退回去的库存要落到原批次上，否则批次台账对不上
        assertServiceException(
                () -> materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID)),
                MATERIAL_REQ_RETURN_BATCH_REQUIRED);
    }

    @Test
    void testCreateMaterialReqLine_returnWithBatchAccepted() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.RETURN.name()));

        Long lineId = materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, 5L));

        assertEquals("LOT-001", materialReqLineMapper.selectById(lineId).getLotNo());
    }

    @Test
    void testUpdateMaterialReqLine_keepProcessedQty() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        Long lineId = materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID));

        MaterialReqLineSaveReqVO updateReqVO = buildLineReqVO(reqId, 1, MesConstants.NONE_ID);
        updateReqVO.setId(lineId);
        updateReqVO.setQty(new BigDecimal("20"));
        materialReqService.updateMaterialReqLine(updateReqVO);

        MaterialReqLineDO line = materialReqLineMapper.selectById(lineId);
        assertEquals(0, line.getQty().compareTo(new BigDecimal("20")));
        assertEquals(0, line.getProcessedQty().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testUpdateMaterialReqLine_notExists() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        MaterialReqLineSaveReqVO updateReqVO = buildLineReqVO(reqId, 1, MesConstants.NONE_ID);
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> materialReqService.updateMaterialReqLine(updateReqVO),
                MATERIAL_REQ_LINE_NOT_EXISTS);
    }

    @Test
    void testDeleteMaterialReqLine_success() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        Long lineId = materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID));

        materialReqService.deleteMaterialReqLine(lineId);

        assertNull(materialReqLineMapper.selectById(lineId));
    }

    @Test
    void testDeleteMaterialReqLineList_success() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        Long first = materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID));
        Long second = materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 2, MesConstants.NONE_ID));

        materialReqService.deleteMaterialReqLineListByIds(List.of(first, second));

        assertTrue(materialReqLineMapper.selectCountByReqId(reqId) == 0);
    }

    @Test
    void testGetMaterialReqPage() {
        materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));

        MaterialReqPageReqVO reqVO = new MaterialReqPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<MaterialReqRespVO> list = materialReqService.getMaterialReqPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("MR-A", list.get(0).getReqNo());
    }

    @Test
    void testGetMaterialReqLinePage() {
        Long reqId = materialReqService.createMaterialReq(buildReqVO("MR-A", MesMaterialReqTypeEnum.ISSUE.name()));
        materialReqService.createMaterialReqLine(buildLineReqVO(reqId, 1, MesConstants.NONE_ID));

        MaterialReqLinePageReqVO reqVO = new MaterialReqLinePageReqVO();
        reqVO.setReqId(reqId);
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<MaterialReqLineRespVO> list = materialReqService.getMaterialReqLinePage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("MAT-001", list.get(0).getMatCode());
    }

}
