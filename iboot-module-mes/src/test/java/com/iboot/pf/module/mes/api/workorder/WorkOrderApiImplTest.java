package com.iboot.pf.module.mes.api.workorder;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseMockitoUnitTest;
import com.iboot.pf.module.mes.api.workorder.dto.WorkOrderRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 作业工单只读门面委托测试。
 *
 * <p>三个方法都是转调 + DTO 转换，断言盯的是"字段有没有一个漏掉"：作业工单的段产出数量、
 * 计划与实际起止四个时间、齐套状态与工单状态都是跨模块调用方判断"这一段做到哪了"的依据。
 */
class WorkOrderApiImplTest extends BaseMockitoUnitTest {

    private static final Long WORK_ORDER_ID = 66L;
    private static final Long PROD_ORDER_ID = 99L;

    @InjectMocks
    private WorkOrderApiImpl workOrderApi;

    @Mock
    private WorkOrderService workOrderService;

    @Test
    void testGetWorkOrder_mapsAllFacts() {
        when(workOrderService.getWorkOrderList(List.of(WORK_ORDER_ID))).thenReturn(List.of(workOrderDO()));

        WorkOrderRespDTO respDTO = workOrderApi.getWorkOrder(WORK_ORDER_ID);

        assertNotNull(respDTO);
        assertEquals(WORK_ORDER_ID, respDTO.getId());
        assertEquals("WO-0001", respDTO.getWorkOrderNo());
        assertEquals(PROD_ORDER_ID, respDTO.getProdOrderId());
        assertEquals("PO-0001", respDTO.getProdOrderNo());
        assertEquals(11L, respDTO.getPlantId());
        assertEquals(21L, respDTO.getWorkshopId());
        assertEquals(31L, respDTO.getWcId());
        assertEquals(0, new BigDecimal("100").compareTo(respDTO.getPlannedQty()));
        assertEquals(0, new BigDecimal("60").compareTo(respDTO.getCompletedQty()));
        assertEquals(LocalDateTime.of(2026, 9, 1, 8, 0), respDTO.getPlannedStartTime());
        assertEquals(LocalDateTime.of(2026, 9, 2, 17, 0), respDTO.getPlannedEndTime());
        assertEquals(LocalDateTime.of(2026, 9, 1, 8, 30), respDTO.getActualStartTime());
        assertNull(respDTO.getActualEndTime());
        assertEquals("FULL_KIT", respDTO.getKitStatus());
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(), respDTO.getStatus());
    }

    @Test
    void testGetWorkOrderList_delegatesInBatch() {
        when(workOrderService.getWorkOrderList(List.of(WORK_ORDER_ID))).thenReturn(List.of(workOrderDO()));

        List<WorkOrderRespDTO> list = workOrderApi.getWorkOrderList(List.of(WORK_ORDER_ID));

        assertEquals(1, list.size());
        assertEquals("WO-0001", list.get(0).getWorkOrderNo());
    }

    @Test
    void testGetWorkOrderListByProdOrderIds_delegatesInBatch() {
        when(workOrderService.getWorkOrderListByProdOrderIds(List.of(PROD_ORDER_ID)))
                .thenReturn(List.of(workOrderDO()));

        List<WorkOrderRespDTO> list = workOrderApi.getWorkOrderListByProdOrderIds(List.of(PROD_ORDER_ID));

        assertEquals(1, list.size());
        assertEquals(PROD_ORDER_ID, list.get(0).getProdOrderId());
    }

    @Test
    void testEmptyInputShortCircuits() {
        assertNull(workOrderApi.getWorkOrder(null));
        assertTrue(workOrderApi.getWorkOrderList(List.of()).isEmpty());
        assertTrue(workOrderApi.getWorkOrderListByProdOrderIds(List.of()).isEmpty());
        // 空集合不该打到 DB：批量门面被空集合调到是常态（上游没有可查的编号）
        verifyNoInteractions(workOrderService);
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = WorkOrderApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : WorkOrderApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

    private WorkOrderDO workOrderDO() {
        return WorkOrderDO.builder()
                .id(WORK_ORDER_ID).workOrderNo("WO-0001")
                .prodOrderId(PROD_ORDER_ID).prodOrderNo("PO-0001")
                .plantId(11L).workshopId(21L).wcId(31L)
                .plannedQty(new BigDecimal("100")).completedQty(new BigDecimal("60"))
                .plannedStartTime(LocalDateTime.of(2026, 9, 1, 8, 0))
                .plannedEndTime(LocalDateTime.of(2026, 9, 2, 17, 0))
                .actualStartTime(LocalDateTime.of(2026, 9, 1, 8, 30))
                .kitStatus("FULL_KIT").status(MesWorkOrderStatusEnum.RUNNING.name())
                .build();
    }

}
