package com.iboot.pf.module.mes.service.optask.gate.impl;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_FAI_NOT_PASSED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 门禁三首件检验通过的测试。
 *
 * <p>最关键的两个用例是"要求首件但还没报工时跳过"与"让步接收不算通过"：
 * 前者防死锁（要开工得先有 FAI，要有 FAI 得先做出首件，做首件得先开工），
 * 后者防门禁替不合格品处置流程放行。
 */
@Import(FirstArticleChecker.class)
class FirstArticleCheckerTest extends BaseDbUnitTest {

    private static final String TASK_NO = "OT-0001";

    @Resource
    private FirstArticleChecker checker;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private ProdReportMapper prodReportMapper;

    @Test
    void testEnabled_faiFlagFalseSkipped() {
        // 不要求首件检验的工序不做这项校验
        OpTaskDO task = insertTask();

        assertFalse(checker.enabled(task, ctx(false)));
    }

    @Test
    void testEnabled_noReportYetSkippedToAvoidDeadlock() {
        // 准备：要求首件检验，但还没有任何报工
        OpTaskDO task = insertTask();

        // 断言：第一次开工必须放行。首件检验的对象是第一件产品，做出第一件必须先开工，
        // 开工就要求 FAI 通过等于死锁
        assertFalse(checker.enabled(task, ctx(true)));
    }

    @Test
    void testEnabled_faiRequiredAndReportedThenChecked() {
        // 准备：要求首件检验且已经报过工，说明首件已产出
        OpTaskDO task = insertTask();
        insertReport(task);

        assertTrue(checker.enabled(task, ctx(true)));
    }

    @Test
    void testCheck_noFaiTaskRejected() {
        // 准备：首件报了工却没有首件检验任务
        OpTaskDO task = insertTask();
        insertReport(task);

        assertServiceException(() -> checker.check(task, ctx(true)),
                OP_TASK_FAI_NOT_PASSED, "下料", "没有首件检验任务");
    }

    @Test
    void testCheck_faiInspectingRejected() {
        // 准备：首件检验任务还在检验中，结论未落定
        OpTaskDO task = insertTask();
        insertReport(task);
        insertQcTask(MesQcTaskStatusEnum.INSPECTING, null);

        // 调用并断言：结论与合格数量只在 DONE 时才落定，只看结论会读到检验中途的中间值。
        // 消息给首件任务号与它当前状态，车间知道该去催检验
        assertServiceException(() -> checker.check(task, ctx(true)),
                OP_TASK_FAI_NOT_PASSED, "下料", "QC-0001 检验中");
    }

    @Test
    void testCheck_faiRejectedConclusionRejected() {
        // 准备：首件检验判了不合格
        OpTaskDO task = insertTask();
        insertReport(task);
        insertQcTask(MesQcTaskStatusEnum.DONE, MesQcConclusionEnum.REJECTED);

        assertServiceException(() -> checker.check(task, ctx(true)),
                OP_TASK_FAI_NOT_PASSED, "下料", "QC-0001 不合格");
    }

    @Test
    void testCheck_concessionNotTreatedAsPassed() {
        // 准备：首件检验判了让步接收
        OpTaskDO task = insertTask();
        insertReport(task);
        insertQcTask(MesQcTaskStatusEnum.DONE, MesQcConclusionEnum.CONCESSION);

        // 调用并断言：让步接收要走不合格品处置单，门禁不替处置流程放行
        assertServiceException(() -> checker.check(task, ctx(true)),
                OP_TASK_FAI_NOT_PASSED, "下料", "QC-0001 让步接收");
    }

    @Test
    void testCheck_faiQualifiedPassed() {
        // 准备：首件检验完成且合格
        OpTaskDO task = insertTask();
        insertReport(task);
        insertQcTask(MesQcTaskStatusEnum.DONE, MesQcConclusionEnum.QUALIFIED);

        checker.check(task, ctx(true));
    }

    @Test
    void testCheck_otherOpTaskFaiNotCounted() {
        // 准备：别的工序任务的首件检验合格了，本工序没有
        OpTaskDO task = insertTask();
        insertReport(task);
        QcTaskDO other = qcTask(MesQcTaskStatusEnum.DONE, MesQcConclusionEnum.QUALIFIED);
        other.setBizNo("OT-9999");
        qcTaskMapper.insert(other);

        // 调用并断言：按业务号匹配，不是"这条产线有过合格首件就放行"
        assertServiceException(() -> checker.check(task, ctx(true)),
                OP_TASK_FAI_NOT_PASSED, "下料", "没有首件检验任务");
    }

    /** 上下文：路线工序的 fai_flag 决定这一项启不启用。 */
    private MesStartGateContext ctx(boolean faiFlag) {
        RouteOpRespDTO routeOp = new RouteOpRespDTO();
        routeOp.setId(101L);
        routeOp.setOpId(201L);
        routeOp.setFaiFlag(faiFlag);
        MesStartGateContext ctx = new MesStartGateContext();
        ctx.setRouteOp(routeOp);
        return ctx;
    }

    private OpTaskDO insertTask() {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo(TASK_NO)
                .workOrderId(9001L)
                .workOrderNo("WO-0001")
                .routeOpId(101L)
                .opId(201L)
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
                .status(MesOpTaskStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

    private void insertReport(OpTaskDO task) {
        prodReportMapper.insert(ProdReportDO.builder()
                .reportNo("PR-0001")
                .prodOrderId(8001L)
                .workOrderId(task.getWorkOrderId())
                .opTaskId(task.getId())
                .workerId(MesConstants.NONE_ID)
                .resId(MesConstants.NONE_ID)
                .shiftId(MesConstants.NONE_ID)
                .qtyGood(BigDecimal.ONE)
                .qtyScrap(MesConstants.ZERO_QTY)
                .qtyRework(MesConstants.ZERO_QTY)
                .reportTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build());
    }

    private void insertQcTask(MesQcTaskStatusEnum status, MesQcConclusionEnum conclusion) {
        qcTaskMapper.insert(qcTask(status, conclusion));
    }

    private QcTaskDO qcTask(MesQcTaskStatusEnum status, MesQcConclusionEnum conclusion) {
        return QcTaskDO.builder()
                .taskNo("QC-0001")
                .ruleId(MesConstants.NONE_ID)
                .qcPlanId(MesConstants.NONE_ID)
                .triggerPoint(MesQcTriggerPointEnum.FAI.name())
                .bizType(MesBizTypeEnum.OP_TASK.name())
                .bizNo(TASK_NO)
                .bizLineId(MesConstants.NONE_ID)
                .matId(1001L)
                .matCode("MAT-1001")
                .qty(BigDecimal.ONE)
                .sampleQty(BigDecimal.ONE)
                .uomId(1L)
                .conclusion(conclusion == null ? null : conclusion.name())
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
    }

}

