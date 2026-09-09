package com.iboot.pf.module.wms.service.inventory;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_ONHAND_QTY_NOT_ENOUGH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * 库存过账核心规则测试。
 *
 * <p>覆盖：可用量公式、入库/出库过账、幂等、乐观锁版本冲突、余额不足拒绝。
 */
@Import({WmsInventoryServiceImpl.class, WmsCodeService.class})
class WmsInventoryServiceImplTest extends BaseDbUnitTest {

    private static final Long INV_ORG = 1L;
    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;
    private static final Long UOM = 1L;

    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsInvBalanceMapper balanceMapper;
    @Resource
    private WmsInvTxnMapper txnMapper;
    @Resource
    private WmsWhMapper whMapper;
    @MockitoBean
    private CodeRuleApi codeRuleApi;

    @BeforeEach
    void setUpWarehouse() {
        WmsWhDO wh = new WmsWhDO();
        wh.setId(WH);
        wh.setCode("WH-001");
        whMapper.insert(wh);
    }

    /** 构造一次入库过账并返回已入库的余额。 */
    private WmsInvBalanceDO postIn(BigDecimal qty) {
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        WmsInvPostBO bo = basePost();
        bo.setTxnType("RECEIPT");
        bo.setIdempotencyKey("WMS:TEST:IN:" + qty);
        bo.setQty(qty);
        bo.setToLocId(LOC);
        bo.setQualityStatusTo("QUALIFIED");
        inventoryService.post(bo);
        return requireBalance();
    }

    private WmsInvBalanceDO requireBalance() {
        WmsInvBalanceDO dim = new WmsInvBalanceDO();
        dim.setInvOrgId(INV_ORG);
        dim.setPlantId(1L);
        dim.setWhId(WH);
        dim.setLocId(LOC);
        dim.setMatId(MAT);
        dim.setBatchId(BATCH);
        dim.setContainerId(0L);
        dim.setProjectId(0L);
        dim.setProjectTaskId(0L);
        dim.setStockScope("COMMON");
        dim.setQualityStatus("QUALIFIED");
        WmsInvBalanceDO balance = balanceMapper.selectByDim(dim);
        assertNotNull(balance, "过账后余额应存在");
        return balance;
    }

    private WmsInvPostBO basePost() {
        WmsInvPostBO bo = new WmsInvPostBO();
        bo.setBizType("TEST");
        bo.setBizNo("BIZ-1");
        bo.setBizLineNo("10");
        bo.setInvOrgId(INV_ORG);
        bo.setPlantId(1L);
        bo.setWhId(WH);
        bo.setMatId(MAT);
        bo.setBatchId(BATCH);
        bo.setContainerId(0L);
        bo.setUomId(UOM);
        bo.setMatCode("M-001");
        bo.setMatName("测试物料");
        bo.setUomCode("EA");
        bo.setLotNo("L-001");
        bo.setToLocCode("LOC-001");
        return bo;
    }

    @Test
    void testIn_post_writesTxnAndUpdatesBalance() {
        WmsInvBalanceDO balance = postIn(new BigDecimal("100"));
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("100")));
        assertEquals(1, balance.getVersion());
        assertEquals("M-001", balance.getMatCode());
        assertEquals("测试物料", balance.getMatName());
        assertEquals("EA", balance.getUomCode());
        assertEquals("L-001", balance.getLotNo());
        assertEquals("WH-001", balance.getWhCode());
        assertEquals("LOC-001", balance.getLocCode());

        WmsInvTxnDO txn = txnMapper.selectByIdempotencyKey("WMS:TEST:IN:100");
        assertNotNull(txn);
        assertEquals(0, txn.getQty().compareTo(new BigDecimal("100")));
        assertEquals("IN", txn.getIoType());
    }

    @Test
    void testIn_existingBalance_snapshotNotOverwritten() {
        // 预插一条快照列与过账参数完全不同的余额行，模拟历史上落库的旧快照
        WmsInvBalanceDO existing = new WmsInvBalanceDO();
        existing.setInvOrgId(INV_ORG);
        existing.setPlantId(1L);
        existing.setWhId(WH);
        existing.setLocId(LOC);
        existing.setMatId(MAT);
        existing.setBatchId(BATCH);
        existing.setContainerId(0L);
        existing.setProjectId(0L);
        existing.setProjectTaskId(0L);
        existing.setStockScope("COMMON");
        existing.setQualityStatus("QUALIFIED");
        existing.setMatCode("OLD-CODE");
        existing.setMatName("旧物料名");
        existing.setUomCode("KG");
        existing.setLotNo("OLD-LOT");
        existing.setWhCode("WH-OLD");
        existing.setLocCode("LOC-OLD");
        existing.setQtyOnhand(BigDecimal.ZERO);
        existing.setQtyReserved(BigDecimal.ZERO);
        existing.setQtyFrozen(BigDecimal.ZERO);
        existing.setVersion(0);
        balanceMapper.insert(existing);

        // 过账参数携带新快照：入方向命中的是已有余额行，快照不应被覆盖
        postIn(new BigDecimal("50"));

        WmsInvBalanceDO balance = requireBalance();
        assertEquals(existing.getId(), balance.getId(), "应命中已有余额行而不是新建");
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("50")));
        assertEquals("OLD-CODE", balance.getMatCode());
        assertEquals("旧物料名", balance.getMatName());
        assertEquals("KG", balance.getUomCode());
        assertEquals("OLD-LOT", balance.getLotNo());
        assertEquals("WH-OLD", balance.getWhCode());
        assertEquals("LOC-OLD", balance.getLocCode());
    }

    @Test
    void testIn_idempotent_returnsOriginalTxn() {
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        WmsInvPostBO bo = basePost();
        bo.setTxnType("RECEIPT");
        bo.setIdempotencyKey("WMS:TEST:IDEM");
        bo.setQty(new BigDecimal("50"));
        bo.setToLocId(LOC);
        bo.setQualityStatusTo("QUALIFIED");

        WmsInvTxnDO first = inventoryService.post(bo);
        WmsInvTxnDO second = inventoryService.post(bo);
        // 幂等命中返回同一条流水（同 ID）
        assertEquals(first.getId(), second.getId());
        // 幂等命中不应重复累加余额
        assertEquals(0, requireBalance().getQtyOnhand().compareTo(new BigDecimal("50")));
        assertEquals(1, txnMapper.selectCount());
    }

    @Test
    void testAvailable_reservedAndFrozenDeducted() {
        // 入库 100
        WmsInvBalanceDO balance = postIn(new BigDecimal("100"));
        // 冻结 20、预留 30
        balance.setQtyFrozen(new BigDecimal("20"));
        balance.setQtyReserved(new BigDecimal("30"));
        balanceMapper.updateById(balance);

        BigDecimal available = inventoryService.getAvailableQty(baseQuery());
        // 可用 = 100 - 30 - 20 = 50
        assertEquals(0, available.compareTo(new BigDecimal("50")));
    }

    @Test
    void testOut_notEnoughOnhand_rejected() {
        postIn(new BigDecimal("10"));
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0002");

        WmsInvPostBO bo = basePost();
        bo.setTxnType("ISSUE");
        bo.setIdempotencyKey("WMS:TEST:OUT:100");
        bo.setQty(new BigDecimal("100"));
        bo.setFromLocId(LOC);
        bo.setQualityStatusFrom("QUALIFIED");
        bo.setQualityStatusTo("QUALIFIED");
        assertServiceException(() -> inventoryService.post(bo), INV_ONHAND_QTY_NOT_ENOUGH);
    }

    @Test
    void testOut_availableNotEnough_postStillSucceeds() {
        // 入库 100，但冻结 80 → 可用仅 20
        WmsInvBalanceDO balance = postIn(new BigDecimal("100"));
        balance.setQtyFrozen(new BigDecimal("80"));
        balanceMapper.updateById(balance);
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0003");

        WmsInvPostBO bo = basePost();
        bo.setTxnType("ISSUE");
        bo.setIdempotencyKey("WMS:TEST:OUT:60");
        bo.setQty(new BigDecimal("60"));
        bo.setFromLocId(LOC);
        bo.setQualityStatusFrom("QUALIFIED");
        bo.setQualityStatusTo("QUALIFIED");
        // 过账本身只校验在库量，可用量不足由出库单 Service 层（getAvailableQty）拦截
        inventoryService.post(bo);
        assertEquals(0, requireBalance().getQtyOnhand().compareTo(new BigDecimal("40")));
    }

    @Test
    void testOut_post_deductsBalance() {
        postIn(new BigDecimal("100"));
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0004");

        WmsInvPostBO bo = basePost();
        bo.setTxnType("ISSUE");
        bo.setIdempotencyKey("WMS:TEST:OUT:40");
        bo.setQty(new BigDecimal("40"));
        bo.setFromLocId(LOC);
        bo.setQualityStatusFrom("QUALIFIED");
        bo.setQualityStatusTo("QUALIFIED");
        inventoryService.post(bo);

        assertEquals(0, requireBalance().getQtyOnhand().compareTo(new BigDecimal("60")));
        assertEquals(2, txnMapper.selectCount());
    }

    @Test
    void testOut_balanceNotExists_rejected() {
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0005");
        WmsInvPostBO bo = basePost();
        bo.setTxnType("ISSUE");
        bo.setIdempotencyKey("WMS:TEST:OUT:NOTEXIST");
        bo.setQty(new BigDecimal("10"));
        bo.setFromLocId(LOC);
        bo.setQualityStatusFrom("QUALIFIED");
        bo.setQualityStatusTo("QUALIFIED");
        assertServiceException(() -> inventoryService.post(bo), INV_BALANCE_NOT_EXISTS);
    }

    @Test
    void testMove_transferBetweenLocs() {
        // 入库到 LOC
        postIn(new BigDecimal("100"));
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0006");

        WmsInvPostBO bo = basePost();
        bo.setTxnType("TRANSFER");
        bo.setIdempotencyKey("WMS:TEST:MOVE");
        bo.setQty(new BigDecimal("30"));
        bo.setFromLocId(LOC);
        bo.setFromLocCode("LOC-1");
        bo.setToLocId(2L);
        bo.setToLocCode("LOC-2");
        bo.setQualityStatusFrom("QUALIFIED");
        bo.setQualityStatusTo("QUALIFIED");
        inventoryService.post(bo);

        assertEquals(0, requireBalance().getQtyOnhand().compareTo(new BigDecimal("70")));
        // 目标库位新增余额 30
        WmsInvBalanceDO dim = new WmsInvBalanceDO();
        dim.setInvOrgId(INV_ORG);
        dim.setPlantId(1L);
        dim.setWhId(WH);
        dim.setLocId(2L);
        dim.setMatId(MAT);
        dim.setBatchId(BATCH);
        dim.setContainerId(0L);
        dim.setProjectId(0L);
        dim.setProjectTaskId(0L);
        dim.setStockScope("COMMON");
        dim.setQualityStatus("QUALIFIED");
        WmsInvBalanceDO target = balanceMapper.selectByDim(dim);
        assertNotNull(target);
        assertEquals(0, target.getQtyOnhand().compareTo(new BigDecimal("30")));
    }

    @Test
    void testPost_versionIncrementedAfterIn() {
        // 每次过账后 version 递增，作为乐观锁比对的依据
        WmsInvBalanceDO afterFirst = postIn(new BigDecimal("10"));
        assertEquals(1, afterFirst.getVersion());
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0008");
        WmsInvPostBO bo = basePost();
        bo.setTxnType("RECEIPT");
        bo.setIdempotencyKey("WMS:TEST:V2");
        bo.setQty(new BigDecimal("5"));
        bo.setToLocId(LOC);
        bo.setQualityStatusTo("QUALIFIED");
        inventoryService.post(bo);
        WmsInvBalanceDO afterSecond = requireBalance();
        assertEquals(2, afterSecond.getVersion());
        assertEquals(0, afterSecond.getQtyOnhand().compareTo(new BigDecimal("15")));
    }

    private WmsInventoryQueryBO baseQuery() {
        WmsInventoryQueryBO query = new WmsInventoryQueryBO();
        query.setInvOrgId(INV_ORG);
        query.setWhId(WH);
        query.setLocId(LOC);
        query.setMatId(MAT);
        query.setBatchId(BATCH);
        query.setQualityStatus("QUALIFIED");
        return query;
    }

}
