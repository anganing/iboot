package com.iboot.pf.server.integration.quality;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesReworkStatusEnum;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.defect.MesDefectDomainService;
import com.iboot.pf.module.mes.service.defect.bo.MesCreateDefectBO;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainService;
import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.qcresult.MesQcResultDomainService;
import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import com.iboot.pf.module.mes.service.rework.MesReworkDomainService;
import com.iboot.pf.module.mes.service.rework.bo.MesReworkActionBO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.lock.InventoryLockApi;
import com.iboot.pf.module.wms.api.lock.dto.InventoryLockRespDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
import com.iboot.pf.server.integration.fixture.QcFixture;
import com.iboot.pf.server.integration.fixture.WmsFixture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-05 端到端用例 8：处置六联动的库存与工序后果。
 *
 * <p>依据 {@code 05-MES质量子域流程.md} 的"处置六联动"对照表——"每种处置类型都必须产生真实的业务事实，
 * 否则会出现'判了报废但库存还在'这种账实分离状态"。
 *
 * <p>本类是六联动唯一端到端验证点：处置单的每个 handler 都真实调用了 WMS 门面
 * （{@code OutOrderApi} / {@code InventoryApi} / {@code InventoryLockApi}）或 MES 内部域服务
 * （返工、调工艺），库存与工序状态由真实过账产生，不 mock 任何门面。
 *
 * <p><b>六种类型的覆盖方式</b>（{@code MesDispositionDomainServiceImpl#requiresApproval} 把处置分两拨）：
 * <table>
 *     <tr><th>类型</th><th>是否审批</th><th>本类覆盖</th></tr>
 *     <tr><td>{@code FREEZE}</td><td>否</td><td>submit -&gt; execute 全链</td></tr>
 *     <tr><td>{@code RETURN_SUPPLIER}</td><td>否</td><td>submit -&gt; execute 全链</td></tr>
 *     <tr><td>{@code ADJUST_PROCESS}</td><td>否</td><td>submit -&gt; execute 全链</td></tr>
 *     <tr><td>{@code REWORK}</td><td>否</td><td>submit -&gt; execute 全链 + 复检合格回补</td></tr>
 *     <tr><td>{@code SCRAP}</td><td>是</td><td>提交发起审批，再走审批通过回调（终态回写入口）</td></tr>
 *     <tr><td>{@code CONCESSION}</td><td>是</td><td>同上</td></tr>
 * </table>
 *
 * <p>需要审批的两类不经 {@code executeDisposition} 直接调（状态机只允许 {@code APPROVED -> EXECUTING}），
 * 而是走 {@code updateScrapApprovalStatus} / {@code updateConcessionApprovalStatus} 这条
 * 真实的审批终态回写入口——它是生产代码里审批通过后触发联动的唯一路径。
 */
class DispositionIntegrationTest extends BaseIntegrationTest {

    private static final String SOURCE_MODULE = MesSourceModuleEnum.INTEGRATION.name();

    /** 处置数量不能超过质检任务的 qty_rejected：拒收场景里送检 100、拒收 20。 */
    private static final BigDecimal REJECTED_QTY = new BigDecimal("20");

    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    @Resource
    private MesDispositionDomainService dispositionDomainService;

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;

    @Resource
    private MesQcResultDomainService qcResultDomainService;

    @Resource
    private MesDefectDomainService defectDomainService;

    @Resource
    private MesReworkDomainService reworkDomainService;

    @Resource
    private MesProdOrderDomainService prodOrderDomainService;

    @Resource
    private ProdOrderService prodOrderService;

    @Resource
    private DispositionMapper dispositionMapper;

    @Resource
    private QcTaskMapper qcTaskMapper;

    @Resource
    private QcResultMapper qcResultMapper;

    @Resource
    private ReworkMapper reworkMapper;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private InventoryApi inventoryApi;

    @Resource
    private InventoryLockApi inventoryLockApi;

    @Resource
    private OutOrderApi outOrderApi;

    @Resource
    private com.iboot.pf.module.mes.api.optask.OpTaskApi opTaskApi;

    @Resource
    private com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService resCalendarService;

    @Resource
    private BomService bomService;

    @Resource
    private MatService matService;

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
        qcFixture.createQcBasics(line);
    }

    // ==================== SCRAP 报废 ====================

    @Test
    void testScrap_ReducesOnhandAndWritesOutOrderNo() {
        // 准备：一张拒收的质检任务（库存取试点子件的 500 在库量，拒收 20）
        RejectContext ctx = rejectQcTask(REJECTED_QTY);
        BigDecimal onhandBefore = onhandOf(ctx.matId());

        // 调用：建报废处置单 -> 提交（发起 mes-scrap 审批）-> 审批通过回调
        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.SCRAP, REJECTED_QTY);
        // 置为"已提交且审批中"：集成测试库没有灌 bpm_process_definition_info，
        // 经 submit 发起流程会抛 PROCESS_DEFINITION_NOT_EXISTS（审批不在 T6-05 范围内，见类注释），
        // 因此直接在单据上落"待审批"这一真实前置状态，再走真正的审批终态回写入口
        DispositionDO submitted = pendingApproval(dispositionId);
        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(), submitted.getStatus());
        assertNotNull(submitted.getProcessInstanceId());
        assertEquals(0, onhandBefore.compareTo(onhandOf(ctx.matId())), "审批未通过前不许动库存");

        // 审批通过：走真实终态回写入口
        approveScrap(dispositionId, submitted.getProcessInstanceId());

        // 断言 1（验收点名）：现存量减少
        DispositionDO done = dispositionMapper.selectById(dispositionId);
        assertEquals(MesDispositionStatusEnum.DONE.name(), done.getStatus(),
                "审批通过后处置应自动执行到 DONE");
        assertEquals(0, onhandBefore.subtract(REJECTED_QTY).compareTo(onhandOf(ctx.matId())),
                "报废后现存量必须真实减少处置数量");

        // 断言 2（验收点名）：target_biz_no 是出库单号
        assertEquals(MesBizTypeEnum.OUT_ORDER.name(), done.getTargetBizType());
        assertNotNull(done.getTargetBizNo());
        OutOrderRespDTO outOrder = outOrderApi.getOutOrderMapByNos(List.of(done.getTargetBizNo()))
                .get(done.getTargetBizNo());
        assertNotNull(outOrder, "回写的出库单号必须是真实存在的出库单");
        assertEquals(WmsOutBizTypeEnum.SCRAP_OUT.name(), outOrder.getBizType(),
                "报废必须走 SCRAP_OUT 业务类型的出库单，而不是别的出库理由");

        // 断言 3：工序报废数不应为负（SCRAP 的工序后果是 scrap_qty 计入，本用例的库存后果是主证据）
        assertTrue(nvl(reload(ctx.opTaskId()).getScrapQty()).signum() >= 0);
    }

    /** 断言"不存在判了报废但库存还在"：SCRAP 到 DONE 后库存确实减少了处置数量。 */
    @Test
    void testScrap_NoStockLeftBehindAfterDone() {
        RejectContext ctx = rejectQcTask(REJECTED_QTY);
        BigDecimal onhandBefore = onhandOf(ctx.matId());

        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.SCRAP, REJECTED_QTY);
        approveScrap(dispositionId, pendingApproval(dispositionId).getProcessInstanceId());

        // 断言：DONE 状态下现存量恰好减少处置数量——账实分离在这里会被抓出来
        assertEquals(MesDispositionStatusEnum.DONE.name(), dispositionMapper.selectById(dispositionId).getStatus());
        assertEquals(0, onhandBefore.subtract(REJECTED_QTY).compareTo(onhandOf(ctx.matId())),
                "判了报废库存必须真的减掉，否则账实分离");
    }

    // ==================== CONCESSION 让步接收 ====================

    @Test
    void testConcession_QualityStatusToQualifiedAndTargetIsTxnNo() {
        // 准备：一张拒收的质检任务；其库存是试点子件的 500 合格在库，
        // 让步接收把它整批过账为合格（重入合格状态，用量 20）
        RejectContext ctx = rejectQcTask(REJECTED_QTY);

        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.CONCESSION,
                REJECTED_QTY);
        // 让步接收的审批说明写在处置单 remark 上（createDisposition 已带）；
        // 审批同样不在 T6-05 范围内，置为待审批后走真实回调（理由见 SCRAP 用例）
        DispositionDO submitted = pendingApproval(dispositionId);
        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(), submitted.getStatus());
        assertNotNull(submitted.getProcessInstanceId());
        approveConcession(dispositionId, submitted.getProcessInstanceId());

        // 断言 1：余额质量状态为 QUALIFIED
        DispositionDO done = dispositionMapper.selectById(dispositionId);
        assertEquals(MesDispositionStatusEnum.DONE.name(), done.getStatus());
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), qualityStatusOf(ctx.matId()),
                "让步接收后库存必须转合格，检验的让步才有账面依据");

        // 断言 2（验收点名）：target_biz_no 为流水号（INV_TXN），存的是流水主键的字符串形式
        assertEquals(MesBizTypeEnum.INV_TXN.name(), done.getTargetBizType());
        assertNotNull(done.getTargetBizNo());
        assertTrue(done.getTargetBizNo().matches("\\d+"),
                "让步接收的回写目标应是库存流水编号（QUALITY_CHANGE 过账产生），实际为："
                        + done.getTargetBizNo());
    }

    // ==================== FREEZE 冻结 ====================

    @Test
    void testFreeze_IncreasesFrozenAndDropsAvailable() {
        RejectContext ctx = rejectQcTask(REJECTED_QTY);
        BigDecimal frozenBefore = frozenOf(ctx.matId());
        BigDecimal availableBefore = availableOf(ctx.matId());

        // 调用：冻结不需要审批，提交即批准，人工再点执行
        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.FREEZE, REJECTED_QTY);
        submitDisposition(dispositionId);
        dispositionDomainService.executeDisposition(actionBO(dispositionId, "EXECUTE_DISPOSITION"));

        // 断言 1（验收点名）：qty_frozen 增加
        assertEquals(0, frozenBefore.add(REJECTED_QTY).compareTo(frozenOf(ctx.matId())),
                "冻结必须真实增加 qty_frozen，否则可用量看不出被占");
        // 断言 2（验收点名）：可用量下降
        assertEquals(0, availableBefore.subtract(REJECTED_QTY).compareTo(availableOf(ctx.matId())),
                "冻结只占可用量、不扣在库量：可用量应下降冻结量");
        // 在库量不变（冻结与报废的本质区别）
        assertEquals(0, BigDecimal.ZERO.compareTo(
                onhandOf(ctx.matId()).subtract(new BigDecimal("500"))),
                "冻结不动 qty_onhand，货还在库里");

        // 断言 3（验收点名）：target_biz_no 为冻结编号
        DispositionDO done = dispositionMapper.selectById(dispositionId);
        assertEquals(MesDispositionStatusEnum.DONE.name(), done.getStatus());
        assertEquals(MesBizTypeEnum.INV_LOCK.name(), done.getTargetBizType());
        InventoryLockRespDTO lock = lockOf(ctx.matId());
        assertNotNull(lock, "回写的冻结编号必须是真实存在的冻结记录");
        assertEquals(done.getTargetBizNo(), lock.getLockNo());
        assertEquals(0, REJECTED_QTY.compareTo(lock.getQty()));
        // 冻结记录的来源业务类型是 QC_HOLD，与报废/退供的出库单区分开
        assertEquals("QC_HOLD", lock.getBizType());
        assertEquals(done.getDispositionNo(), lock.getBizNo());
    }

    // ==================== RETURN_SUPPLIER 退供 ====================

    @Test
    void testReturnSupplier_ReducesOnhandWithPoReturnOut() {
        RejectContext ctx = rejectQcTask(REJECTED_QTY);
        BigDecimal onhandBefore = onhandOf(ctx.matId());

        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.RETURN_SUPPLIER,
                REJECTED_QTY);
        submitDisposition(dispositionId);
        dispositionDomainService.executeDisposition(actionBO(dispositionId, "EXECUTE_DISPOSITION"));

        // 断言 1（验收点名）：现存量减少
        assertEquals(0, onhandBefore.subtract(REJECTED_QTY).compareTo(onhandOf(ctx.matId())),
                "退供的货离开库房，现存量必须真实减少");

        // 断言 2（验收点名）：出库单 biz_type 为 PO_RETURN_OUT
        DispositionDO done = dispositionMapper.selectById(dispositionId);
        assertEquals(MesDispositionStatusEnum.DONE.name(), done.getStatus());
        assertEquals(MesBizTypeEnum.OUT_ORDER.name(), done.getTargetBizType());
        OutOrderRespDTO outOrder = outOrderApi.getOutOrderMapByNos(List.of(done.getTargetBizNo()))
                .get(done.getTargetBizNo());
        assertNotNull(outOrder);
        assertEquals(WmsOutBizTypeEnum.PO_RETURN_OUT.name(), outOrder.getBizType(),
                "退供必须走采购退货出库单，不能与报废共用一种业务类型");
    }

    // ==================== ADJUST_PROCESS 调整工艺 ====================

    @Test
    void testAdjustProcess_HoldsOpTask() {
        // 准备：拒收的任务挂在工序任务上（biz_type = OP_TASK），处置执行才能定位到工序
        RejectContext ctx = rejectQcTask(REJECTED_QTY);
        // rejectQcTask 已把工序推到 RUNNING（挂起动作的合法来源态）
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), reload(ctx.opTaskId()).getStatus());

        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.ADJUST_PROCESS,
                REJECTED_QTY);
        submitDisposition(dispositionId);
        dispositionDomainService.executeDisposition(actionBO(dispositionId, "EXECUTE_DISPOSITION"));

        // 断言（验收点名）：工序任务状态为 HOLD
        assertEquals(MesOpTaskStatusEnum.HOLD.name(), reload(ctx.opTaskId()).getStatus(),
                "调整工艺必须把相关工序挂起，阻止继续生产");
        DispositionDO done = dispositionMapper.selectById(dispositionId);
        assertEquals(MesDispositionStatusEnum.DONE.name(), done.getStatus());
        assertEquals(MesBizTypeEnum.OP_TASK.name(), done.getTargetBizType());
        assertEquals(reload(ctx.opTaskId()).getTaskNo(), done.getTargetBizNo());
    }

    // ==================== REWORK 返工 ====================

    @Test
    void testRework_GeneratesReworkTaskLockingRouteVersion_AndBackfillsCompletedQty() {
        RejectContext ctx = rejectQcTask(REJECTED_QTY);
        BigDecimal completedBefore = nvl(reload(ctx.opTaskId()).getCompletedQty());

        Long dispositionId = createDisposition(ctx.qcTaskId(), MesDispositionTypeEnum.REWORK, REJECTED_QTY);
        submitDisposition(dispositionId);
        dispositionDomainService.executeDisposition(actionBO(dispositionId, "EXECUTE_DISPOSITION"));

        // 断言 1（验收点名）：返工任务已生成
        DispositionDO done = dispositionMapper.selectById(dispositionId);
        assertEquals(MesDispositionStatusEnum.DONE.name(), done.getStatus());
        assertEquals(MesBizTypeEnum.REWORK.name(), done.getTargetBizType());
        ReworkDO rework = reworkMapper.selectList(ReworkDO::getSourceQcTaskId, ctx.qcTaskId()).stream()
                .findFirst().orElseThrow(() -> new AssertionError("处置执行必须生成返工任务"));
        assertEquals(done.getTargetBizNo(), rework.getReworkNo());
        assertEquals(ctx.opTaskId(), rework.getOpTaskId(), "返工任务必须记返工起始工序");
        assertEquals(0, REJECTED_QTY.compareTo(rework.getQty()));

        // 断言 2（验收点名）：返工工艺版本已锁——与工单下达同一套版本化语义
        assertNotNull(rework.getRouteId());
        assertNotNull(rework.getRouteVersion(),
                "返工任务必须锁工艺版本，否则返工过程中改路线会让判定标准漂移");

        // 调用：释放返工（展开返工工序任务）-> 复检合格回调
        releaseRework(rework.getId());
        ReworkDO released = reworkMapper.selectById(rework.getId());
        assertEquals(MesReworkStatusEnum.RELEASED.name(), released.getStatus(),
                "释放后返回任务进入已下达，返工工序任务应已展开");

        // 复检：MES 用 FQC 规则 + biz_type = REWORK 辨识返工复检，合格后回调 completeRework。
        // 复检录的是合格值（与拒收场景相反），结论合格才走 completeRework 分支
        QcTaskDO reinspection = reinspectionTaskOf(rework.getReworkNo());
        submittedQualifiedConclusion(reinspection, REJECTED_QTY);

        // 断言 3（验收点名）：复检合格后原工序 completed_qty 增加，且生产工单不重复计数
        assertEquals(0, completedBefore.add(REJECTED_QTY).compareTo(nvl(reload(ctx.opTaskId()).getCompletedQty())),
                "返工合格数要回补到原工序任务的 completed_qty");
        assertEquals(MesReworkStatusEnum.DONE.name(), reworkMapper.selectById(rework.getId()).getStatus());
        // 不重复计数：返工不新建工序任务来累加产出，原工序任务数不变
        assertEquals(ctx.taskCountBefore(), taskCountOf(ctx.prodOrderId()),
                "返工合格数回补原工序任务，不许新建工序任务累加，否则整单产出重复计数");
    }

    // ==================== 建数据：拒收的质检任务 ====================

    /**
     * 造一张"已拒收"的质检任务，挂在首道工序任务上。
     *
     * <p>为什么需要拒收前置：{@code createDisposition} 校验处置数量不得超过任务的
     * {@code qty_rejected}，而只有拒收结论才写这个字段。因此每个处置用例都要先走一遍
     * "下达 -&gt; 开工 -&gt; 报工 -&gt; 交拒收结论"的真实链路。
     */
    private RejectContext rejectQcTask(BigDecimal rejectedQty) {
        Long orderId = releaseOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO first = tasks.get(0);

        // 走真实的开工序链路（排资源 -> 开工），工序才处于 RUNNING——报工与挂起都要它
        schedule(first, LocalDate.now());
        startOpTask(first);

        // 报工一笔：让工序累计出合格数，后续处置的库存与工序断言有真实基数
        BigDecimal reported = new BigDecimal("100");
        submitReport(first.getId(), reported);

        // 手工建一条 FAI 任务并交拒收结论：FAI 规则按首道工序匹配，是可稳定构造的
        // "挂工序的拒收任务"（IPQC 依赖 qc_mode=PROCESS，正常下达链路下不成立，见 T6-05 自查遗留）
        QcTaskDO qcTask = createManualFaiTask(first, reported);
        reject(qcTask, rejectedQty, reported.subtract(rejectedQty));
        return new RejectContext(orderId, qcTask.getId(), first.getId(), line.anyComponentMatId(),
                tasks.size(), first);
    }

    /** 手工建一条 FAI 质检任务：直接调领域服务，规则解析由 QcFixture 的 FAI 规则命中。 */
    private QcTaskDO createManualFaiTask(OpTaskDO task, BigDecimal qty) {
        com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO bo =
                new com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO();
        bo.setTriggerPoint(MesQcTriggerPointEnum.FAI.name());
        bo.setObjectType(com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum.OP_TASK.name());
        bo.setMatId(line.anyComponentMatId());
        bo.setOpId(task.getOpId());
        bo.setWcId(line.wcId());
        bo.setBizType(MesBizTypeEnum.OP_TASK.name());
        bo.setBizNo(task.getTaskNo());
        bo.setBizLineId(task.getId());
        bo.setQty(qty);
        bo.setUomId(line.uomId());
        Long qcTaskId = qcTaskDomainService.createQcTask(bo)
                .orElseThrow(() -> new AssertionError(
                        "FAI 规则应命中首道工序（QcFixture 的 FAI 规则按 opIds[0] 匹配）"));
        return qcTaskMapper.selectById(qcTaskId);
    }

    /** 交拒收结论：必须先录缺陷（{@code QC_TASK_REJECT_WITHOUT_DEFECT}）。 */
    private void reject(QcTaskDO task, BigDecimal qtyRejected, BigDecimal qtyQualified) {
        // 缺陷码：结论为 REJECTED 时没录缺陷会被 QC_TASK_REJECT_WITHOUT_DEFECT 拦，
        // 取一个通用缺陷码（不限物料分类与工序），保证在任何上下文都落在可选范围内
        Long defectCodeId = defectCodeId();
        MesCreateDefectBO defect = new MesCreateDefectBO();
        defect.setTaskId(task.getId());
        defect.setDefectCodeId(defectCodeId);
        defect.setQty(qtyRejected);
        defect.setDescription("集成测试：尺寸超差");
        defectServiceCreate(defect);

        // 指派 -> 开始检验 -> 录值 -> 交拒收结论
        prepareAndSubmitConclusion(task, MesQcConclusionEnum.REJECTED, qtyQualified, qtyRejected);
    }

    @Resource
    private com.iboot.pf.module.mdm.service.defectcode.DefectCodeService defectCodeService;

    /** 取或建一个通用缺陷码（无物料分类与工序限定即通用码）。 */
    private Long defectCodeId() {
        List<com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO> existing =
                defectCodeService.getDefectCodeList();
        if (!existing.isEmpty()) {
            return existing.get(0).getId();
        }
        com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeSaveReqVO vo =
                new com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeSaveReqVO();
        vo.setCode("IT-DEF-" + KEY_SEQ.incrementAndGet());
        vo.setName("集成测试缺陷");
        vo.setLevel("MAJOR");
        vo.setParentId(0L);
        vo.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return defectCodeService.createDefectCode(vo);
    }

    private void defectServiceCreate(MesCreateDefectBO bo) {
        defectDomainService.createDefect(bo);
    }

    // ==================== 建数据：处置单动作 ====================

    private Long createDisposition(Long qcTaskId, MesDispositionTypeEnum type, BigDecimal qty) {
        DispositionDO disposition = DispositionDO.builder()
                .taskId(qcTaskId)
                .dispositionType(type.name())
                .qty(qty)
                .remark(type == MesDispositionTypeEnum.CONCESSION ? "客户特批让步接收" : "集成测试处置")
                .idempotencyKey(SOURCE_MODULE + ":T6-05-DISPOSITION:CREATE:" + KEY_SEQ.incrementAndGet())
                .build();
        return dispositionDomainService.createDisposition(disposition);
    }

    /**
     * 把处置单置为 SUBMITTED + RUNNING 审批中（审批联动的起始状态）。
     *
     * <p>为什么不经 {@code submitDisposition}：提交会调 {@code BpmProcessInstanceApi.createProcessInstance}，
     * 而集成测试库只灌了 BPM 的字典与编码规则，没有 {@code bpm_process_definition_info} 数据，
     * 必然抛 {@code PROCESS_DEFINITION_NOT_EXISTS}。审批流程本身不在 T6-05 的验收范围内
     * （任务书明确"不测审批"），这里只补上"审批中"这个业务前置，随后走真实的审批终态回写入口。
     */
    private DispositionDO pendingApproval(Long dispositionId) {
        String instanceId = "T6-05-PROC-" + dispositionId;
        dispositionMapper.updateById(new DispositionDO()
                .setId(dispositionId)
                .setStatus(MesDispositionStatusEnum.SUBMITTED.name())
                .setProcessInstanceId(instanceId)
                .setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        return dispositionMapper.selectById(dispositionId);
    }

    private void submitDisposition(Long dispositionId) {
        dispositionDomainService.submitDisposition(actionBO(dispositionId, "SUBMIT_DISPOSITION"));
    }

    private MesDispositionActionBO actionBO(Long dispositionId, String command) {
        MesDispositionActionBO bo = new MesDispositionActionBO();
        bo.setDispositionId(dispositionId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setIdempotencyKey(SOURCE_MODULE + ":T6-05-DISPOSITION:" + command + ":" + KEY_SEQ.incrementAndGet());
        return bo;
    }

    /** 审批通过回调：走真实的 SCRAP 审批终态回写入口。 */
    private void approveScrap(Long dispositionId, String processInstanceId) {
        dispositionDomainService.updateScrapApprovalStatus(dispositionId, processInstanceId,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意报废");
    }

    /** 审批通过回调：走真实的 CONCESSION 审批终态回写入口。 */
    private void approveConcession(Long dispositionId, String processInstanceId) {
        dispositionDomainService.updateConcessionApprovalStatus(dispositionId, processInstanceId,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意让步");
    }

    private void releaseRework(Long reworkId) {
        MesReworkActionBO bo = new MesReworkActionBO();
        bo.setReworkId(reworkId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setIdempotencyKey(SOURCE_MODULE + ":T6-05-REWORK:RELEASE:" + KEY_SEQ.incrementAndGet());
        reworkDomainService.releaseRework(bo);
    }

    private QcTaskDO reinspectionTaskOf(String reworkNo) {
        List<QcTaskDO> tasks = qcTaskMapper.selectList(
                QcTaskDO::getBizNo, reworkNo,
                QcTaskDO::getTriggerPoint, MesQcTriggerPointEnum.FQC.name());
        assertTrue(!tasks.isEmpty(), "返工完成后必须生成复检任务（FQC 规则 + biz_type=REWORK）");
        return tasks.get(0);
    }

    // ==================== 建数据：工单、报工 ====================

    private Long releaseOrder() {
        // 试点产线的 BOM 含虚拟件，需先补一份虚拟件 BOM 才能齐套；本类不齐套，只下达
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setPlantId(line.plantId());
        reqVO.setWorkshopId(line.workshopId());
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(line.fgMatId());
        reqVO.setQty(PilotLine.DEMAND_QTY);
        reqVO.setUomId(line.uomId());
        reqVO.setBomId(line.bomId());
        reqVO.setRouteId(line.routeId());
        reqVO.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        reqVO.setPriority(0);
        Long orderId = prodOrderService.createProdOrder(reqVO);

        MesReleaseOrderBO release = new MesReleaseOrderBO();
        release.setProdOrderId(orderId);
        release.setSourceModule(SOURCE_MODULE);
        release.setSourceBizNo(prodOrderService.getProdOrder(orderId).getOrderNo());
        prodOrderDomainService.releaseProdOrder(release);
        return orderId;
    }

    /** 排资源：写 resId 并把 PENDING 推进到 READY（开工的前置）。 */
    private void schedule(OpTaskDO task, LocalDate workDate) {
        ensureResourceCalendar(workDate);
        com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO req =
                new com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-05-DISPOSITION-SCHEDULE");
        req.setIdempotencyKey(SOURCE_MODULE + ":T6-05-DISPOSITION:SCHEDULE:" + KEY_SEQ.incrementAndGet());
        req.setVersionNo("T6-05-V" + KEY_SEQ.incrementAndGet());
        com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO.Item item =
                new com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO.Item();
        item.setOpTaskId(task.getId());
        item.setResId(line.resId());
        item.setPlannedStartTime(workDate.atTime(8, 0));
        item.setPlannedEndTime(workDate.atTime(12, 0));
        req.setItems(List.of(item));
        opTaskApi.applySchedule(req);
    }

    /** 资源在指定日期有排班（主数据排班幂等补齐）。 */
    private void ensureResourceCalendar(LocalDate workDate) {
        if (resCalendarService.getResCalendarList(List.of(line.resId()), workDate, workDate).isEmpty()) {
            com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarBatchSaveReqVO batch =
                    new com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarBatchSaveReqVO();
            batch.setResIds(List.of(line.resId()));
            batch.setDateFrom(workDate);
            batch.setDateTo(workDate);
            batch.setShiftId(line.shiftId());
            batch.setAvailableMinutes(720);
            resCalendarService.saveResCalendarBatch(batch);
        }
    }

    /** 开工（排资源之后调用）。 */
    private void startOpTask(OpTaskDO task) {
        com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO req =
                new com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-05-DISPOSITION-START");
        req.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":START_OP_TASK:0");
        req.setOpTaskId(task.getId());
        req.setOperatorId(MesConstants.NONE_ID);
        opTaskApi.startOpTask(req);
    }

    private void submitReport(Long opTaskId, BigDecimal qtyGood) {
        OpTaskDO task = reload(opTaskId);
        com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO bo =
                new com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-05-DISPOSITION-REPORT");
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":REPORT:"
                + KEY_SEQ.incrementAndGet());
        bo.setOpTaskId(opTaskId);
        bo.setQtyGood(qtyGood);
        bo.setQtyScrap(BigDecimal.ZERO);
        bo.setQtyRework(BigDecimal.ZERO);
        bo.setWorkerId(0L);
        bo.setResId(0L);
        bo.setShiftId(0L);
        LocalDateTime now = LocalDateTime.now();
        bo.setStartTime(now.minusHours(1));
        bo.setEndTime(now);
        prodReportDomainService.submitReport(bo);
    }

    @Resource
    private com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService prodReportDomainService;

    // ==================== 质检任务动作 ====================

    /** 指派 -&gt; 开始检验 -&gt; 逐格录值 -&gt; 提交结论。 */
    private void prepareAndSubmitConclusion(QcTaskDO task, MesQcConclusionEnum conclusion,
                                            BigDecimal qtyQualified, BigDecimal qtyRejected) {
        QcTaskDO current = qcTaskMapper.selectById(task.getId());
        if (com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum.CREATED.name().equals(current.getStatus())) {
            MesQcTaskActionBO assign = qcActionBO(task, "ASSIGN_QC_TASK");
            assign.setInspectorId(1L);
            qcTaskDomainService.assignQcTask(assign);
        }
        if (com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum.ASSIGNED.name()
                .equals(qcTaskMapper.selectById(task.getId()).getStatus())) {
            qcTaskDomainService.startInspect(qcActionBO(task, "START_INSPECT"));
        }

        List<QcResultDO> results = qcResultMapper.selectListByTaskId(task.getId());
        MesSaveQcResultBO save = new MesSaveQcResultBO();
        save.setTaskId(task.getId());
        save.setInspectorId(1L);
        save.setResults(results.stream().map(result -> {
            MesSaveQcResultBO.Item item = new MesSaveQcResultBO.Item();
            item.setItemId(result.getItemId());
            item.setSampleNo(result.getSampleNo());
            // 拒收场景：数值项给超差的值（上下限 9.90 ~ 10.10），文本项给不符的值，
            // 判定为 FAIL 才与拒收结论自洽（结论本身是人定的，但数据要说得通）
            item.setActualValue("DIM-01".equals(result.getItemCode()) ? "12.00" : "有划伤");
            return item;
        }).toList());
        qcResultDomainService.batchSaveResult(save);

        MesQcTaskConclusionBO bo = new MesQcTaskConclusionBO();
        bo.setTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":SUBMIT_CONCLUSION:0");
        bo.setConclusion(conclusion.name());
        bo.setQtyQualified(qtyQualified);
        bo.setQtyRejected(qtyRejected);
        qcTaskDomainService.submitConclusion(bo);
    }

    /** 交合格结论（复检用）：录合格值，走完整四步。 */
    private void submittedQualifiedConclusion(QcTaskDO task, BigDecimal qtyQualified) {
        QcTaskDO current = qcTaskMapper.selectById(task.getId());
        if (com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum.CREATED.name().equals(current.getStatus())) {
            MesQcTaskActionBO assign = qcActionBO(task, "ASSIGN_QC_TASK");
            assign.setInspectorId(1L);
            qcTaskDomainService.assignQcTask(assign);
        }
        if (com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum.ASSIGNED.name()
                .equals(qcTaskMapper.selectById(task.getId()).getStatus())) {
            qcTaskDomainService.startInspect(qcActionBO(task, "START_INSPECT"));
        }

        List<QcResultDO> results = qcResultMapper.selectListByTaskId(task.getId());
        MesSaveQcResultBO save = new MesSaveQcResultBO();
        save.setTaskId(task.getId());
        save.setInspectorId(1L);
        save.setResults(results.stream().map(result -> {
            MesSaveQcResultBO.Item item = new MesSaveQcResultBO.Item();
            item.setItemId(result.getItemId());
            item.setSampleNo(result.getSampleNo());
            // 合格值：数值项落在上下限 9.90 ~ 10.10 内，文本项等于标准值
            item.setActualValue("DIM-01".equals(result.getItemCode()) ? "10.00" : "无划伤");
            return item;
        }).toList());
        qcResultDomainService.batchSaveResult(save);

        MesQcTaskConclusionBO bo = new MesQcTaskConclusionBO();
        bo.setTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":SUBMIT_CONCLUSION:0");
        bo.setConclusion(MesQcConclusionEnum.QUALIFIED.name());
        bo.setQtyQualified(qtyQualified);
        bo.setQtyRejected(BigDecimal.ZERO);
        qcTaskDomainService.submitConclusion(bo);
    }

    private MesQcTaskActionBO qcActionBO(QcTaskDO task, String command) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":" + command + ":0");
        return bo;
    }

    // ==================== 取数与断言辅助 ====================

    /** 拒收上下文：处置用例都从一张已拒收的质检任务出发。 */
    private record RejectContext(Long prodOrderId, Long qcTaskId, Long opTaskId, Long matId,
                                 int taskCountBefore, OpTaskDO firstTask) {
    }

    private OpTaskDO reload(Long opTaskId) {
        return opTaskMapper.selectById(opTaskId);
    }

    private List<OpTaskDO> tasksBySeq(Long prodOrderId) {
        return opTaskMapper.selectListByWorkOrderIds(workOrderIdsOf(prodOrderId)).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    private int taskCountOf(Long prodOrderId) {
        return opTaskMapper.selectListByWorkOrderIds(workOrderIdsOf(prodOrderId)).size();
    }

    private List<Long> workOrderIdsOf(Long prodOrderId) {
        return workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
    }

    private BigDecimal onhandOf(Long matId) {
        return sumOf(matId, InventoryBalanceRespDTO::getQtyOnhand);
    }

    private BigDecimal frozenOf(Long matId) {
        return sumOf(matId, InventoryBalanceRespDTO::getQtyFrozen);
    }

    private BigDecimal availableOf(Long matId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(matId);
        query.setWhId(stock.whId());
        return inventoryApi.getAvailableQty(query);
    }

    private BigDecimal sumOf(Long matId, java.util.function.Function<InventoryBalanceRespDTO, BigDecimal> getter) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(matId);
        query.setQualifiedOnly(false);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .map(getter).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String qualityStatusOf(Long matId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(matId);
        query.setQualifiedOnly(false);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .filter(balance -> nvl(balance.getQtyOnhand()).signum() > 0)
                .findFirst()
                .map(InventoryBalanceRespDTO::getQualityStatus)
                .orElse(null);
    }

    private InventoryLockRespDTO lockOf(Long matId) {
        com.iboot.pf.module.wms.api.lock.dto.InventoryLockQueryReqDTO query =
                new com.iboot.pf.module.wms.api.lock.dto.InventoryLockQueryReqDTO();
        query.setMatId(matId);
        return inventoryLockApi.getLockList(query).stream().findFirst().orElse(null);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
