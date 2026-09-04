package com.iboot.pf.module.wms.service.countorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.CountOrderLineSaveReqVO;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.CountOrderSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.enums.WmsCountLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsCountTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_LINE_BALANCE_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_LINE_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.COUNT_ORDER_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 盘点单 Service 测试：单号自动分配、单号唯一、明细行号与余额唯一、
 * 账面数量与差异由余额快照推导、主单删除级联清理明细。
 */
@Import(CountOrderServiceImpl.class)
class CountOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CountOrderService countOrderService;
    @Resource
    private WmsCountOrderMapper countOrderMapper;
    @Resource
    private WmsCountOrderLineMapper countOrderLineMapper;
    @Resource
    private WmsInvBalanceMapper invBalanceMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "CNT0001" : invocation.getArgument(1));
    }

    private CountOrderSaveReqVO buildReqVO(String countNo) {
        CountOrderSaveReqVO reqVO = new CountOrderSaveReqVO();
        reqVO.setCountNo(countNo);
        reqVO.setCountType(WmsCountTypeEnum.FULL.name());
        reqVO.setInvOrgId(1L);
        reqVO.setWhId(1L);
        reqVO.setZoneId(0L);
        reqVO.setProjectId(0L);
        reqVO.setSnapshotTime(LocalDateTime.now().withNano(0));
        reqVO.setBlindCountFlag(false);
        reqVO.setStatus(WmsOrderStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    /** 预置一条库存余额，盘点明细的维度与账面数量都来自它。 */
    private Long seedBalance(String matCode, BigDecimal qtyOnhand) {
        WmsInvBalanceDO balance = new WmsInvBalanceDO();
        balance.setInvOrgId(1L);
        balance.setPlantId(1L);
        balance.setWhId(1L);
        balance.setLocId(1L);
        balance.setMatId(100L);
        balance.setBatchId(10L);
        balance.setContainerId(0L);
        balance.setProjectId(0L);
        balance.setProjectTaskId(0L);
        balance.setStockScope("COMMON");
        balance.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        balance.setQtyOnhand(qtyOnhand);
        balance.setQtyReserved(BigDecimal.ZERO);
        balance.setQtyFrozen(BigDecimal.ZERO);
        balance.setMatCode(matCode);
        balance.setMatName("物料A");
        balance.setLotNo("LOT-1");
        balance.setLocCode("LOC-1");
        balance.setVersion(0);
        invBalanceMapper.insert(balance);
        return balance.getId();
    }

    private CountOrderLineSaveReqVO buildLineReqVO(Long orderId, Integer lineNo, Long balanceId,
                                                  BigDecimal actualQty) {
        CountOrderLineSaveReqVO reqVO = new CountOrderLineSaveReqVO();
        reqVO.setOrderId(orderId);
        reqVO.setLineNo(lineNo);
        reqVO.setBalanceId(balanceId);
        reqVO.setActualQty(actualQty);
        reqVO.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setStatus(WmsCountLineStatusEnum.PENDING.name());
        reqVO.setRemark("单测明细");
        return reqVO;
    }

    @Test
    void testCreateCountOrder_blankNoFilledByRule() {
        Long id = countOrderService.createCountOrder(buildReqVO(null));

        WmsCountOrderDO order = countOrderMapper.selectById(id);
        assertNotNull(order);
        assertEquals("CNT0001", order.getCountNo());
    }

    @Test
    void testCreateCountOrder_duplicateNoRejected() {
        countOrderService.createCountOrder(buildReqVO("CNT-A"));

        assertServiceException(() -> countOrderService.createCountOrder(buildReqVO("CNT-A")),
                COUNT_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateCountOrder_keepOwnNo() {
        Long id = countOrderService.createCountOrder(buildReqVO("CNT-A"));

        CountOrderSaveReqVO updateReqVO = buildReqVO("CNT-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("盘点范围调整");
        countOrderService.updateCountOrder(updateReqVO);

        assertEquals("盘点范围调整", countOrderMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateCountOrder_notExists() {
        CountOrderSaveReqVO updateReqVO = buildReqVO("CNT-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> countOrderService.updateCountOrder(updateReqVO), COUNT_ORDER_NOT_EXISTS);
    }

    @Test
    void testCreateCountOrderLine_fillFromBalance() {
        Long orderId = countOrderService.createCountOrder(buildReqVO("CNT-A"));
        Long balanceId = seedBalance("M-001", new BigDecimal("100"));

        Long lineId = countOrderService.createCountOrderLine(
                buildLineReqVO(orderId, 10, balanceId, new BigDecimal("95")));

        // 账面数量必须来自余额，差异按实盘算，前端传什么都不影响
        WmsCountOrderLineDO line = countOrderLineMapper.selectById(lineId);
        assertNotNull(line);
        assertEquals("M-001", line.getMatCode());
        assertEquals("LOC-1", line.getLocCode());
        assertEquals(0, line.getBookQty().compareTo(new BigDecimal("100")));
        assertEquals(0, line.getDiffQty().compareTo(new BigDecimal("-5")));
    }

    @Test
    void testCreateCountOrderLine_duplicateLineNoRejected() {
        Long orderId = countOrderService.createCountOrder(buildReqVO("CNT-A"));
        Long firstBalance = seedBalance("M-001", new BigDecimal("100"));
        Long secondBalance = seedBalance("M-002", new BigDecimal("50"));
        countOrderService.createCountOrderLine(buildLineReqVO(orderId, 10, firstBalance, null));

        assertServiceException(
                () -> countOrderService.createCountOrderLine(buildLineReqVO(orderId, 10, secondBalance, null)),
                COUNT_ORDER_LINE_NO_DUPLICATE);
    }

    @Test
    void testCreateCountOrderLine_duplicateBalanceRejected() {
        Long orderId = countOrderService.createCountOrder(buildReqVO("CNT-A"));
        Long balanceId = seedBalance("M-001", new BigDecimal("100"));
        countOrderService.createCountOrderLine(buildLineReqVO(orderId, 10, balanceId, null));

        // 同一盘点单里一个余额只能盘一次，否则差异会被重复过账
        assertServiceException(
                () -> countOrderService.createCountOrderLine(buildLineReqVO(orderId, 20, balanceId, null)),
                COUNT_ORDER_LINE_BALANCE_DUPLICATE);
    }

    @Test
    void testDeleteCountOrder_cascadeLines() {
        Long orderId = countOrderService.createCountOrder(buildReqVO("CNT-A"));
        Long balanceId = seedBalance("M-001", new BigDecimal("100"));
        countOrderService.createCountOrderLine(buildLineReqVO(orderId, 10, balanceId, null));

        countOrderService.deleteCountOrder(orderId);

        assertNull(countOrderMapper.selectById(orderId));
        assertEquals(0, countOrderLineMapper.selectCount("order_id", orderId));
    }

    @Test
    void testDeleteCountOrder_notExists() {
        assertServiceException(() -> countOrderService.deleteCountOrder(randomLongId()), COUNT_ORDER_NOT_EXISTS);
    }

}
