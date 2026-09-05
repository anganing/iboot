package com.iboot.pf.module.mes.api.order;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseMockitoUnitTest;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 生产工单门面委托测试。
 *
 * <p>门面只做三件事，所以断言也只盯三件事：入参有没有一个字段不落地、调的是不是那个 Service、
 * 结果有没有原样带出。被调方全部 mock —— 领域动作的行为由它自己的测试类保证，这里要证的是"门面接通了"。
 *
 * <p>重点盯两处名字对不上的地方：{@code ProdOrderActionReqDTO.reason} 到
 * {@code MesReleaseOrderBO.remark}（靠 BeanUtils 按名字对会静默留 null），
 * 以及门面 DTO 没有而 codegen 建单 VO 要求非空的 {@code bomId} / {@code routeId}。
 */
class ProdOrderApiImplTest extends BaseMockitoUnitTest {

    private static final String IDEMPOTENCY_KEY = "UI:PO-0001:RELEASE_PROD_ORDER:0";
    private static final String SOURCE_MODULE = "UI";
    private static final String SOURCE_BIZ_NO = "PO-0001";
    private static final Long BOM_ID = 901L;
    private static final Long ROUTE_ID = 902L;
    private static final Long MAT_ID = 51L;

    @InjectMocks
    private ProdOrderApiImpl prodOrderApi;

    @Mock
    private ProdOrderService prodOrderService;
    @Mock
    private MesProdOrderDomainService prodOrderDomainService;
    @Mock
    private MesSnapshotService snapshotService;

    @Test
    void testCreateProdOrder_delegatesToCodegenWithFullMapping() {
        mockEffectiveVersions();
        CreateProdOrderReqDTO reqDTO = new CreateProdOrderReqDTO();
        reqDTO.setIdempotencyKey(IDEMPOTENCY_KEY);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setDemandId(7L);
        reqDTO.setPlantId(11L);
        reqDTO.setWorkshopId(21L);
        reqDTO.setProjectId(31L);
        reqDTO.setProjectTaskId(41L);
        reqDTO.setMatId(MAT_ID);
        reqDTO.setQty(new BigDecimal("100"));
        reqDTO.setUomId(61L);
        reqDTO.setDueDate(LocalDateTime.of(2026, 10, 1, 8, 0));
        reqDTO.setPriority(7);
        reqDTO.setRemark("外部系统建单");
        when(prodOrderService.createProdOrder(any())).thenReturn(1234L);

        assertEquals(1234L, prodOrderApi.createProdOrder(reqDTO));

        ArgumentCaptor<ProdOrderSaveReqVO> captor = ArgumentCaptor.forClass(ProdOrderSaveReqVO.class);
        verify(prodOrderService).createProdOrder(captor.capture());
        ProdOrderSaveReqVO saveReqVO = captor.getValue();
        assertNull(saveReqVO.getId());
        assertNull(saveReqVO.getOrderNo(), "工单号必须留给编码规则生成");
        assertEquals(7L, saveReqVO.getDemandId());
        assertEquals(11L, saveReqVO.getPlantId());
        assertEquals(21L, saveReqVO.getWorkshopId());
        assertEquals(31L, saveReqVO.getProjectId());
        assertEquals(41L, saveReqVO.getProjectTaskId());
        assertEquals(MAT_ID, saveReqVO.getMatId());
        assertEquals(0, new BigDecimal("100").compareTo(saveReqVO.getQty()));
        assertEquals(61L, saveReqVO.getUomId());
        assertEquals(LocalDateTime.of(2026, 10, 1, 8, 0), saveReqVO.getDueDate());
        assertEquals(7, saveReqVO.getPriority());
        assertEquals("外部系统建单", saveReqVO.getRemark());
        // 门面 DTO 不带这两个编号，取物料当天的生效版本补上，否则 codegen 侧 @NotNull 直接拦下
        assertEquals(BOM_ID, saveReqVO.getBomId());
        assertEquals(ROUTE_ID, saveReqVO.getRouteId());
    }

    @Test
    void testCreateProdOrder_nullableIdsFallBackToNone() {
        mockEffectiveVersions();
        CreateProdOrderReqDTO reqDTO = new CreateProdOrderReqDTO();
        reqDTO.setIdempotencyKey(IDEMPOTENCY_KEY);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setPlantId(11L);
        reqDTO.setMatId(MAT_ID);
        reqDTO.setQty(new BigDecimal("100"));
        reqDTO.setUomId(61L);
        reqDTO.setDueDate(LocalDateTime.of(2026, 10, 1, 8, 0));

        prodOrderApi.createProdOrder(reqDTO);

        ArgumentCaptor<ProdOrderSaveReqVO> captor = ArgumentCaptor.forClass(ProdOrderSaveReqVO.class);
        verify(prodOrderService).createProdOrder(captor.capture());
        ProdOrderSaveReqVO saveReqVO = captor.getValue();
        // 四个 codegen 侧 @NotNull 而门面 DTO 允许留空的字段，落 0 而不是 null
        assertEquals(MesConstants.NONE_ID, saveReqVO.getDemandId());
        assertEquals(MesConstants.NONE_ID, saveReqVO.getWorkshopId());
        assertEquals(MesConstants.NONE_ID, saveReqVO.getProjectId());
        assertEquals(MesConstants.NONE_ID, saveReqVO.getProjectTaskId());
        assertEquals(0, saveReqVO.getPriority());
    }

    private void mockEffectiveVersions() {
        BomRespDTO bom = new BomRespDTO();
        bom.setId(BOM_ID);
        RouteRespDTO route = new RouteRespDTO();
        route.setId(ROUTE_ID);
        when(snapshotService.requireEffectiveBom(eq(MAT_ID), any(LocalDate.class))).thenReturn(bom);
        when(snapshotService.requireEffectiveRoute(eq(MAT_ID), any(LocalDate.class))).thenReturn(route);
    }

    @Test
    void testReleaseProdOrder_delegatesWithReasonMappedToRemark() {
        ProdOrderActionReqDTO reqDTO = actionReqDTO("产线已排好，下达");

        prodOrderApi.releaseProdOrder(reqDTO);

        ArgumentCaptor<MesReleaseOrderBO> captor = ArgumentCaptor.forClass(MesReleaseOrderBO.class);
        verify(prodOrderDomainService).releaseProdOrder(captor.capture());
        MesReleaseOrderBO bo = captor.getValue();
        assertEquals(99L, bo.getProdOrderId());
        // DTO 的 reason 必须落到 BO 的 remark 上，它是下达流水里唯一的人写说明
        assertEquals("产线已排好，下达", bo.getRemark());
        assertCommandFields(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo());
    }

    @Test
    void testCloseProdOrder_delegatesWithReasonAndNoWarehouseOverride() {
        ProdOrderActionReqDTO reqDTO = actionReqDTO("订单交付完成，关单");

        prodOrderApi.closeProdOrder(reqDTO);

        ArgumentCaptor<MesCloseOrderBO> captor = ArgumentCaptor.forClass(MesCloseOrderBO.class);
        verify(prodOrderDomainService).closeProdOrder(captor.capture());
        MesCloseOrderBO bo = captor.getValue();
        assertEquals(99L, bo.getProdOrderId());
        assertEquals("订单交付完成，关单", bo.getReason());
        assertCommandFields(bo.getIdempotencyKey(), bo.getSourceModule(), bo.getSourceBizNo());
        // 门面 DTO 没有仓库与库位，留空让倒冲反冲回落到倒冲物料自己的默认仓库
        assertNull(bo.getWhId());
        assertNull(bo.getLocId());
    }

    @Test
    void testGetProdOrder_carriesFourVersionFields() {
        ProdOrderDO prodOrder = ProdOrderDO.builder()
                .id(99L).orderNo("PO-0001").demandId(7L).plantId(11L).workshopId(21L)
                .matId(MAT_ID).matCode("MAT-1001").qty(new BigDecimal("100"))
                .completedQty(new BigDecimal("20")).scrapQty(new BigDecimal("1"))
                .bomId(BOM_ID).bomVersion("V1.0").routeId(ROUTE_ID).routeVersion("V2.0")
                .kitStatus("FULL_KIT").status(MesProdOrderStatusEnum.RELEASED.name())
                .build();
        when(prodOrderService.getProdOrderList(List.of(99L))).thenReturn(List.of(prodOrder));

        ProdOrderRespDTO respDTO = prodOrderApi.getProdOrder(99L);

        assertNotNull(respDTO);
        assertEquals("PO-0001", respDTO.getOrderNo());
        // 四个版本快照字段是 APS 排程与追溯的执行依据，缺一个都不行
        assertEquals(BOM_ID, respDTO.getBomId());
        assertEquals("V1.0", respDTO.getBomVersion());
        assertEquals(ROUTE_ID, respDTO.getRouteId());
        assertEquals("V2.0", respDTO.getRouteVersion());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), respDTO.getStatus());
        assertEquals("FULL_KIT", respDTO.getKitStatus());
        assertEquals(0, new BigDecimal("20").compareTo(respDTO.getCompletedQty()));
    }

    @Test
    void testGetProdOrder_nullIdAndEmptyIds() {
        assertNull(prodOrderApi.getProdOrder(null));
        assertTrue(prodOrderApi.getProdOrderList(List.of()).isEmpty());
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = ProdOrderApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        // 逐方法加会出现"加了一半"的漏洞，类级注解已经兜住全部方法
        for (Method method : ProdOrderApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

    private ProdOrderActionReqDTO actionReqDTO(String reason) {
        ProdOrderActionReqDTO reqDTO = new ProdOrderActionReqDTO();
        reqDTO.setIdempotencyKey(IDEMPOTENCY_KEY);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setProdOrderId(99L);
        reqDTO.setReason(reason);
        return reqDTO;
    }

    /** 幂等三段必须原样带到 BO 上：丢了任何一段，重复下发就会被当成一次新命令。 */
    private void assertCommandFields(String idempotencyKey, String sourceModule, String sourceBizNo) {
        assertEquals(IDEMPOTENCY_KEY, idempotencyKey);
        assertEquals(SOURCE_MODULE, sourceModule);
        assertEquals(SOURCE_BIZ_NO, sourceBizNo);
    }

}
