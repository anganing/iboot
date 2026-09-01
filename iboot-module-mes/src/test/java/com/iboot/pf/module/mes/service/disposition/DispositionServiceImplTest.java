package com.iboot.pf.module.mes.service.disposition;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionPageReqVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_APPROVER_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 不合格品处置单 Service 测试：处置数量不超任务不合格数、审批结论需审批人、只有新建允许删、幂等键唯一。
 */
@Import(DispositionServiceImpl.class)
class DispositionServiceImplTest extends BaseDbUnitTest {

    private static final Long TASK_ID = 1L;

    @Resource
    private DispositionService dispositionService;
    @Resource
    private DispositionMapper dispositionMapper;

    @MockitoBean
    private QcTaskService qcTaskService;
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
                        ? "DSP0001" : invocation.getArgument(1));
        // 处置数量的上限来自质检任务的不合格数量
        when(qcTaskService.validateQcTaskExists(any())).thenReturn(new QcTaskDO()
                .setId(TASK_ID)
                .setStatus(MesQcTaskStatusEnum.DONE.name())
                .setQtyRejected(new BigDecimal("10")));
    }

    private DispositionSaveReqVO buildReqVO(String dispositionNo) {
        DispositionSaveReqVO reqVO = new DispositionSaveReqVO();
        reqVO.setDispositionNo(dispositionNo);
        reqVO.setTaskId(TASK_ID);
        reqVO.setDispositionType(MesDispositionTypeEnum.REWORK.name());
        reqVO.setQty(new BigDecimal("5"));
        reqVO.setApprovedBy(MesConstants.NONE_ID);
        reqVO.setStatus(MesDispositionStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateDisposition_blankNoFilledByRule() {
        Long id = dispositionService.createDisposition(buildReqVO(null));

        DispositionDO disposition = dispositionMapper.selectById(id);
        assertNotNull(disposition);
        assertEquals("DSP0001", disposition.getDispositionNo());
        assertEquals(MesConstants.NONE_ID, disposition.getApprovedBy());
    }

    @Test
    void testCreateDisposition_qtyExceeded() {
        DispositionSaveReqVO reqVO = buildReqVO("DSP-A");
        reqVO.setQty(new BigDecimal("11"));

        assertServiceException(() -> dispositionService.createDisposition(reqVO), DISPOSITION_QTY_EXCEEDED);
    }

    @Test
    void testCreateDisposition_approverRequiredWhenApproved() {
        DispositionSaveReqVO reqVO = buildReqVO("DSP-A");
        reqVO.setStatus(MesDispositionStatusEnum.APPROVED.name());

        // 审批通过或驳回时必须留下审批人，否则事后查不到是谁放的行
        assertServiceException(() -> dispositionService.createDisposition(reqVO), DISPOSITION_APPROVER_REQUIRED);
    }

    @Test
    void testCreateDisposition_approveTimeAutoFilled() {
        DispositionSaveReqVO reqVO = buildReqVO("DSP-A");
        reqVO.setStatus(MesDispositionStatusEnum.APPROVED.name());
        reqVO.setApprovedBy(2L);

        Long id = dispositionService.createDisposition(reqVO);

        assertNotNull(dispositionMapper.selectById(id).getApproveTime());
    }

    @Test
    void testCreateDisposition_duplicateIdempotencyKeyRejected() {
        DispositionSaveReqVO first = buildReqVO("DSP-A");
        first.setIdempotencyKey("QCT-0001:REWORK");
        dispositionService.createDisposition(first);

        DispositionSaveReqVO second = buildReqVO("DSP-B");
        second.setIdempotencyKey("QCT-0001:REWORK");
        assertServiceException(() -> dispositionService.createDisposition(second), IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Test
    void testUpdateDisposition_keepDispositionNo() {
        Long id = dispositionService.createDisposition(buildReqVO("DSP-A"));

        DispositionSaveReqVO updateReqVO = buildReqVO("DSP-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("处置说明补充");
        dispositionService.updateDisposition(updateReqVO);

        DispositionDO disposition = dispositionMapper.selectById(id);
        assertEquals("DSP-A", disposition.getDispositionNo());
        assertEquals("处置说明补充", disposition.getRemark());
    }

    @Test
    void testUpdateDisposition_approvedRejected() {
        DispositionSaveReqVO createReqVO = buildReqVO("DSP-A");
        createReqVO.setStatus(MesDispositionStatusEnum.APPROVED.name());
        createReqVO.setApprovedBy(2L);
        Long id = dispositionService.createDisposition(createReqVO);

        DispositionSaveReqVO updateReqVO = buildReqVO("DSP-A");
        updateReqVO.setId(id);
        // 审批通过后已联动返工、报废或库存冻结，处置主体不允许再改
        assertServiceException(() -> dispositionService.updateDisposition(updateReqVO), DISPOSITION_STATUS_INVALID);
    }

    @Test
    void testUpdateDisposition_notExists() {
        DispositionSaveReqVO updateReqVO = buildReqVO("DSP-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> dispositionService.updateDisposition(updateReqVO), DISPOSITION_NOT_EXISTS);
    }

    @Test
    void testDeleteDisposition_success() {
        Long id = dispositionService.createDisposition(buildReqVO("DSP-A"));

        dispositionService.deleteDisposition(id);

        assertNull(dispositionMapper.selectById(id));
    }

    @Test
    void testDeleteDisposition_submittedRejected() {
        DispositionSaveReqVO createReqVO = buildReqVO("DSP-A");
        createReqVO.setStatus(MesDispositionStatusEnum.SUBMITTED.name());
        Long id = dispositionService.createDisposition(createReqVO);

        assertServiceException(() -> dispositionService.deleteDisposition(id), DISPOSITION_STATUS_INVALID);
    }

    @Test
    void testDeleteDispositionList_submittedRejected() {
        Long first = dispositionService.createDisposition(buildReqVO("DSP-A"));
        DispositionSaveReqVO submitted = buildReqVO("DSP-B");
        submitted.setStatus(MesDispositionStatusEnum.SUBMITTED.name());
        Long second = dispositionService.createDisposition(submitted);

        assertServiceException(() -> dispositionService.deleteDispositionListByIds(List.of(first, second)),
                DISPOSITION_STATUS_INVALID);
        assertNotNull(dispositionMapper.selectById(first));
    }

    @Test
    void testGetDispositionPage() {
        dispositionService.createDisposition(buildReqVO("DSP-A"));

        DispositionPageReqVO reqVO = new DispositionPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<DispositionRespVO> list = dispositionService.getDispositionPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("DSP-A", list.get(0).getDispositionNo());
    }

}
