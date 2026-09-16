package com.iboot.pf.server.integration.mes;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.exception.ErrorCode;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarBatchSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.RouteOpMatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.MemberSkillSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.OpSkillReqSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.SkillSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mdm.service.op.OpService;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.mdm.service.routeopmat.RouteOpMatService;
import com.iboot.pf.module.mdm.service.skill.SkillService;
import com.iboot.pf.module.mes.api.materialreq.MaterialReqApi;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskFinishReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.ErrorCodeConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-03 端到端用例 4：开工门禁五项。
 *
 * <p>依据 {@code 08-跨模块协作与端到端时序.md} 的"端到端主用例清单"第 4 条与
 * {@code 04-MES生产执行流程.md} 的"流程五：工序动作与开工门禁"。
 *
 * <p>门禁是<b>可配置校验链</b>，五项各自独立判定。本类为每一项构造一个"该项确定不满足"的场景，
 * 断言抛的是<b>该项自己的错误码</b>——只断言"开工失败"是不够的：五项写成一条 if 串时也可能失败，
 * 但车间看不到该去补什么（料、资质还是排班）。错误码是这条链唯一的对外契约。
 *
 * <table>
 *     <tr><th>校验项</th><th>错误码</th><th>本类构造的失败场景</th></tr>
 *     <tr><td>prevDone</td><td>{@code OP_TASK_PREV_NOT_DONE}</td><td>前序未完工就开后序</td></tr>
 *     <tr><td>kitOrIssued</td><td>{@code OP_TASK_NOT_KITTED}</td><td>必发料未领</td></tr>
 *     <tr><td>firstArticle</td><td>{@code OP_TASK_FAI_NOT_PASSED}</td><td>首件未检（已有报工）</td></tr>
 *     <tr><td>resourceAvailable</td><td>{@code OP_TASK_RES_UNAVAILABLE}</td><td>资源当天无排班</td></tr>
 *     <tr><td>skillAndTool</td><td>{@code OP_TASK_SKILL_NOT_MATCH}</td><td>操作者技能等级低于要求</td></tr>
 * </table>
 *
 * <p>另外断言"全部满足时开工成功、三级状态一起推进到 {@code RUNNING}"，
 * 以及"并行组只完工一道时后序开工仍被拦"——并行组的语义是"这几道工序可以同时做"，
 * 后序要的是它们合起来的产出，只完工一道就放行会拿到半成品。
 */
class StartGateIntegrationTest extends BaseIntegrationTest {

    private static final String SOURCE_MODULE = MesSourceModuleEnum.INTEGRATION.name();

    /** 并行组名，与 {@link #buildGateLine(PilotLine)} 建的路线一致。 */
    private static final String PARALLEL_GROUP = "GATE-PG1";

    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    @Resource
    private OpTaskApi opTaskApi;

    @Resource
    private ProdOrderApi prodOrderApi;

    @Resource
    private MaterialReqApi materialReqApi;

    @Resource
    private RouteService routeService;

    @Resource
    private RouteOpMatService routeOpMatService;

    @Resource
    private BomService bomService;

    @Resource
    private MatService matService;

    @Resource
    private SkillService skillService;

    @Resource
    private ResCalendarService resCalendarService;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private WmsFixture wmsFixture;

    @Resource
    private OpService opService;

    @Resource
    private MesProdReportDomainService prodReportDomainService;

    @Resource
    private MesStartGateChecker startGateChecker;

    @Resource
    private MesMaterialReqDomainService materialReqDomainService;

    private PilotLine line;

    /**
     * 门禁用产线：在试点产线之上另建一套"可开门禁"的主数据。
     *
     * <p>为什么不用 {@link MdmFixture} 直接建的路线：fixture 的路线是 seq10 → (seq20 ∥ seq30)，
     * 并行组是<b>末步骤</b>，没有后序可以验"并行组只完工一道时后序被拦"；且首道工序
     * {@code fai_flag = false}，首件门禁根本不启用。这里另建一条 V2 路线：
     * seq10（要求首件）→ seq20 ∥ seq30（并行组）→ seq40（后序），一次把两个场景都覆盖掉。
     */
    private GateLine gate;

    @BeforeEach
    void buildGateLine() {
        line = mdmFixture.createPilotLine();
        WmsFixture.WmsStock stock = wmsFixture.createWarehouseWithStock(line);
        line = withStock(line, stock);
        gate = buildGateLine(line);
    }

    // ==================== 门禁一：前序未完工 ====================

    @Test
    void testGate_PrevNotDone_Rejected() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);
        OpTaskDO seq20 = tasks.get(1);

        // 前序 seq10 还停在 READY，直接开 seq20
        assertErrorCode(() -> opTaskApi.startOpTask(startBO(seq20)),
                ErrorCodeConstants.OP_TASK_PREV_NOT_DONE);

        // 消息含未完工的工序号：车间按工序号就能在现场找到那道工序
        String message = serviceExceptionMessage(() -> opTaskApi.startOpTask(startBO(seq20)));
        assertTrue(message.contains("第" + seq10.getSeq() + "道"),
                "错误消息必须含未完工的工序号，实际为：" + message);

        // 状态未被改动：门禁跑在状态推进之前，不通过时一行都不许改
        // （下达后未排程，工序停在 PENDING；门禁不通过就必须原地不动）
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), reload(seq20).getStatus());
    }

    // ==================== 门禁二：未发料 ====================

    @Test
    void testGate_NotKitted_Rejected() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        // 一张领料申请都不建，直接开工——必发料（PICK 件，must_issue_before_start = true）没到工位
        assertErrorCode(() -> opTaskApi.startOpTask(startBO(seq10)),
                ErrorCodeConstants.OP_TASK_NOT_KITTED);

        // 消息含缺料物料编码：车间照着编码就能去仓库催料
        String message = serviceExceptionMessage(() -> opTaskApi.startOpTask(startBO(seq10)));
        assertTrue(message.contains(gateMatCode()),
                "错误消息必须含缺料物料编码，实际为：" + message);

        assertEquals(MesOpTaskStatusEnum.PENDING.name(), reload(seq10).getStatus());
    }

    /** 反向用例：料领齐之后同一道工序的开工门禁不再因缺料被拦（缺料是唯一原因）。 */
    @Test
    void testGate_KitIssued_NotKittedItemPasses() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        issueAndConfirm(seq10, new BigDecimal("100"));

        // 料已到工位，开工不再抛 NOT_KITTED。资源当天无排班，所以这里仍会因资源被拦——
        // 断言的是"不是缺料那条"，把缺料这一项的放行单独锁住
        String message = serviceExceptionMessage(() -> opTaskApi.startOpTask(startBO(seq10)));
        assertTrue(!message.contains("未发齐"), "料已领齐后不该再报缺料，实际为：" + message);
    }

    // ==================== 门禁三：首件未通过 ====================

    @Test
    void testGate_FaiNotPassed_Rejected() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        // 第一次开工不校验首件（否则死锁：做首件得先开工），放行后生成 FAI 任务
        issueAndConfirm(seq10, new BigDecimal("100"));
        scheduleResource(seq10, LocalDate.now());
        opTaskApi.startOpTask(startBO(seq10));

        // 首件已产出但检验还没做：报工一笔，让门禁的"已有报工"前提成立
        submitReport(seq10, new BigDecimal("10"));
        // 开工 -> 报工 -> 暂停，回到可再次开工的状态（RUNNING -> PAUSED -> RUNNING 走 resume，
        // 而门禁只在 start 上跑，因此这里用暂停再开工的做法不成立；直接验第二次 start 前的门禁）
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), reload(seq10).getStatus());

        // 断言：首件门禁在"已有报工 + fai_flag 为真"时启用，检验未通过就拦住
        assertErrorCode(() -> startGateCheckerCheck(seq10),
                ErrorCodeConstants.OP_TASK_FAI_NOT_PASSED);
    }

    // ==================== 门禁四：资源不可用 ====================

    @Test
    void testGate_ResourceUnavailable_NoSchedule_Rejected() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        issueAndConfirm(seq10, new BigDecimal("100"));
        // 排班只建在 2020-01-01（MdmFixture 的 EFFECTIVE_DATE），今天没有排班
        scheduleResource(seq10, LocalDate.of(2020, 1, 1));

        assertErrorCode(() -> opTaskApi.startOpTask(startBO(seq10)),
                ErrorCodeConstants.OP_TASK_RES_UNAVAILABLE);

        String message = serviceExceptionMessage(() -> opTaskApi.startOpTask(startBO(seq10)));
        assertTrue(message.contains("没有排班"), "错误消息要说明是没排班，实际为：" + message);
        assertEquals(MesOpTaskStatusEnum.READY.name(), reload(seq10).getStatus());
    }

    // ==================== 门禁五：技能不足 ====================

    /** 技能等级低于要求：给操作者配 1 级，工序要求 3 级。 */
    @Test
    void testGate_SkillNotMatch_LevelTooLow_Rejected() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        issueAndConfirm(seq10, new BigDecimal("100"));
        scheduleResource(seq10, LocalDate.now());
        // 工序要求技能等级 3
        Long operatorId = 999L;
        Long skillId = createSkillRequirement(gate.opIds().get(0), 3);
        createMemberSkill(operatorId, skillId, 1, LocalDate.of(2020, 1, 1), null);

        assertErrorCode(() -> opTaskApi.startOpTask(startBO(seq10, operatorId)),
                ErrorCodeConstants.OP_TASK_SKILL_NOT_MATCH);

        String message = serviceExceptionMessage(() -> opTaskApi.startOpTask(startBO(seq10, operatorId)));
        assertTrue(message.contains("要求等级 3"), "错误消息要含要求等级，实际为：" + message);
        assertEquals(MesOpTaskStatusEnum.READY.name(), reload(seq10).getStatus());
    }

    /** 技能已过期：等级够但失效日期早于校验基准日，同样按不满足处理。 */
    @Test
    void testGate_SkillNotMatch_Expired_Rejected() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        issueAndConfirm(seq10, new BigDecimal("100"));
        scheduleResource(seq10, LocalDate.now());
        Long operatorId = 998L;
        Long skillId = createSkillRequirement(gate.opIds().get(0), 2);
        // 等级 5 够用，但 2020 年就失效了
        createMemberSkill(operatorId, skillId, 5, LocalDate.of(2019, 1, 1), LocalDate.of(2020, 1, 1));

        assertErrorCode(() -> opTaskApi.startOpTask(startBO(seq10, operatorId)),
                ErrorCodeConstants.OP_TASK_SKILL_NOT_MATCH);
    }

    // ==================== 全满足：开工成功 ====================

    @Test
    void testGate_AllSatisfied_StartSucceedsAndAdvancesThreeLevels() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);

        // 五项全部满足：料已发、当天有排班、无前序、无技能要求、首件门禁未启用
        issueAndConfirm(seq10, new BigDecimal("100"));
        scheduleResource(seq10, LocalDate.now());

        opTaskApi.startOpTask(startBO(seq10));

        // 断言：三级状态都推进到 RUNNING
        OpTaskDO started = reload(seq10);
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), started.getStatus());
        assertNotNull(started.getActualStartTime(), "开工必须写 actual_start_time");

        WorkOrderDO workOrder = workOrderMapper.selectById(seq10.getWorkOrderId());
        assertEquals(MesWorkOrderStatusEnum.RUNNING.name(), workOrder.getStatus(),
                "首道工序开工，作业工单一起进 RUNNING");
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(),
                prodOrderMapper.selectById(orderId).getStatus(),
                "首道工序开工，生产工单一起进 RUNNING");
    }

    // ==================== 并行组：只完工一道不放行后序 ====================

    @Test
    void testGate_ParallelGroup_PartiallyDoneBlocksSuccessor() {
        Long orderId = createAndReleaseGateOrder();
        List<OpTaskDO> tasks = tasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);
        OpTaskDO seq20 = tasks.get(1);
        OpTaskDO seq30 = tasks.get(2);
        OpTaskDO seq40 = tasks.get(3);

        // 前序遍历：seq10 完工，后序并行组两道一起进 READY
        startAndFinish(seq10);
        assertEquals(MesOpTaskStatusEnum.READY.name(), reload(seq20).getStatus());
        assertEquals(MesOpTaskStatusEnum.READY.name(), reload(seq30).getStatus());

        // 并行组只完工第一道（seq20），第二道（seq30）还在 READY
        startAndFinish(seq20);
        assertEquals(MesOpTaskStatusEnum.DONE.name(), reload(seq20).getStatus());
        assertEquals(MesOpTaskStatusEnum.READY.name(), reload(seq30).getStatus());

        // 断言：后序 seq40 的开工门禁要求并行组<b>全部</b>完工，只完工一道仍被拦
        assertErrorCode(() -> opTaskApi.startOpTask(startBO(seq40)),
                ErrorCodeConstants.OP_TASK_PREV_NOT_DONE);

        String message = serviceExceptionMessage(() -> opTaskApi.startOpTask(startBO(seq40)));
        assertTrue(message.contains("第" + seq30.getSeq() + "道"),
                "拦住的消息要点出还没做完的那道并行工序，实际为：" + message);
    }

    // ==================== 建数据：门禁产线 ====================

    /**
     * 在试点产线之上建一条"可验门禁"的路线与配套主数据。
     *
     * <p>路线结构：seq10（fai_flag = true，挂必发料）→ seq20 ∥ seq30 → seq40。
     * 另建一版 BOM 指向同一个子件，保证齐套/领料展开有数据。
     */
    private GateLine buildGateLine(PilotLine pilotLine) {
        // 1. 成品换成新物料：路线与 BOM 都按物料挂，不能复用试点成品（它的生效路线已被占用）
        Long fgMatId = createMaterial(pilotLine, "门禁试点成品");

        // 2. 新 BOM：V2.0，生效日期比试点 BOM 晚，成为该物料的当期生效版本
        Long bomId = createBom(fgMatId, pilotLine);

        // 3. 新路线：3 道工序改为 4 道（10 → 20 ∥ 30 → 40），首道要求首件
        Long routeId = createRoute(fgMatId);

        // 复用试点产线的标准工序，其中再补一道（首道要挂必发料与首件）
        List<Long> opIds = new ArrayList<>(pilotLine.opIds());
        Long seq10OpId = opIds.get(0);
        Long seq20OpId = opIds.get(1);
        Long seq30OpId = opIds.get(2);
        Long seq40OpId = createOp("门禁试点工序四");

        List<Long> routeOpIds = new ArrayList<>();
        routeOpIds.add(createRouteOp(routeId, seq10OpId, pilotLine.wcId(), null, true));
        routeOpIds.add(createRouteOp(routeId, seq20OpId, pilotLine.wcId(), PARALLEL_GROUP, false));
        routeOpIds.add(createRouteOp(routeId, seq30OpId, pilotLine.wcId(), PARALLEL_GROUP, false));
        routeOpIds.add(createRouteOp(routeId, seq40OpId, pilotLine.wcId(), null, false));

        // 4. 首道工序挂必发料：必须开工前发料，门禁的 kitOrIssued 才有判定对象
        RouteOpMatSaveReqVO routeOpMat = new RouteOpMatSaveReqVO();
        routeOpMat.setRouteOpId(routeOpIds.get(0));
        routeOpMat.setMatId(pilotLine.anyComponentMatId());
        routeOpMat.setQty(new BigDecimal("2"));
        routeOpMat.setIssueMethod("PICK");
        routeOpMat.setMustIssueBeforeStart(true);
        routeOpMat.setUomId(pilotLine.uomId());
        Long routeOpMatId = routeOpMatService.createRouteOpMat(routeOpMat);

        // 路线建完再启用（启用态禁止改工序）
        RouteSaveReqVO enable = new RouteSaveReqVO();
        enable.setId(routeId);
        enable.setMatId(fgMatId);
        enable.setVersion("V2.0");
        enable.setStatus(CommonStatusEnum.ENABLE.getStatus());
        enable.setEffectiveDate(LocalDate.of(2020, 2, 1));
        routeService.updateRoute(enable);

        return new GateLine(fgMatId, bomId, routeId, opIds, routeOpIds, routeOpMatId);
    }

    /** 门禁用产线句柄。 */
    private record GateLine(Long fgMatId, Long bomId, Long routeId, List<Long> opIds,
                            List<Long> routeOpIds, Long routeOpMatId) {
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

    private Long createOp(String name) {
        // 走 OpService 建一道固定工时工序
        return opService.createOp(buildOpVO(name));
    }

    private OpSaveReqVO buildOpVO(String name) {
        OpSaveReqVO op = new OpSaveReqVO();
        op.setCode("GATE-OP-" + KEY_SEQ.incrementAndGet());
        op.setName(name);
        op.setDurationType("FIXED");
        op.setStdDuration(30);
        op.setSetupMinutes(0);
        op.setTeardownMinutes(0);
        op.setQueueMinutes(0);
        op.setMoveMinutes(0);
        op.setManQty(1);
        op.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return op;
    }

    // ==================== 建数据：工单、领料、排班、技能、报工 ====================

    private Long createAndReleaseGateOrder() {
        CreateProdOrderReqDTO req = new CreateProdOrderReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-GATE");
        req.setIdempotencyKey(nextKey("CREATE_ORDER"));
        req.setPlantId(line.plantId());
        req.setWorkshopId(line.workshopId());
        req.setProjectId(MesConstants.NONE_ID);
        req.setProjectTaskId(MesConstants.NONE_ID);
        req.setMatId(gate.fgMatId());
        req.setQty(PilotLine.DEMAND_QTY);
        req.setUomId(line.uomId());
        req.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        req.setPriority(0);
        Long orderId = prodOrderApi.createProdOrder(req);

        ProdOrderActionReqDTO release = new ProdOrderActionReqDTO();
        release.setSourceModule(SOURCE_MODULE);
        release.setSourceBizNo("T6-03-GATE");
        release.setIdempotencyKey(nextKey("RELEASE_ORDER"));
        release.setProdOrderId(orderId);
        prodOrderApi.releaseProdOrder(release);
        return orderId;
    }

    /** 给指定工序任务领 100 并确认，让料真正落到工位（门禁的 kitOrIssued 认的是已发料）。 */
    private void issueAndConfirm(OpTaskDO task, BigDecimal qty) {
        CreateMaterialReqReqDTO req = new CreateMaterialReqReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-GATE-ISSUE");
        req.setIdempotencyKey(nextKey("CREATE_REQ"));
        req.setProdOrderId(prodOrderIdOf(task));
        req.setWorkOrderId(task.getWorkOrderId());
        req.setOpTaskId(task.getId());
        req.setReqType(MesMaterialReqTypeEnum.ISSUE.name());
        req.setInvOrgId(line.invOrgId());
        req.setWhId(line.whId());

        CreateMaterialReqReqDTO.Line reqLine = new CreateMaterialReqReqDTO.Line();
        reqLine.setMatId(line.anyComponentMatId());
        reqLine.setQty(qty);
        reqLine.setUomId(line.uomId());
        req.setLines(List.of(reqLine));
        Long reqId = materialReqApi.createMaterialReq(req);

        submitIssue(reqId);
        MesConfirmMaterialReqBO confirm = new MesConfirmMaterialReqBO();
        confirm.setSourceModule(SOURCE_MODULE);
        confirm.setSourceBizNo("T6-03-GATE-CONFIRM_MATERIAL_REQ");
        confirm.setIdempotencyKey(SOURCE_MODULE + ":" + reqId + ":CONFIRM_MATERIAL_REQ:0");
        confirm.setMaterialReqId(reqId);
        materialReqDomainService.confirmMaterialReq(confirm);
    }

    /**
     * 提交领料申请。
     *
     * <p><b>已知生产缺陷</b>：MES 的 {@code MesMaterialReqDomainServiceImpl.reserve()} 从不给
     * {@code CreateReservationReqDTO} 设 {@code locId}，而 {@code wms_inv_reservation.loc_id} 是
     * NOT NULL（{@code sql/deploy/01-schema.sql:3802}），WMS 的 insert 又只做直通复制不做库位解析
     * （{@code WmsInventoryReservationServiceImpl:75}）。结果是<b>任何不带批次的 MES 领料提交都必然
     * 在预留落库这一步失败</b>，领料三段整条链走不通。模块内测试全 mock 了 {@code reservationApi}
     * 所以从未暴露。这里把原始 SQL 异常转成一句能直接定位的失败信息，不让它伪装成测试写错。
     */
    private void submitIssue(Long reqId) {
        try {
            materialReqDomainService.submitMaterialReq(actionBO(reqId, "SUBMIT_MATERIAL_REQ"));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalStateException("领料提交失败：生产缺陷——wms_inv_reservation.loc_id "
                    + "NOT NULL 而 MES reserve() 未传库位（见本方法注释）。原始异常：" + ex.getMessage(), ex);
        }
    }

    /** 经 APS 回写门面给工序任务排资源：写 resId 并把 PENDING 推进到 READY。 */
    private void scheduleResource(OpTaskDO task, LocalDate workDate) {
        // 先把资源当天的排班建出来（门禁要求"资源当天有排班"）
        ensureResourceCalendar(workDate);

        ApplyScheduleReqDTO req = new ApplyScheduleReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-GATE-SCHEDULE");
        req.setIdempotencyKey(nextKey("APPLY_SCHEDULE"));
        req.setVersionNo("T6-03-V" + KEY_SEQ.incrementAndGet());
        ApplyScheduleReqDTO.Item item = new ApplyScheduleReqDTO.Item();
        item.setOpTaskId(task.getId());
        item.setResId(line.resId());
        item.setPlannedStartTime(workDate.atTime(8, 0));
        item.setPlannedEndTime(workDate.atTime(12, 0));
        req.setItems(List.of(item));
        opTaskApi.applySchedule(req);
    }

    /** 资源在指定日期有排班（已存在则跳过，主数据排班是幂等补齐）。 */
    private void ensureResourceCalendar(LocalDate workDate) {
        if (!resourceCalendarExists(workDate)) {
            ResCalendarBatchSaveReqVO batch = new ResCalendarBatchSaveReqVO();
            batch.setResIds(List.of(line.resId()));
            batch.setDateFrom(workDate);
            batch.setDateTo(workDate);
            batch.setShiftId(line.shiftId());
            batch.setAvailableMinutes(720);
            resCalendarService.saveResCalendarBatch(batch);
        }
    }

    private boolean resourceCalendarExists(LocalDate workDate) {
        return !resCalendarService.getResCalendarList(List.of(line.resId()), workDate, workDate).isEmpty();
    }

    /** 建一条工序技能要求，返回技能编号。 */
    private Long createSkillRequirement(Long opId, int minLevel) {
        SkillSaveReqVO skill = new SkillSaveReqVO();
        skill.setCode("GATE-SKILL-" + KEY_SEQ.incrementAndGet());
        skill.setName("门禁试点技能");
        skill.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long skillId = skillService.createSkill(skill);

        OpSkillReqSaveReqVO req = new OpSkillReqSaveReqVO();
        req.setOpId(opId);
        req.setSkillId(skillId);
        req.setMinLevel(minLevel);
        req.setStatus(CommonStatusEnum.ENABLE.getStatus());
        skillService.createOpSkillReq(req);
        return skillId;
    }

    private void createMemberSkill(Long userId, Long skillId, int level, LocalDate effective, LocalDate expiry) {
        MemberSkillSaveReqVO req = new MemberSkillSaveReqVO();
        req.setUserId(userId);
        req.setSkillId(skillId);
        req.setLevel(level);
        req.setEffectiveDate(effective);
        req.setExpiryDate(expiry);
        req.setStatus(CommonStatusEnum.ENABLE.getStatus());
        skillService.createMemberSkill(req);
    }

    /** 报工一笔，让首件门禁的"已有报工"前提成立。 */
    private void submitReport(OpTaskDO task, BigDecimal qtyGood) {
        MesSubmitReportBO bo = new MesSubmitReportBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-03-GATE-REPORT");
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":REPORT:1");
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

    /** 直接调门禁校验链（首件场景：工序已 RUNNING，第二次 start 会被状态机拦，故单独验门禁）。 */
    private void startGateCheckerCheck(OpTaskDO task) {
        startGateChecker.check(reload(task), new MesStartGateContext());
    }

    /** 开工并完工一道工序（数量按计划数全额完工）。 */
    private void startAndFinish(OpTaskDO task) {
        issueAndConfirm(task, new BigDecimal("100"));
        scheduleResource(task, LocalDate.now());
        opTaskApi.startOpTask(startBO(task));

        OpTaskFinishReqDTO finish = new OpTaskFinishReqDTO();
        finish.setSourceModule(SOURCE_MODULE);
        finish.setSourceBizNo("T6-03-GATE-FINISH");
        finish.setIdempotencyKey(nextKey("FINISH_OP_TASK"));
        finish.setOpTaskId(task.getId());
        BigDecimal planned = reload(task).getPlannedQty();
        finish.setQtyGood(planned);
        finish.setQtyScrap(BigDecimal.ZERO);
        opTaskApi.finishOpTask(finish);
    }

    // ==================== 断言与取数辅助 ====================

    private OpTaskDO reload(OpTaskDO task) {
        return opTaskMapper.selectById(task.getId());
    }

    private List<OpTaskDO> tasksBySeq(Long prodOrderId) {
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    private Long prodOrderIdOf(OpTaskDO task) {
        return workOrderMapper.selectById(task.getWorkOrderId()).getProdOrderId();
    }

    private String gateMatCode() {
        // 缺料消息里的物料编码取自 mdm_material，这里用物料主档的真实编码
        return matService.getMat(line.anyComponentMatId()).getCode();
    }

    /** 取服务异常的 message，供"消息内容"类断言使用。 */
    private String serviceExceptionMessage(Runnable action) {
        try {
            action.run();
        } catch (ServiceException ex) {
            return ex.getMessage();
        }
        return "<没有抛出异常>";
    }

    private OpTaskStatusReqDTO startBO(OpTaskDO task) {
        return startBO(task, MesConstants.NONE_ID);
    }

    private OpTaskStatusReqDTO startBO(OpTaskDO task, Long operatorId) {
        OpTaskStatusReqDTO req = new OpTaskStatusReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-GATE-START");
        req.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":START_OP_TASK:0");
        req.setOpTaskId(task.getId());
        req.setOperatorId(operatorId);
        return req;
    }

    private MesMaterialReqActionBO actionBO(Long reqId, String action) {
        MesMaterialReqActionBO bo = new MesMaterialReqActionBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-03-GATE-" + action);
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + reqId + ":" + action + ":0");
        bo.setMaterialReqId(reqId);
        return bo;
    }

    private String nextKey(String action) {
        return SOURCE_MODULE + ":T6-03-GATE:" + action + ":" + KEY_SEQ.incrementAndGet();
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


    /**
     * 断言抛出的是指定错误码。
     *
     * <p>不用 {@code AssertUtils.assertServiceException}：它还会把消息与传入的占位参数逐个比对，
     * 而门禁与库存校验的消息参数都是运行期数据（工序号、物料编码、缺口量），
     * 在测试里再拼一遍等于把被测逻辑抄一份。这里只锁错误码——它是这条链唯一的对外契约，
     * 消息内容由各用例另行用 contains 断言。
     */
    private void assertErrorCode(org.junit.jupiter.api.function.Executable executable, ErrorCode expected) {
        ServiceException ex = assertThrows(ServiceException.class, executable);
        assertEquals(expected.getCode(), ex.getCode(),
                "错误码不匹配，实际消息：" + ex.getMessage());
    }

}
