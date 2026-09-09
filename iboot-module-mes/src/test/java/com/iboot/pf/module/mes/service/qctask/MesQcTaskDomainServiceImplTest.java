package com.iboot.pf.module.mes.service.qctask;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import com.iboot.pf.module.mes.service.qcrule.QcTriggerService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QUALITY_QTY_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 质检任务生成与抽样量计算的领域测试。
 *
 * <p>覆盖任务书 T2-02 的五个场景：四种抽样方式各 1 个（PERCENT 用 qty=105、sample_value=10
 * 断言 sample_qty=11）、方案版本已锁、检验项空行数量正确、未命中规则返回空、幂等重复调用
 * 不产生第二个任务。
 *
 * <p>T2-04 追加：四个生命周期动作各 1 个正常 + 1 个非法状态、数量校验失败、动作幂等。
 * 生命周期用例直接插桩任务，不关心建任务链路；{@link QcTriggerService} 是 T2-01 的产出，
 * 这里只 mock 契约不引真实实现，两边可并行开发互不阻塞。
 */
@Import({MesQcTaskDomainServiceImpl.class, MesCommandServiceImpl.class})
class MesQcTaskDomainServiceImplTest extends BaseDbUnitTest {

    private static final Long RULE_ID = 100L;
    private static final Long PLAN_ID = 200L;
    private static final Long ITEM_ID_1 = 301L;
    private static final Long ITEM_ID_2 = 302L;
    private static final String BIZ_NO = "ARR20260910001";
    private static final String SOURCE_MODULE = "UI";
    private static final Long INSPECTOR_ID = 666L;

    /** 生命周期用例的任务号计数器，同一用例里的任务号互不相同（生产上由编码规则分配）。 */
    private final AtomicInteger taskSeq = new AtomicInteger();

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcPlanMapper qcPlanMapper;
    @Resource
    private QcItemMapper qcItemMapper;

    @MockitoBean
    private QcTriggerService qcTriggerService;
    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        when(codeService.generate(any(), any())).thenReturn("QCT20260910001");
        when(snapshotService.requireMaterial(any())).thenReturn(MesTestFixtures.material());
        when(snapshotService.requireUomCode(any())).thenReturn(MesTestFixtures.uomCode());
        when(snapshotService.lotNoOrNull(any())).thenReturn("LOT-A20260901");
        // 启用规则命中，指向 PLAN_ID 方案
        when(qcTriggerService.resolve(any(), any())).thenReturn(Optional.of(buildRule()));
    }

    // ========== 四种抽样方式 ==========

    @Test
    void testCalcSampleQty_full() {
        insertPlan(MesQcSampleTypeEnum.FULL.name(), null, null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        assertEquals(0, task.getSampleQty().compareTo(new BigDecimal("20")));
    }

    @Test
    void testCalcSampleQty_fixedQty() {
        // 抽样值 5 小于送检 20，取 5
        insertPlan(MesQcSampleTypeEnum.FIXED_QTY.name(), new BigDecimal("5"), null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        assertEquals(0, task.getSampleQty().compareTo(new BigDecimal("5")));
    }

    @Test
    void testCalcSampleQty_fixedQtyExceedQty() {
        // 抽样值 50 大于送检 20，取送检数量 20
        insertPlan(MesQcSampleTypeEnum.FIXED_QTY.name(), new BigDecimal("50"), null);

        Optional<Long> taskId = qcTaskDomainService.createQcTask(buildBO(new BigDecimal("20")));

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        assertEquals(0, task.getSampleQty().compareTo(new BigDecimal("20")));
    }

    @Test
    void testCalcSampleQty_percent() {
        // 任务书指定断言：qty=105、sample_value=10 时 sample_qty 向上取整为 11
        insertPlan(MesQcSampleTypeEnum.PERCENT.name(), new BigDecimal("10"), null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("105"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        assertEquals(0, task.getSampleQty().compareTo(new BigDecimal("11")));
    }

    @Test
    void testCalcSampleQty_aql() {
        // AQL 本轮简化：acceptance_rule 配置固定值 8，取 8
        insertPlan(MesQcSampleTypeEnum.AQL.name(), null, "8");
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        assertEquals(0, task.getSampleQty().compareTo(new BigDecimal("8")));
    }

    @Test
    void testCalcSampleQty_aqlNotConfiguredDegradeToFull() {
        // 未配置 acceptance_rule 退化为全检
        insertPlan(MesQcSampleTypeEnum.AQL.name(), null, null);

        Optional<Long> taskId = qcTaskDomainService.createQcTask(buildBO(new BigDecimal("20")));

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        assertEquals(0, task.getSampleQty().compareTo(new BigDecimal("20")));
    }

    // ========== 建任务语义 ==========

    @Test
    void testCreateQcTask_planVersionLocked() {
        insertPlan(MesQcSampleTypeEnum.FULL.name(), null, null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        // 方案版本在建任务时锁定，此后方案升版不影响本任务的判定口径
        assertEquals("V1.0", task.getQcPlanVersion());
        assertEquals(PLAN_ID, task.getQcPlanId());
        assertEquals(RULE_ID, task.getRuleId());
        assertEquals(MesQcTriggerPointEnum.IQC.name(), task.getTriggerPoint());
        assertEquals(BIZ_NO, task.getBizNo());
        assertEquals(MesQcTaskStatusEnum.CREATED.name(), task.getStatus());
        // 幂等键按任务书格式落库
        assertEquals("MES:" + BIZ_NO + ":QC_CREATE:" + MesQcTriggerPointEnum.IQC.name(),
                task.getIdempotencyKey());
        // 物料与批次快照
        assertEquals("MAT-001", task.getMatCode());
        assertEquals("定子铁芯", task.getMatName());
        assertEquals("LOT-A20260901", task.getLotNo());
        assertEquals(MesConstants.NONE_ID, task.getAssignedInspectorId());
    }

    @Test
    void testCreateQcTask_resultRowsExpanded() {
        insertPlan(MesQcSampleTypeEnum.FIXED_QTY.name(), new BigDecimal("3"), null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        // 2 个启用检验项 × 3 个样本 = 6 行空行，停用项不展开
        List<QcResultDO> results = qcResultMapper.selectList(QcResultDO::getTaskId, taskId.orElseThrow());
        assertEquals(6, results.size());
        assertTrue(results.stream().allMatch(r -> r.getActualValue() == null));
        // 快照检验项编码与名称
        assertTrue(results.stream().allMatch(r -> "IT-01".equals(r.getItemCode())
                || "IT-02".equals(r.getItemCode())));
        assertEquals(3, results.stream().filter(r -> "IT-01".equals(r.getItemCode())).count());
    }

    @Test
    void testCreateQcTask_ruleNotHitReturnEmpty() {
        when(qcTriggerService.resolve(any(), any())).thenReturn(Optional.empty());
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        // 未命中规则返回空，业务动作放行，不产生任务
        assertTrue(taskId.isEmpty());
        assertEquals(0, qcTaskMapper.selectCount());
    }

    @Test
    void testCreateQcTask_idempotentSecondCallNoNewTask() {
        insertPlan(MesQcSampleTypeEnum.FULL.name(), null, null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("20"));

        Optional<Long> first = qcTaskDomainService.createQcTask(bo);
        Optional<Long> second = qcTaskDomainService.createQcTask(buildBO(new BigDecimal("20")));

        // 幂等命中返回首次任务编号，库里只有一条
        assertEquals(first, second);
        assertEquals(1, qcTaskMapper.selectCount());
    }

    @Test
    void testCreateQcTask_oversizedRowsDegenerateToOneSample() {
        // 251 个样本 × 2 项 = 502 行 > 500 上限，降级为每项 1 个样本
        insertPlan(MesQcSampleTypeEnum.FULL.name(), null, null);
        MesCreateQcTaskBO bo = buildBO(new BigDecimal("251"));

        Optional<Long> taskId = qcTaskDomainService.createQcTask(bo);

        QcTaskDO task = qcTaskMapper.selectById(taskId.orElseThrow());
        List<QcResultDO> results = qcResultMapper.selectList(QcResultDO::getTaskId, taskId.orElseThrow());
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getSampleNo() == 1));
        // 降级必须在任务备注里标注
        assertTrue(task.getRemark().contains("仅预生成第 1 个样本"));
    }

    // ========== 生命周期动作（T2-04） ==========

    @Test
    void testAssignQcTask_success() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.CREATED.name());

        qcTaskDomainService.assignQcTask(actionBO(task));

        QcTaskDO assigned = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.ASSIGNED.name(), assigned.getStatus());
        assertEquals(INSPECTOR_ID, assigned.getAssignedInspectorId());
        // 乐观锁版本自增，证明走的是带 version 条件的更新
        assertEquals(MesConstants.INIT_VERSION + 1, assigned.getVersion());
        // 指派不动其他时间字段
        assertNull(assigned.getInspectStartTime());
    }

    @Test
    void testAssignQcTask_illegalStatus() {
        // 已开检的任务没有 assign 出边，重复指派被状态机拒掉
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name());

        assertServiceException(() -> qcTaskDomainService.assignQcTask(actionBO(task)),
                QC_TASK_STATUS_INVALID);
        // 状态与检验员都没被改
        QcTaskDO unchanged = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(), unchanged.getStatus());
        assertEquals(MesConstants.NONE_ID, unchanged.getAssignedInspectorId());
    }

    @Test
    void testStartInspect_success() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.ASSIGNED.name());

        qcTaskDomainService.startInspect(actionBO(task));

        QcTaskDO inspecting = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(), inspecting.getStatus());
        assertNotNull(inspecting.getInspectStartTime());
    }

    @Test
    void testStartInspect_illegalStatus() {
        // 未指派不能直接开检，必须先经 assign
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.CREATED.name());

        assertServiceException(() -> qcTaskDomainService.startInspect(actionBO(task)),
                QC_TASK_STATUS_INVALID);
        assertNull(qcTaskMapper.selectById(task.getId()).getInspectStartTime());
    }

    @Test
    void testSubmitConclusion_success() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.RESULT_POSTING.name());

        qcTaskDomainService.submitConclusion(conclusionBO(task, MesQcConclusionEnum.QUALIFIED.name(),
                new BigDecimal("5"), new BigDecimal("1")));

        QcTaskDO done = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.DONE.name(), done.getStatus());
        assertEquals(MesQcConclusionEnum.QUALIFIED.name(), done.getConclusion());
        assertEquals(0, done.getQtyQualified().compareTo(new BigDecimal("5")));
        assertEquals(0, done.getQtyRejected().compareTo(new BigDecimal("1")));
        assertNotNull(done.getInspectEndTime());
    }

    @Test
    void testSubmitConclusion_illegalStatus() {
        // 结果未录完不能提交结论，INSPECTING 没有 submitConclusion 出边
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name());

        assertServiceException(() -> qcTaskDomainService.submitConclusion(
                        conclusionBO(task, MesQcConclusionEnum.QUALIFIED.name(),
                                new BigDecimal("5"), new BigDecimal("1"))),
                QC_TASK_STATUS_INVALID);
        assertNull(qcTaskMapper.selectById(task.getId()).getConclusion());
    }

    @Test
    void testSubmitConclusion_qtyMismatchRejected() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.RESULT_POSTING.name());

        // 合格 5 + 不合格 2 = 7 != 抽样数 6，数量对不上必须先于状态推进被拦
        assertServiceException(() -> qcTaskDomainService.submitConclusion(
                        conclusionBO(task, MesQcConclusionEnum.QUALIFIED.name(),
                                new BigDecimal("5"), new BigDecimal("2"))),
                QUALITY_QTY_INVALID);
        // 数量校验不过时一行都不许改：DONE 是终态，落了就回不去
        QcTaskDO unchanged = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.RESULT_POSTING.name(), unchanged.getStatus());
        assertNull(unchanged.getConclusion());
        assertNull(unchanged.getInspectEndTime());
    }

    @Test
    void testCancelQcTask_success() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.CREATED.name());

        qcTaskDomainService.cancelQcTask(actionBO(task));

        assertEquals(MesQcTaskStatusEnum.CANCELLED.name(), qcTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testCancelQcTask_illegalStatus() {
        // 检验已开始则产生过检验事实，取消等于销毁记录，被状态机拒掉
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name());

        assertServiceException(() -> qcTaskDomainService.cancelQcTask(actionBO(task)),
                QC_TASK_STATUS_INVALID);
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(), qcTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testLifecycleAction_idempotent() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.CREATED.name());
        MesQcTaskActionBO bo = actionBO(task);

        // 同键两次：第二次若真执行，ASSIGNED 没有 assign 出边，状态机会直接抛错
        qcTaskDomainService.assignQcTask(bo);
        qcTaskDomainService.assignQcTask(bo);

        // 只推进一次、版本只加 1，命令记录把重复下发拦在了状态校验之前
        QcTaskDO assigned = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.ASSIGNED.name(), assigned.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, assigned.getVersion());
    }

    // ========== 桩数据 ==========

    /** 启用规则桩，指向 PLAN_ID。 */
    private QcRuleDO buildRule() {
        return QcRuleDO.builder()
                .id(RULE_ID)
                .triggerPoint(MesQcTriggerPointEnum.IQC.name())
                .objectType(MesQcObjectTypeEnum.MATERIAL.name())
                .qcPlanId(PLAN_ID)
                .priority(10)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
    }

    /** 已发布方案桩，sampleType 与抽样配置由用例指定。 */
    private void insertPlan(String sampleType, BigDecimal sampleValue, String acceptanceRule) {
        qcPlanMapper.insert(QcPlanDO.builder()
                .id(PLAN_ID)
                .code("QCPLAN-001")
                .name("IQC 常规方案")
                .versionNo("V1.0")
                .sampleType(sampleType)
                .sampleValue(sampleValue)
                .acceptanceRule(acceptanceRule)
                .status(MesQcPlanStatusEnum.PUBLISHED.name())
                .build());
        insertItem(ITEM_ID_1, "IT-01", "外观检查", 1, true);
        insertItem(ITEM_ID_2, "IT-02", "尺寸检查", 2, true);
        insertItem(303L, "IT-03", "停用项", 3, false);
    }

    private void insertItem(Long id, String code, String name, int sortNo, boolean enabled) {
        qcItemMapper.insert(QcItemDO.builder()
                .id(id)
                .planId(PLAN_ID)
                .itemCode(code)
                .itemName(name)
                .itemType("NUMERIC")
                .requiredFlag(true)
                .sortNo(sortNo)
                .status(enabled ? CommonStatusEnum.ENABLE.getStatus() : CommonStatusEnum.DISABLE.getStatus())
                .build());
    }

    /** 构造创建参数：业务上下文按 IQC 到货确认的典型形态。 */
    private MesCreateQcTaskBO buildBO(BigDecimal qty) {
        MesCreateQcTaskBO bo = new MesCreateQcTaskBO();
        bo.setTriggerPoint(MesQcTriggerPointEnum.IQC.name());
        bo.setObjectType(MesQcObjectTypeEnum.MATERIAL.name());
        bo.setBizType(MesBizTypeEnum.ARRIVAL.name());
        bo.setBizNo(BIZ_NO);
        bo.setMatId(1L);
        bo.setBatchId(MesConstants.NONE_ID);
        bo.setQty(qty);
        bo.setUomId(1L);
        bo.setRemark("单测数据");
        return bo;
    }

    // ========== 生命周期桩数据 ==========

    /**
     * 插一条指定状态的任务，字段按 {@code createQcTask} 落库形态给全：动作测试只关心
     * 状态推进不关心建任务链路，但乐观锁条件更新要求 version 非空、数量校验要用 sampleQty。
     */
    private QcTaskDO insertTask(String status) {
        QcTaskDO task = QcTaskDO.builder()
                .taskNo("QCT-" + taskSeq.incrementAndGet())
                .ruleId(RULE_ID)
                .qcPlanId(PLAN_ID)
                .qcPlanVersion("V1.0")
                .triggerPoint(MesQcTriggerPointEnum.IQC.name())
                .bizType(MesBizTypeEnum.ARRIVAL.name())
                .bizNo(BIZ_NO)
                .bizLineId(MesConstants.NONE_ID)
                .matId(1L)
                .matCode("MAT-001")
                .matName("定子铁芯")
                .batchId(MesConstants.NONE_ID)
                .qty(new BigDecimal("20"))
                .sampleQty(new BigDecimal("6"))
                .uomId(1L)
                .uomCode("PCS")
                .assignedInspectorId(MesConstants.NONE_ID)
                .qtyQualified(MesConstants.ZERO_QTY)
                .qtyRejected(MesConstants.ZERO_QTY)
                .status(status)
                .version(MesConstants.INIT_VERSION)
                .build();
        qcTaskMapper.insert(task);
        return task;
    }

    /** 生命周期动作参数：不带幂等键，走服务端按四段格式派生的那条路。 */
    private MesQcTaskActionBO actionBO(QcTaskDO task) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setInspectorId(INSPECTOR_ID);
        return bo;
    }

    /** 提交结论参数：合格 5 + 不合格 1 = 抽样数 6，合法。 */
    private MesQcTaskConclusionBO conclusionBO(QcTaskDO task, String conclusion,
                                               BigDecimal qualified, BigDecimal rejected) {
        MesQcTaskConclusionBO bo = new MesQcTaskConclusionBO();
        bo.setTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setConclusion(conclusion);
        bo.setQtyQualified(qualified);
        bo.setQtyRejected(rejected);
        return bo;
    }

}
