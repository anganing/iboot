package com.iboot.pf.server.integration.quality;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarBatchSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mdm.service.op.OpService;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskFinishReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.ErrorCodeConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import com.iboot.pf.module.mes.service.qcresult.MesQcResultDomainService;
import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-05 端到端用例：过程质检三点（FAI 首件、IPQC 过程、FQC 完工）。
 *
 * <p>依据 {@code 05-MES质量子域流程.md} 的"五个检验点"与 {@code 04-MES生产执行流程.md}
 * 的"报工到完工入库"。
 *
 * <p><b>三个检验点的挂载位置与本类的覆盖方式</b>：
 * <table>
 *     <tr><th>检验点</th><th>挂载动作</th><th>门禁效果</th><th>本类覆盖</th></tr>
 *     <tr><td>FAI</td><td>工序开工后</td><td>首件未检通过则再次开工被拦</td>
 *         <td>{@code testFai_*} 两个用例</td></tr>
 *     <tr><td>IPQC</td><td>报工提交</td><td>工序进 {@code QC_WAITING}，合格后回 {@code RUNNING}</td>
 *         <td>{@code testIpqc_*}（见下方说明）</td></tr>
 *     <tr><td>FQC</td><td>末道工序完工</td><td>未合格则完工入库被拦、入库量不超合格数</td>
 *         <td>{@code testFqc_*} 两个用例</td></tr>
 * </table>
 *
 * <p><b>IPQC 用例的写法说明（重要）</b>：{@code MesOrderExpandHelper#createOpTasks} 把工序任务的
 * {@code qc_mode} 硬编码成 {@code NONE}，且 {@code mdm_route_op} 上没有 {@code qc_mode} 列——
 * 按当前实现，走正常下达链路<b>不可能</b>得到 {@code qc_mode = PROCESS} 的工序任务，
 * IPQC 永远不会触发（详见类内 IPQC 用例的注释：这是实现缺口，不是本用例的构造问题）。
 * 因此本类用"下达后把工序任务的 {@code qc_mode} 置为 {@code PROCESS}（等同配置到位的状态）
 * + 走真实报工链路"的方式，验证 IPQC 从触发、建任务、状态推进到结论回落的整条下游逻辑；
 * 缺口本身记在 T6-05 自查的遗留问题里。
 */
class InProcessQcIntegrationTest extends BaseIntegrationTest {

    private static final String SOURCE_MODULE = MesSourceModuleEnum.INTEGRATION.name();

    /** 并行组名，与 {@link #buildQcLine(PilotLine)} 建出的路线一致。 */
    private static final String PARALLEL_GROUP = "QC-PG1";

    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    @Resource
    private OpTaskApi opTaskApi;

    @Resource
    private ProdOrderApi prodOrderApi;

    @Resource
    private MesProdReportDomainService prodReportDomainService;

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;

    @Resource
    private MesQcResultDomainService qcResultDomainService;

    @Resource
    private MesStartGateChecker startGateChecker;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Resource
    private QcTaskMapper qcTaskMapper;

    @Resource
    private QcResultMapper qcResultMapper;

    @Resource
    private RouteService routeService;

    @Resource
    private BomService bomService;

    @Resource
    private MatService matService;

    @Resource
    private OpService opService;

    @Resource
    private ResCalendarService resCalendarService;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private WmsFixture wmsFixture;

    @Resource
    private QcFixture qcFixture;

    private PilotLine line;

    private WmsFixture.WmsStock stock;

    /**
     * 质检用产线：两道串行工序（seq10 要求首件）→ 便于把 FAI 挂在首道、FQC 挂在末道。
     *
     * <p>不用 {@link MdmFixture} 的试点路线：它的三道工序里 {@code fai_flag} 全为 false，
     * 首件门禁根本不启用；且末道是并行组，完工判定要多道一起凑，验 FQC 时噪音大。
     */
    private QcLine qc;

    @BeforeEach
    void buildQcLine() {
        line = mdmFixture.createPilotLine();
        stock = wmsFixture.createWarehouseWithStock(line);
        line = withStock(line, stock);
        qc = buildQcLine(line);
        // 质检规则按"新成品 + 首道工序"覆盖：FQC 规则看成品物料，FAI 规则看首道工序
        qcFixture.createQcBasics(line);
    }

    // ==================== FAI 首件检验 ====================

    @Test
    void testFai_TaskCreatedOnStart_AndBlocksSecondStartWhenNotPassed() {
        Long orderId = createAndReleaseOrder(qc.fgMatId());
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO first = tasks.get(0);

        // 准备：首道要求首件（fai_flag = true），第一次开工不校验首件（否则死锁）
        issueNotRequiredButScheduled(first);
        opTaskApi.startOpTask(startBO(first));

        // 断言 1：开工后按规则生成了 FAI 任务
        List<QcTaskDO> faiTasks = qcTaskMapper.selectList(
                QcTaskDO::getBizNo, first.getTaskNo(),
                QcTaskDO::getTriggerPoint, MesQcTriggerPointEnum.FAI.name());
        assertEquals(1, faiTasks.size(), "fai_flag 为真的工序开工后必须生成一条 FAI 任务");
        assertEquals(MesQcTaskStatusEnum.CREATED.name(), faiTasks.get(0).getStatus());
        assertEquals(0, BigDecimal.ONE.compareTo(faiTasks.get(0).getQty()), "首件送检数量恒为 1");

        // 准备：首件报工一笔，让门禁的"已有报工"前提成立
        submitReport(first, new BigDecimal("10"));

        // 断言 2（验收点名）：FAI 未通过时再次开工被拦，错误码是首件自己的那一项
        ServiceException blocked = assertThrows(ServiceException.class,
                () -> startGateChecker.check(reload(first), new MesStartGateContext()));
        assertEquals(ErrorCodeConstants.OP_TASK_FAI_NOT_PASSED.getCode(), blocked.getCode());
        assertTrue(blocked.getMessage().contains(first.getOpName()),
                "错误消息要点出是哪道工序的首件没过，实际为：" + blocked.getMessage());
    }

    @Test
    void testFai_PassedReleasesGate_AndRejectedHoldsOpTask() {
        Long orderId = createAndReleaseOrder(qc.fgMatId());
        OpTaskDO first = tasksBySeq(orderId).get(0);

        issueNotRequiredButScheduled(first);
        opTaskApi.startOpTask(startBO(first));
        submitReport(first, new BigDecimal("10"));

        QcTaskDO faiTask = faiTaskOf(first);
        // 断言 1：首件合格后门禁放行（不再抛 FAI_NOT_PASSED）
        submitConclusion(faiTask, MesQcConclusionEnum.QUALIFIED, BigDecimal.ONE, BigDecimal.ZERO, null);
        assertEquals(MesQcTaskStatusEnum.DONE.name(), reloadQcTask(faiTask.getId()).getStatus());
        // 门禁跑通即不抛异常——合格结论放行批量生产
        startGateChecker.check(reload(first), new MesStartGateContext());

        // 断言 2：拒收则立即把对应工序挂起（避免现场继续批量生产）
        // 另起一条产线场景：同工序第二次开工后报工，再交一条拒收结论
        Long secondOrderId = createAndReleaseOrder(qc.fgMatId());
        OpTaskDO secondFirst = tasksBySeq(secondOrderId).get(0);
        issueNotRequiredButScheduled(secondFirst);
        opTaskApi.startOpTask(startBO(secondFirst));
        submitReport(secondFirst, new BigDecimal("10"));

        QcTaskDO rejectTask = faiTaskOf(secondFirst);
        submitConclusion(rejectTask, MesQcConclusionEnum.REJECTED, BigDecimal.ZERO, BigDecimal.ONE,
                "首件尺寸超差，退回返修");
        assertEquals(MesOpTaskStatusEnum.HOLD.name(), reload(secondFirst).getStatus(),
                "FAI 拒收后该工序必须被挂起，否则现场会继续批量做下去");
    }

    // ==================== IPQC 过程检验 ====================

    /**
     * IPQC：{@code qc_mode = PROCESS} 时报工后工序进 {@code QC_WAITING}，结论合格后回 {@code RUNNING}。
     *
     * <p><b>为什么要手工置 qc_mode</b>：{@code MesOrderExpandHelper#createOpTasks} 把
     * {@code qcMode} 硬编码为 {@code MesQcModeEnum.NONE}，{@code mdm_route_op} 也没有 {@code qc_mode}
     * 列，因此正常下达链路下 {@code PROCESS} 永远不可能出现，IPQC 触发条件恒不成立
     * （生产实现缺口，已记入自查遗留）。这里把工序任务置成"配置到位"的状态，
     * 覆盖的是报工之后的整条真实链路（触发规则 -&gt; 建任务 -&gt; 工序转待检 -&gt; 结论回落）。
     */
    @Test
    void testIpqc_ReportToQcWaiting_ThenQualifiedReturnsToRunning() {
        Long orderId = createAndReleaseOrder(qc.fgMatId());
        OpTaskDO first = tasksBySeq(orderId).get(0);
        issueNotRequiredButScheduled(first);
        opTaskApi.startOpTask(startBO(first));

        // 置 qc_mode = PROCESS（见方法注释：正常下达链路下恒为 NONE，这是实现缺口）
        setQcMode(first, "PROCESS");

        // 调用：报工提交（IPQC 的挂载点）
        submitReport(first, new BigDecimal("30"));

        // 断言 1：报工后工序进 QC_WAITING，报工单进 QC_PENDING，任务已按规则生成
        OpTaskDO waiting = reload(first);
        assertEquals(MesOpTaskStatusEnum.QC_WAITING.name(), waiting.getStatus(),
                "qc_mode=PROCESS 的工序报工后必须进待检，否则不合格品会被直接做下去");
        List<QcTaskDO> ipqcTasks = ipqcTasksOf(first);
        assertEquals(1, ipqcTasks.size(), "报工提交必须触发一条 IPQC 任务");
        QcTaskDO ipqcTask = ipqcTasks.get(0);

        // 断言 2：检验合格后工序回到 RUNNING（结论回落的落点）
        submitConclusion(ipqcTask, MesQcConclusionEnum.QUALIFIED, new BigDecimal("30"), BigDecimal.ZERO, null);
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), reload(first).getStatus(),
                "IPQC 合格后工序要回到加工中，否则料会被卡在待检不动");
        // 数量未被结论改动：IPQC 只放行门禁，不写合格数
        assertEquals(0, new BigDecimal("30").compareTo(reload(first).getCompletedQty()));
    }

    // ==================== FQC 完工检验 ====================

    @Test
    void testFqc_InboundBlockedUntilQualified_AndQtyCappedByQualified() {
        Long orderId = createAndReleaseOrder(qc.fgMatId());
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO first = tasks.get(0);
        OpTaskDO last = tasks.get(tasks.size() - 1);

        // 准备：走完首道与末道的开工与报工，末道完工前触发 FQC
        completeOpExceptFinish(first);
        completeOpExceptFinish(last);
        // 首道完工，让末道进 READY
        finish(first);

        // 末道完工 -> 因 FQC 规则命中，工序进 QC_WAITING 而不是 DONE
        finish(last);
        assertEquals(MesOpTaskStatusEnum.QC_WAITING.name(), reload(last).getStatus(),
                "末道工序完工前命中 FQC 规则时必须转待检，不能直接 DONE");
        QcTaskDO fqcTask = fqcTaskOf(orderId);
        assertNotNull(fqcTask);
        assertEquals(MesQcTriggerPointEnum.FQC.name(), fqcTask.getTriggerPoint());

        // 断言 1（验收点名）：FQC 未合格时完工入库被拦
        assertThrows(ServiceException.class,
                () -> finishInbound(orderId, new BigDecimal("50")),
                "FQC 未通过时完工入库必须被拦");

        // 调用：FQC 合格，但只判 30 件合格（送检 100）
        submitConclusion(fqcTask, MesQcConclusionEnum.QUALIFIED, new BigDecimal("30"), new BigDecimal("70"), null);

        // 断言 2（验收点名）：入库数量不超过 qty_qualified
        ServiceException exceed = assertThrows(ServiceException.class,
                () -> finishInbound(orderId, new BigDecimal("50")));
        assertEquals(ErrorCodeConstants.FINISH_INBOUND_QC_NOT_PASSED.getCode(), exceed.getCode(),
                "入库量超过合格数必须被拦，检验只放行了多少就只能入多少");

        // 断言 3：合格数以内可以入库
        Long inOrderId = finishInbound(orderId, new BigDecimal("30"));
        assertNotNull(inOrderId, "合格数以内的完工入库应当放行");
    }

    @Test
    void testFqc_QualifiedReleasesLastOpToDone() {
        Long orderId = createAndReleaseOrder(qc.fgMatId());
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO first = tasks.get(0);
        OpTaskDO last = tasks.get(tasks.size() - 1);

        completeOpExceptFinish(first);
        completeOpExceptFinish(last);
        finish(first);
        finish(last);
        QcTaskDO fqcTask = fqcTaskOf(orderId);

        // 调用：FQC 合格
        submitConclusion(fqcTask, MesQcConclusionEnum.QUALIFIED,
                reload(last).getCompletedQty(), BigDecimal.ZERO, null);

        // 断言：合格结论把末道工序从 QC_WAITING 推到 DONE，并上卷工单
        assertEquals(MesOpTaskStatusEnum.DONE.name(), reload(last).getStatus(),
                "FQC 合格后末道工序要收尾，否则工单永远完不了");
        assertEquals(MesProdOrderStatusEnum.DONE.name(), prodOrderMapper.selectById(orderId).getStatus(),
                "末道工序收尾后生产工单应上卷到 DONE");
    }

    // ==================== 建数据：质检用产线 ====================

    /**
     * 建一条"可验过程质检"的产线与配套主数据。
     *
     * <p>路线结构：seq10（{@code fai_flag = true}）→ seq20（末道，FQC 的挂载点）。
     * 成品物料新建，保证它有独立的生效 BOM 与路线；BOM 指向试点产线的 PICK 子件。
     */
    private QcLine buildQcLine(PilotLine pilotLine) {
        Long fgMatId = createMaterial(pilotLine, "质检试点成品");
        Long bomId = createBom(fgMatId, pilotLine);
        Long routeId = createRoute(fgMatId);

        List<Long> opIds = new ArrayList<>(pilotLine.opIds());
        Long seq10OpId = opIds.get(0);
        Long seq20OpId = opIds.get(1);

        List<Long> routeOpIds = new ArrayList<>();
        routeOpIds.add(createRouteOp(routeId, seq10OpId, pilotLine.wcId(), null, true));
        routeOpIds.add(createRouteOp(routeId, seq20OpId, pilotLine.wcId(), null, false));

        RouteSaveReqVO enable = new RouteSaveReqVO();
        enable.setId(routeId);
        enable.setMatId(fgMatId);
        enable.setVersion("V2.0");
        enable.setStatus(CommonStatusEnum.ENABLE.getStatus());
        enable.setEffectiveDate(LocalDate.of(2020, 2, 1));
        routeService.updateRoute(enable);

        return new QcLine(fgMatId, bomId, routeId, opIds, routeOpIds);
    }

    /** 质检用产线句柄。 */
    private record QcLine(Long fgMatId, Long bomId, Long routeId, List<Long> opIds, List<Long> routeOpIds) {
    }

    private Long createMaterial(PilotLine pilotLine, String name) {
        MatSaveReqVO mat = new MatSaveReqVO();
        mat.setCatId(pilotLine.matCatId());
        mat.setName(name);
        mat.setMatType("FG");
        mat.setBaseUomId(pilotLine.uomId());
        mat.setBatchFlag(true);
        mat.setSnFlag(false);
        mat.setPlanStrategy("MTO");
        mat.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return matService.createMat(mat);
    }

    private Long createBom(Long fgMatId, PilotLine pilotLine) {
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(fgMatId);
        bom.setVersion("V2.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(LocalDate.of(2020, 2, 1));
        Long bomId = bomService.createBom(bom);
        bomService.createBomLine(BomLineDO.builder()
                .bomId(bomId).lineNo(10).matId(pilotLine.anyComponentMatId())
                .qty(new BigDecimal("2")).scrapRate(BigDecimal.ZERO)
                .issueMethod("PICK").uomId(pilotLine.uomId())
                .effectiveDate(LocalDate.of(2020, 2, 1)).build());
        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);
        return bomId;
    }

    private Long createRoute(Long fgMatId) {
        RouteSaveReqVO route = new RouteSaveReqVO();
        route.setMatId(fgMatId);
        route.setVersion("V2.0");
        route.setStatus(CommonStatusEnum.DISABLE.getStatus());
        route.setEffectiveDate(LocalDate.of(2020, 2, 1));
        return routeService.createRoute(route);
    }

    private Long createRouteOp(Long routeId, Long opId, Long wcId, String parallelGroup, boolean faiFlag) {
        RouteOpSaveReqVO routeOp = new RouteOpSaveReqVO();
        routeOp.setRouteId(routeId);
        routeOp.setOpId(opId);
        routeOp.setWcId(wcId);
        routeOp.setParallelGroup(parallelGroup);
        routeOp.setKeyFlag(true);
        routeOp.setFaiFlag(faiFlag);
        routeOp.setSkipPolicy("NOT_ALLOWED");
        return routeService.createRouteOp(routeOp);
    }

    // ==================== 建数据：工单、排程、报工、完工、入库 ====================

    private Long createAndReleaseOrder(Long fgMatId) {
        CreateProdOrderReqDTO req = new CreateProdOrderReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-05-QC");
        req.setIdempotencyKey(nextKey("CREATE_ORDER"));
        req.setPlantId(line.plantId());
        req.setWorkshopId(line.workshopId());
        req.setProjectId(MesConstants.NONE_ID);
        req.setProjectTaskId(MesConstants.NONE_ID);
        req.setMatId(fgMatId);
        req.setQty(PilotLine.DEMAND_QTY);
        req.setUomId(line.uomId());
        req.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        req.setPriority(0);
        Long orderId = prodOrderApi.createProdOrder(req);

        ProdOrderActionReqDTO release = new ProdOrderActionReqDTO();
        release.setSourceModule(SOURCE_MODULE);
        release.setSourceBizNo("T6-05-QC");
        release.setIdempotencyKey(nextKey("RELEASE_ORDER"));
        release.setProdOrderId(orderId);
        prodOrderApi.releaseProdOrder(release);
        return orderId;
    }

    /** 给工序排资源（写 resId 并把 PENDING 推进到 READY），并补齐资源当天的排班。 */
    private void schedule(OpTaskDO task, LocalDate workDate) {
        ensureResourceCalendar(workDate);
        ApplyScheduleReqDTO req = new ApplyScheduleReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-05-QC-SCHEDULE");
        req.setIdempotencyKey(nextKey("APPLY_SCHEDULE"));
        req.setVersionNo("T6-05-V" + KEY_SEQ.incrementAndGet());
        ApplyScheduleReqDTO.Item item = new ApplyScheduleReqDTO.Item();
        item.setOpTaskId(task.getId());
        item.setResId(line.resId());
        item.setPlannedStartTime(workDate.atTime(8, 0));
        item.setPlannedEndTime(workDate.atTime(12, 0));
        req.setItems(List.of(item));
        opTaskApi.applySchedule(req);
    }

    /** 排班补齐：主数据排班是幂等补齐，已存在就跳过。 */
    private void ensureResourceCalendar(LocalDate workDate) {
        if (resCalendarService.getResCalendarList(List.of(line.resId()), workDate, workDate).isEmpty()) {
            ResCalendarBatchSaveReqVO batch = new ResCalendarBatchSaveReqVO();
            batch.setResIds(List.of(line.resId()));
            batch.setDateFrom(workDate);
            batch.setDateTo(workDate);
            batch.setShiftId(line.shiftId());
            batch.setAvailableMinutes(720);
            resCalendarService.saveResCalendarBatch(batch);
        }
    }

    /**
     * 开工但不报工：排资源 -&gt; 开工。
     *
     * <p>不领料——本产线的路线工序没有挂必发料（{@code buildQcLine} 不建 {@code RouteOpMat}），
     * 门禁的 {@code kitOrIssued} 只要该工序没有必发料就放行，这属于"配置上本来就不该发"，
     * 不是绕过门禁。
     */
    private void issueNotRequiredButScheduled(OpTaskDO task) {
        schedule(task, LocalDate.now());
        opTaskApi.startOpTask(startBO(task));
    }

    /** 开工、报满数量并完工（用于首道工序，让末道进 READY）。 */
    private void completeOpExceptFinish(OpTaskDO task) {
        if (MesOpTaskStatusEnum.PENDING.name().equals(reload(task).getStatus())) {
            schedule(task, LocalDate.now());
        }
        if (MesOpTaskStatusEnum.PENDING.name().equals(reload(task).getStatus())
                || MesOpTaskStatusEnum.READY.name().equals(reload(task).getStatus())) {
            opTaskApi.startOpTask(startBO(task));
        }
        if (MesOpTaskStatusEnum.RUNNING.name().equals(reload(task).getStatus())) {
            submitReport(task, reload(task).getPlannedQty());
        }
    }

    /** 完工一道工序（数量按计划数全额，需已有报工凑齐数量）。 */
    private void finish(OpTaskDO task) {
        OpTaskFinishReqDTO finish = new OpTaskFinishReqDTO();
        finish.setSourceModule(SOURCE_MODULE);
        finish.setSourceBizNo("T6-05-QC-FINISH");
        finish.setIdempotencyKey(nextKey("FINISH_OP_TASK"));
        finish.setOpTaskId(task.getId());
        BigDecimal planned = reload(task).getPlannedQty();
        finish.setQtyGood(planned);
        finish.setQtyScrap(BigDecimal.ZERO);
        opTaskApi.finishOpTask(finish);
    }

    /** 报工一笔：数量按传入值。 */
    private void submitReport(OpTaskDO task, BigDecimal qtyGood) {
        MesSubmitReportBO bo = new MesSubmitReportBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-05-QC-REPORT");
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":REPORT:"
                + KEY_SEQ.incrementAndGet());
        bo.setOpTaskId(task.getId());
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

    /** 完工入库：返回入库单编号（合格数以内会成功，超限或未检通过会抛异常）。 */
    private Long finishInbound(Long orderId, BigDecimal qty) {
        com.iboot.pf.module.mes.service.prodorder.bo.MesFinishInboundBO bo =
                new com.iboot.pf.module.mes.service.prodorder.bo.MesFinishInboundBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-05-QC-INBOUND");
        bo.setIdempotencyKey(nextKey("FINISH_INBOUND"));
        bo.setProdOrderId(orderId);
        bo.setWhId(stock.whId());
        bo.setLocId(stock.locId());
        bo.setQty(qty);
        return finishInboundService.createFinishInbound(bo);
    }

    @Resource
    private com.iboot.pf.module.mes.service.prodorder.MesFinishInboundService finishInboundService;

    // ==================== 质检任务动作 ====================

    /**
     * 提交结论：走完整的 指派 -&gt; 开始检验 -&gt; 逐格录值 -&gt; 提交结论 四步。
     *
     * <p>录值不能跳：任务要推进到 {@code RESULT_POSTING} 才允许提交结论，
     * 而那条边只在全部必检项录完时由结果录入动作给出。
     */
    private void submitConclusion(QcTaskDO task, MesQcConclusionEnum conclusion,
                                  BigDecimal qtyQualified, BigDecimal qtyRejected, String remark) {
        MesQcTaskActionBO assign = actionBO(task, "ASSIGN_QC_TASK");
        assign.setInspectorId(1L);
        if (MesQcTaskStatusEnum.CREATED.name().equals(reloadQcTask(task.getId()).getStatus())) {
            qcTaskDomainService.assignQcTask(assign);
        }
        if (MesQcTaskStatusEnum.ASSIGNED.name().equals(reloadQcTask(task.getId()).getStatus())) {
            qcTaskDomainService.startInspect(actionBO(task, "START_INSPECT"));
        }

        List<QcResultDO> results = qcResultMapper.selectListByTaskId(task.getId());
        MesSaveQcResultBO save = new MesSaveQcResultBO();
        save.setTaskId(task.getId());
        save.setInspectorId(1L);
        save.setResults(results.stream().map(result -> {
            MesSaveQcResultBO.Item item = new MesSaveQcResultBO.Item();
            item.setItemId(result.getItemId());
            item.setSampleNo(result.getSampleNo());
            item.setActualValue("DIM-01".equals(result.getItemCode()) ? "10.00" : "无划伤");
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
        if (remark != null) {
            bo.setRemark(remark);
        }
        qcTaskDomainService.submitConclusion(bo);
    }

    private MesQcTaskActionBO actionBO(QcTaskDO task, String command) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":" + command + ":0");
        return bo;
    }

    // ==================== 取数与断言辅助 ====================

    private OpTaskDO reload(OpTaskDO task) {
        return opTaskMapper.selectById(task.getId());
    }

    private QcTaskDO reloadQcTask(Long qcTaskId) {
        return qcTaskMapper.selectById(qcTaskId);
    }

    /** 手工置工序任务的 qc_mode（IPQC 用例专用，见该用例注释）。 */
    private void setQcMode(OpTaskDO task, String qcMode) {
        OpTaskDO update = new OpTaskDO();
        update.setId(task.getId());
        update.setQcMode(qcMode);
        opTaskMapper.updateById(update);
    }

    private List<OpTaskDO> tasksBySeq(Long prodOrderId) {
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    private QcTaskDO faiTaskOf(OpTaskDO task) {
        List<QcTaskDO> tasks = qcTaskMapper.selectList(
                QcTaskDO::getBizNo, task.getTaskNo(),
                QcTaskDO::getTriggerPoint, MesQcTriggerPointEnum.FAI.name());
        assertEquals(1, tasks.size(), "该工序应恰有一条 FAI 任务");
        return tasks.get(0);
    }

    private List<QcTaskDO> ipqcTasksOf(OpTaskDO task) {
        return qcTaskMapper.selectList(QcTaskDO::getTriggerPoint, MesQcTriggerPointEnum.IPQC.name());
    }

    private QcTaskDO fqcTaskOf(Long orderId) {
        String orderNo = prodOrderMapper.selectById(orderId).getOrderNo();
        List<QcTaskDO> tasks = qcTaskMapper.selectFqcTasksByProdOrderNo(orderNo);
        assertTrue(!tasks.isEmpty(), "末道工序完工必须生成 FQC 任务");
        return tasks.get(0);
    }

    private OpTaskStatusReqDTO startBO(OpTaskDO task) {
        OpTaskStatusReqDTO bo = new OpTaskStatusReqDTO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-05-QC-START");
        bo.setIdempotencyKey(nextKey("START_OP_TASK"));
        bo.setOpTaskId(task.getId());
        return bo;
    }

    private String nextKey(String command) {
        return SOURCE_MODULE + ":T6-05-QC:" + command + ":" + KEY_SEQ.incrementAndGet();
    }

    private PilotLine withStock(PilotLine pilotLine, WmsFixture.WmsStock stock) {
        return new PilotLine(pilotLine.invOrgId(), pilotLine.plantId(), pilotLine.workshopId(),
                pilotLine.wcId(), pilotLine.uomId(), pilotLine.fgMatId(), pilotLine.componentMatIds(),
                pilotLine.matCatId(), pilotLine.shiftId(), pilotLine.calendarId(), pilotLine.resId(),
                pilotLine.resCapabilityId(), pilotLine.resCalendarId(), pilotLine.opIds(),
                pilotLine.routeId(), pilotLine.routeOpIds(), pilotLine.routeOpMatIds(), pilotLine.bomId(),
                stock.whId(), stock.zoneId(), stock.locId(), stock.inOrderIds(),
                pilotLine.qcPlanId(), pilotLine.qcItemIds(), pilotLine.qcRuleIds());
    }
}
