package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpExecModeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_ROUTE_OP_EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 工序展开测试：作业工单按工作中心分组、工序任务快照与默认值、前后序串联（含并行组）。
 *
 * <p>并行组串联是下达动作里最容易写错的点。串成链就变成"必须先喷漆再标识"，后序工序的开工门禁
 * 也会在其中一道完工时就放行；因此这里逐条断言同组工序的 prev 与 next 都指向同一个邻居。
 */
@Import(MesOrderExpandHelper.class)
class MesOrderExpandHelperTest extends BaseDbUnitTest {

    /** 工艺路线编号，工单锁定后展开的依据。 */
    private static final Long ROUTE_ID = 21L;
    /** 并行组名，对应 04 文档"工序串联规则"里的 parallel_group=A。 */
    private static final String GROUP_A = "A";

    private static final Long ROUTE_OP_CUT = 101L;
    private static final Long ROUTE_OP_MACHINE = 102L;
    private static final Long ROUTE_OP_PAINT = 103L;
    private static final Long ROUTE_OP_MARK = 104L;
    private static final Long ROUTE_OP_FINISH = 105L;

    private static final Long WC_CUT = 201L;
    private static final Long WC_MACHINE = 202L;
    private static final Long WC_PAINT = 203L;
    private static final Long WC_MARK = 204L;

    private static final BigDecimal ORDER_QTY = new BigDecimal("100");

    @Resource
    private MesOrderExpandHelper expandHelper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private OpApi opApi;
    @MockitoBean
    private MesCodeService codeService;

    @BeforeEach
    void mockCodeRule() {
        // 编码规则在 infra，单测里按规则码分别发号，保证作业工单号与工序任务号各自连续且互不重号
        Map<String, AtomicInteger> counters = new HashMap<>();
        when(codeService.generate(anyString(), any())).thenAnswer(invocation -> {
            String ruleCode = invocation.getArgument(0);
            return invocation.getArgument(1) != null ? invocation.getArgument(1)
                    : ruleCode + "-" + counters.computeIfAbsent(ruleCode, key -> new AtomicInteger())
                    .incrementAndGet();
        });
    }

    @Test
    void testExpand_oneWorkOrderPerWorkCenter() {
        // 准备：5 道工序落在 4 个工作中心，seq=40 的精加工回到 seq=20 的加工中心
        ProdOrderDO order = mockRoute(parallelRoute());

        // 调用
        expandHelper.expand(order);

        // 断言：作业工单数等于工作中心数（4），不是工序数（5）——同一个工作中心只出一张单
        List<WorkOrderDO> workOrders = workOrderMapper.selectListByProdOrderIds(List.of(order.getId()));
        assertEquals(4, workOrders.size());
        assertEquals(List.of(WC_CUT, WC_MACHINE, WC_PAINT, WC_MARK),
                workOrders.stream().map(WorkOrderDO::getWcId).sorted().toList());
        // 每张单：计划数量等于工单数量，状态 PENDING（推进到 READY 由齐套或发料触发），进度归零
        for (WorkOrderDO workOrder : workOrders) {
            assertEquals(order.getOrderNo(), workOrder.getProdOrderNo());
            assertEquals(order.getPlantId(), workOrder.getPlantId());
            assertEquals(order.getWorkshopId(), workOrder.getWorkshopId());
            assertEquals(0, ORDER_QTY.compareTo(workOrder.getPlannedQty()));
            assertEquals(0, BigDecimal.ZERO.compareTo(workOrder.getCompletedQty()));
            assertEquals(MesWorkOrderStatusEnum.PENDING.name(), workOrder.getStatus());
            assertEquals(MesKitStatusEnum.UNCHECKED.name(), workOrder.getKitStatus());
            assertEquals(MesConstants.INIT_VERSION, workOrder.getVersion());
            assertTrue(workOrder.getWorkOrderNo().startsWith("MES_WORK_ORDER_NO-"));
        }
        // 加工中心那张单挂着两道工序（加工与精加工），另外三张各一道
        Map<Long, Long> taskCountByWc = tasksOf(order.getId()).stream().collect(Collectors.groupingBy(
                task -> wcOf(workOrders, task.getWorkOrderId()), Collectors.counting()));
        assertEquals(2L, taskCountByWc.get(WC_MACHINE));
        assertEquals(1L, taskCountByWc.get(WC_CUT));
    }

    @Test
    void testExpand_opTaskSnapshotAndDefaults() {
        // 准备
        ProdOrderDO order = mockRoute(parallelRoute());

        // 调用
        expandHelper.expand(order);

        // 断言：工序任务数等于路线工序数
        List<OpTaskDO> tasks = tasksOf(order.getId());
        assertEquals(5, tasks.size());
        // 快照逐项核对：工序编码与名称来自 OpApi，顺序与并行组、关键工序标记来自 mdm_route_op
        OpTaskDO paint = taskOf(tasks, ROUTE_OP_PAINT);
        assertEquals("OP43", paint.getOpCode());
        assertEquals("工序43", paint.getOpName());
        assertEquals(43L, paint.getOpId());
        assertEquals(30, paint.getSeq());
        assertEquals(GROUP_A, paint.getParallelGroup());
        assertEquals(Boolean.FALSE, paint.getKeyFlag());
        assertEquals(Boolean.TRUE, taskOf(tasks, ROUTE_OP_CUT).getKeyFlag());
        assertNull(taskOf(tasks, ROUTE_OP_CUT).getParallelGroup());
        // 默认值：计划数量等于工单数量、进度归零、未分配资源与计划时间、状态 PENDING
        for (OpTaskDO task : tasks) {
            assertEquals(0, ORDER_QTY.compareTo(task.getPlannedQty()));
            assertEquals(0, BigDecimal.ZERO.compareTo(task.getCompletedQty()));
            assertEquals(0, BigDecimal.ZERO.compareTo(task.getScrapQty()));
            assertEquals(MesConstants.NONE_ID, task.getResId());
            assertNull(task.getPlannedStartTime());
            assertEquals(MesOpTaskStatusEnum.PENDING.name(), task.getStatus());
            assertEquals(MesQcModeEnum.NONE.name(), task.getQcMode());
            assertEquals(MesSkipPolicyEnum.NOT_ALLOWED.name(), task.getSkipPolicy());
            assertEquals(MesConstants.INIT_VERSION, task.getVersion());
            assertTrue(task.getTaskNo().startsWith("MES_OP_TASK_NO-"));
        }
        // 执行模式由并行组推导：有并行组的可并行，其余必须按序
        assertEquals(MesOpExecModeEnum.PARALLEL.name(), paint.getExecMode());
        assertEquals(MesOpExecModeEnum.PARALLEL.name(), taskOf(tasks, ROUTE_OP_MARK).getExecMode());
        assertEquals(MesOpExecModeEnum.SEQUENTIAL.name(), taskOf(tasks, ROUTE_OP_CUT).getExecMode());
    }

    @Test
    void testExpand_chainWithParallelGroup() {
        // 准备：下料(10) -> 加工(20) -> 喷漆(30,A) ‖ 标识(30,A) -> 精加工(40)
        ProdOrderDO order = mockRoute(parallelRoute());

        // 调用
        expandHelper.expand(order);

        // 断言
        List<OpTaskDO> tasks = tasksOf(order.getId());
        OpTaskDO cut = taskOf(tasks, ROUTE_OP_CUT);
        OpTaskDO machine = taskOf(tasks, ROUTE_OP_MACHINE);
        OpTaskDO paint = taskOf(tasks, ROUTE_OP_PAINT);
        OpTaskDO mark = taskOf(tasks, ROUTE_OP_MARK);
        OpTaskDO finish = taskOf(tasks, ROUTE_OP_FINISH);

        // 这条是验收点名的硬指标：同一并行组的两道工序，prev 指向同一个前序、next 指向同一个后序
        assertEquals(machine.getId(), paint.getPrevTaskId());
        assertEquals(machine.getId(), mark.getPrevTaskId());
        assertEquals(paint.getPrevTaskId(), mark.getPrevTaskId());
        assertEquals(finish.getId(), paint.getNextTaskId());
        assertEquals(finish.getId(), mark.getNextTaskId());
        assertEquals(paint.getNextTaskId(), mark.getNextTaskId());
        // 组内互不为前后序：两道并行工序谁都不指向对方，否则就变成必须先喷漆再标识
        assertTrue(!paint.getNextTaskId().equals(mark.getId()) && !paint.getPrevTaskId().equals(mark.getId()));
        assertTrue(!mark.getNextTaskId().equals(paint.getId()) && !mark.getPrevTaskId().equals(paint.getId()));
        // 并行步骤的邻居只能指一个成员：取步骤内第一道（seq 最小、seq 相同时编号最小）作代表，
        // 开工门禁再按 parallel_group 展开同组全部工序判"是否全部完工"
        assertEquals(paint.getId(), machine.getNextTaskId());
        assertEquals(paint.getId(), finish.getPrevTaskId());
        // 串行段照 seq 顺序相接
        assertEquals(MesConstants.NONE_ID, cut.getPrevTaskId());
        assertEquals(machine.getId(), cut.getNextTaskId());
        assertEquals(cut.getId(), machine.getPrevTaskId());
        // 末道工序无后序
        assertEquals(MesConstants.NONE_ID, finish.getNextTaskId());
    }

    @Test
    void testExpand_chainPureSequential() {
        // 准备：三道工序全无并行组
        ProdOrderDO order = mockRoute(List.of(
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, false),
                routeOp(ROUTE_OP_MACHINE, 20, 42L, WC_MACHINE, null, false),
                routeOp(ROUTE_OP_PAINT, 30, 43L, WC_PAINT, null, false)));

        // 调用
        expandHelper.expand(order);

        // 断言：首尾为 0，中间两两相接
        List<OpTaskDO> tasks = tasksOf(order.getId());
        OpTaskDO first = taskOf(tasks, ROUTE_OP_CUT);
        OpTaskDO second = taskOf(tasks, ROUTE_OP_MACHINE);
        OpTaskDO third = taskOf(tasks, ROUTE_OP_PAINT);
        assertEquals(MesConstants.NONE_ID, first.getPrevTaskId());
        assertEquals(second.getId(), first.getNextTaskId());
        assertEquals(first.getId(), second.getPrevTaskId());
        assertEquals(third.getId(), second.getNextTaskId());
        assertEquals(second.getId(), third.getPrevTaskId());
        assertEquals(MesConstants.NONE_ID, third.getNextTaskId());
    }

    @Test
    void testExpand_routeOpsSortedBySeqRegardlessOfInputOrder() {
        // 准备：门面把工序乱序返回。串联完全依赖顺序，因此展开必须自己排一次而不是信上游
        List<RouteOpRespDTO> shuffled = List.of(
                routeOp(ROUTE_OP_FINISH, 40, 45L, WC_MACHINE, null, true),
                routeOp(ROUTE_OP_PAINT, 30, 43L, WC_PAINT, GROUP_A, false),
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, true),
                routeOp(ROUTE_OP_MARK, 30, 44L, WC_MARK, GROUP_A, false),
                routeOp(ROUTE_OP_MACHINE, 20, 42L, WC_MACHINE, null, false));
        ProdOrderDO order = mockRoute(shuffled);

        // 调用
        expandHelper.expand(order);

        // 断言：串联结果与顺序输入时完全一致
        List<OpTaskDO> tasks = tasksOf(order.getId());
        OpTaskDO cut = taskOf(tasks, ROUTE_OP_CUT);
        OpTaskDO machine = taskOf(tasks, ROUTE_OP_MACHINE);
        OpTaskDO paint = taskOf(tasks, ROUTE_OP_PAINT);
        OpTaskDO mark = taskOf(tasks, ROUTE_OP_MARK);
        OpTaskDO finish = taskOf(tasks, ROUTE_OP_FINISH);
        assertEquals(MesConstants.NONE_ID, cut.getPrevTaskId());
        assertEquals(machine.getId(), cut.getNextTaskId());
        assertEquals(paint.getId(), machine.getNextTaskId());
        assertEquals(machine.getId(), paint.getPrevTaskId());
        assertEquals(machine.getId(), mark.getPrevTaskId());
        assertEquals(finish.getId(), paint.getNextTaskId());
        assertEquals(finish.getId(), mark.getNextTaskId());
        assertEquals(MesConstants.NONE_ID, finish.getNextTaskId());
    }

    @Test
    void testExpand_routeWithoutOpRejected() {
        // 准备：路线下一道工序都没有
        ProdOrderDO order = order();
        when(routeApi.getRouteOpList(ROUTE_ID)).thenReturn(List.of());

        // 调用并断言：拒绝下达，而不是产出一张没有工序任务的空工单
        assertServiceException(() -> expandHelper.expand(order), PROD_ORDER_ROUTE_OP_EMPTY);
        assertEquals(0L, workOrderMapper.selectCount());
    }

    @Test
    void testExpand_standardOpMissingRejected() {
        // 准备：路线工序引用的标准工序在主数据里查不到
        ProdOrderDO order = order();
        when(routeApi.getRouteOpList(ROUTE_ID))
                .thenReturn(List.of(routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, false)));
        when(opApi.getOpMap(any())).thenReturn(Map.of());

        // 调用并断言：工序编码与名称是快照，取不到就不许落库
        assertServiceException(() -> expandHelper.expand(order), OP_NOT_EXISTS);
    }

    @Test
    void testExpand_skipPolicySnapshotAllowWithReason() {
        // 准备：路线工序配置"填原因即可跳序"
        ProdOrderDO order = mockRoute(List.of(
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, false,
                        MesSkipPolicyEnum.ALLOW_WITH_REASON.name())));

        // 调用
        expandHelper.expand(order);

        // 断言：下达时把路线上的跳序策略快照到工序任务，而不是写死 NOT_ALLOWED
        assertEquals(MesSkipPolicyEnum.ALLOW_WITH_REASON.name(),
                taskOf(tasksOf(order.getId()), ROUTE_OP_CUT).getSkipPolicy());
    }

    @Test
    void testExpand_skipPolicySnapshotApprovalRequired() {
        // 准备：路线工序配置"需审批后跳序"
        ProdOrderDO order = mockRoute(List.of(
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, false,
                        MesSkipPolicyEnum.APPROVAL_REQUIRED.name())));

        // 调用
        expandHelper.expand(order);

        // 断言：快照值与路线配置一致
        assertEquals(MesSkipPolicyEnum.APPROVAL_REQUIRED.name(),
                taskOf(tasksOf(order.getId()), ROUTE_OP_CUT).getSkipPolicy());
    }

    @Test
    void testExpand_skipPolicyNullFallsBackToNotAllowed() {
        // 准备：老工艺路线没有 skip_policy，门面返回 null
        ProdOrderDO order = mockRoute(List.of(
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, false, null)));

        // 调用
        expandHelper.expand(order);

        // 断言：兜底 NOT_ALLOWED——mes_op_task.skip_policy 是 NOT NULL 列，落 null 会直接插不进去
        assertEquals(MesSkipPolicyEnum.NOT_ALLOWED.name(),
                taskOf(tasksOf(order.getId()), ROUTE_OP_CUT).getSkipPolicy());
    }

    /**
     * 04 文档"工序串联规则"的原型路线：下料 -&gt; 加工 -&gt; （喷漆 ‖ 标识） -&gt; 精加工。
     *
     * <p>seq=40 的精加工特意回到 seq=20 的加工中心，用来验证"一个工作中心一张作业工单"：
     * 5 道工序只应产出 4 张单。
     */
    private List<RouteOpRespDTO> parallelRoute() {
        return List.of(
                routeOp(ROUTE_OP_CUT, 10, 41L, WC_CUT, null, true),
                routeOp(ROUTE_OP_MACHINE, 20, 42L, WC_MACHINE, null, false),
                routeOp(ROUTE_OP_PAINT, 30, 43L, WC_PAINT, GROUP_A, false),
                routeOp(ROUTE_OP_MARK, 30, 44L, WC_MARK, GROUP_A, false),
                routeOp(ROUTE_OP_FINISH, 40, 45L, WC_MACHINE, null, true));
    }

    private RouteOpRespDTO routeOp(Long id, int seq, Long opId, Long wcId,
                                   String parallelGroup, boolean keyFlag) {
        RouteOpRespDTO routeOp = new RouteOpRespDTO();
        routeOp.setId(id);
        routeOp.setRouteId(ROUTE_ID);
        routeOp.setSeq(seq);
        routeOp.setOpId(opId);
        routeOp.setWcId(wcId);
        routeOp.setParallelGroup(parallelGroup);
        routeOp.setKeyFlag(keyFlag);
        return routeOp;
    }

    /** 带跳序策略的路线工序桩：跳序策略用例专用，在既有桩的基础上补设 skipPolicy。 */
    private RouteOpRespDTO routeOp(Long id, int seq, Long opId, Long wcId,
                                   String parallelGroup, boolean keyFlag, String skipPolicy) {
        RouteOpRespDTO routeOp = routeOp(id, seq, opId, wcId, parallelGroup, keyFlag);
        routeOp.setSkipPolicy(skipPolicy);
        return routeOp;
    }

    /** 挂好路线工序与标准工序两个门面的返回值，返回待展开的工单。 */
    private ProdOrderDO mockRoute(List<RouteOpRespDTO> routeOps) {
        when(routeApi.getRouteOpList(ROUTE_ID)).thenReturn(routeOps);
        Map<Long, OpRespDTO> opMap = routeOps.stream().map(RouteOpRespDTO::getOpId).distinct()
                .collect(Collectors.toMap(opId -> opId, this::op));
        when(opApi.getOpMap(any())).thenReturn(opMap);
        return order();
    }

    /** 标准工序桩：编码与名称按编号推导，断言时能看出快照确实来自门面。 */
    private OpRespDTO op(Long opId) {
        OpRespDTO op = new OpRespDTO();
        op.setId(opId);
        op.setCode("OP" + opId);
        op.setName("工序" + opId);
        return op;
    }

    /** 已锁定版本快照的生产工单，展开只读它的字段，不回查库。 */
    private ProdOrderDO order() {
        return ProdOrderDO.builder()
                .id(randomLongId())
                .orderNo("PO-0001")
                .plantId(1L)
                .workshopId(2L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(1L)
                .qty(ORDER_QTY)
                .routeId(ROUTE_ID)
                .routeVersion("R1.0")
                .build();
    }

    /** 取本工单展开出的全部工序任务，按 seq 升序。 */
    private List<OpTaskDO> tasksOf(Long prodOrderId) {
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId))
                .toList();
    }

    /** 按路线工序编号定位工序任务，不按下标，避免断言被生成顺序绑住。 */
    private OpTaskDO taskOf(List<OpTaskDO> tasks, Long routeOpId) {
        return tasks.stream().filter(task -> routeOpId.equals(task.getRouteOpId())).findFirst().orElseThrow();
    }

    private Long wcOf(List<WorkOrderDO> workOrders, Long workOrderId) {
        return workOrders.stream().filter(workOrder -> workOrderId.equals(workOrder.getId()))
                .findFirst().orElseThrow().getWcId();
    }

}
