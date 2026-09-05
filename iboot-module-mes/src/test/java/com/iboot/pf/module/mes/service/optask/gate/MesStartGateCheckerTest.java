package com.iboot.pf.module.mes.service.optask.gate;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.skill.SkillApi;
import com.iboot.pf.module.mdm.api.skill.dto.OpSkillReqRespDTO;
import com.iboot.pf.module.mdm.api.tool.ToolApi;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispatchTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.optask.gate.impl.FirstArticleChecker;
import com.iboot.pf.module.mes.service.optask.gate.impl.KitOrIssuedChecker;
import com.iboot.pf.module.mes.service.optask.gate.impl.PrevDoneChecker;
import com.iboot.pf.module.mes.service.optask.gate.impl.ResourceAvailableChecker;
import com.iboot.pf.module.mes.service.optask.gate.impl.SkillAndToolChecker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_PREV_NOT_DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 开工门禁编排的测试。
 *
 * <p>两个入口对应两种问法，用例分别锁死：{@code check} 回答"能不能开工"，遇到第一个不满足就抛错；
 * {@code inspect} 回答"现在为什么不能开工"，把所有不满足的项一次列全——车间才能一趟把料领齐、
 * 把工装准备好，而不是修一项再点一次开工试试。
 *
 * <p>跳过的项在预检结果里记为 skipped 而不是 passed：显示"通过"会让车间以为这项校验过了，
 * 其实是这道工序压根不做这项校验。
 */
@Import({MesStartGateChecker.class, PrevDoneChecker.class, KitOrIssuedChecker.class,
        FirstArticleChecker.class, ResourceAvailableChecker.class, SkillAndToolChecker.class,
        MesKitRequirementExpander.class})
class MesStartGateCheckerTest extends BaseDbUnitTest {

    private static final Long ROUTE_OP_ID = 101L;
    private static final Long OP_ID = 201L;
    private static final Long MAT_A = 1001L;
    private static final Long OPERATOR_ID = 601L;
    private static final Long DISPATCH_OPERATOR_ID = 602L;

    @Resource
    private MesStartGateChecker gateChecker;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private DispatchTaskMapper dispatchTaskMapper;

    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private BomApi bomApi;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private ResourceApi resourceApi;
    @MockitoBean
    private SkillApi skillApi;
    @MockitoBean
    private ToolApi toolApi;

    @BeforeEach
    void mockMdm() {
        when(routeApi.getRouteOpMap(any())).thenReturn(Map.of(ROUTE_OP_ID, routeOp(false)));
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of());
        when(materialApi.getMaterialMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> material((Long) id)));
        });
        when(skillApi.getOpSkillReqList(any())).thenReturn(List.of());
        when(skillApi.getMemberSkillList(any())).thenReturn(List.of());
        when(skillApi.getSkillList()).thenReturn(List.of());
        when(toolApi.getRouteOpToolList(any())).thenReturn(List.of());
        when(toolApi.getToolList()).thenReturn(List.of());
    }

    @Test
    void testCheck_allPassed() {
        // 首道工序、没有必发料、不要求首件、未分配资源、无技能与工装要求
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);

        gateChecker.check(task, null);
    }

    @Test
    void testCheck_stopsAtFirstFailure() {
        // 准备：前序未完工，同时还缺料
        OpTaskDO prev = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.RUNNING);
        OpTaskDO task = insertTask(prev.getId(), MesOpTaskStatusEnum.READY);
        givenMustIssueMat();

        // 调用并断言：抛的是排在最前面那一项的错。顺序按"最可能拦住且最便宜"排，
        // 前序未完工只查 MES 本地表，先跑它能让大多数不满足的情况少走几次跨模块调用
        assertServiceException(() -> gateChecker.check(task, null), OP_TASK_PREV_NOT_DONE,
                "第10道 下料（加工中）");
    }

    @Test
    void testInspect_collectsAllFailures() {
        // 准备：前序未完工 + 缺料，两项都不满足
        OpTaskDO prev = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.RUNNING);
        OpTaskDO task = insertTask(prev.getId(), MesOpTaskStatusEnum.READY);
        givenMustIssueMat();

        // 调用
        MesStartGateResult result = gateChecker.inspect(task, null);

        // 断言：一次列全两项，车间一趟就能把该做的都做完
        assertFalse(result.isPassed());
        assertEquals(2, result.getItems().stream().filter(item -> !item.isPassed()).count());
        assertTrue(failureOf(result, MesStartGateNames.PREV_DONE).contains("第10道"));
        assertTrue(failureOf(result, MesStartGateNames.KIT_OR_ISSUED).contains("MAT-" + MAT_A));
        // 五项都有记录，不管通过、失败还是跳过
        assertEquals(5, result.getItems().size());
        assertTrue(result.firstFailureMessage().contains("第10道"));
    }

    @Test
    void testInspect_recordsSkippedItems() {
        // 准备：首道工序、无资源、不要求首件——五项里有三项本来就不适用
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);

        // 调用
        MesStartGateResult result = gateChecker.inspect(task, null);

        // 断言：跳过的项记为 skipped 而不是 passed。显示"通过"会让车间以为这项校验过了
        assertTrue(result.isPassed());
        assertTrue(itemOf(result, MesStartGateNames.PREV_DONE).isSkipped());
        assertTrue(itemOf(result, MesStartGateNames.FIRST_ARTICLE).isSkipped());
        assertTrue(itemOf(result, MesStartGateNames.RESOURCE_AVAILABLE).isSkipped());
        // 挂了路线工序的两项照样跑，只是没有要求可拦
        assertFalse(itemOf(result, MesStartGateNames.KIT_OR_ISSUED).isSkipped());
        assertFalse(itemOf(result, MesStartGateNames.SKILL_AND_TOOL).isSkipped());
    }

    @Test
    void testInspect_itemsOrderedByOrder() {
        // 准备
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);

        // 调用
        MesStartGateResult result = gateChecker.inspect(task, null);

        // 断言：结果顺序与 04 文档的校验链流程图一致，前端照着渲染就是文档里那五步
        assertEquals(List.of(MesStartGateNames.PREV_DONE, MesStartGateNames.KIT_OR_ISSUED,
                        MesStartGateNames.FIRST_ARTICLE, MesStartGateNames.RESOURCE_AVAILABLE,
                        MesStartGateNames.SKILL_AND_TOOL),
                result.getItems().stream().map(MesStartGateResult.Item::getName).toList());
    }

    @Test
    void testPrepare_operatorTakenFromDispatchTask() {
        // 准备：工序要求技能，操作者没在入参里给，但派工任务上指到了人
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);
        insertDispatch(task, DISPATCH_OPERATOR_ID, MesDispatchTaskStatusEnum.ACCEPTED);
        givenSkillReq();

        // 调用
        MesStartGateResult result = gateChecker.inspect(task, null);

        // 断言：从派工取到了人，所以报的是"这个人技能不够"而不是"没指定作业人员"
        String failure = failureOf(result, MesStartGateNames.SKILL_AND_TOOL);
        assertFalse(failure.contains("未指定作业人员"));
    }

    @Test
    void testPrepare_cancelledDispatchIgnored() {
        // 准备：派工被取消了
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);
        insertDispatch(task, DISPATCH_OPERATOR_ID, MesDispatchTaskStatusEnum.CANCELLED);
        givenSkillReq();

        // 调用
        MesStartGateResult result = gateChecker.inspect(task, null);

        // 断言：取消的派工不算派了人。按它取到人就等于让一个已经不干这活的人的资质放行
        assertTrue(failureOf(result, MesStartGateNames.SKILL_AND_TOOL).contains("未指定作业人员"));
    }

    @Test
    void testPrepare_paramOperatorWinsOverDispatch() {
        // 准备：派工指到甲，入参给了乙（现场换人）
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);
        insertDispatch(task, DISPATCH_OPERATOR_ID, MesDispatchTaskStatusEnum.ACCEPTED);
        givenSkillReq();
        MesStartGateContext ctx = new MesStartGateContext();
        ctx.setOperatorId(OPERATOR_ID);

        // 调用
        gateChecker.inspect(task, ctx);

        // 断言：以入参为准，上下文里留下的是乙
        assertEquals(OPERATOR_ID, ctx.getOperatorId());
    }

    @Test
    void testPrepare_routeOpLoadedFromApi() {
        // 准备：上下文没带路线工序，由编排者去 mdm 加载
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);
        when(routeApi.getRouteOpMap(any())).thenReturn(Map.of(ROUTE_OP_ID, routeOp(true)));
        MesStartGateContext ctx = new MesStartGateContext();

        // 调用
        gateChecker.inspect(task, ctx);

        // 断言：加载一次供各项复用，fai_flag 与 op_id 都能读到
        assertTrue(ctx.faiRequired());
        assertEquals(OP_ID, ctx.opId());
    }

    @Test
    void testPrepare_missingRouteOpDegradesToSkip() {
        // 准备：路线工序被删，mdm 查不到
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);
        when(routeApi.getRouteOpMap(any())).thenReturn(Map.of());

        // 调用
        MesStartGateResult result = gateChecker.inspect(task, null);

        // 断言：主数据缺失时退化成"不拦"而不是"全拦"。门禁的职责是拦住确定不满足的情况，
        // 不是拦住查不到数据的情况——否则一条脏数据能让整个车间停工
        assertTrue(result.isPassed());
        assertTrue(itemOf(result, MesStartGateNames.FIRST_ARTICLE).isSkipped());
    }

    private MesStartGateResult.Item itemOf(MesStartGateResult result, String name) {
        return result.getItems().stream().filter(item -> name.equals(item.getName()))
                .findFirst().orElseThrow();
    }

    private String failureOf(MesStartGateResult result, String name) {
        return itemOf(result, name).getMessage();
    }

    private RouteOpRespDTO routeOp(boolean faiFlag) {
        RouteOpRespDTO routeOp = new RouteOpRespDTO();
        routeOp.setId(ROUTE_OP_ID);
        routeOp.setOpId(OP_ID);
        routeOp.setSeq(10);
        routeOp.setFaiFlag(faiFlag);
        return routeOp;
    }

    private MaterialRespDTO material(Long matId) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(matId);
        material.setCode("MAT-" + matId);
        material.setName("物料" + matId);
        material.setBaseUomId(1L);
        return material;
    }

    /** 让工序有一种开工前必发的料，且完全没发。 */
    private void givenMustIssueMat() {
        RouteOpMatRespDTO opMat = new RouteOpMatRespDTO();
        opMat.setId(401L);
        opMat.setRouteOpId(ROUTE_OP_ID);
        opMat.setMatId(MAT_A);
        opMat.setQty(BigDecimal.ONE);
        opMat.setScrapRate(BigDecimal.ZERO);
        opMat.setIssueMethod(MesIssueMethodEnum.PICK.name());
        opMat.setMustIssueBeforeStart(Boolean.TRUE);
        opMat.setUomId(1L);
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of(opMat));
    }

    /** 让工序有一条技能要求，且操作者没有任何技能。 */
    private void givenSkillReq() {
        OpSkillReqRespDTO req = new OpSkillReqRespDTO();
        req.setId(1L);
        req.setOpId(OP_ID);
        req.setSkillId(301L);
        req.setMinLevel(3);
        req.setStatus(0);
        when(skillApi.getOpSkillReqList(any())).thenReturn(List.of(req));
    }

    private void insertDispatch(OpTaskDO task, Long operatorId, MesDispatchTaskStatusEnum status) {
        DispatchTaskDO dispatch = new DispatchTaskDO();
        dispatch.setDispatchNo("DP-" + System.nanoTime());
        dispatch.setOpTaskId(task.getId());
        dispatch.setOperatorId(operatorId);
        dispatch.setTeamId(MesConstants.NONE_ID);
        dispatch.setResId(MesConstants.NONE_ID);
        dispatch.setShiftId(MesConstants.NONE_ID);
        dispatch.setStatus(status.name());
        dispatchTaskMapper.insert(dispatch);
    }

    private OpTaskDO insertTask(Long prevTaskId, MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + System.nanoTime())
                .workOrderId(9001L)
                .workOrderNo("WO-0001")
                .routeOpId(ROUTE_OP_ID)
                .opId(OP_ID)
                .opCode("OP010")
                .opName("下料")
                .seq(10)
                .prevTaskId(prevTaskId)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

}


