package com.iboot.pf.module.mes.service.qcresult;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_CONCLUSION_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_HAS_DEFECT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_SAMPLE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_TASK_DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 检验结果 Service 测试：任务完成后禁改、检验项快照带出、结论与合格标记一致、样本序号唯一、已挂缺陷禁删。
 */
@Import(QcResultServiceImpl.class)
class QcResultServiceImplTest extends BaseDbUnitTest {

    private static final Long RUNNING_TASK = 1L;
    private static final Long DONE_TASK = 2L;

    @Resource
    private QcResultService qcResultService;
    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private DefectMapper defectMapper;

    @MockitoBean
    private QcTaskService qcTaskService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long itemId;

    @BeforeEach
    void mockExternalDependencies() {
        when(qcTaskService.validateQcTaskExists(eq(RUNNING_TASK))).thenReturn(new QcTaskDO()
                .setId(RUNNING_TASK).setStatus(MesQcTaskStatusEnum.INSPECTING.name()));
        when(qcTaskService.validateQcTaskExists(eq(DONE_TASK))).thenReturn(new QcTaskDO()
                .setId(DONE_TASK).setStatus(MesQcTaskStatusEnum.DONE.name()));
        QcItemDO qcItem = new QcItemDO().setPlanId(1L).setItemCode("IT-01").setItemName("外观检查");
        qcItemMapper.insert(qcItem);
        itemId = qcItem.getId();
    }

    private QcResultSaveReqVO buildReqVO(Long taskId, Integer sampleNo) {
        QcResultSaveReqVO reqVO = new QcResultSaveReqVO();
        reqVO.setTaskId(taskId);
        reqVO.setItemId(itemId);
        reqVO.setSampleNo(sampleNo);
        reqVO.setActualValue("合格");
        reqVO.setPassFlag(Boolean.TRUE);
        reqVO.setInspectorId(1L);
        reqVO.setQtyQualified(new BigDecimal("1"));
        reqVO.setQtyRejected(BigDecimal.ZERO);
        reqVO.setConclusion(MesQcItemConclusionEnum.PASS.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateQcResult_itemSnapshotFilled() {
        Long id = qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));

        QcResultDO qcResult = qcResultMapper.selectById(id);
        assertNotNull(qcResult);
        // 检验项编码与名称是快照，由后端从检验项目带出
        assertEquals("IT-01", qcResult.getItemCode());
        assertEquals("外观检查", qcResult.getItemName());
        // 检验时间留空取当前时间，避免流水没有时间戳
        assertNotNull(qcResult.getInspectTime());
    }

    @Test
    void testCreateQcResult_taskDoneRejected() {
        assertServiceException(() -> qcResultService.createQcResult(buildReqVO(DONE_TASK, 1)), QC_RESULT_TASK_DONE);
    }

    @Test
    void testCreateQcResult_itemNotExists() {
        QcResultSaveReqVO reqVO = buildReqVO(RUNNING_TASK, 1);
        reqVO.setItemId(randomLongId());

        assertServiceException(() -> qcResultService.createQcResult(reqVO), QC_ITEM_NOT_EXISTS);
    }

    @Test
    void testCreateQcResult_conclusionConflict() {
        QcResultSaveReqVO reqVO = buildReqVO(RUNNING_TASK, 1);
        reqVO.setPassFlag(Boolean.FALSE);
        reqVO.setConclusion(MesQcItemConclusionEnum.PASS.name());

        // 结论与合格标记互相矛盾时报表统计口径就乱了，必须拦住
        assertServiceException(() -> qcResultService.createQcResult(reqVO), QC_RESULT_CONCLUSION_CONFLICT);
    }

    @Test
    void testCreateQcResult_duplicateSampleRejected() {
        qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));

        assertServiceException(() -> qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1)),
                QC_RESULT_SAMPLE_DUPLICATE);
    }

    @Test
    void testCreateQcResult_differentSampleAccepted() {
        qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));

        assertNotNull(qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 2)));
    }

    @Test
    void testUpdateQcResult_keepOwnSample() {
        Long id = qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));

        QcResultSaveReqVO updateReqVO = buildReqVO(RUNNING_TASK, 1);
        updateReqVO.setId(id);
        updateReqVO.setActualValue("复检合格");
        qcResultService.updateQcResult(updateReqVO);

        assertEquals("复检合格", qcResultMapper.selectById(id).getActualValue());
    }

    @Test
    void testUpdateQcResult_notExists() {
        QcResultSaveReqVO updateReqVO = buildReqVO(RUNNING_TASK, 1);
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> qcResultService.updateQcResult(updateReqVO), QC_RESULT_NOT_EXISTS);
    }

    @Test
    void testDeleteQcResult_success() {
        Long id = qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));

        qcResultService.deleteQcResult(id);

        assertNull(qcResultMapper.selectById(id));
    }

    @Test
    void testDeleteQcResult_hasDefectRejected() {
        Long id = qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));
        defectMapper.insert(new DefectDO().setTaskId(RUNNING_TASK).setResultId(id).setDefectCode("DEF-001"));

        assertServiceException(() -> qcResultService.deleteQcResult(id), QC_RESULT_HAS_DEFECT);
    }

    @Test
    void testDeleteQcResultList_hasDefectRejected() {
        Long first = qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));
        Long second = qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 2));
        defectMapper.insert(new DefectDO().setTaskId(RUNNING_TASK).setResultId(second).setDefectCode("DEF-001"));

        assertServiceException(() -> qcResultService.deleteQcResultListByIds(List.of(first, second)),
                QC_RESULT_HAS_DEFECT);
        assertNotNull(qcResultMapper.selectById(first));
    }

    @Test
    void testGetQcResultPage() {
        qcResultService.createQcResult(buildReqVO(RUNNING_TASK, 1));

        QcResultPageReqVO reqVO = new QcResultPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<QcResultRespVO> list = qcResultService.getQcResultPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("IT-01", list.get(0).getItemCode());
    }

}
