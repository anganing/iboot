package com.iboot.pf.module.mes.api.materialreq;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqActionReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 领退补料申请门面委托测试。
 *
 * <p>领域动作 mock 掉（预留与过账的行为由它自己的测试类保证），这里证三件事：明细行逐字段落地、
 * 确认的 {@code reason} 落到 BO 的 {@code remark}、查询一次带出头与行。
 *
 * <p>查询走真实 H2：明细是按 {@code req_id} 查出来的，用替身断言不了"查的是这张单的行"。
 */
@Import(MaterialReqApiImpl.class)
class MaterialReqApiImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String SOURCE_BIZ_NO = "PO-0001";

    @Resource
    private MaterialReqApi materialReqApi;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;

    @MockitoBean
    private MesMaterialReqDomainService materialReqDomainService;

    @Test
    void testCreateMaterialReq_mapsHeaderAndLines() {
        CreateMaterialReqReqDTO reqDTO = new CreateMaterialReqReqDTO();
        reqDTO.setIdempotencyKey("UI:PO-0001:CREATE_REQ:0");
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setProdOrderId(99L);
        reqDTO.setWorkOrderId(66L);
        reqDTO.setOpTaskId(77L);
        reqDTO.setReqType(MesMaterialReqTypeEnum.ISSUE.name());
        reqDTO.setInvOrgId(1L);
        reqDTO.setWhId(2L);
        reqDTO.setRemark("按工序领料");
        CreateMaterialReqReqDTO.Line line = new CreateMaterialReqReqDTO.Line();
        line.setMatId(1001L);
        line.setQty(new BigDecimal("200"));
        line.setUomId(61L);
        line.setBatchId(301L);
        line.setRouteOpMatId(401L);
        line.setRemark("行备注");
        reqDTO.setLines(List.of(line));
        when(materialReqDomainService.createMaterialReq(any())).thenReturn(555L);

        assertEquals(555L, materialReqApi.createMaterialReq(reqDTO));

        ArgumentCaptor<MesCreateMaterialReqBO> captor = ArgumentCaptor.forClass(MesCreateMaterialReqBO.class);
        verify(materialReqDomainService).createMaterialReq(captor.capture());
        MesCreateMaterialReqBO bo = captor.getValue();
        assertEquals(99L, bo.getProdOrderId());
        assertEquals(66L, bo.getWorkOrderId());
        assertEquals(77L, bo.getOpTaskId());
        assertEquals(MesMaterialReqTypeEnum.ISSUE.name(), bo.getReqType());
        assertEquals(1L, bo.getInvOrgId());
        assertEquals(2L, bo.getWhId());
        assertEquals("按工序领料", bo.getRemark());
        assertEquals("UI:PO-0001:CREATE_REQ:0", bo.getIdempotencyKey());
        assertEquals(SOURCE_MODULE, bo.getSourceModule());
        assertEquals(SOURCE_BIZ_NO, bo.getSourceBizNo());
        // 行上任何一个字段被静默丢掉都会变成"领料数量对不上"，落库后极难反查，所以逐字段断言
        assertEquals(1, bo.getLines().size());
        MesCreateMaterialReqBO.Line boLine = bo.getLines().get(0);
        assertEquals(1001L, boLine.getMatId());
        assertEquals(0, new BigDecimal("200").compareTo(boLine.getQty()));
        assertEquals(61L, boLine.getUomId());
        assertEquals(301L, boLine.getBatchId());
        assertEquals(401L, boLine.getRouteOpMatId());
        assertEquals("行备注", boLine.getRemark());
        // 门面 DTO 上没有这个字段，留 null 由领域侧按 true 处理（漏发开工前必需的料的代价更大）
        assertNull(boLine.getMustIssueBeforeStart());
    }

    @Test
    void testCreateMaterialReq_nullLinesLeftForServerExpansion() {
        CreateMaterialReqReqDTO reqDTO = new CreateMaterialReqReqDTO();
        reqDTO.setIdempotencyKey("UI:PO-0001:CREATE_REQ:0");
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setProdOrderId(99L);
        reqDTO.setReqType(MesMaterialReqTypeEnum.ISSUE.name());

        materialReqApi.createMaterialReq(reqDTO);

        ArgumentCaptor<MesCreateMaterialReqBO> captor = ArgumentCaptor.forClass(MesCreateMaterialReqBO.class);
        verify(materialReqDomainService).createMaterialReq(captor.capture());
        // 行留空表示"按用料关系展开"，门面不许自己造一张空表把这个语义抹掉
        assertNull(captor.getValue().getLines());
    }

    @Test
    void testCancelMaterialReq_delegatesWithReason() {
        materialReqApi.cancelMaterialReq(actionReqDTO("UI:MR-0001:CANCEL_MATERIAL_REQ:0", "车间不领了"));

        ArgumentCaptor<MesMaterialReqActionBO> captor = ArgumentCaptor.forClass(MesMaterialReqActionBO.class);
        verify(materialReqDomainService).cancelMaterialReq(captor.capture());
        assertEquals(555L, captor.getValue().getMaterialReqId());
        assertEquals("车间不领了", captor.getValue().getReason());
        assertEquals("UI:MR-0001:CANCEL_MATERIAL_REQ:0", captor.getValue().getIdempotencyKey());
    }

    @Test
    void testConfirmMaterialReq_reasonMappedToRemarkAndFullConfirm() {
        materialReqApi.confirmMaterialReq(actionReqDTO("MES:MR-0001:CONFIRM_MATERIAL_REQ:0", "仓库已发齐"));

        ArgumentCaptor<MesConfirmMaterialReqBO> captor = ArgumentCaptor.forClass(MesConfirmMaterialReqBO.class);
        verify(materialReqDomainService).confirmMaterialReq(captor.capture());
        MesConfirmMaterialReqBO bo = captor.getValue();
        assertEquals(555L, bo.getMaterialReqId());
        assertEquals("仓库已发齐", bo.getRemark());
        assertEquals("MES:MR-0001:CONFIRM_MATERIAL_REQ:0", bo.getIdempotencyKey());
        // 行留空 = 全部行按剩余量全额确认，门面 DTO 没有行级确认量与库位
        assertNull(bo.getLines());
    }

    @Test
    void testGetMaterialReq_carriesHeaderAndLines() {
        MaterialReqDO req = MaterialReqDO.builder()
                .reqNo("MR-0001").prodOrderId(99L).prodOrderNo("PO-0001")
                .workOrderId(66L).opTaskId(77L)
                .reqType(MesMaterialReqTypeEnum.ISSUE.name())
                .invOrgId(1L).whId(2L)
                .projectId(MesConstants.NONE_ID).projectTaskId(MesConstants.NONE_ID)
                .wmsBizNo("OUT-0001").status(MesMaterialReqStatusEnum.DONE.name())
                .build();
        materialReqMapper.insert(req);
        materialReqLineMapper.insert(line(req.getId(), 1, 1001L, "MAT-1001", new BigDecimal("200")));
        materialReqLineMapper.insert(line(req.getId(), 2, 1002L, "MAT-1002", new BigDecimal("50")));
        // 另一张单的行，用来证明查的是本单的行而不是全表
        MaterialReqDO other = MaterialReqDO.builder().reqNo("MR-0002").prodOrderId(99L)
                .reqType(MesMaterialReqTypeEnum.ISSUE.name())
                .status(MesMaterialReqStatusEnum.DRAFT.name()).build();
        materialReqMapper.insert(other);
        materialReqLineMapper.insert(line(other.getId(), 1, 1003L, "MAT-1003", new BigDecimal("9")));

        MaterialReqRespDTO respDTO = materialReqApi.getMaterialReq(req.getId());

        assertNotNull(respDTO);
        assertEquals("MR-0001", respDTO.getReqNo());
        assertEquals("PO-0001", respDTO.getProdOrderNo());
        assertEquals(66L, respDTO.getWorkOrderId());
        assertEquals(77L, respDTO.getOpTaskId());
        assertEquals(MesMaterialReqTypeEnum.ISSUE.name(), respDTO.getReqType());
        assertEquals("OUT-0001", respDTO.getWmsBizNo());
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), respDTO.getStatus());
        assertEquals(2, respDTO.getLines().size());
        MaterialReqRespDTO.Line first = respDTO.getLines().stream()
                .filter(l -> l.getLineNo() == 1).findFirst().orElseThrow();
        assertEquals(1001L, first.getMatId());
        assertEquals("MAT-1001", first.getMatCode());
        assertEquals("物料1001", first.getMatName());
        assertEquals(0, new BigDecimal("200").compareTo(first.getQty()));
        assertEquals(0, new BigDecimal("200").compareTo(first.getProcessedQty()));
        assertEquals("PCS", first.getUomCode());
        assertEquals(301L, first.getBatchId());
        assertEquals("LOT-01", first.getLotNo());
        assertTrue(first.getMustIssueBeforeStart());
        assertEquals(MesMaterialReqLineStatusEnum.DONE.name(), first.getStatus());
    }

    @Test
    void testGetMaterialReq_nullAndNotExists() {
        assertNull(materialReqApi.getMaterialReq(null));
        assertNull(materialReqApi.getMaterialReq(-1L));
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = MaterialReqApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : MaterialReqApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

    private MaterialReqActionReqDTO actionReqDTO(String idempotencyKey, String reason) {
        MaterialReqActionReqDTO reqDTO = new MaterialReqActionReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo("MR-0001");
        reqDTO.setMaterialReqId(555L);
        reqDTO.setReason(reason);
        return reqDTO;
    }

    private MaterialReqLineDO line(Long reqId, Integer lineNo, Long matId, String matCode, BigDecimal qty) {
        return MaterialReqLineDO.builder()
                .reqId(reqId).lineNo(lineNo).routeOpMatId(401L)
                .matId(matId).matCode(matCode).matName("物料" + (matId % 10000)).matSpec("规格A")
                .qty(qty).processedQty(qty).uomId(61L).uomCode("PCS")
                .batchId(301L).lotNo("LOT-01").reservationId(MesConstants.NONE_ID)
                .mustIssueBeforeStart(true).status(MesMaterialReqLineStatusEnum.DONE.name())
                .build();
    }

}
