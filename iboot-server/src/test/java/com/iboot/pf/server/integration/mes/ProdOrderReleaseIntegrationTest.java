package com.iboot.pf.server.integration.mes;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.ErrorCodeConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpExecModeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-02 端到端用例 1：需求到工单下达。
 *
 * <p>依据 {@code dev-docs/业务流程/08-跨模块协作与端到端时序.md} 的"端到端主用例清单"第 1 条
 * 与 {@code 04-MES生产执行流程.md} 的"流程二：工单下达"。
 *
 * <p>与模块内 {@code MesProdOrderDomainServiceImplTest} 的本质区别：这里是<b>真实 Spring 上下文</b>，
 * 主数据由 {@code MdmFixture} 走真实 Service 建出、BOM 与工艺路线的生效版本由 MDM 真实解析，
 * 不 mock 任何业务门面。模块内测试验证的是"下达调了 BomApi"，这里验证的是"下达锁到的是当天生效的那一版"。
 *
 * <p>试点产线的展开预期（{@link MdmFixture} 建出）：3 道工序 seq=10/20/30，其中 20 与 30 同属并行组
 * {@code PG1}，且三道工序都挂在同一个工作中心上，因此作业工单 1 张、工序任务 3 条。
 */
class ProdOrderReleaseIntegrationTest extends BaseIntegrationTest {

    /** MES 动作的调用方来源模块：控制器固定传 {@code UI}，测试传 {@code INTEGRATION}（T6-01 自查的约定）。 */
    private static final String SOURCE_MODULE = "INTEGRATION";

    private static final String PARALLEL_GROUP = "PG1";

    @Resource
    private ProdOrderService prodOrderService;

    @Resource
    private MesProdOrderDomainService domainService;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private BomService bomService;

    @Resource
    private RouteService routeService;

    @Resource
    private MatService matService;

    @Resource
    private MdmFixture mdmFixture;

    /** 每个用例独立的试点产线（基类事务回滚，用例之间不共享）。 */
    private PilotLine line;

    @BeforeEach
    void buildPilotLine() {
        line = mdmFixture.createPilotLine();
    }

    @Test
    void testRelease_LocksEffectiveVersionsAndExpandsWorkOrderAndOpTask() {
        // 准备：一张 DRAFT 工单，建单时锁的是"当前生效版本"
        Long orderId = createDraftOrder(line, PilotLine.DEMAND_QTY);

        // 调用：真实下达
        domainService.releaseProdOrder(releaseBO(orderId));

        // 断言 1：bom_version / route_version 已锁且等于生效版本（不是建单时随手给的值）
        ProdOrderDO released = prodOrderMapper.selectById(orderId);
        assertEquals(line.bomId(), released.getBomId());
        assertEquals(effectiveBomVersion(line), released.getBomVersion());
        assertEquals(line.routeId(), released.getRouteId());
        assertEquals(effectiveRouteVersion(line), released.getRouteVersion());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), released.getStatus());
        assertNotNull(released.getReleaseTime());

        // 断言 2：作业工单数 = 工艺路线的工作中心数。
        // 试点路线 3 道工序全在同一个工作中心上（作业工单是车间派工的单位，
        // 同一工作中心的多道工序挂同一张单），因此是 1 张
        List<WorkOrderDO> workOrders = workOrdersOf(orderId);
        assertEquals(1, workOrders.size(), "3 道工序同属一个工作中心，只应产出 1 张作业工单");
        assertEquals(line.wcId(), workOrders.get(0).getWcId());
        assertEquals(MesWorkOrderStatusEnum.PENDING.name(), workOrders.get(0).getStatus());
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), workOrders.get(0).getKitStatus());
        assertEquals(0, PilotLine.DEMAND_QTY.compareTo(workOrders.get(0).getPlannedQty()));

        // 断言 3：工序任务数 = 路线工序数（3 条，seq 10/20/30）
        List<OpTaskDO> tasks = orderTasksBySeq(orderId);
        assertEquals(3, tasks.size(), "工序任务数等于路线工序数");
        assertEquals(List.of(10, 20, 30), tasks.stream().map(OpTaskDO::getSeq).toList());
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), tasks.get(0).getStatus());
        // 工序编码与名称是下达时的快照，主数据之后改名不影响在制任务
        assertNotNull(tasks.get(0).getOpCode());
        assertNotNull(tasks.get(0).getOpName());
        // 计划数量等于工单数量（整单投产）
        assertTrue(tasks.stream().allMatch(task -> PilotLine.DEMAND_QTY.compareTo(task.getPlannedQty()) == 0));
    }

    @Test
    void testRelease_ChainsPrevAndNextTaskBySeq() {
        // 准备 + 调用
        Long orderId = createDraftOrder(line, PilotLine.DEMAND_QTY);
        domainService.releaseProdOrder(releaseBO(orderId));

        // 断言 4：prev_task_id / next_task_id 按 seq 串联正确。
        // 首步骤 prev 为 0、末步骤 next 为 0；串行段首尾相接
        List<OpTaskDO> tasks = orderTasksBySeq(orderId);
        OpTaskDO seq10 = tasks.get(0);
        OpTaskDO seq20 = tasks.get(1);
        OpTaskDO seq30 = tasks.get(2);

        assertEquals(MesConstants.NONE_ID, seq10.getPrevTaskId(), "首道工序没有前序");
        assertEquals(seq20.getId(), seq10.getNextTaskId(), "seq10 的后序是并行步骤的代表工序");
        // 并行组的两道工序 prev/next 一致（见下一条断言），这里先锁定它们同属一个步骤
        assertEquals(seq10.getId(), seq20.getPrevTaskId());
        assertEquals(seq10.getId(), seq30.getPrevTaskId());
        assertEquals(MesConstants.NONE_ID, seq20.getNextTaskId(), "并行步骤是末步骤，没有后序");
        assertEquals(MesConstants.NONE_ID, seq30.getNextTaskId(), "并行步骤是末步骤，没有后序");
    }

    @Test
    void testRelease_ParallelGroupSharedPrevAndNext() {
        // 准备 + 调用
        Long orderId = createDraftOrder(line, PilotLine.DEMAND_QTY);
        domainService.releaseProdOrder(releaseBO(orderId));

        // 断言 5（验收点名）：并行组场景——同组两道工序的 prev 指向同一前序、next 指向同一后序
        List<OpTaskDO> tasks = orderTasksBySeq(orderId);
        List<OpTaskDO> parallel = tasks.stream()
                .filter(task -> PARALLEL_GROUP.equals(task.getParallelGroup())).toList();
        assertEquals(2, parallel.size(), "试点路线有一组两道并行工序");

        OpTaskDO first = parallel.get(0);
        OpTaskDO second = parallel.get(1);
        long prevShared = first.getPrevTaskId();
        assertEquals(prevShared, second.getPrevTaskId(), "并行组两道工序的 prev 必须指向同一前序");
        assertEquals(first.getNextTaskId(), second.getNextTaskId(), "并行组两道工序的 next 必须指向同一后序");
        // 组内互不为前后序：串成链就变成必须先做完第一道才能做第二道，"并行"名存实亡
        assertTrue(!first.getPrevTaskId().equals(second.getId())
                && !first.getNextTaskId().equals(second.getId()), "并行组内不许互相串联");
        // 成员指向同一前序 = 串行段的那道工序
        assertEquals(tasks.get(0).getId(), prevShared);
    }

    @Test
    void testRelease_CarriesRouteConfigFieldsToOpTask() {
        // 准备 + 调用
        Long orderId = createDraftOrder(line, PilotLine.DEMAND_QTY);
        domainService.releaseProdOrder(releaseBO(orderId));

        // 断言 6：parallel_group / exec_mode / qc_mode / skip_policy / key_flag 已从路线带过来
        List<OpTaskDO> tasks = orderTasksBySeq(orderId);
        OpTaskDO serial = tasks.get(0);
        List<OpTaskDO> parallel = tasks.stream()
                .filter(task -> PARALLEL_GROUP.equals(task.getParallelGroup())).toList();

        // parallel_group 原样抄自 mdm_route_op：无并行组的工序为空，并行组的两道一致
        assertNull(serial.getParallelGroup(), "无并行组的工序 parallel_group 为空");
        assertTrue(parallel.stream().allMatch(task -> PARALLEL_GROUP.equals(task.getParallelGroup())));

        // exec_mode 由并行组推导：有组可并行、无组必须按序（mdm_route_op 上没有 exec_mode 列）
        assertEquals(MesOpExecModeEnum.SEQUENTIAL.name(), serial.getExecMode());
        assertTrue(parallel.stream().allMatch(
                task -> MesOpExecModeEnum.PARALLEL.name().equals(task.getExecMode())));

        // skip_policy 抄自路线工序；fixture 建路线时显式落了 NOT_ALLOWED
        assertTrue(tasks.stream().allMatch(
                task -> MesSkipPolicyEnum.NOT_ALLOWED.name().equals(task.getSkipPolicy())));

        // key_flag 抄自路线工序；fixture 建路线时显式落了 true
        assertTrue(tasks.stream().allMatch(OpTaskDO::getKeyFlag), "关键工序标记必须从路线带过来");

        // qc_mode 由质检规则解析结果决定，本轮 T1 阶段无规则命中，落 NONE
        assertTrue(tasks.stream().allMatch(
                task -> MesQcModeEnum.NONE.name().equals(task.getQcMode())));
    }

    @Test
    void testRelease_ThenChangeBomDoesNotAffectOrder() {
        // 准备：先下达，锁住当时的生效版本
        Long orderId = createDraftOrder(line, PilotLine.DEMAND_QTY);
        domainService.releaseProdOrder(releaseBO(orderId));
        ProdOrderDO released = prodOrderMapper.selectById(orderId);

        // 改 MDM 的 BOM：为成品新建一版 BOM（生效日期更晚，成为新的生效版本）并挂一行明细
        Long newBomId = createNewerEnabledBom(line);

        // 断言 7：下达后改 BOM 不影响本工单——重查工单，四个版本快照一个都没动。
        // 这是基石 #8：在制工单的执行依据固定在下达那一刻
        ProdOrderDO reloaded = prodOrderMapper.selectById(orderId);
        assertEquals(released.getBomId(), reloaded.getBomId(), "在制工单的 bom_id 不许被主数据变动带走");
        assertEquals(released.getBomVersion(), reloaded.getBomVersion());
        assertEquals(released.getRouteId(), reloaded.getRouteId());
        assertEquals(released.getRouteVersion(), reloaded.getRouteVersion());
        // 新版本确实是"当前生效版本"，证明上面比的不是两个都没变过的空值
        assertEquals(newBomId, bomService.getEffectiveBom(line.fgMatId(), LocalDate.now()).getId());
        assertTrue(!reloaded.getBomId().equals(newBomId), "工单锁的仍是下达那一刻的版本");
    }

    @Test
    void testRelease_NoEffectiveBomRejected() {
        // 准备：一个没有 BOM 的成品物料——工单照建（BOM 外键指向试点 BOM 只为过建单校验），
        // 但下达时按"工单物料 + 当天"解析生效 BOM 会落空
        PilotLine noBomLine = pilotLineWithoutBom();
        Long orderId = createDraftOrder(noBomLine, PilotLine.DEMAND_QTY);

        // 调用并断言 8：无生效 BOM 时下达抛 BOM_NOT_EXISTS，且一张作业工单都不许摊出来
        assertServiceException(() -> domainService.releaseProdOrder(releaseBO(orderId)),
                ErrorCodeConstants.BOM_NOT_EXISTS);
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), prodOrderMapper.selectById(orderId).getStatus());
        assertEquals(0, workOrdersOf(orderId).size());
        assertEquals(0, tasksOf(orderId).size());
    }

    @Test
    void testRelease_NoEffectiveRouteRejected() {
        // 准备：一个有 BOM 但没有工艺路线的成品物料
        PilotLine noRouteLine = pilotLineWithoutRoute();
        Long orderId = createDraftOrder(noRouteLine, PilotLine.DEMAND_QTY);

        // 调用并断言 8：无生效工艺时下达抛 ROUTE_NOT_EXISTS
        assertServiceException(() -> domainService.releaseProdOrder(releaseBO(orderId)),
                ErrorCodeConstants.ROUTE_NOT_EXISTS);
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), prodOrderMapper.selectById(orderId).getStatus());
        assertEquals(0, workOrdersOf(orderId).size());
        assertEquals(0, tasksOf(orderId).size());
    }

    /* ------------------------------------------------------------------ 建数据 ------------------------------------------------------------------ */

    /**
     * 经真实 Service 建一张 DRAFT 生产工单。
     *
     * <p>建单走 {@link ProdOrderService#createProdOrder}，状态由服务端固定落 {@code DRAFT}，
     * 表单里没有 {@code status} 字段。
     */
    private Long createDraftOrder(PilotLine pilotLine, BigDecimal qty) {
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setPlantId(pilotLine.plantId());
        reqVO.setWorkshopId(pilotLine.workshopId());
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(pilotLine.fgMatId());
        reqVO.setQty(qty);
        reqVO.setUomId(pilotLine.uomId());
        // BOM 与工艺路线按当前生效版本锁定（真实导入流程也是这么做的）
        reqVO.setBomId(pilotLine.bomId());
        reqVO.setRouteId(pilotLine.routeId());
        reqVO.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        reqVO.setPriority(0);
        return prodOrderService.createProdOrder(reqVO);
    }

    /**
     * 为成品新建一版生效 BOM，用来验证"下达后改 BOM 不影响在制工单"。
     *
     * <p>生效日期取今天，比试点 BOM 的 2020-01-01 新，因此 {@code getEffectiveBom(matId, today)}
     * 会命中它（生效日期晚于今天的版本会被过滤掉，所以不能写明天）。MDM 不允许改已启用 BOM 的明细，
     * 故只能新起一版——这正是真实主数据变更的方式。
     */
    private Long createNewerEnabledBom(PilotLine pilotLine) {
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(pilotLine.fgMatId());
        bom.setVersion("V2.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(LocalDate.now());
        Long bomId = bomService.createBom(bom);

        bomService.createBomLine(BomLineDO.builder()
                .bomId(bomId)
                .lineNo(10)
                .matId(pilotLine.anyComponentMatId())
                .qty(new BigDecimal("9"))
                .scrapRate(BigDecimal.ZERO)
                .issueMethod("PICK")
                .uomId(pilotLine.uomId())
                .effectiveDate(bom.getEffectiveDate())
                .build());

        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);
        return bomId;
    }

    /**
     * 一条"成品没有 BOM"的试点产线：产线其余主数据照旧，只把成品换成新物料、不给它建 BOM。
     *
     * <p>工单的 BOM 外键仍指向试点 BOM（只为一过建单的 {@code @NotNull} 与版本快照校验），
     * 下达时按"工单物料 + 当天"重新解析生效 BOM 就会落空——这正是要验的那条路。
     */
    private PilotLine pilotLineWithoutBom() {
        Long newFgMatId = createMaterial(line, "试点成品-无BOM");
        return new PilotLine(line.invOrgId(), line.plantId(), line.workshopId(), line.wcId(), line.uomId(),
                newFgMatId, line.componentMatIds(), line.matCatId(), line.shiftId(), line.calendarId(),
                line.resId(), line.resCapabilityId(), line.resCalendarId(), line.opIds(), line.routeId(),
                line.routeOpIds(), line.routeOpMatIds(), line.bomId(), line.whId(), line.zoneId(), line.locId(),
                line.initialInOrderIds(), line.qcPlanId(), line.qcItemIds(), line.qcRuleIds());
    }

    /** 一条"成品有 BOM 但没有工艺路线"的试点产线：成品换成新物料，给它建 BOM，不建路线。 */
    private PilotLine pilotLineWithoutRoute() {
        Long newFgMatId = createMaterial(line, "试点成品-无工艺");
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(newFgMatId);
        bom.setVersion("V1.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(LocalDate.of(2020, 1, 1));
        Long bomId = bomService.createBom(bom);
        bomService.createBomLine(BomLineDO.builder()
                .bomId(bomId).lineNo(10).matId(line.anyComponentMatId())
                .qty(BigDecimal.ONE).scrapRate(BigDecimal.ZERO)
                .issueMethod("PICK").uomId(line.uomId())
                .effectiveDate(LocalDate.of(2020, 1, 1)).build());
        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);
        return new PilotLine(line.invOrgId(), line.plantId(), line.workshopId(), line.wcId(), line.uomId(),
                newFgMatId, line.componentMatIds(), line.matCatId(), line.shiftId(), line.calendarId(),
                line.resId(), line.resCapabilityId(), line.resCalendarId(), line.opIds(), line.routeId(),
                line.routeOpIds(), line.routeOpMatIds(), bomId, line.whId(), line.zoneId(), line.locId(),
                line.initialInOrderIds(), line.qcPlanId(), line.qcItemIds(), line.qcRuleIds());
    }

    /** 建一个成品物料（走真实 Service，编码由编码规则分配）。 */
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

    /* ------------------------------------------------------------------ 断言辅助 ------------------------------------------------------------------ */

    private MesReleaseOrderBO releaseBO(Long orderId) {
        MesReleaseOrderBO bo = new MesReleaseOrderBO();
        bo.setProdOrderId(orderId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        bo.setRemark("T6-02 集成下达");
        return bo;
    }

    private String effectiveBomVersion(PilotLine pilotLine) {
        return bomService.getEffectiveBom(pilotLine.fgMatId(), LocalDate.now()).getVersion();
    }

    private String effectiveRouteVersion(PilotLine pilotLine) {
        return routeService.getEffectiveRoute(pilotLine.fgMatId(), LocalDate.now()).getVersion();
    }

    private List<WorkOrderDO> workOrdersOf(Long prodOrderId) {
        return workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId));
    }

    private List<OpTaskDO> tasksOf(Long prodOrderId) {
        List<Long> workOrderIds = workOrdersOf(prodOrderId).stream().map(WorkOrderDO::getId).toList();
        return workOrderIds.isEmpty() ? List.of() : opTaskMapper.selectListByWorkOrderIds(workOrderIds);
    }

    /** 按 seq 排序取工序任务，让断言不依赖生成顺序。 */
    private List<OpTaskDO> orderTasksBySeq(Long prodOrderId) {
        return tasksOf(prodOrderId).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

}
