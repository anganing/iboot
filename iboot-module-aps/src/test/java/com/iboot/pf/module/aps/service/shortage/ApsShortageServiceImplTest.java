package com.iboot.pf.module.aps.service.shortage;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageHandleReqVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.shortage.ApsShortageMapper;
import com.iboot.pf.module.aps.enums.ApsShortageStatusEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
