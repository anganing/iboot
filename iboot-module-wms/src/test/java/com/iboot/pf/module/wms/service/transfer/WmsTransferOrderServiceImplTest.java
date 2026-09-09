package com.iboot.pf.module.wms.service.transfer;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.transfer.WmsTransferOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.transfer.WmsTransferOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandServiceImpl;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.transfer.bo.WmsCreateTransferOrderBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_CANNOT_SUBMIT;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_SAME_TARGET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * 转移单流程测试：创建、改单、删单、提交、取消、MOVE 过账。
 *
 * <p>领域侧原来缺这个测试类，T0-06 补上。
 */
@Import({WmsTransferOrderServiceImpl.class, WmsWarehouseServiceImpl.class,
        WmsInventoryServiceImpl.class, WmsCodeService.class, WmsCommandServiceImpl.class})
class WmsTransferOrderServiceImplTest extends BaseDbUnitTest {

    private static final Long WH = 1L;
    private static final Long FROM_LOC = 1L;
    private static final Long TO_LOC = 2L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;

    @Resource
    private WmsTransferOrderService transferOrderService;
    @Resource
    private WmsTransferOrderMapper wmsTransferOrderMapper;
    @Resource
    private WmsTransferOrderLineMapper wmsTransferOrderLineMapper;
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

    @BeforeEach
    void setUp() {
        wmsWhMapper.insert(new WmsWhDO().setId(WH).setPlantId(1L).setCode("WH-1").setName("主仓库").setStatus(0));
        wmsLocMapper.insert(new WmsLocDO().setId(FROM_LOC).setWhId(WH).setZoneId(0L).setCode("LOC-1")
                .setStatus("ENABLED"));
        wmsLocMapper.insert(new WmsLocDO().setId(TO_LOC).setWhId(WH).setZoneId(0L).setCode("LOC-2")
                .setStatus("ENABLED"));
        when(codeRuleApi.generate("WMS_TRANSFER_NO")).thenReturn("TR-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
    }

    /** 在来源库位预置在库余额。 */
    private void seedBalance(BigDecimal qty) {
        WmsInvBalanceDO balance = new WmsInvBalanceDO();
        balance.setInvOrgId(1L);
        balance.setPlantId(1L);
        balance.setWhId(WH);
        balance.setLocId(FROM_LOC);
        balance.setMatId(MAT);
        balance.setBatchId(BATCH);
        balance.setContainerId(0L);
        balance.setProjectId(0L);
        balance.setProjectTaskId(0L);
        balance.setStockScope("COMMON");
        balance.setQualityStatus("QUALIFIED");
        balance.setMatCode("M-001");
        balance.setMatName("物料A");
        balance.setLotNo("L-001");
        balance.setLocCode("LOC-1");
        balance.setQtyOnhand(qty);
        balance.setQtyReserved(BigDecimal.ZERO);
        balance.setQtyFrozen(BigDecimal.ZERO);
        balance.setVersion(0);
        wmsInvBalanceMapper.insert(balance);
    }

    private WmsCreateTransferOrderBO createBO(BigDecimal qty) {
        WmsCreateTransferOrderBO bo = new WmsCreateTransferOrderBO();
        bo.setIdempotencyKey("WMS:TR:1");
        bo.setSourceModule("UI");
        bo.setSourceBizNo("TR-SRC-1");
        bo.setTransferType("LOC_TRANSFER");
        bo.setFromInvOrgId(1L);
        bo.setToInvOrgId(1L);
        bo.setFromWhId(WH);
        bo.setToWhId(WH);

        WmsCreateTransferOrderBO.Line line = new WmsCreateTransferOrderBO.Line();
        line.setLineNo(10);
        line.setMatId(MAT);
        line.setMatCode("M-001");
        line.setMatName("物料A");
        line.setBatchId(BATCH);
        line.setLotNo("L-001");
        line.setUomId(1L);
        line.setFromLocId(FROM_LOC);
        line.setToLocId(TO_LOC);
        line.setQualityStatusFrom("QUALIFIED");
        line.setPlanQty(qty);
        bo.setLines(List.of(line));
        return bo;
    }

    private WmsSubmitOrderBO submitBO(Long orderId, String key) {
        WmsSubmitOrderBO bo = new WmsSubmitOrderBO();
        bo.setIdempotencyKey(key);
        bo.setSourceModule("UI");
        bo.setSourceBizNo("TR-0001");
        bo.setOrderId(orderId);
        return bo;
    }

    private WmsConfirmOrderBO confirmBO(Long orderId) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey("WMS:TR:1");
        bo.setSourceModule("UI");
        bo.setSourceBizNo("TR-SRC-1");
        bo.setOrderId(orderId);
        return bo;
    }

    private WmsInvBalanceDO balanceAt(Long locId) {
        return wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)
                        .eq(WmsInvBalanceDO::getLocId, locId)).stream().findFirst().orElse(null);
    }

    @Test
    void testCreate_andIdempotent() {
        Long id1 = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        Long id2 = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        assertEquals(id1, id2, "幂等命中应返回原单据编号");

        WmsTransferOrderDO order = wmsTransferOrderMapper.selectById(id1);
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), order.getStatus());
        assertEquals("TR-0001", order.getTransferNo());
        assertEquals(1, wmsTransferOrderLineMapper.selectListByOrderId(id1).size());
    }

    @Test
    void testCreate_emptyLines_rejected() {
        WmsCreateTransferOrderBO bo = createBO(new BigDecimal("100"));
        bo.setLines(List.of());
        assertServiceException(() -> transferOrderService.createTransferOrder(bo), TRANSFER_ORDER_LINE_EMPTY);
    }

    @Test
    void testSubmit_draftToSubmitted_andIdempotent() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));

        transferOrderService.submitTransferOrder(submitBO(id, "UI:TR-0001:SUBMIT:0"));
        assertEquals(WmsOrderStatusEnum.SUBMITTED.name(), wmsTransferOrderMapper.selectById(id).getStatus());

        // 同一幂等键重复提交：命令层直接返回，不再执行动作
        transferOrderService.submitTransferOrder(submitBO(id, "UI:TR-0001:SUBMIT:0"));
        assertEquals(WmsOrderStatusEnum.SUBMITTED.name(), wmsTransferOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testSubmit_nonDraft_rejected() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        transferOrderService.submitTransferOrder(submitBO(id, "UI:TR-0001:SUBMIT:0"));

        // 换幂等键才会真正执行动作，此时状态已是 SUBMITTED
        assertServiceException(() -> transferOrderService.submitTransferOrder(submitBO(id, "UI:TR-0001:SUBMIT:1")),
                TRANSFER_ORDER_CANNOT_SUBMIT);
    }

    @Test
    void testUpdate_replacesLines() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        Long oldLineId = wmsTransferOrderLineMapper.selectListByOrderId(id).get(0).getId();

        WmsCreateTransferOrderBO update = createBO(new BigDecimal("60"));
        update.getLines().get(0).setLineNo(20);
        transferOrderService.updateTransferOrder(id, update);

        List<WmsTransferOrderLineDO> lines = wmsTransferOrderLineMapper.selectListByOrderId(id);
        assertEquals(1, lines.size());
        assertEquals(20, lines.get(0).getLineNo());
        assertEquals(0, lines.get(0).getPlanQty().compareTo(new BigDecimal("60")));
        assertNull(wmsTransferOrderLineMapper.selectById(oldLineId));
        // 单号与状态是单据身份，改单不动
        assertEquals("TR-0001", wmsTransferOrderMapper.selectById(id).getTransferNo());
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), wmsTransferOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testUpdate_nonDraft_rejected() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        transferOrderService.submitTransferOrder(submitBO(id, "UI:TR-0001:SUBMIT:0"));

        assertServiceException(() -> transferOrderService.updateTransferOrder(id, createBO(new BigDecimal("60"))),
                TRANSFER_ORDER_CANNOT_MODIFY);
    }

    @Test
    void testDelete_draftOnly() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        transferOrderService.deleteTransferOrder(id);

        assertNull(wmsTransferOrderMapper.selectById(id));
        assertEquals(0, wmsTransferOrderLineMapper.selectListByOrderId(id).size());
    }

    @Test
    void testDelete_nonDraft_rejected() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));
        transferOrderService.submitTransferOrder(submitBO(id, "UI:TR-0001:SUBMIT:0"));

        assertServiceException(() -> transferOrderService.deleteTransferOrder(id), TRANSFER_ORDER_CANNOT_MODIFY);
    }

    @Test
    void testCancel_draftOnly() {
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("100")));

        WmsCancelOrderBO cancel = new WmsCancelOrderBO();
        cancel.setOrderId(id);
        cancel.setReason("取消");
        transferOrderService.cancelTransferOrder(cancel);
        assertEquals(WmsOrderStatusEnum.CANCELLED.name(), wmsTransferOrderMapper.selectById(id).getStatus());

        // 已取消不能再取消
        assertServiceException(() -> transferOrderService.cancelTransferOrder(cancel), TRANSFER_ORDER_CANNOT_CANCEL);
    }

    @Test
    void testConfirm_movesBetweenLocations() {
        seedBalance(new BigDecimal("100"));
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("40")));
        transferOrderService.confirmTransferOrder(confirmBO(id));

        // MOVE 过账：来源库位减、目标库位加，总量不变
        assertEquals(0, balanceAt(FROM_LOC).getQtyOnhand().compareTo(new BigDecimal("60")));
        WmsInvBalanceDO target = balanceAt(TO_LOC);
        assertNotNull(target, "目标库位余额行应被自动创建");
        assertEquals(0, target.getQtyOnhand().compareTo(new BigDecimal("40")));
        assertEquals(1, wmsInvTxnMapper.selectCount(), "一行转移写一条 MOVE 流水");
        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsTransferOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testConfirm_sameTarget_rejected() {
        seedBalance(new BigDecimal("100"));
        WmsCreateTransferOrderBO bo = createBO(new BigDecimal("40"));
        // 来源与目标完全一致，转移没有意义
        bo.getLines().get(0).setToLocId(FROM_LOC);
        Long id = transferOrderService.createTransferOrder(bo);

        assertServiceException(() -> transferOrderService.confirmTransferOrder(confirmBO(id)),
                TRANSFER_ORDER_SAME_TARGET);
    }

    @Test
    void testConfirm_sameKeyRepeated_noDoublePosting() {
        seedBalance(new BigDecimal("100"));
        Long id = transferOrderService.createTransferOrder(createBO(new BigDecimal("40")));
        transferOrderService.confirmTransferOrder(confirmBO(id));

        // 界面连点、网关重发：同一幂等键重复确认，命令层原样返回，
        // postedQty / movedQty 不被二次累加，也不产生第二条流水
        transferOrderService.confirmTransferOrder(confirmBO(id));

        WmsTransferOrderLineDO line = wmsTransferOrderLineMapper.selectListByOrderId(id).get(0);
        assertEquals(0, line.getPostedQty().compareTo(new BigDecimal("40")), "连点不应把过账量翻倍");
        assertEquals(0, line.getMovedQty().compareTo(new BigDecimal("40")));
        assertEquals(0, balanceAt(FROM_LOC).getQtyOnhand().compareTo(new BigDecimal("60")));
        assertEquals(0, balanceAt(TO_LOC).getQtyOnhand().compareTo(new BigDecimal("40")));
        assertEquals(1, wmsInvTxnMapper.selectCount(), "重复确认不应产生第二条流水");
        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsTransferOrderMapper.selectById(id).getStatus());
    }
}
