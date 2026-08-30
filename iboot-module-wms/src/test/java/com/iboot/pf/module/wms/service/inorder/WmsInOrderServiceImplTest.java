package com.iboot.pf.module.wms.service.inorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.batch.WmsBatchServiceImpl;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyCollection;

/** 入库单流程测试：创建（批次注册幂等）、取消、过账。 */
@Import({WmsInOrderServiceImpl.class, WmsWarehouseServiceImpl.class, WmsBatchServiceImpl.class,
        WmsInventoryServiceImpl.class, WmsCodeService.class})
class WmsInOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsInOrderService inOrderService;
    @Resource
    private WmsInOrderMapper wmsInOrderMapper;
    @Resource
    private WmsInOrderLineMapper wmsInOrderLineMapper;
    @Resource
    private WmsBatchMapper wmsBatchMapper;
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

    private static final Long WH = 1L;
    private static final Long LOC = 1L;
    private static final Long MAT = 100L;

    @BeforeEach
    void setUp() {
        wmsWhMapper.insert(new WmsWhDO().setId(WH).setPlantId(1L).setCode("WH-1").setName("主仓库").setStatus(0));
        wmsLocMapper.insert(new WmsLocDO().setId(LOC).setWhId(WH).setZoneId(0L).setCode("LOC-1").setStatus("ENABLED"));
        when(codeRuleApi.generate("WMS_IN_ORDER_NO")).thenReturn("IN-0001");
        when(codeRuleApi.generate("WMS_INV_TXN_NO")).thenReturn("TXN-0001");
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(MAT);
        material.setCode("M-001");
        material.setName("物料A");
        when(materialApi.getMaterialMap(anyCollection())).thenReturn(Map.of(MAT, material));
    }

    private WmsCreateInOrderBO createInBO(BigDecimal qty) {
        WmsCreateInOrderBO bo = new WmsCreateInOrderBO();
        bo.setIdempotencyKey("WMS:IN:1");
        bo.setSourceModule("MES");
        bo.setSourceBizNo("MO-1");
        bo.setBizType("PO_IN");
        bo.setInvOrgId(1L);
        bo.setPlantId(1L);
        bo.setWhId(WH);
        bo.setQualityStatus("QUALIFIED");

        WmsCreateInOrderBO.Line line = new WmsCreateInOrderBO.Line();
        line.setLineNo(10);
        line.setMatId(MAT);
        line.setMatCode("M-001");
        line.setMatName("物料A");
        line.setLotNo("L-001");
        line.setUomId(1L);
        line.setLocId(LOC);
        line.setPlanQty(qty);
        bo.setLines(List.of(line));
        return bo;
    }

    private WmsConfirmOrderBO confirmBO(Long orderId) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey("WMS:IN:1");
        bo.setSourceModule("MES");
        bo.setSourceBizNo("MO-1");
        bo.setOrderId(orderId);
        return bo;
    }

    @Test
    void testCreate_registersBatch_andIdempotent() {
        Long id1 = inOrderService.createInOrder(createInBO(new BigDecimal("100")));
        Long id2 = inOrderService.createInOrder(createInBO(new BigDecimal("100")));
        assertEquals(id1, id2, "幂等命中应返回原单据编号");

        // 批次注册：同一物料+批次号只产生一条
        WmsBatchDO batch = wmsBatchMapper.selectByMatAndLot(MAT, "L-001", null);
        assertNotNull(batch);
        assertEquals("L-001", batch.getLotNo());

        WmsInOrderDO order = wmsInOrderMapper.selectById(id1);
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), order.getStatus());
        assertEquals("IN-0001", order.getInNo());
    }

    @Test
    void testCreate_positiveQty_required() {
        WmsCreateInOrderBO bo = createInBO(new BigDecimal("100"));
        bo.getLines().get(0).setPlanQty(BigDecimal.ZERO);
        assertServiceException(() -> inOrderService.createInOrder(bo), QTY_MUST_BE_POSITIVE);
    }

    @Test
    void testCreate_emptyLines_rejected() {
        WmsCreateInOrderBO bo = createInBO(new BigDecimal("100"));
        bo.setLines(List.of());
        assertServiceException(() -> inOrderService.createInOrder(bo), IN_ORDER_LINE_EMPTY);
    }

    @Test
    void testCancel_draftOnly() {
        Long id = inOrderService.createInOrder(createInBO(new BigDecimal("100")));

        WmsCancelOrderBO cancel = new WmsCancelOrderBO();
        cancel.setOrderId(id);
        cancel.setReason("取消");
        inOrderService.cancelInOrder(cancel);
        assertEquals(WmsOrderStatusEnum.CANCELLED.name(), wmsInOrderMapper.selectById(id).getStatus());

        // 已取消不能再取消
        assertServiceException(() -> inOrderService.cancelInOrder(cancel), IN_ORDER_CANNOT_CANCEL);
    }

    @Test
    void testConfirm_postsBalanceAndBatch() {
        Long id = inOrderService.createInOrder(createInBO(new BigDecimal("100")));
        inOrderService.confirmInOrder(confirmBO(id));

        // 单据过账
        assertEquals(WmsOrderStatusEnum.DONE.name(), wmsInOrderMapper.selectById(id).getStatus());
        WmsInOrderLineDO line = wmsInOrderLineMapper.selectListByOrderId(id).get(0);
        assertEquals(WmsOrderStatusEnum.DONE.name(), line.getStatus());
        assertEquals(0, line.getPostedQty().compareTo(new BigDecimal("100")));

        // 库存余额增加
        WmsInvBalanceDO balance = wmsInvBalanceMapper.selectById(requireBalanceId());
        assertEquals(0, balance.getQtyOnhand().compareTo(new BigDecimal("100")));
        // 流水落库
        assertEquals(1, wmsInvTxnMapper.selectCount());
    }

    @Test
    void testConfirm_postsOnce_thenStateBlocksReConfirm() {
        Long id = inOrderService.createInOrder(createInBO(new BigDecimal("100")));
        inOrderService.confirmInOrder(confirmBO(id));

        // 已过账单据状态为 DONE，二次过账被状态机拒绝，流水不重复
        assertServiceException(() -> inOrderService.confirmInOrder(confirmBO(id)), IN_ORDER_CANNOT_CONFIRM);
        assertEquals(0, requireBalance().getQtyOnhand().compareTo(new BigDecimal("100")));
        assertEquals(1, wmsInvTxnMapper.selectCount(), "重复过账不应产生第二条流水");
    }

    private Long requireBalanceId() {
        WmsInvBalanceDO balance = requireBalance();
        return balance == null ? null : balance.getId();
    }

    private WmsInvBalanceDO requireBalance() {
        return wmsInvBalanceMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<WmsInvBalanceDO>lambdaQuery()
                        .eq(WmsInvBalanceDO::getMatId, MAT)
                        .eq(WmsInvBalanceDO::getLocId, LOC)).stream().findFirst().orElse(null);
    }

}
