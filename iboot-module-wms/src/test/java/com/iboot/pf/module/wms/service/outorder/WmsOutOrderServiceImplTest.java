package com.iboot.pf.module.wms.service.outorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_AVAILABLE_QTY_NOT_ENOUGH;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyCollection;

/** 出库单流程测试：创建、取消、过账、可用量不足拒绝。 */
@Import({WmsOutOrderServiceImpl.class, WmsWarehouseServiceImpl.class,
        WmsInventoryServiceImpl.class, WmsCodeService.class})
class WmsOutOrderServiceImplTest extends BaseDbUnitTest {

    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;

    @Resource
    private WmsOutOrderService outOrderService;
    @Resource
    private WmsOutOrderMapper wmsOutOrderMapper;
    @Resource
    private WmsOutOrderLineMapper wmsOutOrderLineMapper;
    @Resource
    private WmsInvBalanceMapper wmsInvBalanceMapper;
    @Resource
    private WmsInvTxnMapper wmsInvTxnMapper;
    @Resource
    private WmsWhMapper wmsWhMapper;
    @Resource
    private WmsLocMapper wmsLocMapper;
    @MockitoBean
    private CodeRuleApi codeRuleApi;
    @MockitoBean
    private MaterialApi materialApi;

    @BeforeEach
    void setUp() {
        wmsWhMapper.insert(new WmsWhDO().setId(WH).setPlantId(1L).setCode("WH-1").setName("主仓库").setStatus(0));
        wmsLocMapper.insert(new WmsLocDO().setId(LOC).setWhId(WH).setZoneId(0L).setCode("LOC-1").setStatus("ENABLED"));
        when(codeRuleApi.generate("WMS_OUT_ORDER_NO")).thenReturn("OUT-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(MAT);
        material.setCode("M-001");
        material.setName("物料A");
        when(materialApi.getMaterialMap(anyCollection())).thenReturn(Map.of(MAT, material));
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
        balance.setStockScope("COMMON");
        balance.setQualityStatus("QUALIFIED");
        balance.setQtyOnhand(onhand);
        balance.setQtyFrozen(frozen == null ? BigDecimal.ZERO : frozen);
        balance.setQtyReserved(reserved == null ? BigDecimal.ZERO : reserved);
        balance.setVersion(0);
        wmsInvBalanceMapper.insert(balance);
    }

    private WmsCreateOutOrderBO createOutBO(BigDecimal qty) {
        WmsCreateOutOrderBO bo = new WmsCreateOutOrderBO();
        bo.setIdempotencyKey("WMS:OUT:1");
        bo.setSourceModule("MES");
        bo.setSourceBizNo("MO-1");
        bo.setBizType("PROD_ISSUE_OUT");
        bo.setInvOrgId(1L);
        bo.setPlantId(1L);
        bo.setWhId(WH);

        WmsCreateOutOrderBO.Line line = new WmsCreateOutOrderBO.Line();
        line.setLineNo(10);
        line.setMatId(MAT);
        line.setMatCode("M-001");
        line.setMatName("物料A");
        line.setBatchId(BATCH);
        line.setLotNo("L-001");
        line.setUomId(1L);
        line.setFromLocId(LOC);
        line.setFromLocCode("LOC-1");
        line.setQualityStatus("QUALIFIED");
        line.setPlanQty(qty);
        bo.setLines(List.of(line));
        return bo;
    }

    private WmsConfirmOrderBO confirmBO(Long orderId) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey("WMS:OUT:1");
        bo.setSourceModule("MES");
        bo.setSourceBizNo("MO-1");
        bo.setOrderId(orderId);
        return bo;
    }

    @Test
    void testCreate_andIdempotent() {
        seedBalance(new BigDecimal("100"), null, null);
        Long id1 = outOrderService.createOutOrder(createOutBO(new BigDecimal("10")));
        Long id2 = outOrderService.createOutOrder(createOutBO(new BigDecimal("10")));
        assertEquals(id1, id2);
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), wmsOutOrderMapper.selectById(id1).getStatus());
        assertEquals("OUT-0001", wmsOutOrderMapper.selectById(id1).getOutNo());
    }

    @Test
    void testCreate_emptyLines_rejected() {
        WmsCreateOutOrderBO bo = createOutBO(new BigDecimal("10"));
        bo.setLines(List.of());
        assertServiceException(() -> outOrderService.createOutOrder(bo), OUT_ORDER_LINE_EMPTY);
    }

    @Test
    void testCreate_positiveQty_required() {
        seedBalance(new BigDecimal("100"), null, null);
        WmsCreateOutOrderBO bo = createOutBO(new BigDecimal("10"));
        bo.getLines().get(0).setPlanQty(BigDecimal.ZERO);
        assertServiceException(() -> outOrderService.createOutOrder(bo), QTY_MUST_BE_POSITIVE);
    }

    @Test
    void testConfirm_postsAndDeducts() {
        seedBalance(new BigDecimal("100"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("40")));
        outOrderService.confirmOutOrder(confirmBO(id));

        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsOutOrderMapper.selectById(id).getStatus());
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("60")));
        assertEquals(1, wmsInvTxnMapper.selectCount());
    }

    @Test
    void testConfirm_availableNotEnough_rejected() {
        // 在库 100、冻结 80 → 可用仅 20，出库 40 应被拒
        seedBalance(new BigDecimal("100"), new BigDecimal("80"), null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("40")));
        assertServiceException(() -> outOrderService.confirmOutOrder(confirmBO(id)),
                INV_AVAILABLE_QTY_NOT_ENOUGH);
        // 余额未被扣减
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("100")));
        assertNotNull(balance);
    }

    @Test
    void testCancel_draftOnly() {
        seedBalance(new BigDecimal("100"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("10")));

        WmsCancelOrderBO cancel = new WmsCancelOrderBO();
        cancel.setOrderId(id);
        cancel.setReason("取消");
        outOrderService.cancelOutOrder(cancel);
        assertEquals(WmsOrderStatusEnum.CANCELLED.name(), wmsOutOrderMapper.selectById(id).getStatus());

        assertServiceException(() -> outOrderService.cancelOutOrder(cancel), OUT_ORDER_CANNOT_CANCEL);
    }

    @Test
    void testConfirm_postsOnce_thenStateBlocksReConfirm() {
        seedBalance(new BigDecimal("100"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("30")));
        outOrderService.confirmOutOrder(confirmBO(id));

        // 已过账单据状态为 DONE，二次过账被状态机拒绝，流水不重复
        assertServiceException(() -> outOrderService.confirmOutOrder(confirmBO(id)), OUT_ORDER_CANNOT_CONFIRM);
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("70")));
        assertEquals(1, wmsInvTxnMapper.selectCount());
    }

}
