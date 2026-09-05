package com.iboot.pf.module.mes.service.optask.gate.impl;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.skill.SkillApi;
import com.iboot.pf.module.mdm.api.skill.dto.MemberSkillRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.OpSkillReqRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.SkillRespDTO;
import com.iboot.pf.module.mdm.api.tool.ToolApi;
import com.iboot.pf.module.mdm.api.tool.dto.RouteOpToolRespDTO;
import com.iboot.pf.module.mdm.api.tool.dto.ToolRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_SKILL_NOT_MATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_TOOL_NOT_READY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 门禁五技能与工装的测试。
 *
 * <p>技能有效性由 MES 侧判定（{@code getMemberSkillList} 是通用批量取数，不过滤状态与有效期），
 * 所以停用、未生效、已过期三种无效情形各有独立用例——漏掉任一种就等于让过期资质放行。
 *
 * <p>另一组关键用例是"有技能要求但没有操作者时拦住"：放过去等于把资质校验变成
 * "填了就查、不填就免检"，那还不如不做这一关。
 */
@Import(SkillAndToolChecker.class)
class SkillAndToolCheckerTest extends BaseDbUnitTest {

    private static final Long OP_ID = 201L;
    private static final Long ROUTE_OP_ID = 101L;
    private static final Long SKILL_WELD = 301L;
    private static final Long TOOL_ID = 401L;
    private static final Long OPERATOR_ID = 601L;
    private static final LocalDate CHECK_DATE = LocalDate.of(2026, 9, 5);

    @Resource
    private SkillAndToolChecker checker;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private SkillApi skillApi;
    @MockitoBean
    private ToolApi toolApi;

    @BeforeEach
    void mockMdm() {
        when(skillApi.getOpSkillReqList(any())).thenReturn(List.of());
        when(skillApi.getMemberSkillList(any())).thenReturn(List.of());
        when(skillApi.getSkillList()).thenReturn(List.of(skill()));
        when(toolApi.getRouteOpToolList(any())).thenReturn(List.of());
        when(toolApi.getToolList()).thenReturn(List.of());
    }

    @Test
    void testCheck_noRequirementPassed() {
        // 工序既没有技能要求也没有工装要求
        checker.check(insertTask(), ctx(OPERATOR_ID));
    }

    @Test
    void testCheck_operatorMissingRejected() {
        // 准备：工序要求焊接技能 3 级，但派工没指到具体人
        givenSkillReq(3, 0);

        // 调用并断言：无法证明干活的人有资质就等于不满足。
        // 放过去等于把资质校验变成"填了就查、不填就免检"
        assertServiceException(() -> checker.check(insertTask(), ctx(MesConstants.NONE_ID)),
                OP_TASK_SKILL_NOT_MATCH, "下料", "未指定作业人员，无法校验资质");
    }

    @Test
    void testCheck_operatorWithoutSkillRejected() {
        // 准备：操作者压根没有这项技能
        givenSkillReq(3, 0);

        // 调用并断言：消息给技能名称、要求等级与实际情况，车间知道是该换人还是去补资质
        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_SKILL_NOT_MATCH, "下料", "焊接 要求等级 3，实际 无此技能");
    }

    @Test
    void testCheck_levelTooLowRejected() {
        // 准备：操作者有这项技能但只有 2 级
        givenSkillReq(3, 0);
        givenMemberSkill(2, 0, CHECK_DATE.minusDays(30), null);

        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_SKILL_NOT_MATCH, "下料", "焊接 要求等级 3，实际 2");
    }

    @Test
    void testCheck_levelMeetsPassed() {
        // 准备：等级刚好达到要求
        givenSkillReq(3, 0);
        givenMemberSkill(3, 0, CHECK_DATE.minusDays(30), CHECK_DATE.plusDays(30));

        checker.check(insertTask(), ctx(OPERATOR_ID));
    }

    @Test
    void testCheck_expiredSkillRejected() {
        // 准备：资质昨天过期
        givenSkillReq(3, 0);
        givenMemberSkill(5, 0, CHECK_DATE.minusDays(300), CHECK_DATE.minusDays(1));

        // 调用并断言：过期资质等于没资质。门面不过滤有效期，这一步只能 MES 自己做
        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_SKILL_NOT_MATCH, "下料", "焊接 要求等级 3，实际 无此技能");
    }

    @Test
    void testCheck_notYetEffectiveSkillRejected() {
        // 准备：资质明天才生效
        givenSkillReq(3, 0);
        givenMemberSkill(5, 0, CHECK_DATE.plusDays(1), null);

        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_SKILL_NOT_MATCH, "下料", "焊接 要求等级 3，实际 无此技能");
    }

    @Test
    void testCheck_disabledSkillRecordRejected() {
        // 准备：资质记录被停用（比如考核不合格被吊销）
        givenSkillReq(3, 0);
        givenMemberSkill(5, 1, CHECK_DATE.minusDays(30), null);

        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_SKILL_NOT_MATCH, "下料", "焊接 要求等级 3，实际 无此技能");
    }

    @Test
    void testCheck_highestLevelAmongDuplicatesUsed() {
        // 准备：同一技能有升级前后两条记录，都有效
        givenSkillReq(3, 0);
        when(skillApi.getMemberSkillList(any())).thenReturn(List.of(
                memberSkill(2, 0, CHECK_DATE.minusDays(300), null),
                memberSkill(4, 0, CHECK_DATE.minusDays(30), null)));

        // 调用：取最高等级。按查询顺序取会随机拿到升级前的旧等级，把有资质的人挡在外面
        checker.check(insertTask(), ctx(OPERATOR_ID));
    }

    @Test
    void testCheck_disabledSkillRequirementIgnored() {
        // 准备：技能要求本身被停用（主数据调整后作废的要求）
        givenSkillReq(9, 1);

        checker.check(insertTask(), ctx(OPERATOR_ID));
    }

    @Test
    void testCheck_requiredToolDisabledRejected() {
        // 准备：必备工装被停用
        givenTool(true, 1);

        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_TOOL_NOT_READY, "下料", "TL-401 焊接夹具 已停用");
    }

    @Test
    void testCheck_requiredToolMissingRejected() {
        // 准备：路线上配了必备工装，工装主数据里查不到
        when(toolApi.getRouteOpToolList(any())).thenReturn(List.of(routeOpTool(true)));
        when(toolApi.getToolList()).thenReturn(List.of());

        assertServiceException(() -> checker.check(insertTask(), ctx(OPERATOR_ID)),
                OP_TASK_TOOL_NOT_READY, "下料", TOOL_ID + " 工装不存在");
    }

    @Test
    void testCheck_optionalToolDisabledPassed() {
        // 准备：非必备工装被停用
        givenTool(false, 1);

        // 调用：选配工装不该挡开工，它只是"有更好"
        checker.check(insertTask(), ctx(OPERATOR_ID));
    }

    @Test
    void testCheck_requiredToolEnabledPassed() {
        // 准备：必备工装启用
        givenTool(true, 0);

        checker.check(insertTask(), ctx(OPERATOR_ID));
    }

    private MesStartGateContext ctx(Long operatorId) {
        RouteOpRespDTO routeOp = new RouteOpRespDTO();
        routeOp.setId(ROUTE_OP_ID);
        routeOp.setOpId(OP_ID);
        MesStartGateContext ctx = new MesStartGateContext();
        ctx.setRouteOp(routeOp);
        ctx.setOperatorId(operatorId);
        ctx.setCheckDate(CHECK_DATE);
        return ctx;
    }

    private void givenSkillReq(int minLevel, int status) {
        OpSkillReqRespDTO req = new OpSkillReqRespDTO();
        req.setId(1L);
        req.setOpId(OP_ID);
        req.setSkillId(SKILL_WELD);
        req.setMinLevel(minLevel);
        req.setStatus(status);
        when(skillApi.getOpSkillReqList(any())).thenReturn(List.of(req));
    }

    private void givenMemberSkill(int level, int status, LocalDate effectiveDate, LocalDate expiryDate) {
        when(skillApi.getMemberSkillList(any()))
                .thenReturn(List.of(memberSkill(level, status, effectiveDate, expiryDate)));
    }

    private MemberSkillRespDTO memberSkill(int level, int status, LocalDate effectiveDate,
                                           LocalDate expiryDate) {
        MemberSkillRespDTO skill = new MemberSkillRespDTO();
        skill.setId(System.nanoTime());
        skill.setUserId(OPERATOR_ID);
        skill.setSkillId(SKILL_WELD);
        skill.setLevel(level);
        skill.setStatus(status);
        skill.setEffectiveDate(effectiveDate);
        skill.setExpiryDate(expiryDate);
        return skill;
    }

    private SkillRespDTO skill() {
        SkillRespDTO skill = new SkillRespDTO();
        skill.setId(SKILL_WELD);
        skill.setCode("SK-WELD");
        skill.setName("焊接");
        skill.setStatus(0);
        return skill;
    }

    private void givenTool(boolean requiredFlag, int status) {
        when(toolApi.getRouteOpToolList(any())).thenReturn(List.of(routeOpTool(requiredFlag)));
        ToolRespDTO tool = new ToolRespDTO();
        tool.setId(TOOL_ID);
        tool.setCode("TL-" + TOOL_ID);
        tool.setName("焊接夹具");
        tool.setStatus(status);
        when(toolApi.getToolList()).thenReturn(List.of(tool));
    }

    private RouteOpToolRespDTO routeOpTool(boolean requiredFlag) {
        RouteOpToolRespDTO opTool = new RouteOpToolRespDTO();
        opTool.setId(1L);
        opTool.setRouteOpId(ROUTE_OP_ID);
        opTool.setToolId(TOOL_ID);
        opTool.setQty(1);
        opTool.setRequiredFlag(requiredFlag);
        return opTool;
    }

    private OpTaskDO insertTask() {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-0001")
                .workOrderId(9001L)
                .workOrderNo("WO-0001")
                .routeOpId(ROUTE_OP_ID)
                .opId(OP_ID)
                .opCode("OP010")
                .opName("下料")
                .seq(10)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .status(MesOpTaskStatusEnum.READY.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

}


