package com.iboot.pf.module.aps.service.projection.kit;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 物料齐套判定测试。
 *
 * <p>覆盖四件事：可用量够时判齐套、不够时算出缺口、同料被多个工单争用按交期先后分配、
 * 只认合格库存。
 */
@Import(ApsMaterialKitCalculator.class)
class ApsMaterialKitCalculatorTest extends BaseDbUnitTest {

    private static final Long SCENARIO_ID = 100L;
    private static final Long FINISHED_MAT_ID = 900L;
    private static final Long COMPONENT_MAT_ID = 901L;

    @Resource
    private ApsMaterialKitCalculator calculator;
    @Resource
    private ApsInventoryProjectionMapper inventoryProjectionMapper;

    @MockitoBean
    private BomApi bomApi;

    /** 单层 BOM：成品 900 用 2 个子件 901，无损耗，绑定首道工序。 */
    private void mockBom(BigDecimal lineQty, Integer routeOpSeq) {
        BomRespDTO bom = new BomRespDTO();
        bom.setId(1L);
        bom.setMatId(FINISHED_MAT_ID);
        when(bomApi.getEffectiveBom(anyLong(), any(LocalDate.class))).thenReturn(bom);
        BomLineRespDTO line = new BomLineRespDTO();
        line.setBomId(1L);
        line.setMatId(COMPONENT_MAT_ID);
        line.setQty(lineQty);
        line.setRouteOpSeq(routeOpSeq);
        when(bomApi.getBomLineList(1L)).thenReturn(List.of(line));
    }

    private void insertInventory(Long matId, String qtyAvailable, String qualityStatus) {
        inventoryProjectionMapper.insert(ApsInventoryProjectionDO.builder()
                .scenarioId(SCENARIO_ID)
                .sourceBalanceId(matId)
                .invOrgId(1L).plantId(1L).whId(1L).locId(1L)
                .matId(matId).matCode("MAT" + matId)
                .batchId(ApsConstants.NONE_ID).lotNo("")
                .projectId(ApsConstants.NONE_ID).projectTaskId(ApsConstants.NONE_ID)
                .stockScope("NORMAL").qualityStatus(qualityStatus)
                .qtyOnhand(new BigDecimal(qtyAvailable))
                .qtyReserved(ApsConstants.ZERO_QTY)
                .qtyFrozen(ApsConstants.ZERO_QTY)
                .qtyAvailable(new BigDecimal(qtyAvailable))
                .sourceUpdateTime(LocalDateTime.now())
                .snapshotTime(LocalDateTime.now())
                .build());
    }

    private ApsOrderProjectionDO orderRow(Long prodOrderId, String qty, LocalDateTime dueDate) {
        return ApsOrderProjectionDO.builder()
                .scenarioId(SCENARIO_ID)
                .sourceType("OP_TASK").sourceId(prodOrderId).sourceNo("OPT" + prodOrderId)
                .prodOrderId(prodOrderId).workOrderId(ApsConstants.NONE_ID).opTaskId(prodOrderId)
                .plantId(1L).workshopId(1L)
                .projectId(ApsConstants.NONE_ID).projectTaskId(ApsConstants.NONE_ID)
                .matId(FINISHED_MAT_ID).matCode("MAT900")
                .routeOpId(1L).seq(10).opId(10L).opCode("OP010").opName("装配")
                .resId(ApsConstants.NONE_ID)
                .qty(new BigDecimal(qty)).completedQty(ApsConstants.ZERO_QTY)
                .dueDate(dueDate).priority(0)
                .setupMinutes(0).stdMinutesPerUnit(BigDecimal.ONE).teardownMinutes(0)
                .queueMinutes(0).moveMinutes(0)
                .durationType("PER_UNIT").stdDuration(0).manQty(1).changeoverGroup("")
                .currentStatus("PENDING").lockedFlag(false)
                .sourceUpdateTime(LocalDateTime.now()).snapshotTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testFillMaterialShortage_enoughInventoryIsKitted() {
        mockBom(new BigDecimal("2"), 10);
        insertInventory(COMPONENT_MAT_ID, "100", WmsQualityStatusEnum.QUALIFIED.name());
        List<ApsOrderProjectionDO> rows = List.of(orderRow(1L, "10", LocalDateTime.of(2026, 9, 20, 8, 0)));

        calculator.fillMaterialShortage(SCENARIO_ID, rows);

        // 需求 2 × 10 = 20，库存 100，够
        assertFalse(rows.get(0).getMaterialShortageFlag());
        assertEquals(0, rows.get(0).getMaterialShortQty().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testFillMaterialShortage_computesShortQty() {
        mockBom(new BigDecimal("2"), 10);
        insertInventory(COMPONENT_MAT_ID, "15", WmsQualityStatusEnum.QUALIFIED.name());
        List<ApsOrderProjectionDO> rows = List.of(orderRow(1L, "10", LocalDateTime.of(2026, 9, 20, 8, 0)));

        calculator.fillMaterialShortage(SCENARIO_ID, rows);

        // 需求 20，库存 15，缺 5
        assertTrue(rows.get(0).getMaterialShortageFlag());
        assertEquals(0, rows.get(0).getMaterialShortQty().compareTo(new BigDecimal("5")));
    }

    @Test
    void testFillMaterialShortage_allocatesByDueDate() {
        mockBom(new BigDecimal("2"), 10);
        insertInventory(COMPONENT_MAT_ID, "20", WmsQualityStatusEnum.QUALIFIED.name());
        ApsOrderProjectionDO urgent = orderRow(1L, "10", LocalDateTime.of(2026, 9, 15, 8, 0));
        ApsOrderProjectionDO later = orderRow(2L, "10", LocalDateTime.of(2026, 9, 25, 8, 0));
        List<ApsOrderProjectionDO> rows = List.of(later, urgent);

        calculator.fillMaterialShortage(SCENARIO_ID, rows);

        // 库存只够一个工单：交期早的先占，晚的判缺料。各自独立判断会让两个工单都以为有料
        assertFalse(urgent.getMaterialShortageFlag());
        assertTrue(later.getMaterialShortageFlag());
        assertEquals(0, later.getMaterialShortQty().compareTo(new BigDecimal("20")));
    }

    @Test
    void testFillMaterialShortage_ignoresUnqualifiedInventory() {
        mockBom(new BigDecimal("2"), 10);
        // 待检库存不能拿来兑需求，否则算出来的齐套结论是假的
        insertInventory(COMPONENT_MAT_ID, "100", "PENDING_INSPECT");
        List<ApsOrderProjectionDO> rows = List.of(orderRow(1L, "10", LocalDateTime.of(2026, 9, 20, 8, 0)));

        calculator.fillMaterialShortage(SCENARIO_ID, rows);

        assertTrue(rows.get(0).getMaterialShortageFlag());
        assertEquals(0, rows.get(0).getMaterialShortQty().compareTo(new BigDecimal("20")));
    }

    @Test
    void testFillMaterialShortage_noBomIsKitted() {
        when(bomApi.getEffectiveBom(anyLong(), any(LocalDate.class))).thenReturn(null);
        List<ApsOrderProjectionDO> rows = List.of(orderRow(1L, "10", LocalDateTime.of(2026, 9, 20, 8, 0)));

        calculator.fillMaterialShortage(SCENARIO_ID, rows);

        // 没有 BOM 说明没有用料需求，不能因此判缺料
        assertFalse(rows.get(0).getMaterialShortageFlag());
    }

    @Test
    void testFillMaterialShortage_skipsLockedTask() {
        mockBom(new BigDecimal("2"), 10);
        insertInventory(COMPONENT_MAT_ID, "0", WmsQualityStatusEnum.QUALIFIED.name());
        ApsOrderProjectionDO locked = orderRow(1L, "10", LocalDateTime.of(2026, 9, 20, 8, 0));
        locked.setLockedFlag(true);
        List<ApsOrderProjectionDO> rows = List.of(locked);

        calculator.fillMaterialShortage(SCENARIO_ID, rows);

        // 已开工的工序料已经领了，不该被判缺料把它挪走
        assertFalse(locked.getMaterialShortageFlag());
    }

}
