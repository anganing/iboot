package com.iboot.pf.module.mes.service.opskiprecord;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordPageReqVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesSkipReasonTypeEnum;
import com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_APPROVER_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_APPROVE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_NOT_ALLOWED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_RECORD_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 工序跳过记录 Service 测试：按工序任务的跳序策略裁决、审批人必填、需审批策略未批准不能生效。
 */
@Import(OpSkipRecordServiceImpl.class)
class OpSkipRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OpSkipRecordService opSkipRecordService;
    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long reasonOnlyTaskId;
    private Long notAllowedTaskId;
    private Long approvalRequiredTaskId;

    @BeforeEach
    void prepareOpTasks() {
        reasonOnlyTaskId = insertOpTask("OPT-REASON", MesSkipPolicyEnum.ALLOW_WITH_REASON.name());
        notAllowedTaskId = insertOpTask("OPT-DENY", MesSkipPolicyEnum.NOT_ALLOWED.name());
        approvalRequiredTaskId = insertOpTask("OPT-APPROVE", MesSkipPolicyEnum.APPROVAL_REQUIRED.name());
    }

    /** 跳序策略挂在工序任务上，是这里所有规则的判定依据。 */
    private Long insertOpTask(String taskNo, String skipPolicy) {
        OpTaskDO opTask = new OpTaskDO()
                .setTaskNo(taskNo)
                .setWorkOrderId(1L)
                .setRouteOpId(31L)
                .setSkipPolicy(skipPolicy);
        opTaskMapper.insert(opTask);
        return opTask.getId();
    }

    private OpSkipRecordSaveReqVO buildReqVO(Long opTaskId) {
        OpSkipRecordSaveReqVO reqVO = new OpSkipRecordSaveReqVO();
        reqVO.setOpTaskId(opTaskId);
        reqVO.setSkipReasonType(MesSkipReasonTypeEnum.NOT_APPLICABLE.name());
        reqVO.setSkipReason("该型号不需要该道工序");
        reqVO.setOperatorId(1L);
        reqVO.setApprovedBy(MesConstants.NONE_ID);
        reqVO.setApproveStatus(MesSkipApproveStatusEnum.NOT_REQUIRED.name());
        reqVO.setStatus(MesSkipRecordStatusEnum.PENDING.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateOpSkipRecord_skipTimeAutoFilled() {
        Long id = opSkipRecordService.createOpSkipRecord(buildReqVO(reasonOnlyTaskId));

        OpSkipRecordDO record = opSkipRecordMapper.selectById(id);
        assertNotNull(record);
        // 跳过时间留空取当前时间，否则事后查不到什么时候跳的
        assertNotNull(record.getSkipTime());
    }

    @Test
    void testCreateOpSkipRecord_opTaskNotExists() {
        assertServiceException(() -> opSkipRecordService.createOpSkipRecord(buildReqVO(randomLongId())),
                OP_TASK_NOT_EXISTS);
    }

    @Test
    void testCreateOpSkipRecord_policyNotAllowed() {
        assertServiceException(() -> opSkipRecordService.createOpSkipRecord(buildReqVO(notAllowedTaskId)),
                OP_SKIP_NOT_ALLOWED);
    }

    @Test
    void testCreateOpSkipRecord_approverRequiredWhenDecided() {
        OpSkipRecordSaveReqVO reqVO = buildReqVO(approvalRequiredTaskId);
        reqVO.setApproveStatus(MesSkipApproveStatusEnum.APPROVED.name());

        assertServiceException(() -> opSkipRecordService.createOpSkipRecord(reqVO), OP_SKIP_APPROVER_REQUIRED);
    }

    @Test
    void testCreateOpSkipRecord_effectiveNeedsApproval() {
        OpSkipRecordSaveReqVO reqVO = buildReqVO(approvalRequiredTaskId);
        reqVO.setStatus(MesSkipRecordStatusEnum.EFFECTIVE.name());
        reqVO.setApproveStatus(MesSkipApproveStatusEnum.PENDING.name());

        // 需审批策略下未批准就生效等于绕过管控
        assertServiceException(() -> opSkipRecordService.createOpSkipRecord(reqVO), OP_SKIP_APPROVE_REQUIRED);
    }

    @Test
    void testCreateOpSkipRecord_approvedEffectiveAccepted() {
        OpSkipRecordSaveReqVO reqVO = buildReqVO(approvalRequiredTaskId);
        reqVO.setStatus(MesSkipRecordStatusEnum.EFFECTIVE.name());
        reqVO.setApproveStatus(MesSkipApproveStatusEnum.APPROVED.name());
        reqVO.setApprovedBy(2L);

        assertNotNull(opSkipRecordService.createOpSkipRecord(reqVO));
    }

    @Test
    void testUpdateOpSkipRecord_effectiveOnlyRemark() {
        OpSkipRecordSaveReqVO createReqVO = buildReqVO(approvalRequiredTaskId);
        createReqVO.setStatus(MesSkipRecordStatusEnum.EFFECTIVE.name());
        createReqVO.setApproveStatus(MesSkipApproveStatusEnum.APPROVED.name());
        createReqVO.setApprovedBy(2L);
        Long id = opSkipRecordService.createOpSkipRecord(createReqVO);

        OpSkipRecordSaveReqVO updateReqVO = buildReqVO(reasonOnlyTaskId);
        updateReqVO.setId(id);
        updateReqVO.setEffectRemark("已通知后段工序");
        updateReqVO.setRemark("补充说明");
        opSkipRecordService.updateOpSkipRecord(updateReqVO);

        OpSkipRecordDO record = opSkipRecordMapper.selectById(id);
        // 已生效的记录已经把工序置为已跳过，只放行影响说明与备注
        assertEquals("已通知后段工序", record.getEffectRemark());
        assertEquals("补充说明", record.getRemark());
        assertEquals(approvalRequiredTaskId, record.getOpTaskId());
    }

    @Test
    void testUpdateOpSkipRecord_notExists() {
        OpSkipRecordSaveReqVO updateReqVO = buildReqVO(reasonOnlyTaskId);
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> opSkipRecordService.updateOpSkipRecord(updateReqVO),
                OP_SKIP_RECORD_NOT_EXISTS);
    }

    @Test
    void testDeleteOpSkipRecord_success() {
        Long id = opSkipRecordService.createOpSkipRecord(buildReqVO(reasonOnlyTaskId));

        opSkipRecordService.deleteOpSkipRecord(id);

        assertNull(opSkipRecordMapper.selectById(id));
    }

    @Test
    void testDeleteOpSkipRecordList_success() {
        Long first = opSkipRecordService.createOpSkipRecord(buildReqVO(reasonOnlyTaskId));
        Long second = opSkipRecordService.createOpSkipRecord(buildReqVO(reasonOnlyTaskId));

        opSkipRecordService.deleteOpSkipRecordListByIds(List.of(first, second));

        assertNull(opSkipRecordMapper.selectById(first));
        assertNull(opSkipRecordMapper.selectById(second));
    }

    @Test
    void testDeleteOpSkipRecord_notExists() {
        assertServiceException(() -> opSkipRecordService.deleteOpSkipRecord(randomLongId()),
                OP_SKIP_RECORD_NOT_EXISTS);
    }

    @Test
    void testGetOpSkipRecordPage() {
        opSkipRecordService.createOpSkipRecord(buildReqVO(reasonOnlyTaskId));

        OpSkipRecordPageReqVO reqVO = new OpSkipRecordPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<OpSkipRecordRespVO> list = opSkipRecordService.getOpSkipRecordPage(reqVO).getList();

        assertEquals(1, list.size());
        // 工序任务号由模块内 JOIN 出来
        assertEquals("OPT-REASON", list.get(0).getOpTaskNo());
    }

}
