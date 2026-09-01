package com.iboot.pf.module.mes.service.qctask;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_HAS_RESULT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_NOT_PUBLISHED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_SAMPLE_QTY_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 质检任务 Service 测试：只能引用已发布方案、方案版本与物料快照锁定、抽样数量不超送检数量、已录结果禁删。
 */
@Import(QcTaskServiceImpl.class)
class QcTaskServiceImplTest extends BaseDbUnitTest {

    private static final Long PUBLISHED_PLAN = 1L;
    private static final Long DRAFT_PLAN = 2L;

    @Resource
    private QcTaskService qcTaskService;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private QcResultMapper qcResultMapper;

    @MockitoBean
    private QcPlanService qcPlanService;
    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "QCT0001" : invocation.getArgument(1));
        when(qcPlanService.validateQcPlanExists(eq(PUBLISHED_PLAN))).thenReturn(new QcPlanDO()
                .setId(PUBLISHED_PLAN).setVersionNo("V1.0").setStatus(MesQcPlanStatusEnum.PUBLISHED.name()));
        when(qcPlanService.validateQcPlanExists(eq(DRAFT_PLAN))).thenReturn(new QcPlanDO()
                .setId(DRAFT_PLAN).setVersionNo("V1.0").setStatus(MesQcPlanStatusEnum.DRAFT.name()));
        when(snapshotService.requireMaterial(any())).thenReturn(MesTestFixtures.material());
        when(snapshotService.requireUomCode(any())).thenReturn(MesTestFixtures.uomCode());
        when(snapshotService.lotNoOrNull(any())).thenReturn(null);
    }

    private QcTaskSaveReqVO buildReqVO(String taskNo) {
        QcTaskSaveReqVO reqVO = new QcTaskSaveReqVO();
        reqVO.setTaskNo(taskNo);
        reqVO.setRuleId(MesConstants.NONE_ID);
        reqVO.setQcPlanId(PUBLISHED_PLAN);
        reqVO.setTriggerPoint(MesQcTriggerPointEnum.IPQC.name());
        reqVO.setBizType(MesBizTypeEnum.OP_TASK.name());
        reqVO.setBizNo("OPT-0001");
        reqVO.setBizLineId(MesConstants.NONE_ID);
        reqVO.setMatId(1L);
        reqVO.setBatchId(MesConstants.NONE_ID);
        reqVO.setQty(new BigDecimal("100"));
        reqVO.setSampleQty(new BigDecimal("10"));
        reqVO.setUomId(1L);
        reqVO.setAssignedInspectorId(MesConstants.NONE_ID);
        reqVO.setStatus(MesQcTaskStatusEnum.CREATED.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateQcTask_planVersionAndSnapshotLocked() {
        Long id = qcTaskService.createQcTask(buildReqVO(null));

        QcTaskDO qcTask = qcTaskMapper.selectById(id);
        assertNotNull(qcTask);
        assertEquals("QCT0001", qcTask.getTaskNo());
        // 方案版本在建单时锁定，方案升版不影响历史任务的判定口径
        assertEquals("V1.0", qcTask.getQcPlanVersion());
        assertEquals("MAT-001", qcTask.getMatCode());
        assertEquals("PCS", qcTask.getUomCode());
        // 合格与不合格数量由检验结果汇总，创建时归零
        assertEquals(0, qcTask.getQtyQualified().compareTo(BigDecimal.ZERO));
        assertEquals(0, qcTask.getQtyRejected().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testCreateQcTask_draftPlanRejected() {
        QcTaskSaveReqVO reqVO = buildReqVO("QCT-A");
        reqVO.setQcPlanId(DRAFT_PLAN);

        assertServiceException(() -> qcTaskService.createQcTask(reqVO), QC_TASK_PLAN_NOT_PUBLISHED);
    }

    @Test
    void testCreateQcTask_sampleQtyExceeded() {
        QcTaskSaveReqVO reqVO = buildReqVO("QCT-A");
        reqVO.setSampleQty(new BigDecimal("101"));

        assertServiceException(() -> qcTaskService.createQcTask(reqVO), QC_TASK_SAMPLE_QTY_INVALID);
    }

    @Test
    void testCreateQcTask_duplicateIdempotencyKeyRejected() {
        QcTaskSaveReqVO first = buildReqVO("QCT-A");
        first.setIdempotencyKey("OPT-0001:IPQC");
        qcTaskService.createQcTask(first);

        QcTaskSaveReqVO second = buildReqVO("QCT-B");
        second.setIdempotencyKey("OPT-0001:IPQC");
        assertServiceException(() -> qcTaskService.createQcTask(second), IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Test
    void testUpdateQcTask_keepTaskNoAndSummaryQty() {
        Long id = qcTaskService.createQcTask(buildReqVO("QCT-A"));

        QcTaskSaveReqVO updateReqVO = buildReqVO("QCT-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setStatus(MesQcTaskStatusEnum.ASSIGNED.name());
        qcTaskService.updateQcTask(updateReqVO);

        QcTaskDO qcTask = qcTaskMapper.selectById(id);
        assertEquals("QCT-A", qcTask.getTaskNo());
        assertEquals(MesQcTaskStatusEnum.ASSIGNED.name(), qcTask.getStatus());
        assertEquals(0, qcTask.getQtyQualified().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testUpdateQcTask_doneRejected() {
        QcTaskSaveReqVO createReqVO = buildReqVO("QCT-A");
        createReqVO.setStatus(MesQcTaskStatusEnum.DONE.name());
        Long id = qcTaskService.createQcTask(createReqVO);

        QcTaskSaveReqVO updateReqVO = buildReqVO("QCT-A");
        updateReqVO.setId(id);
        assertServiceException(() -> qcTaskService.updateQcTask(updateReqVO), QC_TASK_STATUS_INVALID);
    }

    @Test
    void testUpdateQcTask_notExists() {
        QcTaskSaveReqVO updateReqVO = buildReqVO("QCT-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> qcTaskService.updateQcTask(updateReqVO), QC_TASK_NOT_EXISTS);
    }

    @Test
    void testDeleteQcTask_success() {
        Long id = qcTaskService.createQcTask(buildReqVO("QCT-A"));

        qcTaskService.deleteQcTask(id);

        assertNull(qcTaskMapper.selectById(id));
    }

    @Test
    void testDeleteQcTask_hasResultRejected() {
        Long id = qcTaskService.createQcTask(buildReqVO("QCT-A"));
        qcResultMapper.insert(new QcResultDO().setTaskId(id).setItemId(1L).setSampleNo(1));

        assertServiceException(() -> qcTaskService.deleteQcTask(id), QC_TASK_HAS_RESULT);
    }

    @Test
    void testDeleteQcTaskList_hasResultRejected() {
        Long first = qcTaskService.createQcTask(buildReqVO("QCT-A"));
        Long second = qcTaskService.createQcTask(buildReqVO("QCT-B"));
        qcResultMapper.insert(new QcResultDO().setTaskId(second).setItemId(1L).setSampleNo(1));

        assertServiceException(() -> qcTaskService.deleteQcTaskListByIds(List.of(first, second)),
                QC_TASK_HAS_RESULT);
        assertNotNull(qcTaskMapper.selectById(first));
    }

    @Test
    void testGetQcTaskPage() {
        qcTaskService.createQcTask(buildReqVO("QCT-A"));

        QcTaskPageReqVO reqVO = new QcTaskPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<QcTaskRespVO> list = qcTaskService.getQcTaskPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("QCT-A", list.get(0).getTaskNo());
    }

}
