package com.iboot.pf.module.wms.service.reservation;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.reservation.WmsInvReservationMapper;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReserveBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_AVAILABLE_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_NOT_ACTIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.RESERVATION_REMAIN_NOT_ENOUGH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/** 库存预留测试：可用量不足拒绝；重复提交返回原 ID；预留计入冻结后可用量扣减。 */
@Import({WmsInventoryReservationServiceImpl.class, WmsInventoryServiceImpl.class, WmsCodeService.class})
class WmsInventoryReservationServiceImplTest extends BaseDbUnitTest {

    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;

    @Resource
    private WmsInventoryReservationService reservationService;
    @Resource
    private WmsInvReservationMapper reservationMapper;
    @Resource
    private WmsInvBalanceMapper balanceMapper;
    @MockitoBean
    private CodeRuleApi codeRuleApi;

    /** 预置在库余额。 */
    private void seedBalance(BigDecimal onhand, BigDecimal frozen) {
        WmsInvBalanceDO balance = new WmsInvBalanceDO();
        balance.setInvOrgId(1L);
        balance.setPlantId(1L);
        balance.setWhId(WH);
        balance.setLocId(LOC);
        balance.setMatId(MAT);
        balance.setBatchId(BATCH);
        balance.setContainerId(0L);
        balance.setProjectId(0L);
        balance.setProjectTaskId(0L);
        balance.setStockScope("COMMON");
        balance.setQualityStatus("QUALIFIED");
        balance.setQtyOnhand(onhand);
        balance.setQtyFrozen(frozen == null ? BigDecimal.ZERO : frozen);
        balance.setQtyReserved(BigDecimal.ZERO);
        balance.setVersion(0);
        balanceMapper.insert(balance);
    }

    private WmsReserveBO reserveBO(BigDecimal qty) {
        WmsReserveBO bo = new WmsReserveBO();
        bo.setIdempotencyKey("WMS:RSV:1");
        bo.setBizType("PROD_RESERVE");
        bo.setBizNo("MO-1");
        bo.setBizLineNo("10");
        bo.setInvOrgId(1L);
        bo.setWhId(WH);
        bo.setLocId(LOC);
        bo.setMatId(MAT);
        bo.setBatchId(BATCH);
        bo.setQualityStatus("QUALIFIED");
        bo.setQty(qty);
        return bo;
    }

    private WmsReservationHandleBO releaseBO(Long reservationId, BigDecimal qty, String idemKey) {
        WmsReservationHandleBO bo = new WmsReservationHandleBO();
        bo.setIdempotencyKey(idemKey);
        bo.setReservationId(reservationId);
        bo.setQty(qty);
        return bo;
    }

    private WmsInvBalanceDO requireBalance() {
        return balanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
    }

    @Test
    void testReserve_availableEnough_succeedsAndDeductsAvailable() {
        seedBalance(new BigDecimal("100"), null);
        when(codeRuleApi.generate("WMS_RESERVATION_NO")).thenReturn("RSV-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");

        Long id = reservationService.reserve(reserveBO(new BigDecimal("30")));
        WmsInvReservationDO reservation = reservationMapper.selectById(id);
        assertEquals(WmsReservationStatusEnum.ACTIVE.name(), reservation.getStatus());

        // 预留后 qty_reserved 增加，可用量从 100 降到 70
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyReserved().compareTo(new BigDecimal("30")));
    }

    @Test
    void testReserve_idempotent_returnsSameId() {
        seedBalance(new BigDecimal("100"), null);
        when(codeRuleApi.generate("WMS_RESERVATION_NO")).thenReturn("RSV-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");

        Long id1 = reservationService.reserve(reserveBO(new BigDecimal("30")));
        Long id2 = reservationService.reserve(reserveBO(new BigDecimal("30")));
        assertEquals(id1, id2);
        // 只产生一条预留、一次 reserved 累加
        assertEquals(1, reservationMapper.selectCount());
        assertEquals(0, requireBalance().getQtyReserved().compareTo(new BigDecimal("30")));
    }

    @Test
    void testReserve_availableNotEnough_rejected() {
        // 可用仅 20（在库 100 冻结 80），预留 30 应被拒
        seedBalance(new BigDecimal("100"), new BigDecimal("80"));
        when(codeRuleApi.generate("WMS_RESERVATION_NO")).thenReturn("RSV-0001");

        assertServiceException(() -> reservationService.reserve(reserveBO(new BigDecimal("30"))),
                INV_AVAILABLE_QTY_NOT_ENOUGH);
        // 未写入预留，也未扣减
        assertEquals(0, reservationMapper.selectCount());
        assertEquals(0, requireBalance().getQtyReserved().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testRelease_partialAndFull() {
        seedBalance(new BigDecimal("100"), null);
        when(codeRuleApi.generate("WMS_RESERVATION_NO")).thenReturn("RSV-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        Long id = reservationService.reserve(reserveBO(new BigDecimal("50")));

        // 部分释放 20
        reservationService.release(releaseBO(id, new BigDecimal("20"), "WMS:RSV:1:REL:1"));
        WmsInvReservationDO partial = reservationMapper.selectById(id);
        assertEquals(WmsReservationStatusEnum.PARTIAL.name(), partial.getStatus());
        assertEquals(0, partial.getReleasedQty().compareTo(new BigDecimal("20")));
        assertEquals(0, requireBalance().getQtyReserved().compareTo(new BigDecimal("30")));

        // 释放超出剩余 → 拒绝（不同幂等键，走状态与余量校验）
        assertServiceException(() -> reservationService.release(releaseBO(id, new BigDecimal("40"), "WMS:RSV:1:REL:2")),
                RESERVATION_REMAIN_NOT_ENOUGH);
    }

    @Test
    void testRelease_full_thenRejected() {
        seedBalance(new BigDecimal("100"), null);
        when(codeRuleApi.generate("WMS_RESERVATION_NO")).thenReturn("RSV-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        Long id = reservationService.reserve(reserveBO(new BigDecimal("50")));

        reservationService.release(releaseBO(id, new BigDecimal("50"), "WMS:RSV:1:REL:1"));
        WmsInvReservationDO full = reservationMapper.selectById(id);
        assertEquals(WmsReservationStatusEnum.RELEASED.name(), full.getStatus());
        assertEquals(0, requireBalance().getQtyReserved().compareTo(BigDecimal.ZERO));

        // 已释放的预留不能再次释放（不同幂等键，触发状态校验）
        assertServiceException(() -> reservationService.release(releaseBO(id, new BigDecimal("10"), "WMS:RSV:1:REL:2")),
                RESERVATION_NOT_ACTIVE);
    }

}
