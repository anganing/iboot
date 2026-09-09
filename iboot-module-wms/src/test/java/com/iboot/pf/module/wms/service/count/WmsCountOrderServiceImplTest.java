package com.iboot.pf.module.wms.service.count;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsCountLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.batch.WmsBatchServiceImpl;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderServiceImpl;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.outorder.WmsOutOrderServiceImpl;
import com.iboot.pf.module.wms.service.reservation.WmsInventoryReservationServiceImpl;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_CANNOT_FREEZE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/** 盘点流程测试：生成盘点行、冻结范围、改单删单、提交差异、差异确认走单据过账不直接改余额。 */
@Import({WmsCountOrderServiceImpl.class, WmsWarehouseServiceImpl.class, WmsInventoryServiceImpl.class,
        WmsInOrderServiceImpl.class, WmsOutOrderServiceImpl.class, WmsBatchServiceImpl.class, WmsCodeService.class,
        WmsInventoryReservationServiceImpl.class,
        com.iboot.pf.module.wms.service.command.WmsCommandServiceImpl.class})
class WmsCountOrderServiceImplTest extends BaseDbUnitTest {

    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;
    private static final Long BATCH = 10L;

    @Resource
    private WmsCountOrderService countOrderService;
    @Resource
    private WmsCountOrderMapper wmsCountOrderMapper;
    @Resource
    private WmsCountOrderLineMapper wmsCountOrderLineMapper;
    @Resource
    private WmsInvBalanceMapper wmsInvBalanceMapper;
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
        when(codeRuleApi.generate("WMS_COUNT_NO")).thenReturn("CNT-0001");
        when(codeRuleApi.generate("WMS_IN_ORDER_NO")).thenReturn("IN-0001");
        when(codeRuleApi.generate("WMS_OUT_ORDER_NO")).thenReturn("OUT-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        // 盘盈盘亏会走入库/出库单，物料快照来自 mdm 门面
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(MAT);
        material.setCode("M-001");
        material.setName("物料A");
        when(materialApi.getMaterialMap(anyCollection())).thenReturn(Map.of(MAT, material));
    }

    /** 预置 100 在库余额。 */
    private void seedBalance(BigDecimal qty) {
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
        balance.setMatCode("M-001");
        balance.setMatName("物料A");
        balance.setUomCode("EA");
        balance.setLotNo("L-001");
        balance.setLocCode("LOC-1");
        balance.setQtyOnhand(qty);
        balance.setQtyReserved(BigDecimal.ZERO);
        balance.setQtyFrozen(BigDecimal.ZERO);
        balance.setVersion(0);
        wmsInvBalanceMapper.insert(balance);
    }

    private WmsCreateCountOrderBO createCountBO() {
        WmsCreateCountOrderBO bo = new WmsCreateCountOrderBO();
        bo.setIdempotencyKey("WMS:CNT:CREATE:1");
        bo.setSourceModule("UI");
        bo.setSourceBizNo("CNT-SRC-1");
        bo.setCountType("FULL");
        bo.setInvOrgId(1L);
        bo.setWhId(WH);
        return bo;
    }

    private WmsSubmitCountResultBO submitBO(Long orderId, Long lineId, BigDecimal actualQty) {
        WmsSubmitCountResultBO bo = new WmsSubmitCountResultBO();
        // 与确认差异的键区分开：两个动作都登记命令记录，同键会互相误判幂等
        bo.setIdempotencyKey("UI:CNT-0001:SUBMIT_COUNT_RESULT:0");
        bo.setSourceModule("UI");
        bo.setSourceBizNo("CNT-SRC-1");
        bo.setOrderId(orderId);

        WmsSubmitCountResultBO.LineResult r = new WmsSubmitCountResultBO.LineResult();
        r.setLineId(lineId);
        r.setActualQty(actualQty);
        bo.setResults(List.of(r));
        return bo;
    }

    private WmsConfirmCountDiffBO confirmDiffBO(Long orderId) {
        WmsConfirmCountDiffBO bo = new WmsConfirmCountDiffBO();
        bo.setIdempotencyKey("UI:CNT-0001:CONFIRM_COUNT_DIFF:0");
        bo.setSourceModule("UI");
        bo.setSourceBizNo("CNT-SRC-1");
        bo.setOrderId(orderId);
        return bo;
    }

    private WmsCountOrderLineDO firstLine(Long orderId) {
        return wmsCountOrderLineMapper.selectListByOrderId(orderId).get(0);
    }

    private com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO freezeBO(Long orderId, String key) {
        com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO bo =
                new com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO();
        bo.setIdempotencyKey(key);
        bo.setSourceModule("UI");
        bo.setSourceBizNo("CNT-0001");
        bo.setOrderId(orderId);
        return bo;
    }

    private WmsInvBalanceDO requireBalance() {
        return wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)
                        .eq(WmsInvBalanceDO::getLocId, LOC)).stream().findFirst().orElse(null);
    }

    @Test
    void testFreeze_postsFreezeAndCutsAvailableQty() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());

        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));

        // 单据推进到已冻结
        assertEquals(WmsOrderStatusEnum.FROZEN.name(), wmsCountOrderMapper.selectById(id).getStatus());
        // 冻结走 FREEZE 过账：只加 qty_frozen，不动 qty_onhand，可用量因此下降
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("100")));
        assertEquals(0, balance.getQtyFrozen().compareTo(new BigDecimal("100")));
        assertEquals(0, WmsInventoryService.availableQtyOf(balance).compareTo(BigDecimal.ZERO),
                "盘点期间可用量应为 0，否则正在盘的货还能被领走");
    }

    @Test
    void testFreeze_idempotent() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());

        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));
        // 同一幂等键重复冻结：命令层直接返回，冻结量不翻倍
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));

        assertEquals(0, requireBalance().getQtyFrozen().compareTo(new BigDecimal("100")),
                "重复冻结不应把冻结量叠加成 200");
    }

    @Test
    void testFreeze_nonDraft_rejected() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));

        // 换幂等键才会真正执行动作，此时状态已是 FROZEN
        assertServiceException(() -> countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:1")),
                COUNT_ORDER_CANNOT_FREEZE);
    }

    @Test
    void testSubmitResult_afterFreeze() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));

        // 冻结后录实盘是常规路径，状态校验要放行 FROZEN
        countOrderService.submitCountResult(submitBO(id, firstLine(id).getId(), new BigDecimal("90")));
        assertEquals(WmsOrderStatusEnum.COUNTING.name(), wmsCountOrderMapper.selectById(id).getStatus());
        assertEquals(0, firstLine(id).getDiffQty().compareTo(new BigDecimal("-10")));
    }

    @Test
    void testUpdate_regeneratesLines() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long oldLineId = firstLine(id).getId();

        WmsCreateCountOrderBO update = createCountBO();
        update.setBlindCountFlag(true);
        countOrderService.updateCountOrder(id, update);

        // 盘点行按新范围整批重建，旧行不复用
        assertEquals(null, wmsCountOrderLineMapper.selectById(oldLineId));
        assertEquals(1, wmsCountOrderLineMapper.selectListByOrderId(id).size());
        WmsCountOrderDO order = wmsCountOrderMapper.selectById(id);
        assertEquals("CNT-0001", order.getCountNo());
        assertEquals(Boolean.TRUE, order.getBlindCountFlag());
    }

    @Test
    void testUpdate_nonDraft_rejected() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));

        assertServiceException(() -> countOrderService.updateCountOrder(id, createCountBO()),
                COUNT_ORDER_CANNOT_MODIFY);
    }

    @Test
    void testDelete_draftOnly() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        countOrderService.deleteCountOrder(id);

        assertEquals(null, wmsCountOrderMapper.selectById(id));
        assertEquals(0, wmsCountOrderLineMapper.selectListByOrderId(id).size());
    }

    @Test
    void testDelete_nonDraft_rejected() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));

        assertServiceException(() -> countOrderService.deleteCountOrder(id), COUNT_ORDER_CANNOT_MODIFY);
    }

    @Test
    void testCreate_generatesLineFromBalance() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        WmsCountOrderDO order = wmsCountOrderMapper.selectById(id);
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), order.getStatus());
        assertEquals("CNT-0001", order.getCountNo());

        WmsCountOrderLineDO line = firstLine(id);
        assertEquals(0, line.getBookQty().compareTo(new BigDecimal("100")));
        assertEquals(WmsCountLineStatusEnum.PENDING.name(), line.getStatus());
    }

    @Test
    void testSubmit_computesDiff() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();

        // 实盘 130 → 盘盈 30
        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("130")));
        WmsCountOrderLineDO line = firstLine(id);
        assertEquals(WmsCountLineStatusEnum.DIFF.name(), line.getStatus());
        assertEquals(0, line.getDiffQty().compareTo(new BigDecimal("30")));
    }

    @Test
    void testSubmit_noDiff_whenEqual() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();

        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("100")));
        assertEquals(WmsCountLineStatusEnum.COUNTED.name(), firstLine(id).getStatus());
        assertEquals(0, firstLine(id).getDiffQty().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testConfirmDiff_zeroDiff_marksDone_andReleasesFrozen() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));
        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("100")));

        // 零差异确认：全盘平也是合法结论，直接置 DONE，不再抛"无差异"
        countOrderService.confirmCountDiff(confirmDiffBO(id));

        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsCountOrderMapper.selectById(id).getStatus());
        // 冻结量归零：盘点收尾必须解冻，否则这批货可用量永久偏低
        WmsInvBalanceDO balance = requireBalance();
        assertEquals(0, balance.getQtyFrozen().compareTo(BigDecimal.ZERO), "零差异收单后冻结量应归零");
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("100")), "解冻不动现存");
        assertEquals(0, WmsInventoryService.availableQtyOf(balance).compareTo(new BigDecimal("100")),
                "收单后可用量应恢复 100");
    }

    @Test
    void testConfirmDiff_gain_inPostsIncrementNotDirectModify() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));
        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("150")));

        countOrderService.confirmCountDiff(confirmDiffBO(id));

        // 盘盈 50 走入库单过账：余额增加
        WmsCountOrderLineDO line = firstLine(id);
        assertEquals(WmsCountLineStatusEnum.ADJUSTED.name(), line.getStatus());
        assertNotNull(line.getAdjustBizNo(), "差异应生成调整单据号");
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("150")));
        // 冻结量随收单释放
        assertEquals(0, balance.getQtyFrozen().compareTo(BigDecimal.ZERO), "差异收单后冻结量应归零");
        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsCountOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testConfirmDiff_loss_outPostsDeductNotDirectModify() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));
        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("70")));

        countOrderService.confirmCountDiff(confirmDiffBO(id));

        // 盘亏 30 走出库单过账：余额减少，且不直接改余额（差异通过单据过账体现）
        WmsCountOrderLineDO line = firstLine(id);
        assertEquals(WmsCountLineStatusEnum.ADJUSTED.name(), line.getStatus());
        assertNotNull(line.getAdjustBizNo());
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("70")));
        // 冻结量随收单释放
        assertEquals(0, balance.getQtyFrozen().compareTo(BigDecimal.ZERO), "盘亏收单后冻结量应归零");
    }

    @Test
    void testConfirmDiff_unfrozenDraftOrder_directlyDone() {
        // 新建直接盘、未冻结：确认时不应因 UNFREEZE 过账无冻结量而抛错
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();
        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("100")));

        countOrderService.confirmCountDiff(confirmDiffBO(id));

        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsCountOrderMapper.selectById(id).getStatus());
        assertEquals(0, requireBalance().getQtyFrozen().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testConfirmDiff_statusInvalid_forDraft() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        // 未提交实盘（状态 DRAFT）直接确认 → 状态不合法
        assertServiceException(() -> countOrderService.confirmCountDiff(confirmDiffBO(id)),
                COUNT_ORDER_STATUS_INVALID);
    }

    @Test
    void testConfirmDiff_duplicate_sameKeyReturnsSilently() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();
        countOrderService.freezeCountOrder(freezeBO(id, "UI:CNT-0001:FREEZE:0"));
        countOrderService.submitCountResult(submitBO(id, lineId, new BigDecimal("120")));

        countOrderService.confirmCountDiff(confirmDiffBO(id));
        // 同一幂等键重复确认：命令层原样返回，不再推进、不再过账
        countOrderService.confirmCountDiff(confirmDiffBO(id));

        // 余额仅 +20 一次，冻结量只解一次
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)).get(0);
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("120")));
        assertEquals(0, balance.getQtyFrozen().compareTo(BigDecimal.ZERO));
        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsCountOrderMapper.selectById(id).getStatus());
    }

    @Test
    void testSubmitResult_duplicate_sameKeyDoesNotReAdvance() {
        seedBalance(new BigDecimal("100"));
        Long id = countOrderService.createCountOrder(createCountBO());
        Long lineId = firstLine(id).getId();

        WmsSubmitCountResultBO bo = submitBO(id, lineId, new BigDecimal("130"));
        countOrderService.submitCountResult(bo);
        // 同一幂等键重复提交实盘：命令层直接返回，不重复更新行
        countOrderService.submitCountResult(bo);

        WmsCountOrderLineDO line = firstLine(id);
        assertEquals(0, line.getDiffQty().compareTo(new BigDecimal("30")));
        assertEquals(WmsCountLineStatusEnum.DIFF.name(), line.getStatus());
        assertEquals(WmsOrderStatusEnum.COUNTING.name(), wmsCountOrderMapper.selectById(id).getStatus());
    }

}
