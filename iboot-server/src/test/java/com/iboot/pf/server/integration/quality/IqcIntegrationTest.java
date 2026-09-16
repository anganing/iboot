package com.iboot.pf.server.integration.quality;

import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.qcresult.MesQcResultDomainService;
import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import com.iboot.pf.module.wms.enums.WmsArrivalLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.service.arrivalorder.WmsArrivalOrderService;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsConfirmArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsCreateArrivalBO;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
import com.iboot.pf.server.integration.fixture.QcFixture;
import com.iboot.pf.server.integration.fixture.WmsFixture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_NOT_READY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-05 端到端用例 6：IQC 来料检验的两种模式。
 *
 * <p>依据 {@code 05-MES质量子域流程.md} 的"五个检验点"与 {@code 08-跨模块协作与端到端时序.md}
 * 的"链路一：采购到货入库"。
 *
 * <p>与模块内测试的本质区别：这里是<b>完整的跨模块链路</b>——WMS 的 {@code confirmArrival}
 * 发 {@code ArrivalConfirmedEvent} 领域事件，MES 的 {@code MesIqcTriggerListener} 监听后经
 * {@code QcTriggerService} 按 {@code mes_qc_rule} 解析规则建质检任务，再经 {@code ArrivalOrderApi}
 * 把 {@code qcTaskId} 回写到货明细。全链无 mock。
 *
 * <p>三种场景（对应 T6-05 条目的 1.1 ~ 1.3）：
 * <ol>
 *     <li>{@code PRE_INBOUND} 先检后入：检验前建入库单被拦（{@code ARRIVAL_LINE_NOT_READY}），
 *         检验合格后方可建单，余额质量状态为 {@code QUALIFIED}</li>
 *     <li>{@code POST_INBOUND_PENDING} 先入待检：到货即放行、入库余额 {@code QC_PENDING}；
 *         检验合格后 {@code QUALITY_CHANGE} 过账转 {@code QUALIFIED}，且 {@code qty_onhand} 不变</li>
 *     <li>规则未命中时行直接 {@code READY}，无质检任务</li>
 * </ol>
 *
 * <p><b>关于 {@code iqcMode} 的写法</b>：{@code WmsArrivalOrderServiceImpl} 把 {@code iqcMode}
 * 当"调用方入参"用，全仓没有一处按 IQC 规则解析结果写它（{@code iqcMode} 的写入点只有
 * 到货 VO 与 {@code WmsCreateArrivalBO}，Service 内无赋值）。因此本类在到货参数上显式指定模式，
 * 覆盖的是"两种模式下结论回流的后果不同"这一段真实行为，而不是绕开规则解析器造假场景。
 */
class IqcIntegrationTest extends BaseIntegrationTest {

    /** 调用方来源模块：控制器固定传 {@code UI}，集成测试传 {@code INTEGRATION}。 */
    private static final String SOURCE_MODULE = "INTEGRATION";

    /** 到货数量：检验、入库、余额断言都围着它转。 */
    private static final BigDecimal ARRIVAL_QTY = new BigDecimal("100");

    /** 同一用例内可能建多张到货单，靠它区分单号与批次号。 */
    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    @Resource
    private WmsArrivalOrderService arrivalOrderService;

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;

    @Resource
    private MesQcResultDomainService qcResultDomainService;

    @Resource
    private QcResultMapper qcResultMapper;

    @Resource
    private QcTaskMapper qcTaskMapper;

    @Resource
    private InventoryApi inventoryApi;

    @Resource
    private InOrderApi inOrderApi;

    @Resource
    private WmsInOrderService inOrderService;

    @Resource
    private BatchApi batchApi;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private WmsFixture wmsFixture;

    @Resource
    private QcFixture qcFixture;

    private PilotLine line;

    private WmsFixture.WmsStock stock;

    @BeforeEach
    void buildPilotLine() {
        line = mdmFixture.createPilotLine();
        stock = wmsFixture.createWarehouseWithStock(line);
        // QcFixture 的 IQC 规则按"第一个子件"匹配（见 QcFixture#createQcBasics），
        // 故 IQC 场景一律按这个物料到货；第二条规则覆盖 IPQC/FAI 按首道工序、FQC/OQC 按成品
        qcFixture.createQcBasics(line);
    }

    // ==================== 场景一：PRE_INBOUND 先检后入 ====================

    @Test
    void testIqc_PreInbound_BlocksInboundUntilQualified() {
        // 准备：到货确认（need_qc = true、PRE_INBOUND），规则命中后明细应停在 QC_PENDING
        Long arrivalId = createArrival(WmsIqcModeEnum.PRE_INBOUND);
        ArrivalLineDO pending = confirmAndGetLine(arrivalId);

        // 断言 1：到货确认后挂上了质检任务，明细状态是待检
        assertNotNull(pending.getQcTaskId(), "IQC 规则命中后必须把 qcTaskId 回写到到货明细");
        assertEquals(WmsArrivalLineStatusEnum.QC_PENDING.name(), pending.getStatus());
        QcTaskDO task = qcTaskMapper.selectById(pending.getQcTaskId());
        assertEquals(MesQcTriggerPointEnum.IQC.name(), task.getTriggerPoint());
        // 规则解析可解释：命中的规则编号落在任务上，事后能回答"这批为什么检"
        assertNotNull(task.getRuleId());
        assertNotNull(task.getQcPlanVersion());

        // 断言 2（验收点名）：检验前建入库单被拦
        ServiceException blocked = assertThrows(ServiceException.class,
                () -> arrivalOrderService.createInOrderFromArrival(arrivalId, List.of(pending.getId())));
        assertEquals(ARRIVAL_LINE_NOT_READY.getCode(), blocked.getCode(),
                "待检明细生成入库单必须被 ARRIVAL_LINE_NOT_READY 拦住");

        // 调用：检验合格（走真实的结果录入 + 结论提交链路）
        submitQualifiedConclusion(pending.getQcTaskId(), ARRIVAL_QTY);

        // 断言 3：结论回写后明细放行到 READY，合格数量落库
        ArrivalLineDO released = reloadLine(arrivalId);
        assertEquals(WmsArrivalLineStatusEnum.READY.name(), released.getStatus());
        assertEquals(0, ARRIVAL_QTY.compareTo(released.getQualifiedQty()));

        // 断言 4：现在可以建单并过账，余额质量状态为 QUALIFIED
        postInbound(released);
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), qualityStatusOf(batchIdOf(released)),
                "检验合格后放行的入库必须落合格库存，否则有账不能用的货");
        assertEquals(0, ARRIVAL_QTY.compareTo(onhandOfBatch(batchIdOf(released))),
                "本批应恰好入库 100");
    }

    // ==================== 场景二：POST_INBOUND_PENDING 先入待检 ====================

    @Test
    void testIqc_PostInboundPending_QualityChangeKeepsOnhandUnchanged() {
        // 准备：先入待检模式到货确认
        Long arrivalId = createArrival(WmsIqcModeEnum.POST_INBOUND_PENDING);
        ArrivalLineDO ready = confirmAndGetLine(arrivalId);

        // 断言 1：先入待检的行在规则命中后立即放行到 READY（货可以先进库房）
        assertNotNull(ready.getQcTaskId(), "先入待检同样要挂上质检任务，只是不挡入库");
        assertEquals(WmsArrivalLineStatusEnum.READY.name(), ready.getStatus());

        // 断言 2（验收点名）：入库即得，余额质量状态是 QC_PENDING
        postInbound(ready);
        assertEquals(WmsQualityStatusEnum.QC_PENDING.name(), qualityStatusOf(batchIdOf(ready)),
                "先入待检的货必须标 QC_PENDING，否则待检货会被当成可用料");
        BigDecimal onhandAfterInbound = onhandOfBatch(batchIdOf(ready));
        assertEquals(0, ARRIVAL_QTY.compareTo(onhandAfterInbound),
                "先入后检也是真入库：现存量要真实增加");

        // 调用：检验合格
        submitQualifiedConclusion(ready.getQcTaskId(), ARRIVAL_QTY);

        // 断言 3：QUALITY_CHANGE 过账把余额质量状态迁到 QUALIFIED
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), qualityStatusOf(batchIdOf(ready)),
                "合格结论必须做 QUALITY_CHANGE 过账，否则库里永远是一批待检货");
        // 断言 4（验收点名）：总量不变——状态迁移只换质量维度，不动实物数量
        assertEquals(0, onhandAfterInbound.compareTo(onhandOfBatch(batchIdOf(ready))),
                "QUALITY_CHANGE 只改 quality_status，qty_onhand 必须一分不动");
    }

    // ==================== 场景三：规则未命中直接放行 ====================

    @Test
    void testIqc_RuleNotMatched_LineReadyWithoutQcTask() {
        // 准备：用"没有 IQC 规则匹配"的物料到货——试点产线的倒冲件不在 IQC 规则的 matId 维度上
        Long otherMatId = line.componentMatIds().get(1);
        Long arrivalId = createArrivalFor(otherMatId, WmsIqcModeEnum.PRE_INBOUND);
        ArrivalLineDO lineDO = confirmAndGetLine(arrivalId);

        // 断言：规则未命中时不生成质检任务，明细直接放行到 READY
        assertNull(lineDO.getQcTaskId(), "规则未命中时 qc_task_id 应为空，业务动作直接放行");
        assertEquals(WmsArrivalLineStatusEnum.READY.name(), lineDO.getStatus(),
                "未命中规则的行必须能直接入库，否则漏配规则会把入库卡死");
        assertTrue(qcTaskMapper.selectList(QcTaskDO::getTriggerPoint, MesQcTriggerPointEnum.IQC.name())
                        .stream().noneMatch(task -> otherMatId.equals(task.getMatId())),
                "未命中规则的物料不该有 IQC 任务");
    }

    // ==================== 建数据 ====================

    private Long createArrival(WmsIqcModeEnum iqcMode) {
        return createArrivalFor(line.anyComponentMatId(), iqcMode);
    }

    /** 建一张到货单（单行），need_qc 为真、按指定 IQC 模式；返回到货单编号。 */
    private Long createArrivalFor(Long matId, WmsIqcModeEnum iqcMode) {
        WmsCreateArrivalBO bo = new WmsCreateArrivalBO();
        bo.setSourceType("PURCHASE_ORDER");
        bo.setSourceNo("T6-05-IQC-" + KEY_SEQ.incrementAndGet());
        bo.setInvOrgId(line.invOrgId());
        bo.setPlantId(line.plantId());
        bo.setWhId(stock.whId());

        WmsCreateArrivalBO.Line lineBO = new WmsCreateArrivalBO.Line();
        lineBO.setLineNo(10);
        lineBO.setMatId(matId);
        lineBO.setArrivalQty(ARRIVAL_QTY);
        lineBO.setUomId(line.uomId());
        lineBO.setNeedQc(true);
        lineBO.setIqcMode(iqcMode.name());
        lineBO.setSupplierLotNo("T6-05-SLOT-" + KEY_SEQ.incrementAndGet());
        bo.setLines(List.of(lineBO));
        return arrivalOrderService.createArrival(bo);
    }

    /** 确认到货：走真实事件链路（MES 侧监听器会解析规则、建任务并回写）并取回明细。 */
    private ArrivalLineDO confirmAndGetLine(Long arrivalId) {
        WmsConfirmArrivalBO confirm = new WmsConfirmArrivalBO();
        confirm.setSourceModule(SOURCE_MODULE);
        confirm.setSourceBizNo("T6-05-IQC");
        confirm.setIdempotencyKey(SOURCE_MODULE + ":T6-05-IQC:CONFIRM_ARRIVAL:" + arrivalId);
        confirm.setArrivalId(arrivalId);
        arrivalOrderService.confirmArrival(confirm);
        return arrivalOrderService.getArrivalLineList(arrivalId).get(0);
    }

    /**
     * 提交质检任务的合格结论，走完整的"开始检验 -&gt; 逐格录实测值 -&gt; 提交结论"三步。
     *
     * <p>必须真的录完结果：{@code submitConclusion} 要求任务处于 {@code RESULT_POSTING}
     * （录完全部必检项才自动推进到那），跳过录入直接交结论会被状态机拦。
     * 合格结论会经 {@code MesIqcResultHandler} 回调 WMS 的 {@code completeIqc}。
     */
    private void submitQualifiedConclusion(Long qcTaskId, BigDecimal qty) {
        QcTaskDO task = qcTaskMapper.selectById(qcTaskId);
        assertNotNull(task, "质检任务必须存在，否则结论提交无从谈起");

        // 开始检验：CREATED -> ASSIGNED -> INSPECTING
        MesQcTaskActionBO assign = actionBO(qcTaskId, task.getTaskNo(), "ASSIGN_QC_TASK");
        assign.setInspectorId(1L);
        qcTaskDomainService.assignQcTask(assign);
        qcTaskDomainService.startInspect(actionBO(qcTaskId, task.getTaskNo(), "START_INSPECT"));

        // 逐格录实测值：QcFixture 的方案是 FULL 抽样，样本数 = 送检数量；
        // 两个检验项（数值型 DIM-01、文本型 APP-01）都必须录到才推得进 RESULT_POSTING。
        // QcFixture 的 DIM-01 标准值 10.00、上下限 9.90 ~ 10.10（合格），APP-01 标准值"无划伤"
        List<QcResultDO> results = qcResultMapper.selectListByTaskId(qcTaskId);
        assertTrue(!results.isEmpty(), "任务生成时必须预展开检验结果空行");
        MesSaveQcResultBO save = new MesSaveQcResultBO();
        save.setTaskId(qcTaskId);
        save.setInspectorId(1L);
        save.setResults(results.stream().map(result -> {
            MesSaveQcResultBO.Item item = new MesSaveQcResultBO.Item();
            item.setItemId(result.getItemId());
            item.setSampleNo(result.getSampleNo());
            // 按检验项编码给各自量纲的合格值：数值项给 10.00，文本项给"无划伤"
            item.setActualValue("DIM-01".equals(result.getItemCode()) ? "10.00" : "无划伤");
            return item;
        }).toList());
        qcResultDomainService.batchSaveResult(save);

        // 提交结论：合格数量必须与抽检数（= 送检数量，FULL 抽样）相等
        MesQcTaskConclusionBO conclusion = new MesQcTaskConclusionBO();
        conclusion.setTaskId(qcTaskId);
        conclusion.setSourceModule(SOURCE_MODULE);
        conclusion.setSourceBizNo(task.getTaskNo());
        conclusion.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":SUBMIT_CONCLUSION:0");
        conclusion.setConclusion(MesQcConclusionEnum.QUALIFIED.name());
        conclusion.setQtyQualified(qty);
        conclusion.setQtyRejected(BigDecimal.ZERO);
        qcTaskDomainService.submitConclusion(conclusion);
    }

    private MesQcTaskActionBO actionBO(Long qcTaskId, String taskNo, String command) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(qcTaskId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(taskNo);
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + taskNo + ":" + command + ":0");
        return bo;
    }

    /**
     * 从到货明细生成入库单，补上架库位后过账，落出真实库存余额。
     *
     * <p><b>为什么要补上架这一步</b>：{@code WmsArrivalOrderServiceImpl#buildInOrderBO} 不给入库行设
     * {@code locId}（源码注释写明"目标库位不在这里定：上架时才知道货放哪"），而
     * {@code WmsInOrderServiceImpl#confirmInOrder} 对每行强制 {@code IN_ORDER_LOC_REQUIRED}。
     * 因此到货链路必须经"改单指定库位"补上架信息才能过账，本方法走的就是 {@code updateInOrder}
     * 这条真实的改单入口（入库单建出来是 DRAFT，符合改单前置）。
     */
    private void postInbound(ArrivalLineDO lineDO) {
        Long inOrderId = arrivalOrderService.createInOrderFromArrival(
                lineDO.getArrivalId(), List.of(lineDO.getId()));
        assertNotNull(inOrderId);

        // 上架：把入库行的目标库位补上（改单是全量替换，批次与数量按原值带上）
        WmsCreateInOrderBO putaway = new WmsCreateInOrderBO();
        putaway.setSourceModule(SOURCE_MODULE);
        putaway.setSourceBizNo("T6-05-IQC");
        putaway.setBizType(WmsInBizTypeEnum.PO_IN.name());
        putaway.setInvOrgId(line.invOrgId());
        putaway.setPlantId(line.plantId());
        putaway.setWhId(stock.whId());
        // 上架行的质量状态取到货行的 iqcMode 口径（先入待检 = QC_PENDING、先检后入 = QUALIFIED），
        // 与 WmsArrivalOrderServiceImpl#qualityStatusOf 同一套规则；不能按库存余额反查，
        // 那会把供应商既有批次的其它状态串到本批上
        String putawayQualityStatus = WmsIqcModeEnum.POST_INBOUND_PENDING.name().equals(lineDO.getIqcMode())
                ? WmsQualityStatusEnum.QC_PENDING.name() : WmsQualityStatusEnum.QUALIFIED.name();
        putaway.setQualityStatus(putawayQualityStatus);
        WmsCreateInOrderBO.Line putawayLine = new WmsCreateInOrderBO.Line();
        putawayLine.setLineNo(10);
        putawayLine.setMatId(lineDO.getMatId());
        putawayLine.setMatCode(lineDO.getMatCode());
        putawayLine.setMatName(lineDO.getMatName());
        putawayLine.setLotNo(lineDO.getLotNo());
        putawayLine.setUomId(lineDO.getUomId());
        putawayLine.setUomCode(lineDO.getUomCode());
        putawayLine.setLocId(stock.locId());
        putawayLine.setLocCode("IT-LOC-RAW-01");
        putawayLine.setQualityStatus(putaway.getQualityStatus());
        putawayLine.setPlanQty(lineDO.getArrivalQty());
        putaway.setLines(List.of(putawayLine));
        inOrderService.updateInOrder(inOrderId, putaway);

        ConfirmOrderReqDTO confirm = new ConfirmOrderReqDTO();
        confirm.setSourceModule(SOURCE_MODULE);
        confirm.setSourceBizNo("T6-05-IQC");
        confirm.setIdempotencyKey(SOURCE_MODULE + ":T6-05-IQC:CONFIRM_IN:" + inOrderId);
        confirm.setOrderId(inOrderId);
        inOrderApi.confirmInOrder(confirm);
    }

    // ==================== 断言辅助 ====================

    private ArrivalLineDO reloadLine(Long arrivalId) {
        return arrivalOrderService.getArrivalLineList(arrivalId).get(0);
    }

    /**
     * 取到货行对应批次的编号。
     *
     * <p>{@code wms_arrival_line} 上只有 {@code lot_no} 没有 {@code batch_id}（确认到货时注册批次后
     * 只回写批次号，见 {@code WmsArrivalOrderServiceImpl.confirmArrival}），因此按批次号反查。
     */
    private Long batchIdOf(ArrivalLineDO lineDO) {
        Map<String, List<BatchRespDTO>> batchMap =
                batchApi.getBatchMapByLotNos(List.of(lineDO.getLotNo()));
        List<BatchRespDTO> batches = batchMap.get(lineDO.getLotNo());
        assertNotNull(batches, "到货确认时应已注册批次，批次号 " + lineDO.getLotNo());
        assertTrue(!batches.isEmpty());
        return batches.get(0).getId();
    }

    /**
     * 本批到货入库后的余额质量状态。
     *
     * <p>必须按<b>本批批次</b>取：{@code WmsFixture} 建仓时已经给同一物料过了 500 的合格库存，
     * 只按物料查会先撞上那批的 QUALIFIED，场景二的 QC_PENDING 永远看不到。
     */
    private String qualityStatusOf(Long batchId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setBatchId(batchId);
        query.setQualifiedOnly(false);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .filter(balance -> nvl(balance.getQtyOnhand()).signum() > 0)
                .findFirst()
                .map(InventoryBalanceRespDTO::getQualityStatus)
                .orElse(null);
    }

    /** 本批到货入库的现存量合计。 */
    private BigDecimal onhandOfBatch(Long batchId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setBatchId(batchId);
        query.setQualifiedOnly(false);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .map(InventoryBalanceRespDTO::getQtyOnhand)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
