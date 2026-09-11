package com.iboot.pf.module.aps.service.shortage;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageHandleReqVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsOrderProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.shortage.ApsShortageMapper;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.enums.ApsShortageStatusEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.projection.kit.ApsMaterialKitCalculator;
import com.iboot.pf.module.aps.service.projection.kit.ApsMaterialKitCalculator.MaterialRequirementAllocation;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SHORTAGE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 排程缺料 Service 测试。
 *
 * <p>重点验证刷新逻辑：可用量只认合格库存，短缺数量不为负，已忽略的记录不被重算拉回。
 */
@Import({ApsShortageServiceImpl.class, ApsCommandServiceImpl.class})
class ApsShortageServiceImplTest extends BaseDbUnitTest {

    private static final Long VERSION_ID = 100L;
    private static final Long SCENARIO_ID = 10L;
    private static final Long MAT_ID = 1000L;

    @Resource
    private ApsShortageService shortageService;
    @Resource
    private ApsShortageMapper shortageMapper;
    @Resource
    private ApsInventoryProjectionMapper inventoryProjectionMapper;

    @MockitoBean
    private ApsVersionService versionService;
    @MockitoBean
    private ApsRelationFiller relationFiller;
    @MockitoBean
    private ApsTaskMapper taskMapper;
    @MockitoBean
    private ApsOrderProjectionMapper orderProjectionMapper;
    @MockitoBean
    private ApsMaterialKitCalculator materialKitCalculator;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private UomApi uomApi;

    @BeforeEach
    void mockVersion() {
        when(versionService.getVersionDO(anyLong())).thenReturn(ApsVersionDO.builder()
                .id(VERSION_ID).scenarioId(SCENARIO_ID).versionNo(1)
                .status(ApsVersionStatusEnum.SOLVED.name()).version(0).build());
    }

    private Long insertShortage(BigDecimal reqQty, String status) {
        ApsShortageDO shortage = ApsShortageDO.builder()
                .versionId(VERSION_ID).prodOrderId(0L).opTaskId(0L).projectId(0L).projectTaskId(0L)
                .matId(MAT_ID).matCode("M001").matName("钢板")
                .reqQty(reqQty).availQty(BigDecimal.ZERO).shortQty(reqQty)
                .uomId(1L).uomCode("KG").needDate(LocalDateTime.of(2026, 8, 5, 8, 0))
                .status(status)
                .build();
        shortageMapper.insert(shortage);
        return shortage.getId();
    }

    private void insertInventory(BigDecimal qtyAvailable, String qualityStatus) {
        inventoryProjectionMapper.insert(ApsInventoryProjectionDO.builder()
                .scenarioId(SCENARIO_ID).sourceBalanceId(randomLongId())
                .invOrgId(1L).plantId(1L).whId(1L).locId(1L)
                .matId(MAT_ID).matCode("M001").batchId(0L).lotNo("")
                .projectId(0L).projectTaskId(0L).stockScope("COMMON").qualityStatus(qualityStatus)
                .qtyOnhand(qtyAvailable).qtyReserved(BigDecimal.ZERO).qtyFrozen(BigDecimal.ZERO)
                .qtyAvailable(qtyAvailable)
                .sourceUpdateTime(LocalDateTime.now()).snapshotTime(LocalDateTime.now())
                .build());
    }

    private RefreshShortageReqDTO buildCommand(String idempotencyKey) {
        RefreshShortageReqDTO reqDTO = new RefreshShortageReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(VERSION_ID));
        reqDTO.setVersionId(VERSION_ID);
        return reqDTO;
    }

    @Test
    void testRefreshShortage_partialWhenSomeStock() {
        Long id = insertShortage(new BigDecimal("100"), ApsShortageStatusEnum.OPEN.name());
        insertInventory(new BigDecimal("60"), WmsQualityStatusEnum.QUALIFIED.name());

        shortageService.refreshShortage(buildCommand("UI:REFRESH:1"));

        ApsShortageDO entity = shortageMapper.selectById(id);
        assertEquals(0, new BigDecimal("60").compareTo(entity.getAvailQty()));
        assertEquals(0, new BigDecimal("40").compareTo(entity.getShortQty()));
        assertEquals(ApsShortageStatusEnum.PARTIAL.name(), entity.getStatus());
    }

    @Test
    void testRefreshShortage_resolvedWhenStockEnough() {
        Long id = insertShortage(new BigDecimal("100"), ApsShortageStatusEnum.OPEN.name());
        insertInventory(new BigDecimal("120"), WmsQualityStatusEnum.QUALIFIED.name());

        shortageService.refreshShortage(buildCommand("UI:REFRESH:1"));

        ApsShortageDO entity = shortageMapper.selectById(id);
        // 库存富余时短缺数量取 0，不能出现负数
        assertEquals(0, BigDecimal.ZERO.compareTo(entity.getShortQty()));
        assertEquals(ApsShortageStatusEnum.RESOLVED.name(), entity.getStatus());
    }

    @Test
    void testRefreshShortage_ignoreNonQualifiedStock() {
        Long id = insertShortage(new BigDecimal("100"), ApsShortageStatusEnum.OPEN.name());
        insertInventory(new BigDecimal("80"), WmsQualityStatusEnum.QC_PENDING.name());
        insertInventory(new BigDecimal("50"), WmsQualityStatusEnum.REJECTED.name());

        shortageService.refreshShortage(buildCommand("UI:REFRESH:1"));

        ApsShortageDO entity = shortageMapper.selectById(id);
        // 待检与不合格库存不能拿来兑需求
        assertEquals(0, BigDecimal.ZERO.compareTo(entity.getAvailQty()));
        assertEquals(ApsShortageStatusEnum.OPEN.name(), entity.getStatus());
    }

    @Test
    void testRefreshShortage_keepIgnoredStatus() {
        Long id = insertShortage(new BigDecimal("100"), ApsShortageStatusEnum.IGNORED.name());
        insertInventory(new BigDecimal("10"), WmsQualityStatusEnum.QUALIFIED.name());

        shortageService.refreshShortage(buildCommand("UI:REFRESH:1"));

        // 人工判断过“这条不用管”，重算不应该把它拉回未解决
        assertEquals(ApsShortageStatusEnum.IGNORED.name(), shortageMapper.selectById(id).getStatus());
    }

    @Test
    void testRefreshShortage_rebuildsFromVersionTaskProjection() {
        Long opTaskId = 88L;
        LocalDateTime planStart = LocalDateTime.of(2026, 9, 20, 8, 0);
        ApsTaskDO task = ApsTaskDO.builder()
                .versionId(VERSION_ID).opTaskId(opTaskId).startTime(planStart).build();
        ApsOrderProjectionDO projection = ApsOrderProjectionDO.builder()
                .prodOrderId(66L).opTaskId(opTaskId).projectId(0L).projectTaskId(0L)
                .dueDate(planStart.plusDays(2)).build();
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(MAT_ID);
        material.setCode("M001");
        material.setName("钢板");
        material.setBaseUomId(1L);
        UomRespDTO uom = new UomRespDTO();
        uom.setId(1L);
        uom.setCode("KG");
        when(taskMapper.selectListByVersionId(VERSION_ID)).thenReturn(List.of(task));
        when(orderProjectionMapper.selectListByScenario(SCENARIO_ID, null, null, null, null))
                .thenReturn(List.of(projection));
        when(materialKitCalculator.calculateRequirementAllocations(SCENARIO_ID, List.of(projection)))
                .thenReturn(List.of(new MaterialRequirementAllocation(projection, MAT_ID,
                        new BigDecimal("100"), new BigDecimal("60"), new BigDecimal("40"))));
        when(materialApi.getMaterialMap(org.mockito.ArgumentMatchers.any())).thenReturn(Map.of(MAT_ID, material));
        when(uomApi.getUomMap(org.mockito.ArgumentMatchers.any())).thenReturn(Map.of(1L, uom));

        shortageService.refreshShortage(buildCommand("UI:REFRESH:REBUILD"));

        List<ApsShortageDO> rows = shortageMapper.selectListByVersionId(VERSION_ID);
        assertEquals(1, rows.size());
        assertEquals(opTaskId, rows.get(0).getOpTaskId());
        assertEquals(0, new BigDecimal("100").compareTo(rows.get(0).getReqQty()));
        assertEquals(0, new BigDecimal("40").compareTo(rows.get(0).getShortQty()));
        assertEquals(planStart, rows.get(0).getNeedDate());
    }

    @Test
    void testRefreshShortage_preservesManualHandlingForSameShortage() {
        Long opTaskId = 89L;
        LocalDateTime planStart = LocalDateTime.of(2026, 9, 21, 8, 0);
        LocalDateTime expectedSupplyDate = LocalDateTime.of(2026, 9, 22, 8, 0);
        ApsTaskDO task = ApsTaskDO.builder()
                .versionId(VERSION_ID).opTaskId(opTaskId).startTime(planStart).build();
        ApsOrderProjectionDO projection = ApsOrderProjectionDO.builder()
                .prodOrderId(67L).opTaskId(opTaskId).projectId(0L).projectTaskId(0L)
                .dueDate(planStart.plusDays(2)).build();
        shortageMapper.insert(ApsShortageDO.builder()
                .versionId(VERSION_ID).prodOrderId(67L).opTaskId(opTaskId).projectId(0L).projectTaskId(0L)
                .matId(MAT_ID).matCode("M001").matName("钢板")
                .reqQty(new BigDecimal("100")).availQty(BigDecimal.ZERO).shortQty(new BigDecimal("100"))
                .uomId(1L).uomCode("KG").needDate(planStart)
                .expectedSupplyDate(expectedSupplyDate).remark("采购已确认")
                .status(ApsShortageStatusEnum.IGNORED.name()).build());
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(MAT_ID);
        material.setCode("M001");
        material.setName("钢板");
        material.setBaseUomId(1L);
        UomRespDTO uom = new UomRespDTO();
        uom.setId(1L);
        uom.setCode("KG");
        when(taskMapper.selectListByVersionId(VERSION_ID)).thenReturn(List.of(task));
        when(orderProjectionMapper.selectListByScenario(SCENARIO_ID, null, null, null, null))
                .thenReturn(List.of(projection));
        when(materialKitCalculator.calculateRequirementAllocations(SCENARIO_ID, List.of(projection)))
                .thenReturn(List.of(new MaterialRequirementAllocation(projection, MAT_ID,
                        new BigDecimal("100"), new BigDecimal("20"), new BigDecimal("80"))));
        when(materialApi.getMaterialMap(org.mockito.ArgumentMatchers.any())).thenReturn(Map.of(MAT_ID, material));
        when(uomApi.getUomMap(org.mockito.ArgumentMatchers.any())).thenReturn(Map.of(1L, uom));

        shortageService.refreshShortage(buildCommand("UI:REFRESH:PRESERVE"));

        ApsShortageDO row = shortageMapper.selectListByVersionId(VERSION_ID).get(0);
        assertEquals(ApsShortageStatusEnum.IGNORED.name(), row.getStatus());
        assertEquals(expectedSupplyDate, row.getExpectedSupplyDate());
        assertEquals("采购已确认", row.getRemark());
    }

    @Test
    void testHandleShortage_writeExpectedSupplyDate() {
        Long id = insertShortage(new BigDecimal("100"), ApsShortageStatusEnum.OPEN.name());

        ShortageHandleReqVO handleReqVO = new ShortageHandleReqVO();
        handleReqVO.setId(id);
        handleReqVO.setExpectedSupplyDate(LocalDateTime.of(2026, 8, 3, 10, 0));
        handleReqVO.setRemark("供应商已排产");
        shortageService.handleShortage(handleReqVO);

        ApsShortageDO entity = shortageMapper.selectById(id);
        assertEquals(LocalDateTime.of(2026, 8, 3, 10, 0), entity.getExpectedSupplyDate());
        assertEquals("供应商已排产", entity.getRemark());
    }

    @Test
    void testIgnoreShortage_success() {
        Long id = insertShortage(new BigDecimal("100"), ApsShortageStatusEnum.OPEN.name());

        shortageService.ignoreShortage(id);

        assertEquals(ApsShortageStatusEnum.IGNORED.name(), shortageMapper.selectById(id).getStatus());
    }

    @Test
    void testGetShortage_notExists() {
        assertServiceException(() -> shortageService.getShortage(randomLongId()), SHORTAGE_NOT_EXISTS);
    }

}
