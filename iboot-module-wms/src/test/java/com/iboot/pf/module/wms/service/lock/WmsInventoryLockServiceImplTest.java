package com.iboot.pf.module.wms.service.lock;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.lock.WmsInvLockDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.lock.WmsInvLockMapper;
import com.iboot.pf.module.wms.enums.WmsLockStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStockScopeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.lock.bo.WmsLockBO;
import com.iboot.pf.module.wms.service.lock.bo.WmsUnlockBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_NOT_ACTIVE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_REASON_BLANK;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOCK_REMAIN_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * 库存冻结测试：冻结占用可用量、不扣在库量；解冻按数量归还；两个动作都幂等。
 *
 * <p>本类是 T0-17 删除 codegen {@code InvLockService} 写入方法后补的覆盖。冻结直接决定可用量
 * （可用量 = 在库 − 预留 − 冻结），漏一条就等于"该动不了的货照样能被领走"，所以每个用例都用
 * 生产代码的 {@link WmsInventoryService#availableQtyOf} 算可用量，不在测试里另写一份公式。
 */
@Import({WmsInventoryLockServiceImpl.class, WmsInventoryServiceImpl.class, WmsCodeService.class})
class WmsInventoryLockServiceImplTest extends BaseDbUnitTest {

    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;
    /** 该维度上没有余额行，用于验证"无库存不许冻结"。 */
    private static final Long MAT_WITHOUT_BALANCE = 999L;

    @Resource
    private WmsInventoryLockService lockService;
    @Resource
    private WmsInvLockMapper lockMapper;
    @Resource
    private WmsInvBalanceMapper balanceMapper;
    @Resource
    private WmsInvTxnMapper txnMapper;
    @MockitoBean
    private CodeRuleApi codeRuleApi;

    @BeforeEach
    void setUp() {
        when(codeRuleApi.generate("WMS_LOCK_NO")).thenReturn("LOCK-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
    }

    /** 预置在库余额。 */
    private void seedBalance(BigDecimal onhand, BigDecimal frozen, BigDecimal reserved) {
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
        balance.setStockScope(WmsStockScopeEnum.COMMON.name());
        balance.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        balance.setQtyOnhand(onhand);
        balance.setQtyFrozen(frozen == null ? BigDecimal.ZERO : frozen);
        balance.setQtyReserved(reserved == null ? BigDecimal.ZERO : reserved);
        balance.setVersion(0);
        balanceMapper.insert(balance);
    }

    private WmsLockBO lockBO(BigDecimal qty, String idemKey) {
        WmsLockBO bo = new WmsLockBO();
        bo.setIdempotencyKey(idemKey);
        bo.setBizType("QC_HOLD");
        bo.setBizNo("QC-1");
        bo.setInvOrgId(1L);
        bo.setWhId(WH);
        bo.setLocId(LOC);
        bo.setMatId(MAT);
        bo.setBatchId(BATCH);
        bo.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        bo.setQty(qty);
        bo.setReason("质量待判");
        return bo;
    }

    private WmsUnlockBO unlockBO(Long lockId, BigDecimal qty, String idemKey) {
        WmsUnlockBO bo = new WmsUnlockBO();
        bo.setIdempotencyKey(idemKey);
        bo.setLockId(lockId);
        bo.setQty(qty);
        return bo;
    }

    private WmsInvBalanceDO requireBalance() {
        return balanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
    }

    /** 可用量：现存 − 预留 − 冻结，走生产代码的公式。 */
    private BigDecimal availableQty() {
        return WmsInventoryService.availableQtyOf(requireBalance());
    }

    @Test
    void testLock_freezesAndCutsAvailableQty() {
        seedBalance(new BigDecimal("500"), null, null);
        // 先钉住冻结前的绝对值，否则 before 本身算错会出现"两边都错但相等"的假绿
        assertEquals(0, availableQty().compareTo(new BigDecimal("500")));

        Long id = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));

        WmsInvLockDO lock = lockMapper.selectById(id);
        assertNotNull(lock);
        assertEquals(WmsLockStatusEnum.ACTIVE.name(), lock.getStatus());
        assertEquals(0, lock.getQty().compareTo(new BigDecimal("120")));
        assertEquals(0, lock.getReleasedQty().compareTo(BigDecimal.ZERO));

        // 冻结只占用可用量：在库量一分不动，可用量按冻结量下降
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("500")));
        assertEquals(0, balance.getQtyFrozen().compareTo(new BigDecimal("120")));
        assertEquals(0, availableQty().compareTo(new BigDecimal("380")));
        // 一次冻结一条 FREEZE 流水
        assertEquals(1, txnMapper.selectCount());
    }

    @Test
    void testLock_idempotent_notAccumulated() {
        seedBalance(new BigDecimal("500"), null, null);

        Long first = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));
        Long second = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));

        // 同一幂等键只冻结一次：返回原记录，冻结量不叠加成 240
        assertEquals(first, second);
        assertEquals(1, lockMapper.selectCount());
        assertEquals(0, requireBalance().getQtyFrozen().compareTo(new BigDecimal("120")));
        assertEquals(0, availableQty().compareTo(new BigDecimal("380")));
        assertEquals(1, txnMapper.selectCount());
    }

    @Test
    void testLock_balanceNotExists_rejected() {
        seedBalance(new BigDecimal("500"), null, null);
        WmsLockBO bo = lockBO(new BigDecimal("10"), "UI:1:LOCK:0");
        bo.setMatId(MAT_WITHOUT_BALANCE);

        // 该维度上根本没有余额行，冻结无从谈起
        assertServiceException(() -> lockService.lock(bo), INV_BALANCE_NOT_EXISTS);
        // 过账与冻结记录同事务，拦下后不许留下一条"占着可用量却没有货"的冻结
        assertEquals(0, lockMapper.selectCount());
        assertEquals(0, txnMapper.selectCount());
        assertEquals(0, availableQty().compareTo(new BigDecimal("500")));
    }

    @Test
    void testLock_qtyNotPositive_rejected() {
        seedBalance(new BigDecimal("500"), null, null);

        assertServiceException(() -> lockService.lock(lockBO(BigDecimal.ZERO, "UI:1:LOCK:0")),
                QTY_MUST_BE_POSITIVE);
        assertEquals(0, lockMapper.selectCount());
        assertEquals(0, availableQty().compareTo(new BigDecimal("500")));
    }

    @Test
    void testLock_blankReason_rejected() {
        seedBalance(new BigDecimal("500"), null, null);
        WmsLockBO bo = lockBO(new BigDecimal("120"), "UI:1:LOCK:0");
        bo.setReason(null);

        // 冻结原因是解冻时唯一的判断依据，缺了就是一笔说不清的占用
        assertServiceException(() -> lockService.lock(bo), LOCK_REASON_BLANK);
        assertEquals(0, lockMapper.selectCount());
        assertEquals(0, availableQty().compareTo(new BigDecimal("500")));
    }

    @Test
    void testUnlock_releasesAndRestoresAvailableQty() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));
        assertEquals(0, availableQty().compareTo(new BigDecimal("380")));

        // 不传数量表示解冻全部剩余
        lockService.unlock(unlockBO(id, null, "UI:1:UNLOCK:0"));

        WmsInvLockDO lock = lockMapper.selectById(id);
        assertEquals(WmsLockStatusEnum.RELEASED.name(), lock.getStatus());
        assertEquals(0, lock.getReleasedQty().compareTo(new BigDecimal("120")));

        // 冻结量归零、可用量回到冻结前，在库量始终没变
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyFrozen().compareTo(BigDecimal.ZERO));
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("500")));
        assertEquals(0, availableQty().compareTo(new BigDecimal("500")));
        // FREEZE + UNFREEZE 两条流水
        assertEquals(2, txnMapper.selectCount());
    }

    @Test
    void testUnlock_idempotent_notAccumulated() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));

        lockService.unlock(unlockBO(id, new BigDecimal("50"), "UI:1:UNLOCK:0"));
        lockService.unlock(unlockBO(id, new BigDecimal("50"), "UI:1:UNLOCK:0"));

        // 同一幂等键只解冻一次：释放量不叠加成 100，冻结量也不被扣两遍
        WmsInvLockDO lock = lockMapper.selectById(id);
        assertEquals(0, lock.getReleasedQty().compareTo(new BigDecimal("50")));
        assertEquals(WmsLockStatusEnum.PARTIAL.name(), lock.getStatus());
        assertEquals(0, requireBalance().getQtyFrozen().compareTo(new BigDecimal("70")));
        assertEquals(0, availableQty().compareTo(new BigDecimal("430")));
        assertEquals(2, txnMapper.selectCount());
    }

    @Test
    void testUnlock_partialThenRemainNotEnough() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));

        lockService.unlock(unlockBO(id, new BigDecimal("50"), "UI:1:UNLOCK:1"));
        assertEquals(WmsLockStatusEnum.PARTIAL.name(), lockMapper.selectById(id).getStatus());
        assertEquals(0, availableQty().compareTo(new BigDecimal("430")));

        // 剩余只有 70，再解 80 必须被拦（换幂等键才会真正执行动作、走到余量校验）
        assertServiceException(() -> lockService.unlock(unlockBO(id, new BigDecimal("80"), "UI:1:UNLOCK:2")),
                LOCK_REMAIN_NOT_ENOUGH);
        assertEquals(0, requireBalance().getQtyFrozen().compareTo(new BigDecimal("70")));
    }

    @Test
    void testUnlock_releasedLock_rejected() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = lockService.lock(lockBO(new BigDecimal("120"), "UI:1:LOCK:0"));
        lockService.unlock(unlockBO(id, null, "UI:1:UNLOCK:1"));

        // 已解冻的记录不能再解冻，否则冻结量会被扣成负数
        assertServiceException(() -> lockService.unlock(unlockBO(id, new BigDecimal("10"), "UI:1:UNLOCK:2")),
                LOCK_NOT_ACTIVE);
        assertEquals(0, requireBalance().getQtyFrozen().compareTo(BigDecimal.ZERO));
    }

}
