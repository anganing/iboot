package com.iboot.pf.module.wms.service.arrivalorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.mysql.arrival.ArrivalMapper;
import com.iboot.pf.module.wms.dal.mysql.arrivalline.ArrivalLineMapper;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderLineMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsWhMapper;
import com.iboot.pf.module.wms.enums.WmsArrivalLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalStatusEnum;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsConfirmArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsCreateArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsUpdateArrivalBO;
import com.iboot.pf.module.wms.service.batch.WmsBatchServiceImpl;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.command.WmsCommandServiceImpl;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderServiceImpl;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.warehouse.WmsWarehouseServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_CANNOT_CANCEL;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_CANNOT_CONFIRM;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_CANNOT_MODIFY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_EMPTY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_NOT_READY;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * 到货登记流程测试：登记、改单、删单、确认到货（批次注册与按 need_qc 分流）、取消、生成入库单。
 */
@Import({WmsArrivalOrderServiceImpl.class, WmsInOrderServiceImpl.class, WmsWarehouseServiceImpl.class,
        WmsBatchServiceImpl.class, WmsInventoryServiceImpl.class, WmsCodeService.class,
        WmsCommandServiceImpl.class})
class WmsArrivalOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsArrivalOrderService arrivalOrderService;
    @Resource
    private ArrivalMapper arrivalMapper;
    @Resource
    private ArrivalLineMapper arrivalLineMapper;
    @Resource
    private WmsBatchMapper wmsBatchMapper;
    @Resource
    private WmsInOrderMapper wmsInOrderMapper;
    @Resource
    private WmsInOrderLineMapper wmsInOrderLineMapper;
    @Resource
    private WmsWhMapper wmsWhMapper;
    @MockitoBean
    private CodeRuleApi codeRuleApi;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private UomApi uomApi;

    private static final Long WH = 1L;
    private static final Long MAT = 100L;
    private static final Long MAT_B = 200L;
    private static final Long UOM = 1L;
    private static final Long SUPPLIER = 9L;
    private static final String ARRIVAL_NO = "AR-0001";
    private static final String CONFIRM_KEY = "UI:AR-0001:CONFIRM_ARRIVAL:0";

    @BeforeEach
    void setUp() {
        wmsWhMapper.insert(new WmsWhDO().setId(WH).setPlantId(1L).setCode("WH-1").setName("主仓库").setStatus(0));
        when(codeRuleApi.generate("WMS_ARRIVAL_NO")).thenReturn(ARRIVAL_NO);
        when(codeRuleApi.generate("WMS_IN_ORDER_NO")).thenReturn("IN-0001");
        when(materialApi.getMaterialMap(anyCollection()))
                .thenReturn(Map.of(MAT, material(MAT, "M-001", "物料A"), MAT_B, material(MAT_B, "M-002", "物料B")));
        UomRespDTO uom = new UomRespDTO();
        uom.setId(UOM);
        uom.setCode("PCS");
        when(uomApi.getUomMap(anyCollection())).thenReturn(Map.of(UOM, uom));
    }

    private MaterialRespDTO material(Long id, String code, String name) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(id);
        material.setCode(code);
        material.setName(name);
        return material;
    }

    private WmsCreateArrivalBO.Line line(Integer lineNo, Long matId, String lotNo,
                                         Boolean needQc, String iqcMode) {
        WmsCreateArrivalBO.Line line = new WmsCreateArrivalBO.Line();
        line.setLineNo(lineNo);
        line.setMatId(matId);
        line.setLotNo(lotNo);
        line.setSupplierLotNo("SL-" + lineNo);
        line.setArrivalQty(new BigDecimal("100"));
        line.setUomId(UOM);
        line.setNeedQc(needQc);
        line.setIqcMode(iqcMode);
        return line;
    }

    private void fillHeader(WmsCreateArrivalBO bo) {
        bo.setSourceType("PURCHASE_ORDER");
        bo.setSourceNo("PO-1");
        bo.setSupplierId(SUPPLIER);
        bo.setSupplierCode("SUP-01");
        bo.setSupplierName("供应商甲");
        bo.setInvOrgId(1L);
        bo.setPlantId(1L);
        bo.setWhId(WH);
    }

    /** 单行到货登记参数，第一行行号 10、物料 A、批次 L-001。 */
    private WmsCreateArrivalBO createBO(Boolean needQc, String iqcMode) {
        WmsCreateArrivalBO bo = new WmsCreateArrivalBO();
        fillHeader(bo);
        bo.setLines(new ArrayList<>(List.of(line(10, MAT, "L-001", needQc, iqcMode))));
        return bo;
    }

    private WmsUpdateArrivalBO updateBO(Long arrivalId, Integer lineNo, BigDecimal qty) {
        WmsUpdateArrivalBO bo = new WmsUpdateArrivalBO();
        fillHeader(bo);
        bo.setArrivalId(arrivalId);
        WmsCreateArrivalBO.Line line = line(lineNo, MAT, "L-001", false, null);
        line.setArrivalQty(qty);
        bo.setLines(new ArrayList<>(List.of(line)));
        return bo;
    }

    private WmsConfirmArrivalBO confirmBO(Long arrivalId, String key) {
        WmsConfirmArrivalBO bo = new WmsConfirmArrivalBO();
        bo.setIdempotencyKey(key);
        bo.setSourceModule("UI");
        bo.setSourceBizNo(ARRIVAL_NO);
        bo.setArrivalId(arrivalId);
        return bo;
    }

    private WmsCancelOrderBO cancelBO(Long arrivalId) {
        WmsCancelOrderBO bo = new WmsCancelOrderBO();
        bo.setOrderId(arrivalId);
        bo.setReason("供应商送错货");
        return bo;
    }

    /** 建一张两行到货单并确认：行 10 不检验落 READY，行 20 需检验落 QC_PENDING。 */
    private Long confirmedTwoLineArrival() {
        WmsCreateArrivalBO bo = createBO(false, null);
        bo.getLines().add(line(20, MAT_B, "L-002", true, WmsIqcModeEnum.PRE_INBOUND.name()));
        Long id = arrivalOrderService.createArrival(bo);
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));
        return id;
    }

    private List<ArrivalLineDO> lines(Long arrivalId) {
        return arrivalOrderService.getArrivalLineList(arrivalId);
    }

    @Test
    void testCreate_draftWithoutArrivalTime() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));

        ArrivalDO arrival = arrivalMapper.selectById(id);
        assertEquals(ARRIVAL_NO, arrival.getArrivalNo());
        assertEquals(WmsArrivalStatusEnum.DRAFT.name(), arrival.getStatus());
        assertNull(arrival.getArrivalTime(), "登记只是预告，实际到货时间要等确认动作写");

        ArrivalLineDO line = lines(id).get(0);
        assertEquals(WmsArrivalLineStatusEnum.PENDING.name(), line.getStatus());
        // 物料与单位可读字段是快照，调用方不传也要落库
        assertEquals("M-001", line.getMatCode());
        assertEquals("物料A", line.getMatName());
        assertEquals("PCS", line.getUomCode());
    }

    @Test
    void testCreate_positiveQty_required() {
        WmsCreateArrivalBO bo = createBO(false, null);
        bo.getLines().get(0).setArrivalQty(BigDecimal.ZERO);
        assertServiceException(() -> arrivalOrderService.createArrival(bo), QTY_MUST_BE_POSITIVE);
    }

    @Test
    void testCreate_emptyLines_rejected() {
        WmsCreateArrivalBO bo = createBO(false, null);
        bo.setLines(List.of());
        assertServiceException(() -> arrivalOrderService.createArrival(bo), ARRIVAL_LINE_EMPTY);
    }

    @Test
    void testConfirm_draftToArrived_registersBatch() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));

        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        ArrivalDO arrival = arrivalMapper.selectById(id);
        assertEquals(WmsArrivalStatusEnum.ARRIVED.name(), arrival.getStatus());
        assertNotNull(arrival.getArrivalTime(), "确认到货必须写实际到货时间");

        // 批次注册：供应商维度取自单头、供应商批次号取自明细，两者都要带到批次上
        WmsBatchDO batch = wmsBatchMapper.selectByMatAndLot(MAT, "L-001", null);
        assertNotNull(batch, "确认到货应注册批次");
        assertEquals(SUPPLIER, batch.getSupplierId());
        assertEquals("SUP-01", batch.getSupplierCode());
        assertEquals("SL-10", batch.getSupplierLotNo());
        // 批次号回写明细，后续生成入库单直接复用
        assertEquals("L-001", lines(id).get(0).getLotNo());
    }

    @Test
    void testConfirm_blankLotNo_fallsBackToDefaultBatch() {
        WmsCreateArrivalBO bo = createBO(false, null);
        bo.getLines().get(0).setLotNo(null);
        Long id = arrivalOrderService.createArrival(bo);

        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        // 无批次物料落默认批次号，否则 registerBatch 会因批次号为空直接抛错
        assertNotNull(wmsBatchMapper.selectByMatAndLot(MAT, "DEFAULT", null));
        assertEquals("DEFAULT", lines(id).get(0).getLotNo());
    }

    @Test
    void testConfirm_nonDraft_rejected() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        // 换幂等键才会真正执行动作，此时状态已是 ARRIVED，应被状态校验拦住
        assertServiceException(
                () -> arrivalOrderService.confirmArrival(confirmBO(id, "UI:AR-0001:CONFIRM_ARRIVAL:1")),
                ARRIVAL_CANNOT_CONFIRM);
    }

    @Test
    void testConfirm_idempotent_noSecondBatch() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));

        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));
        assertEquals(1L, wmsBatchMapper.selectCount());

        // 同一幂等键重复确认：命令层直接返回，不再执行动作，不会多出一批批次
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));
        assertEquals(1L, wmsBatchMapper.selectCount(), "重复确认不应产生第二批批次");
        assertEquals(WmsArrivalStatusEnum.ARRIVED.name(), arrivalMapper.selectById(id).getStatus());
    }

    @Test
    void testConfirm_lineStatus_splitByNeedQc() {
        Long id = confirmedTwoLineArrival();

        List<ArrivalLineDO> lines = lines(id);
        assertEquals(WmsArrivalLineStatusEnum.READY.name(), lines.get(0).getStatus());
        assertEquals(WmsArrivalLineStatusEnum.QC_PENDING.name(), lines.get(1).getStatus());
        // 待质检是明细维度的事，单头统一停在 ARRIVED
        assertEquals(WmsArrivalStatusEnum.ARRIVED.name(), arrivalMapper.selectById(id).getStatus());
        // 两个物料两条批次
        assertEquals(2L, wmsBatchMapper.selectCount());
    }

    @Test
    void testCreateInOrder_poIn_qualified_andArrivalDone() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        Long inOrderId = arrivalOrderService.createInOrderFromArrival(id, null);

        WmsInOrderDO inOrder = wmsInOrderMapper.selectById(inOrderId);
        assertEquals(WmsInBizTypeEnum.PO_IN.name(), inOrder.getBizType());
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), inOrder.getQualityStatus());
        assertEquals(id, inOrder.getArrivalId());
        assertEquals(SUPPLIER, inOrder.getSupplierId());

        WmsInOrderLineDO inLine = wmsInOrderLineMapper.selectListByOrderId(inOrderId).get(0);
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), inLine.getQualityStatus());
        assertEquals(0, inLine.getPlanQty().compareTo(new BigDecimal("100")));
        // 入库单复用到货时注册的批次，不新建
        assertEquals(1L, wmsBatchMapper.selectCount());
        assertEquals(wmsBatchMapper.selectByMatAndLot(MAT, "L-001", null).getId(), inLine.getBatchId());

        // 明细回写 DONE，全部明细 DONE 时单头置 DONE
        assertEquals(WmsArrivalLineStatusEnum.DONE.name(), lines(id).get(0).getStatus());
        assertEquals(WmsArrivalStatusEnum.DONE.name(), arrivalMapper.selectById(id).getStatus());
    }

    @Test
    void testCreateInOrder_postInboundPending_qcPending() {
        Long id = arrivalOrderService.createArrival(
                createBO(false, WmsIqcModeEnum.POST_INBOUND_PENDING.name()));
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        Long inOrderId = arrivalOrderService.createInOrderFromArrival(id, null);

        // 先入待检：库存进账时质量状态是待检，要靠 QUALITY_CHANGE 过账才转正
        assertEquals(WmsQualityStatusEnum.QC_PENDING.name(),
                wmsInOrderMapper.selectById(inOrderId).getQualityStatus());
        assertEquals(WmsQualityStatusEnum.QC_PENDING.name(),
                wmsInOrderLineMapper.selectListByOrderId(inOrderId).get(0).getQualityStatus());
    }

    @Test
    void testCreateInOrder_partialLines_arrivalStaysArrived() {
        Long id = confirmedTwoLineArrival();
        Long readyLineId = lines(id).get(0).getId();
        Long qcLineId = lines(id).get(1).getId();

        Long inOrderId = arrivalOrderService.createInOrderFromArrival(id, List.of(readyLineId));

        assertEquals(1, wmsInOrderLineMapper.selectListByOrderId(inOrderId).size());
        assertEquals(WmsArrivalLineStatusEnum.DONE.name(), arrivalLineMapper.selectById(readyLineId).getStatus());
        // 还有待质检明细没走完，单头不能置 DONE
        assertEquals(WmsArrivalStatusEnum.ARRIVED.name(), arrivalMapper.selectById(id).getStatus());
        // 待质检明细必须等 IQC 结论，不能直接生成入库单
        assertServiceException(() -> arrivalOrderService.createInOrderFromArrival(id, List.of(qcLineId)),
                ARRIVAL_LINE_NOT_READY);
    }

    @Test
    void testCreateInOrder_beforeConfirm_rejected() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));

        // 未确认到货的明细还没有批次，不允许生成入库单
        assertServiceException(() -> arrivalOrderService.createInOrderFromArrival(id, null),
                ARRIVAL_LINE_NOT_READY);
    }

    @Test
    void testUpdate_replacesLines() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));
        Long oldLineId = lines(id).get(0).getId();

        arrivalOrderService.updateArrival(updateBO(id, 20, new BigDecimal("60")));

        // 明细整批替换：旧行不在了，新行按新数量与行号重建
        List<ArrivalLineDO> lines = lines(id);
        assertEquals(1, lines.size());
        assertEquals(20, lines.get(0).getLineNo());
        assertEquals(0, lines.get(0).getArrivalQty().compareTo(new BigDecimal("60")));
        assertNull(arrivalLineMapper.selectById(oldLineId));
        // 单号与状态是单据身份，改单不动
        ArrivalDO arrival = arrivalMapper.selectById(id);
        assertEquals(ARRIVAL_NO, arrival.getArrivalNo());
        assertEquals(WmsArrivalStatusEnum.DRAFT.name(), arrival.getStatus());
    }

    @Test
    void testUpdate_nonDraft_rejected() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        assertServiceException(() -> arrivalOrderService.updateArrival(updateBO(id, 20, new BigDecimal("60"))),
                ARRIVAL_CANNOT_MODIFY);
    }

    @Test
    void testDelete_draftOnly() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));

        arrivalOrderService.deleteArrival(id);

        assertNull(arrivalMapper.selectById(id));
        assertEquals(0, lines(id).size());
    }

    @Test
    void testDelete_nonDraft_rejected() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));

        assertServiceException(() -> arrivalOrderService.deleteArrival(id), ARRIVAL_CANNOT_MODIFY);
    }

    @Test
    void testCancel_keepsLinesAlreadyInbounded() {
        Long id = confirmedTwoLineArrival();
        Long readyLineId = lines(id).get(0).getId();
        arrivalOrderService.createInOrderFromArrival(id, List.of(readyLineId));

        arrivalOrderService.cancelArrival(cancelBO(id));

        assertEquals(WmsArrivalStatusEnum.CANCELLED.name(), arrivalMapper.selectById(id).getStatus());
        // 已生成入库单的明细保持 DONE：入库单有自己的取消动作，这里回退会让两边对不上
        assertEquals(WmsArrivalLineStatusEnum.DONE.name(), arrivalLineMapper.selectById(readyLineId).getStatus());
        assertEquals(WmsArrivalLineStatusEnum.CANCELLED.name(), lines(id).get(1).getStatus());

        // 已取消不能再取消
        assertServiceException(() -> arrivalOrderService.cancelArrival(cancelBO(id)), ARRIVAL_CANNOT_CANCEL);
    }

    @Test
    void testCancel_doneArrival_rejected() {
        Long id = arrivalOrderService.createArrival(createBO(false, null));
        arrivalOrderService.confirmArrival(confirmBO(id, CONFIRM_KEY));
        arrivalOrderService.createInOrderFromArrival(id, null);

        // 全部明细已生成入库单，单据是终态，不能取消
        assertServiceException(() -> arrivalOrderService.cancelArrival(cancelBO(id)), ARRIVAL_CANNOT_CANCEL);
    }

}
