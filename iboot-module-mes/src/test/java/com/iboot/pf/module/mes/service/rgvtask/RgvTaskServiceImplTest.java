package com.iboot.pf.module.mes.service.rgvtask;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.rgvtask.RgvTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesRgvDispatchStatusEnum;
import com.iboot.pf.module.mes.enums.MesRgvTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesRgvTaskTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_EXTERNAL_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_SAME_STATION;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * RGV 流转任务 Service 测试：起终点不能相同、外部任务号按系统唯一、已下发禁改起终点与禁删、幂等键唯一。
 */
@Import(RgvTaskServiceImpl.class)
class RgvTaskServiceImplTest extends BaseDbUnitTest {

    private static final String EXTERNAL_SYSTEM = "WCS-A";

    @Resource
    private RgvTaskService rgvTaskService;
    @Resource
    private RgvTaskMapper rgvTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long workOrderId;
    private Long opTaskId;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "RGV0001" : invocation.getArgument(1));
        WorkOrderDO workOrder = new WorkOrderDO().setWorkOrderNo("WO-0001").setProdOrderId(1L);
        workOrderMapper.insert(workOrder);
        workOrderId = workOrder.getId();
        OpTaskDO opTask = new OpTaskDO().setTaskNo("OPT-0001").setWorkOrderId(workOrderId).setRouteOpId(31L);
        opTaskMapper.insert(opTask);
        opTaskId = opTask.getId();
    }

    private RgvTaskSaveReqVO buildReqVO(String taskNo, String externalTaskNo) {
        RgvTaskSaveReqVO reqVO = new RgvTaskSaveReqVO();
        reqVO.setTaskNo(taskNo);
        reqVO.setExternalTaskNo(externalTaskNo);
        reqVO.setTaskType(MesRgvTaskTypeEnum.STATION_TRANSFER.name());
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setOpTaskId(opTaskId);
        reqVO.setTargetOpTaskId(MesConstants.NONE_ID);
        reqVO.setFromStationId(1L);
        reqVO.setToStationId(2L);
        reqVO.setCarrierNo("TRAY-001");
        reqVO.setExternalSystemCode(EXTERNAL_SYSTEM);
        reqVO.setDispatchStatus(MesRgvDispatchStatusEnum.PENDING.name());
        reqVO.setStatus(MesRgvTaskStatusEnum.CREATED.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateRgvTask_blankNoFilledByRule() {
        Long id = rgvTaskService.createRgvTask(buildReqVO(null, "EXT-001"));

        RgvTaskDO rgvTask = rgvTaskMapper.selectById(id);
        assertNotNull(rgvTask);
        assertEquals("RGV0001", rgvTask.getTaskNo());
        assertEquals(MesConstants.INIT_VERSION, rgvTask.getVersion());
    }

    @Test
    void testCreateRgvTask_workOrderNotExists() {
        RgvTaskSaveReqVO reqVO = buildReqVO("RGV-A", "EXT-001");
        reqVO.setWorkOrderId(randomLongId());

        assertServiceException(() -> rgvTaskService.createRgvTask(reqVO), WORK_ORDER_NOT_EXISTS);
    }

    @Test
    void testCreateRgvTask_opTaskNotExists() {
        RgvTaskSaveReqVO reqVO = buildReqVO("RGV-A", "EXT-001");
        reqVO.setOpTaskId(randomLongId());

        assertServiceException(() -> rgvTaskService.createRgvTask(reqVO), OP_TASK_NOT_EXISTS);
    }

    @Test
    void testCreateRgvTask_sameStationRejected() {
        RgvTaskSaveReqVO reqVO = buildReqVO("RGV-A", "EXT-001");
        reqVO.setToStationId(reqVO.getFromStationId());

        assertServiceException(() -> rgvTaskService.createRgvTask(reqVO), RGV_TASK_SAME_STATION);
    }

    @Test
    void testCreateRgvTask_duplicateExternalNoRejected() {
        rgvTaskService.createRgvTask(buildReqVO("RGV-A", "EXT-001"));

        assertServiceException(() -> rgvTaskService.createRgvTask(buildReqVO("RGV-B", "EXT-001")),
                RGV_TASK_EXTERNAL_NO_DUPLICATE);
    }

    @Test
    void testCreateRgvTask_sameExternalNoDifferentSystemAccepted() {
        rgvTaskService.createRgvTask(buildReqVO("RGV-A", "EXT-001"));

        RgvTaskSaveReqVO other = buildReqVO("RGV-B", "EXT-001");
        other.setExternalSystemCode("WCS-B");
        // 唯一键是外部系统 + 外部任务号，不同系统各自编号不冲突
        assertNotNull(rgvTaskService.createRgvTask(other));
    }

    @Test
    void testCreateRgvTask_duplicateIdempotencyKeyRejected() {
        RgvTaskSaveReqVO first = buildReqVO("RGV-A", "EXT-001");
        first.setIdempotencyKey("OPT-0001:MOVE");
        rgvTaskService.createRgvTask(first);

        RgvTaskSaveReqVO second = buildReqVO("RGV-B", "EXT-002");
        second.setIdempotencyKey("OPT-0001:MOVE");
        assertServiceException(() -> rgvTaskService.createRgvTask(second), IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Test
    void testUpdateRgvTask_sentStationChangeRejected() {
        RgvTaskSaveReqVO createReqVO = buildReqVO("RGV-A", "EXT-001");
        createReqVO.setDispatchStatus(MesRgvDispatchStatusEnum.SENT.name());
        Long id = rgvTaskService.createRgvTask(createReqVO);

        RgvTaskSaveReqVO updateReqVO = buildReqVO("RGV-A", "EXT-001");
        updateReqVO.setId(id);
        updateReqVO.setToStationId(3L);
        // 已下发的任务改搬运终点会与外部调度系统不一致
        assertServiceException(() -> rgvTaskService.updateRgvTask(updateReqVO), RGV_TASK_STATUS_INVALID);
    }

    @Test
    void testUpdateRgvTask_sentRemarkAllowed() {
        RgvTaskSaveReqVO createReqVO = buildReqVO("RGV-A", "EXT-001");
        createReqVO.setDispatchStatus(MesRgvDispatchStatusEnum.SENT.name());
        Long id = rgvTaskService.createRgvTask(createReqVO);

        RgvTaskSaveReqVO updateReqVO = buildReqVO("RGV-A", "EXT-001");
        updateReqVO.setId(id);
        updateReqVO.setDispatchStatus(MesRgvDispatchStatusEnum.SENT.name());
        updateReqVO.setRemark("补充调度说明");
        rgvTaskService.updateRgvTask(updateReqVO);

        assertEquals("补充调度说明", rgvTaskMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateRgvTask_notExists() {
        RgvTaskSaveReqVO updateReqVO = buildReqVO("RGV-A", "EXT-001");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> rgvTaskService.updateRgvTask(updateReqVO), RGV_TASK_NOT_EXISTS);
    }

    @Test
    void testDeleteRgvTask_success() {
        Long id = rgvTaskService.createRgvTask(buildReqVO("RGV-A", "EXT-001"));

        rgvTaskService.deleteRgvTask(id);

        assertNull(rgvTaskMapper.selectById(id));
    }

    @Test
    void testDeleteRgvTask_sentRejected() {
        RgvTaskSaveReqVO createReqVO = buildReqVO("RGV-A", "EXT-001");
        createReqVO.setDispatchStatus(MesRgvDispatchStatusEnum.SENT.name());
        Long id = rgvTaskService.createRgvTask(createReqVO);

        assertServiceException(() -> rgvTaskService.deleteRgvTask(id), RGV_TASK_STATUS_INVALID);
    }

    @Test
    void testDeleteRgvTaskList_sentRejected() {
        Long first = rgvTaskService.createRgvTask(buildReqVO("RGV-A", "EXT-001"));
        RgvTaskSaveReqVO sent = buildReqVO("RGV-B", "EXT-002");
        sent.setDispatchStatus(MesRgvDispatchStatusEnum.SENT.name());
        Long second = rgvTaskService.createRgvTask(sent);

        assertServiceException(() -> rgvTaskService.deleteRgvTaskListByIds(List.of(first, second)),
                RGV_TASK_STATUS_INVALID);
        assertNotNull(rgvTaskMapper.selectById(first));
    }

    @Test
    void testGetRgvTaskPage() {
        rgvTaskService.createRgvTask(buildReqVO("RGV-A", "EXT-001"));

        RgvTaskPageReqVO reqVO = new RgvTaskPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<RgvTaskRespVO> list = rgvTaskService.getRgvTaskPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("RGV-A", list.get(0).getTaskNo());
    }

}
