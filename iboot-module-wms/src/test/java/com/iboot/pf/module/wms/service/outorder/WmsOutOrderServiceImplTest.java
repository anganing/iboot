package com.iboot.pf.module.wms.service.outorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.reservation.WmsInvReservationMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.service.reservation.WmsInventoryReservationService;
import com.iboot.pf.module.wms.service.reservation.WmsInventoryReservationServiceImpl;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReserveBO;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_CANNOT_SUBMIT;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyCollection;

/** 出库单流程测试：创建、改单、删单、提交、取消、过账、可用量不足拒绝、过账核销预留。 */
@Import({WmsOutOrderServiceImpl.class, WmsWarehouseServiceImpl.class,
        WmsInventoryServiceImpl.class, WmsCodeService.class,
        WmsInventoryReservationServiceImpl.class,
        com.iboot.pf.module.wms.service.command.WmsCommandServiceImpl.class})
class WmsOutOrderServiceImplTest extends BaseDbUnitTest {

    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;

    @Resource
    private WmsOutOrderService outOrderService;
    @Resource
    private WmsInventoryReservationService reservationService;
    @Resource
    private WmsOutOrderMapper wmsOutOrderMapper;
    @Resource
    private WmsOutOrderLineMapper wmsOutOrderLineMapper;
    @Resource
    private WmsInvBalanceMapper wmsInvBalanceMapper;
    @Resource
    private WmsInvTxnMapper wmsInvTxnMapper;
    @Resource
    private WmsInvReservationMapper wmsInvReservationMapper;
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
        when(codeRuleApi.generate("WMS_RESERVATION_NO")).thenReturn("RSV-0001");
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
        WmsCreateOutOrderBO bo = baseOutBO();
        bo.setLines(List.of(outLine(10, qty, WmsConstants.NONE_ID)));
        return bo;
    }

    /** 出库单头，明细由各用例按需装配。 */
    private WmsCreateOutOrderBO baseOutBO() {
        WmsCreateOutOrderBO bo = new WmsCreateOutOrderBO();
        bo.setIdempotencyKey("WMS:OUT:1");
        bo.setSourceModule("MES");
        bo.setSourceBizNo("MO-1");
        bo.setBizType("PROD_ISSUE_OUT");
        bo.setInvOrgId(1L);
        bo.setPlantId(1L);
        bo.setWhId(WH);
        return bo;
    }

    /** 造一条出库行，维度与 {@link #seedBalance} 的余额行一致，否则过账匹配不到余额。 */
    private WmsCreateOutOrderBO.Line outLine(int lineNo, BigDecimal qty, Long reservationId) {
        WmsCreateOutOrderBO.Line line = new WmsCreateOutOrderBO.Line();
        line.setLineNo(lineNo);
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
        line.setReservationId(reservationId);
        return line;
    }

    /** 建一条预留，维度与出库行一致，核销时才会命中同一条余额。 */
    private Long reserve(BigDecimal qty, String idempotencyKey) {
        WmsReserveBO bo = new WmsReserveBO();
        bo.setIdempotencyKey(idempotencyKey);
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
        return reservationService.reserve(bo);
    }

    private WmsInvBalanceDO requireBalance() {
        return wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
    }

    /** 可用量：现存 - 预留 - 冻结。 */
    private BigDecimal availableQty() {
        return WmsInventoryService.availableQtyOf(requireBalance());
    }

    private WmsConfirmOrderBO confirmBO(Long orderId) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey("WMS:OUT:1");
        bo.setSourceModule("MES");
        bo.setSourceBizNo("MO-1");
        bo.setOrderId(orderId);
        return bo;
    }

    private com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO submitOrderBO(Long orderId, String key) {
        com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO bo =
                new com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO();
        bo.setIdempotencyKey(key);
        bo.setSourceModule("UI");
        bo.setSourceBizNo("OUT-0001");
        bo.setOrderId(orderId);
        return bo;
    }

    @Test
    void testSubmit_draftToSubmitted_andIdempotent() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));

        outOrderService.submitOutOrder(submitOrderBO(id, "UI:OUT-0001:SUBMIT:0"));
        assertEquals(WmsOrderStatusEnum.SUBMITTED.name(), wmsOutOrderMapper.selectById(id).getStatus());

        // 同一幂等键重复提交：命令层直接返回，不再执行动作
        outOrderService.submitOutOrder(submitOrderBO(id, "UI:OUT-0001:SUBMIT:0"));
        assertEquals(WmsOrderStatusEnum.SUBMITTED.name(), wmsOutOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testSubmit_nonDraft_rejected() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));
        outOrderService.submitOutOrder(submitOrderBO(id, "UI:OUT-0001:SUBMIT:0"));

        // 换幂等键才会真正执行动作，此时状态已是 SUBMITTED
        assertServiceException(() -> outOrderService.submitOutOrder(submitOrderBO(id, "UI:OUT-0001:SUBMIT:1")),
                OUT_ORDER_CANNOT_SUBMIT);
    }

    @Test
    void testUpdate_replacesLines() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));
        Long oldLineId = wmsOutOrderLineMapper.selectListByOrderId(id).get(0).getId();

        WmsCreateOutOrderBO update = createOutBO(new BigDecimal("60"));
        update.getLines().get(0).setLineNo(20);
        outOrderService.updateOutOrder(id, update);

        List<WmsOutOrderLineDO> lines = wmsOutOrderLineMapper.selectListByOrderId(id);
        assertEquals(1, lines.size());
        assertEquals(20, lines.get(0).getLineNo());
        assertEquals(0, lines.get(0).getPlanQty().compareTo(new BigDecimal("60")));
        assertEquals(null, wmsOutOrderLineMapper.selectById(oldLineId));
        assertEquals("OUT-0001", wmsOutOrderMapper.selectById(id).getOutNo());
    }

    @Test
    void testUpdate_nonDraft_rejected() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));
        outOrderService.submitOutOrder(submitOrderBO(id, "UI:OUT-0001:SUBMIT:0"));

        assertServiceException(() -> outOrderService.updateOutOrder(id, createOutBO(new BigDecimal("60"))),
                OUT_ORDER_CANNOT_MODIFY);
    }

    @Test
    void testDelete_draftOnly() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));
        outOrderService.deleteOutOrder(id);

        assertEquals(null, wmsOutOrderMapper.selectById(id));
        assertEquals(0, wmsOutOrderLineMapper.selectListByOrderId(id).size());
    }

    @Test
    void testDelete_nonDraft_rejected() {
        seedBalance(new BigDecimal("500"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));
        outOrderService.submitOutOrder(submitOrderBO(id, "UI:OUT-0001:SUBMIT:0"));

        assertServiceException(() -> outOrderService.deleteOutOrder(id), OUT_ORDER_CANNOT_MODIFY);
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

        // 已过账单据状态为 DONE，换键二次过账被状态机拒绝，流水不重复
        WmsConfirmOrderBO otherKey = confirmBO(id);
        otherKey.setIdempotencyKey("WMS:OUT:1:RETRY");
        assertServiceException(() -> outOrderService.confirmOutOrder(otherKey), OUT_ORDER_CANNOT_CONFIRM);
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("70")));
        assertEquals(1, wmsInvTxnMapper.selectCount());
    }

    @Test
    void testConfirm_sameKeyRepeated_noDoublePosting() {
        seedBalance(new BigDecimal("100"), null, null);
        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("30")));
        outOrderService.confirmOutOrder(confirmBO(id));

        // 界面连点、网关重发：同一幂等键重复确认，命令层原样返回，
        // postedQty / pickedQty 不被二次累加，也不产生第二批流水
        outOrderService.confirmOutOrder(confirmBO(id));

        WmsOutOrderLineDO line = wmsOutOrderLineMapper.selectListByOrderId(id).get(0);
        assertEquals(0, line.getPostedQty().compareTo(new BigDecimal("30")), "连点不应把过账量翻倍");
        assertEquals(0, line.getPickedQty().compareTo(new BigDecimal("30")));
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("70")));
        assertEquals(1, wmsInvTxnMapper.selectCount(), "重复确认不应产生第二条流水");
        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsOutOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testConfirm_consumesReservation_availableQtyUnchanged() {
        seedBalance(new BigDecimal("500"), null, null);
        Long reservationId = reserve(new BigDecimal("100"), "MES:MR-1:RESERVE:10");
        // 领料提交后：现存 500 预留 100 → 可用 400
        BigDecimal availableBefore = availableQty();
        assertEquals(0, availableBefore.compareTo(new BigDecimal("400")));

        WmsCreateOutOrderBO createBO = createOutBO(new BigDecimal("100"));
        createBO.getLines().get(0).setReservationId(reservationId);
        Long id = outOrderService.createOutOrder(createBO);
        outOrderService.confirmOutOrder(confirmBO(id));

        // 现存扣 100，预留核销 100
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("400")));
        assertEquals(0, balance.getQtyReserved().compareTo(BigDecimal.ZERO));
        // 核心断言：带预留的行过账前后可用量相同，预留占用与实际扣减没有双重占用
        assertEquals(0, availableQty().compareTo(availableBefore));

        WmsInvReservationDO reservation = wmsInvReservationMapper.selectById(reservationId);
        assertEquals(WmsReservationStatusEnum.CONSUMED.name(), reservation.getStatus());
        assertEquals(0, reservation.getReleasedQty().compareTo(new BigDecimal("100")));
        // RESERVE、ISSUE、核销的 RELEASE 三条流水
        assertEquals(3, wmsInvTxnMapper.selectCount());
    }

    @Test
    void testConfirm_withoutReservation_reservationUntouched() {
        seedBalance(new BigDecimal("500"), null, null);
        // 另一笔业务的预留：本次出库行的 reservation_id 为 0，不该被动到
        Long otherId = reserve(new BigDecimal("100"), "MES:MR-9:RESERVE:10");

        Long id = outOrderService.createOutOrder(createOutBO(new BigDecimal("100")));
        assertEquals(WmsConstants.NONE_ID, wmsOutOrderLineMapper.selectListByOrderId(id).get(0).getReservationId());
        outOrderService.confirmOutOrder(confirmBO(id));

        WmsInvReservationDO other = wmsInvReservationMapper.selectById(otherId);
        assertEquals(WmsReservationStatusEnum.ACTIVE.name(), other.getStatus());
        assertEquals(0, other.getReleasedQty().compareTo(BigDecimal.ZERO));
        // 预留未被核销，本次出的是自由库存：可用量由 400 降到 300 才是正确结果
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("400")));
        assertEquals(0, balance.getQtyReserved().compareTo(new BigDecimal("100")));
        assertEquals(0, availableQty().compareTo(new BigDecimal("300")));
        // 只有 RESERVE 与 ISSUE 两条流水，没有核销产生的 RELEASE
        assertEquals(2, wmsInvTxnMapper.selectCount());
    }

    @Test
    void testConfirm_repeated_consumesReservationOnce() {
        seedBalance(new BigDecimal("500"), null, null);
        Long reservationId = reserve(new BigDecimal("100"), "MES:MR-1:RESERVE:10");
        WmsCreateOutOrderBO createBO = createOutBO(new BigDecimal("100"));
        createBO.getLines().get(0).setReservationId(reservationId);
        Long id = outOrderService.createOutOrder(createBO);
        outOrderService.confirmOutOrder(confirmBO(id));

        // 第一层：同一幂等键二次确认，命令层原样返回，不再抛"状态非法"
        outOrderService.confirmOutOrder(confirmBO(id));

        // 第二层：把状态与已过账量改回去也无法穿透命令幂等——过账与核销都不重复执行
        WmsOutOrderDO order = wmsOutOrderMapper.selectById(id);
        order.setStatus(WmsOrderStatusEnum.SUBMITTED.name());
        wmsOutOrderMapper.updateById(order);
        WmsOutOrderLineDO line = wmsOutOrderLineMapper.selectListByOrderId(id).get(0);
        line.setPostedQty(BigDecimal.ZERO);
        wmsOutOrderLineMapper.updateById(line);
        outOrderService.confirmOutOrder(confirmBO(id));

        WmsInvReservationDO reservation = wmsInvReservationMapper.selectById(reservationId);
        // 核销量没叠加成 200，状态仍是已核销
        assertEquals(0, reservation.getReleasedQty().compareTo(new BigDecimal("100")));
        assertEquals(WmsReservationStatusEnum.CONSUMED.name(), reservation.getStatus());
        // 现存没被二次扣减，流水也没多出来
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("400")));
        assertEquals(0, balance.getQtyReserved().compareTo(BigDecimal.ZERO));
        assertEquals(3, wmsInvTxnMapper.selectCount());
    }

    @Test
    void testConfirm_twoLines_eachConsumesOwnReservation() {
        seedBalance(new BigDecimal("500"), null, null);
        Long reservationA = reserve(new BigDecimal("100"), "MES:MR-1:RESERVE:10");
        Long reservationB = reserve(new BigDecimal("60"), "MES:MR-1:RESERVE:20");
        // 现存 500 预留 160 → 可用 340
        BigDecimal availableBefore = availableQty();
        assertEquals(0, availableBefore.compareTo(new BigDecimal("340")));

        WmsCreateOutOrderBO createBO = baseOutBO();
        createBO.setLines(List.of(outLine(10, new BigDecimal("100"), reservationA),
                outLine(20, new BigDecimal("60"), reservationB)));
        Long id = outOrderService.createOutOrder(createBO);
        outOrderService.confirmOutOrder(confirmBO(id));

        // 两行各核销自己的预留：幂等键必须含行号，否则第二行会被第一行的键判重跳过
        assertEquals(0, wmsInvReservationMapper.selectById(reservationA)
                .getReleasedQty().compareTo(new BigDecimal("100")));
        assertEquals(0, wmsInvReservationMapper.selectById(reservationB)
                .getReleasedQty().compareTo(new BigDecimal("60")));
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("340")));
        assertEquals(0, balance.getQtyReserved().compareTo(BigDecimal.ZERO));
        assertEquals(0, availableQty().compareTo(availableBefore));
    }

    @Test
    void testConfirm_partialQty_reservationStaysPartial() {
        seedBalance(new BigDecimal("500"), null, null);
        Long reservationId = reserve(new BigDecimal("100"), "MES:MR-1:RESERVE:10");
        BigDecimal availableBefore = availableQty();

        // 预留 100 只出 60：部分出库是正常业务，不抛异常
        WmsCreateOutOrderBO createBO = createOutBO(new BigDecimal("60"));
        createBO.getLines().get(0).setReservationId(reservationId);
        Long id = outOrderService.createOutOrder(createBO);
        outOrderService.confirmOutOrder(confirmBO(id));

        WmsInvReservationDO reservation = wmsInvReservationMapper.selectById(reservationId);
        // 只核销 60，剩余 40 继续占用，状态是部分处理而不是已核销
        assertEquals(WmsReservationStatusEnum.PARTIAL.name(), reservation.getStatus());
        assertEquals(0, reservation.getReleasedQty().compareTo(new BigDecimal("60")));
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("440")));
        assertEquals(0, balance.getQtyReserved().compareTo(new BigDecimal("40")));
        // 剩余预留仍占用可用量，过账前后可用量依然相同
        assertEquals(0, availableQty().compareTo(availableBefore));
    }

    @Test
    void testConfirm_qtyOverReservation_consumesRemainingOnly() {
        seedBalance(new BigDecimal("500"), null, null);
        Long reservationId = reserve(new BigDecimal("60"), "MES:MR-1:RESERVE:10");

        // 预留 60 却出 100：超出预留的 40 出的是自由库存，不该抛预留余量不足
        WmsCreateOutOrderBO createBO = createOutBO(new BigDecimal("100"));
        createBO.getLines().get(0).setReservationId(reservationId);
        Long id = outOrderService.createOutOrder(createBO);
        outOrderService.confirmOutOrder(confirmBO(id));

        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsOutOrderMapper.selectById(id).getStatus());
        WmsInvReservationDO reservation = wmsInvReservationMapper.selectById(reservationId);
        // 只核销预留剩余的 60，而不是过账的 100
        assertEquals(0, reservation.getReleasedQty().compareTo(new BigDecimal("60")));
        assertEquals(WmsReservationStatusEnum.CONSUMED.name(), reservation.getStatus());
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("400")));
        assertEquals(0, balance.getQtyReserved().compareTo(BigDecimal.ZERO));
        // 超发的 40 是自由库存，可用量由 440 降到 400
        assertEquals(0, availableQty().compareTo(new BigDecimal("400")));
    }

}
