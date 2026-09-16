package com.iboot.pf.server.integration.mes;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarBatchSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.service.op.OpService;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.ErrorCodeConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesCancelReportBO;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-04 端到端用例 5：报工推进三级数量。
 *
 * <p>依据 {@code dev-docs/业务流程/08-跨模块协作与端到端时序.md} 的"端到端主用例清单"第 5 条
 * 与 {@code 04-MES生产执行流程.md} 的"流程六：报工推进链"与"数量推进口径"表。
 *
 * <p>本类要钉死的核心事实是<b>"5 道工序各报 100 件，工单产出仍是 100 件而不是 500 件"</b>——
 * 工序任务累加、作业工单与生产工单取末道。为此本类另建一条 5 道工序的工艺路线（试点路线只有 3 道），
 * 5 道工序落在同一个工作中心上，因此下达后是 1 张作业工单 + 5 条工序任务。
 *
 * <p>与模块内 {@code MesProdReportDomainServiceImplTest} 的本质区别：这里走真实 Spring 上下文，
 * 数量推进落在真实的 H2 表上、在制流水由真实 Recorder 写，断言的是"三级数量与流水这两类账
 * 同时对得上"，不是"服务调了哪个方法"。
 *
 * <p>工序一律经真实开工路径（领料确认 + 真实 start）推进到 {@code RUNNING}：报工只认加工中的工序，
 * 绕过开工直接改状态就会把本类要验的"报工是唯一数量推进路径"这件事架空。
 */
class ProdReportIntegrationTest extends BaseIntegrationTest {

    /** MES 动作的调用方来源模块：控制器固定传 {@code UI}，测试传 {@code INTEGRATION}。 */
    private static final String SOURCE_MODULE = "INTEGRATION";

    /** 5 道工序的工艺路线：seq 由 Service 按 10 递增分配，故为 10/20/30/40/50。 */
    private static final int OP_COUNT = 5;

    /** 每道工序的计划数量。5 道合计 500，但工单产出必须是 100。 */
    private static final BigDecimal PLANNED_QTY = new BigDecimal("100");

    /** 防幂等键撞车的类内自增序号（同一个 H2 库内不会回滚流水号）。 */
    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    /** 本类新建主数据的生效日期：写成过去日期才能被 {@code getEffectiveRoute} 命中。 */
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2020, 1, 1);

    @Resource
    private ProdOrderService prodOrderService;

    @Resource
    private MesProdOrderDomainService prodOrderDomainService;

    @Resource
    private MesProdReportDomainService reportDomainService;

    @Resource
    private MesOpTaskDomainService opTaskDomainService;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private ProdReportMapper prodReportMapper;

    @Resource
    private WipTxnMapper wipTxnMapper;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private RouteService routeService;

    @Resource
    private OpService opService;

    @Resource
    private OpTaskApi opTaskApi;

    @Resource
    private ResCalendarService resCalendarService;

    private PilotLine line;

    @BeforeEach
    void buildPilotLine() {
        line = mdmFixture.createPilotLine();
    }

    @Test
    void testReport_FiveOpsOf100NeverSumTo500() {
        // 准备：一张 5 道工序的工单，逐道开工并全额报工
        Long orderId = releaseFiveOpOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        assertEquals(OP_COUNT, tasks.size(), "本用例的路线应有 5 道工序");

        BigDecimal expectedCompleted = BigDecimal.ZERO;
        for (OpTaskDO task : tasks) {
            startTask(task);
            report(task, PLANNED_QTY, BigDecimal.ZERO);
            expectedCompleted = expectedCompleted.add(PLANNED_QTY);

            // 断言 1：工序任务 completed_qty 逐次累加——每道工序各自累自己的账
            OpTaskDO reloaded = reload(task);
            assertEquals(0, PLANNED_QTY.compareTo(reloaded.getCompletedQty()),
                    "工序 " + task.getSeq() + " 累计合格数应为本次报工量");
            // 断言 2：生产工单 completed_qty 始终等于末道"已完工"工序的值。
            // 此刻只有本道工序有数量，其余工序还是 0，因此取值仍是 0 而不是已报的量
            assertEquals(0, expectedCompleted.subtract(PLANNED_QTY).compareTo(
                            nvl(prodOrderMapper.selectById(orderId).getCompletedQty())),
                    "整单产出取末道已执行工序，未完工的工序不参与上卷");
            // 本道完工后才能开下一道（门禁 prevDone），逐道推进才是真实产线节奏
            finish(task);
        }

        // 断言 3（验收点名）：5 道工序各 100 件，生产工单 completed_qty 必须是 100，绝不是 500
        ProdOrderDO order = prodOrderMapper.selectById(orderId);
        assertEquals(0, PLANNED_QTY.compareTo(order.getCompletedQty()),
                "流水线上 100 件依次过 5 道工序，产出仍是 100；各工序相加得到的是加工次数不是产出");
        assertTrue(order.getCompletedQty().compareTo(new BigDecimal("500")) != 0,
                "5 × 100 = 500 是这个项目里最出名的错法");

        // 断言 4：作业工单 completed_qty 等于本段末道工序的值（本用例只有 1 张作业工单）
        WorkOrderDO workOrder = workOrdersOf(orderId).get(0);
        assertEquals(0, PLANNED_QTY.compareTo(workOrder.getCompletedQty()),
                "作业工单取本段末道工序的产出");
        assertEquals(MesWorkOrderStatusEnum.DONE.name(), workOrder.getStatus(), "本段全部工序完工");

        // 断言 5：每道工序任务各自累计 100，没有一道被漏掉或被覆盖
        assertTrue(tasksBySeq(orderId).stream()
                        .allMatch(task -> PLANNED_QTY.compareTo(task.getCompletedQty()) == 0),
                "每道工序都要单独报工，各自累计");

        // 断言 6：生产工单报废数为各工序之和（报废是不同批件，要相加）
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getScrapQty()));
        assertEquals(MesProdOrderStatusEnum.DONE.name(), order.getStatus(), "末道工序完工且数量达标");
    }

    @Test
    void testReport_BatchReportingAccumulates() {
        // 准备：工单下达，首道工序开工
        Long orderId = releaseFiveOpOrder();
        OpTaskDO first = tasksBySeq(orderId).get(0);
        startTask(first);

        // 调用：同一道工序分批报 30 / 30 / 40
        report(first, new BigDecimal("30"), BigDecimal.ZERO, 1);
        assertEquals(0, new BigDecimal("30").compareTo(reload(first).getCompletedQty()));
        report(first, new BigDecimal("30"), BigDecimal.ZERO, 2);
        assertEquals(0, new BigDecimal("60").compareTo(reload(first).getCompletedQty()));
        report(first, new BigDecimal("40"), BigDecimal.ZERO, 3);

        // 断言 4：分批累加正确——覆盖写会让累计停在最后一批的 40
        OpTaskDO reloaded = reload(first);
        assertEquals(0, PLANNED_QTY.compareTo(reloaded.getCompletedQty()), "30 + 30 + 40 = 100");
        assertTrue(reloaded.getCompletedQty().compareTo(new BigDecimal("40")) != 0,
                "取最后一批的值是最容易犯的写法");

        // 三笔报工各自留痕，幂等键不同
        List<ProdReportDO> reports = reportsOf(first.getId());
        assertEquals(3, reports.size(), "分批报工每次都建一条报工记录");
        assertTrue(reports.stream()
                .allMatch(report -> MesProdReportStatusEnum.CONFIRMED.name().equals(report.getStatus())));

        // 断言 5：每次报工写一条 REPORT 流水，qty 记本次合格数
        List<WipTxnDO> txns = reportTxnsOf(first.getId());
        assertEquals(3, txns.size(), "三次报工写三条 REPORT 流水");
        assertEquals(List.of(0, 0, 0), txns.stream()
                .map(txn -> new BigDecimal("30").compareTo(txn.getQty()) == 0
                        || new BigDecimal("40").compareTo(txn.getQty()) == 0 ? 0 : 1).toList(),
                "每条流水的数量必须是本次合格数（30/30/40），不是累计值");
        assertTrue(txns.stream().allMatch(txn -> MesWipTxnTypeEnum.REPORT.name().equals(txn.getTxnType())));
    }

    @Test
    void testReport_FinishRequiresGoodPlusScrapEqualsPlanned() {
        // 准备：首道工序开工，先报 60 合格 + 30 报废（累计 90，不足 100）
        Long orderId = releaseFiveOpOrder();
        OpTaskDO first = tasksBySeq(orderId).get(0);
        startTask(first);
        report(first, new BigDecimal("60"), new BigDecimal("30"), 1);
        assertEquals(0, new BigDecimal("90").compareTo(reload(first).getCompletedQty().add(
                reload(first).getScrapQty())));

        // 调用 + 断言 6：数量没凑齐时完工被拦
        ServiceException mismatch = assertThrows(ServiceException.class, () -> finish(first));
        assertEquals(ErrorCodeConstants.OP_TASK_REPORT_QTY_INVALID.getCode(), mismatch.getCode(),
                "完工要求合格数 + 报废数 = 计划数，90 ≠ 100 必须拦下");

        // 补报 10 合格后凑齐，完工通过
        report(first, new BigDecimal("10"), BigDecimal.ZERO, 2);
        finish(first);
        OpTaskDO done = reload(first);
        assertEquals(MesOpTaskStatusEnum.DONE.name(), done.getStatus());
        assertEquals(0, PLANNED_QTY.compareTo(done.getCompletedQty().add(done.getScrapQty())));

        // 断言：报废数不混进合格数（70 合格 + 30 报废）——混在一起就分不清工序产出了多少良品
        assertEquals(0, new BigDecimal("70").compareTo(done.getCompletedQty()));
        assertEquals(0, new BigDecimal("30").compareTo(done.getScrapQty()));
        // 生产工单报废数取各工序之和
        assertEquals(0, new BigDecimal("30").compareTo(nvl(prodOrderMapper.selectById(orderId).getScrapQty())));
    }

    @Test
    void testCancelReport_ReversesThreeLevelQty() {
        // 准备：5 道工序全部报满 100 并完工，工单已 DONE（产出 100）
        Long orderId = releaseFiveOpOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        for (OpTaskDO task : tasks) {
            startTask(task);
            report(task, PLANNED_QTY, BigDecimal.ZERO);
            finish(task);
        }
        assertEquals(0, PLANNED_QTY.compareTo(prodOrderMapper.selectById(orderId).getCompletedQty()));

        // 已完工工序的报工不允许取消：完工按"合格 + 报废 = 计划数"校验过并把工序封版，
        // 抽掉一笔报工会让工序停在"数量不够却已完工"，而 DONE 是终态没有回退边
        ProdReportDO lastReport = reportsOf(tasks.get(4).getId()).get(0);
        ServiceException blocked = assertThrows(ServiceException.class, () -> cancel(lastReport.getId()));
        assertEquals(ErrorCodeConstants.PROD_REPORT_CANCEL_OP_TASK_DONE.getCode(), blocked.getCode(),
                "已完工工序的报工必须拦下，否则工序会被扣成数量不足的 DONE");

        // 断言 7：取消报工反向扣减三级数量。首道工序尚未完工时可取消，用它验反向推进
        Long orderId2 = releaseFiveOpOrder();
        OpTaskDO firstOf2 = tasksBySeq(orderId2).get(0);
        startTask(firstOf2);
        report(firstOf2, new BigDecimal("100"), BigDecimal.ZERO);
        assertEquals(0, PLANNED_QTY.compareTo(nvl(prodOrderMapper.selectById(orderId2).getCompletedQty())),
                "整单产出取末道已执行工序——此刻只有首道有数量但仍不是末道，产出仍为 0");
        Long reportId = reportsOf(firstOf2.getId()).get(0).getId();

        cancel(reportId);

        // 工序任务被扣回 0，报工记录置 CANCELLED
        assertEquals(0, BigDecimal.ZERO.compareTo(reload(firstOf2).getCompletedQty()),
                "取消把工序任务的累计数扣回去");
        assertEquals(MesProdReportStatusEnum.CANCELLED.name(),
                prodReportMapper.selectById(reportId).getStatus());
        // 反向流水用负数量表达冲销，与 WMS 的红字冲销同形
        List<WipTxnDO> txns = reportTxnsOf(firstOf2.getId());
        assertEquals(2, txns.size(), "提交与取消各写一条 REPORT 流水");
        assertTrue(txns.stream().anyMatch(txn -> txn.getQty().signum() < 0),
                "取消要写一条负数量的反向流水");
        // 三级数量重算后回到零：两级工单靠重算自然回到正确值，不需要另写一套反向公式
        assertEquals(0, BigDecimal.ZERO.compareTo(nvl(prodOrderMapper.selectById(orderId2).getCompletedQty())));
        assertEquals(0, BigDecimal.ZERO.compareTo(nvl(workOrdersOf(orderId2).get(0).getCompletedQty())));
    }

    @Test
    void testReport_QtyExceededAndEmptyRejected() {
        // 准备：首道工序开工
        Long orderId = releaseFiveOpOrder();
        OpTaskDO first = tasksBySeq(orderId).get(0);
        startTask(first);

        // 断言：三个数量全为零的报工没有意义
        ServiceException empty = assertThrows(ServiceException.class,
                () -> report(first, BigDecimal.ZERO, BigDecimal.ZERO, 1));
        assertEquals(ErrorCodeConstants.PROD_REPORT_QTY_EMPTY.getCode(), empty.getCode());

        // 断言：累计超过计划数必须拦下——超报之后那道工序永远凑不出"合格 + 报废 = 计划数"，会被永久卡住
        ServiceException exceeded = assertThrows(ServiceException.class,
                () -> report(first, new BigDecimal("101"), BigDecimal.ZERO, 2));
        assertEquals(ErrorCodeConstants.PROD_REPORT_QTY_EXCEEDED.getCode(), exceeded.getCode());
        // 拦下之后工序数量原地不动
        assertEquals(0, BigDecimal.ZERO.compareTo(reload(first).getCompletedQty()));
        assertTrue(reportsOf(first.getId()).isEmpty(), "被拦下的报工不落记录");
    }

    @Test
    void testReport_IdempotentByCallerKey() {
        // 准备：首道工序开工
        Long orderId = releaseFiveOpOrder();
        OpTaskDO first = tasksBySeq(orderId).get(0);
        startTask(first);

        // 用同一个幂等键提交两次（模拟扫码枪连扫 / 弱网重发）
        String key = SOURCE_MODULE + ":" + first.getTaskNo() + ":REPORT:1";
        Long firstId = submit(first, new BigDecimal("30"), BigDecimal.ZERO, key);
        Long secondId = submit(first, new BigDecimal("30"), BigDecimal.ZERO, key);

        // 断言：返回同一条报工，表里只有一条记录，数量只推进一次
        assertEquals(firstId, secondId, "重复提交返回首次那条报工");
        assertEquals(1, reportsOf(first.getId()).size(), "幂等键重复提交不产生第二条记录");
        assertEquals(1, reportTxnsOf(first.getId()).size(), "流水也只有一条");
        assertEquals(0, new BigDecimal("30").compareTo(reload(first).getCompletedQty()),
                "数量只推进一次——推进两次会把产量算成 60");
    }

    /* ------------------------------------------------------------------ 建数据 ------------------------------------------------------------------ */

    /**
     * 建一条 5 道工序的工艺路线并建单下达，返回生产工单编号。
     *
     * <p>为什么不用试点路线：它只有 3 道工序，"5 道工序 100 件不算成 500 件"这条验收硬指标
     * 要 5 道才成立。5 道工序全挂在试点工作中心上，因此下达后派生出 1 张作业工单 + 5 条工序任务。
     *
     * <p>本类只新增路线与工序主数据，不改 fixture、不动试点产线（红线）。
     */
    private Long releaseFiveOpOrder() {
        Long routeId = createFiveOpRoute();
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setPlantId(line.plantId());
        reqVO.setWorkshopId(line.workshopId());
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(line.fgMatId());
        reqVO.setQty(PLANNED_QTY);
        reqVO.setUomId(line.uomId());
        reqVO.setBomId(line.bomId());
        reqVO.setRouteId(routeId);
        reqVO.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        reqVO.setPriority(0);
        Long orderId = prodOrderService.createProdOrder(reqVO);

        MesReleaseOrderBO release = new MesReleaseOrderBO();
        release.setProdOrderId(orderId);
        release.setSourceModule(SOURCE_MODULE);
        release.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        release.setIdempotencyKey(nextKey("RELEASE_PROD_ORDER"));
        prodOrderDomainService.releaseProdOrder(release);
        return orderId;
    }

    /**
     * 建一条 5 道工序的生效工艺路线。
     *
     * <p>工序复用试点产线的 3 道，另建 2 道补足；路线用料显式 {@code mustIssueBeforeStart = false}，
     * 让开工门禁不因未领料而拦下（领料三段是 T6-03 的覆盖面，见 {@link #startTask}）。
     */
    private Long createFiveOpRoute() {
        List<Long> opIds = new ArrayList<>(line.opIds());
        while (opIds.size() < OP_COUNT) {
            opIds.add(createOp("T6-04 报工工序-" + opIds.size()));
        }
        RouteSaveReqVO route = new RouteSaveReqVO();
        route.setMatId(line.fgMatId());
        // 版本号带上类内序号：同一个测试类里多次建路线，物料相同而版本相同会撞唯一键
        route.setVersion("V-T604-" + KEY_SEQ.incrementAndGet());
        route.setStatus(CommonStatusEnum.DISABLE.getStatus());
        route.setEffectiveDate(EFFECTIVE_DATE);
        Long routeId = routeService.createRoute(route);
        for (Long opId : opIds) {
            createRouteOp(routeId, opId);
        }
        // 路线工序建完后再启用：启用态禁止改工序
        route.setId(routeId);
        route.setStatus(CommonStatusEnum.ENABLE.getStatus());
        routeService.updateRoute(route);
        return routeId;
    }

    /** 建一道工序（{@code mdm_op.code} 必填且唯一，用类内序号保证不撞码）。 */
    private Long createOp(String name) {
        OpSaveReqVO op = new OpSaveReqVO();
        op.setCode("T604-OP-" + KEY_SEQ.incrementAndGet());
        op.setName(name);
        op.setDurationType("FIXED");
        op.setStdDuration(30);
        op.setSetupMinutes(0);
        op.setTeardownMinutes(0);
        op.setQueueMinutes(0);
        op.setMoveMinutes(0);
        op.setManQty(1);
        op.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return opService.createOp(op);
    }

    /** 建一道路线工序：全部挂在试点工作中心，无并行组（逐道串行才验得出"末道"口径）。 */
    private Long createRouteOp(Long routeId, Long opId) {
        RouteOpSaveReqVO routeOp = new RouteOpSaveReqVO();
        routeOp.setRouteId(routeId);
        routeOp.setOpId(opId);
        routeOp.setWcId(line.wcId());
        routeOp.setKeyFlag(true);
        routeOp.setFaiFlag(false);
        routeOp.setSkipPolicy("NOT_ALLOWED");
        return routeService.createRouteOp(routeOp);
    }

    /**
     * 经真实开工路径把工序推到 {@code RUNNING}（报工只认加工中的工序）。
     *
     * <p>门禁的 {@code kitOrIssued} 项按 {@code mdm_route_op_mat.must_issue_before_start} 判定，
     * 本类建的路线用料显式置为 {@code false}——报工用例关注的是数量推进，不该被领料三段链路绊住
     * （领料三段是 T6-03 的覆盖面）。因此这里只走"状态机放行"的开工。
     *
     * <p>其余门禁项在本类场景下不启用：前序在循环里逐道完工后才开下一道；
     * 资源项要求 {@code res_id} 非零，本类不排资源故跳过；技能项要求工序挂了技能要求，试点工序没有。
     */
    private void startTask(OpTaskDO task) {
        // 首道工序下达后停在 PENDING（还没排计划），要先经 APS 回写排上资源才转 READY 可开工；
        // 后序工序由上道工序完工时自动推进到 READY，此处重排一次是无副作用的幂等写
        scheduleTask(task);
        MesOpTaskActionBO bo = new MesOpTaskActionBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-START");
        bo.setIdempotencyKey(nextKey("START_OP_TASK"));
        bo.setOpTaskId(task.getId());
        opTaskDomainService.startOpTask(bo);
    }

    /**
     * 经 APS 回写门面给工序任务排资源：写 res_id 并把 {@code PENDING} 推进到 {@code READY}。
     *
     * <p>这是 {@code PENDING -> READY} 的两条合法入口之一（另一条是前序完工时的 {@code prevDone}），
     * 首道工序只能走这条。资源当天的排班必须先建出来，否则回写会被"资源当天无排班"挡下。
     */
    private void scheduleTask(OpTaskDO task) {
        ensureResourceCalendar();
        ApplyScheduleReqDTO req = new ApplyScheduleReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-04-SCHEDULE");
        req.setIdempotencyKey(nextKey("APPLY_SCHEDULE"));
        req.setVersionNo("T6-04-V" + KEY_SEQ.incrementAndGet());
        ApplyScheduleReqDTO.Item item = new ApplyScheduleReqDTO.Item();
        item.setOpTaskId(task.getId());
        item.setResId(line.resId());
        item.setPlannedStartTime(LocalDate.now().atTime(8, 0));
        item.setPlannedEndTime(LocalDate.now().atTime(12, 0));
        req.setItems(List.of(item));
        opTaskApi.applySchedule(req);
    }

    /** 资源当天有排班（试点 fixture 只给 2020-01-01 建了排班）。 */
    private void ensureResourceCalendar() {
        ResCalendarBatchSaveReqVO batch = new ResCalendarBatchSaveReqVO();
        batch.setResIds(List.of(line.resId()));
        batch.setDateFrom(LocalDate.now());
        batch.setDateTo(LocalDate.now());
        batch.setShiftId(line.shiftId());
        batch.setAvailableMinutes(720);
        resCalendarService.saveResCalendarBatch(batch);
    }

    /** 全额报工（合格数 = 计划数）。 */
    private void report(OpTaskDO task, BigDecimal qtyGood, BigDecimal qtyScrap) {
        report(task, qtyGood, qtyScrap, KEY_SEQ.incrementAndGet());
    }

    /** 报工，seq 显式给出以保持幂等键稳定。 */
    private void report(OpTaskDO task, BigDecimal qtyGood, BigDecimal qtyScrap, int seq) {
        submit(task, qtyGood, qtyScrap, SOURCE_MODULE + ":" + task.getTaskNo() + ":REPORT:" + seq);
    }

    /** 提交报工，返回报工记录编号。 */
    private Long submit(OpTaskDO task, BigDecimal qtyGood, BigDecimal qtyScrap, String idempotencyKey) {
        MesSubmitReportBO bo = new MesSubmitReportBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-REPORT");
        bo.setIdempotencyKey(idempotencyKey);
        bo.setOpTaskId(task.getId());
        bo.setQtyGood(qtyGood);
        bo.setQtyScrap(qtyScrap);
        bo.setQtyRework(BigDecimal.ZERO);
        bo.setWorkerId(MesConstants.NONE_ID);
        bo.setResId(MesConstants.NONE_ID);
        bo.setShiftId(MesConstants.NONE_ID);
        LocalDateTime now = LocalDateTime.now();
        bo.setStartTime(now.minusHours(1));
        bo.setEndTime(now);
        return reportDomainService.submitReport(bo);
    }

    /** 工序完工（不声明对账数量，只校验累计值本身）。 */
    private void finish(OpTaskDO task) {
        MesOpTaskFinishBO bo = new MesOpTaskFinishBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-FINISH");
        bo.setIdempotencyKey(nextKey("FINISH_OP_TASK"));
        bo.setOpTaskId(task.getId());
        opTaskDomainService.finishOpTask(bo);
    }

    /** 取消报工，幂等键留空由服务端按报工单号派生。 */
    private void cancel(Long prodReportId) {
        MesCancelReportBO bo = new MesCancelReportBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-CANCEL");
        bo.setProdReportId(prodReportId);
        bo.setReason("报错数量，冲销");
        reportDomainService.cancelReport(bo);
    }

    /* ------------------------------------------------------------------ 取数与断言辅助 ------------------------------------------------------------------ */

    private OpTaskDO reload(OpTaskDO task) {
        return opTaskMapper.selectById(task.getId());
    }

    private List<WorkOrderDO> workOrdersOf(Long prodOrderId) {
        return workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId));
    }

    /** 按 seq 排序取工序任务，让断言不依赖生成顺序。 */
    private List<OpTaskDO> tasksBySeq(Long prodOrderId) {
        List<Long> workOrderIds = workOrdersOf(prodOrderId).stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    private List<ProdReportDO> reportsOf(Long opTaskId) {
        return prodReportMapper.selectList(ProdReportDO::getOpTaskId, opTaskId);
    }

    /** 某道工序的报工流水（含取消产生的反向流水）。 */
    private List<WipTxnDO> reportTxnsOf(Long opTaskId) {
        return wipTxnMapper.selectList(WipTxnDO::getOpTaskId, opTaskId,
                WipTxnDO::getTxnType, MesWipTxnTypeEnum.REPORT.name());
    }

    private String nextKey(String action) {
        return SOURCE_MODULE + ":T6-04:OP:" + KEY_SEQ.incrementAndGet() + ":" + action;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
