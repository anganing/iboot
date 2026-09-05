package com.iboot.pf.module.mes.service.optask;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpExecModeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_HAS_DISPATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_ROUTE_OP_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 工序任务 Service 测试：工序快照从路线工序带出、执行模式按并行组推导、同工单同路线工序唯一、已派工禁删。
 */
@Import(OpTaskServiceImpl.class)
class OpTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OpTaskService opTaskService;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private DispatchTaskMapper dispatchTaskMapper;

    @MockitoBean
    private WorkOrderService workOrderService;
    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long workOrderId;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "OPT0001" : invocation.getArgument(1));
        // 路线工序与标准工序都在 mdm，通过门面取快照
        when(snapshotService.requireRouteOp(any())).thenReturn(MesTestFixtures.routeOp());
        when(snapshotService.requireOp(any())).thenReturn(MesTestFixtures.op());
        WorkOrderDO workOrder = new WorkOrderDO().setWorkOrderNo("WO-0001").setProdOrderId(1L);
        workOrderMapper.insert(workOrder);
        workOrderId = workOrder.getId();
    }

    private OpTaskSaveReqVO buildReqVO(String taskNo, Long routeOpId) {
        OpTaskSaveReqVO reqVO = new OpTaskSaveReqVO();
        reqVO.setTaskNo(taskNo);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setRouteOpId(routeOpId);
        reqVO.setQcMode(MesQcModeEnum.NONE.name());
        reqVO.setSkipPolicy(MesSkipPolicyEnum.NOT_ALLOWED.name());
        reqVO.setPlannedQty(new BigDecimal("100"));
        reqVO.setResId(MesConstants.NONE_ID);
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    /**
     * 表单不再带状态，需要非初始状态的场景直接改库。
     *
     * <p>工序任务状态只能由开工、暂停、完工、跳序等动作推进，测试里也不许绕表单塞状态。
     */
    private void pushStatus(Long id, String status) {
        opTaskMapper.updateById(OpTaskDO.builder().id(id).status(status).build());
    }

    @Test
    void testCreateOpTask_snapshotFromRouteOp() {
        Long id = opTaskService.createOpTask(buildReqVO(null, 31L));

        OpTaskDO opTask = opTaskMapper.selectById(id);
        assertNotNull(opTask);
        assertEquals("OPT0001", opTask.getTaskNo());
        // 工序编码、名称、顺序、关键标记都是主数据快照，前端不提交
        assertEquals("WO-0001", opTask.getWorkOrderNo());
        assertEquals("OP010", opTask.getOpCode());
        assertEquals("定子绕线", opTask.getOpName());
        assertEquals(10, opTask.getSeq());
        assertTrue(opTask.getKeyFlag());
        // 无并行组时推导为顺序执行
        assertEquals(MesOpExecModeEnum.SEQUENTIAL.name(), opTask.getExecMode());
        assertEquals(0, opTask.getCompletedQty().compareTo(BigDecimal.ZERO));
        assertEquals(MesConstants.NONE_ID, opTask.getPrevTaskId());
        // 状态不由表单维护，新建一律落 PENDING
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), opTask.getStatus());
    }

    @Test
    void testUpdateOpTask_statusNotOverwritten() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));
        pushStatus(id, MesOpTaskStatusEnum.RUNNING.name());

        OpTaskSaveReqVO updateReqVO = buildReqVO("OPT-A", 31L);
        updateReqVO.setId(id);
        updateReqVO.setRemark("计划调整");
        opTaskService.updateOpTask(updateReqVO);

        // 改单不碰状态：加工中的工序不能被表单一键改回待排产
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(id).getStatus());
    }

    @Test
    void testCreateOpTask_workOrderNotExists() {
        OpTaskSaveReqVO reqVO = buildReqVO("OPT-A", 31L);
        reqVO.setWorkOrderId(randomLongId());

        assertServiceException(() -> opTaskService.createOpTask(reqVO), WORK_ORDER_NOT_EXISTS);
    }

    @Test
    void testCreateOpTask_duplicateRouteOpRejected() {
        opTaskService.createOpTask(buildReqVO("OPT-A", 31L));

        // 同一作业工单下一道路线工序只能生成一个任务
        assertServiceException(() -> opTaskService.createOpTask(buildReqVO("OPT-B", 31L)),
                OP_TASK_ROUTE_OP_DUPLICATE);
    }

    @Test
    void testUpdateOpTask_keepTaskNoAndProgress() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));

        OpTaskSaveReqVO updateReqVO = buildReqVO("OPT-CHANGED", 31L);
        updateReqVO.setId(id);
        updateReqVO.setRemark("计划调整");
        opTaskService.updateOpTask(updateReqVO);

        OpTaskDO opTask = opTaskMapper.selectById(id);
        assertEquals("OPT-A", opTask.getTaskNo());
        assertEquals("计划调整", opTask.getRemark());
    }

    @Test
    void testUpdateOpTask_finalStatusRejected() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));
        pushStatus(id, MesOpTaskStatusEnum.DONE.name());

        OpTaskSaveReqVO updateReqVO = buildReqVO("OPT-A", 31L);
        updateReqVO.setId(id);
        assertServiceException(() -> opTaskService.updateOpTask(updateReqVO), OP_TASK_STATUS_INVALID);
    }

    @Test
    void testUpdateOpTask_notExists() {
        OpTaskSaveReqVO updateReqVO = buildReqVO("OPT-A", 31L);
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> opTaskService.updateOpTask(updateReqVO), OP_TASK_NOT_EXISTS);
    }

    @Test
    void testDeleteOpTask_success() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));

        opTaskService.deleteOpTask(id);

        assertNull(opTaskMapper.selectById(id));
    }

    @Test
    void testDeleteOpTask_runningRejected() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));
        pushStatus(id, MesOpTaskStatusEnum.RUNNING.name());

        assertServiceException(() -> opTaskService.deleteOpTask(id), OP_TASK_STATUS_INVALID);
    }

    @Test
    void testDeleteOpTask_hasDispatchRejected() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));
        dispatchTaskMapper.insert(new DispatchTaskDO().setOpTaskId(id).setDispatchNo("DSP-0001"));

        assertServiceException(() -> opTaskService.deleteOpTask(id), OP_TASK_HAS_DISPATCH);
    }

    @Test
    void testDeleteOpTaskList_hasDispatchRejected() {
        Long first = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));
        Long second = opTaskService.createOpTask(buildReqVO("OPT-B", 32L));
        dispatchTaskMapper.insert(new DispatchTaskDO().setOpTaskId(second).setDispatchNo("DSP-0001"));

        assertServiceException(() -> opTaskService.deleteOpTaskListByIds(List.of(first, second)),
                OP_TASK_HAS_DISPATCH);
        assertNotNull(opTaskMapper.selectById(first));
    }

    @Test
    void testGetOpTaskPage() {
        opTaskService.createOpTask(buildReqVO("OPT-A", 31L));

        OpTaskPageReqVO reqVO = new OpTaskPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<OpTaskRespVO> list = opTaskService.getOpTaskPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("OPT-A", list.get(0).getTaskNo());
        assertEquals("OP010", list.get(0).getOpCode());
    }

    @Test
    void testApplySchedule_writesResourceAndPlannedTime() {
        Long id = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));

        int affected = opTaskService.applySchedule(buildApplyReqVO(id, 900L,
                LocalDateTime.of(2026, 9, 10, 8, 0), LocalDateTime.of(2026, 9, 10, 16, 0)));

        assertEquals(1, affected);
        OpTaskDO opTask = opTaskMapper.selectById(id);
        assertEquals(900L, opTask.getResId());
        assertEquals(LocalDateTime.of(2026, 9, 10, 8, 0), opTask.getPlannedStartTime());
        assertEquals(LocalDateTime.of(2026, 9, 10, 16, 0), opTask.getPlannedEndTime());
        // 计划回写不动数量与状态，它们由派工与报工推进
        assertEquals(0, opTask.getCompletedQty().compareTo(BigDecimal.ZERO));
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), opTask.getStatus());
    }

    @Test
    void testApplySchedule_skipsStartedTask() {
        Long running = opTaskService.createOpTask(buildReqVO("OPT-A", 31L));
        pushStatus(running, MesOpTaskStatusEnum.RUNNING.name());
        Long pending = opTaskService.createOpTask(buildReqVO("OPT-B", 32L));

        ApplyScheduleReqDTO reqDTO = buildApplyReqVO(running, 900L,
                LocalDateTime.of(2026, 9, 10, 8, 0), LocalDateTime.of(2026, 9, 10, 16, 0));
        reqDTO.getItems().add(buildItem(pending, 901L,
                LocalDateTime.of(2026, 9, 11, 8, 0), LocalDateTime.of(2026, 9, 11, 12, 0)));

        // 加工中的工序不允许被新计划挪走，跳过属于正常结果，不让整批失败
        assertEquals(1, opTaskService.applySchedule(reqDTO));
        assertNull(opTaskMapper.selectById(running).getPlannedStartTime());
        assertEquals(901L, opTaskMapper.selectById(pending).getResId());
    }

    @Test
    void testApplySchedule_unknownTaskIgnored() {
        // 版本里的任务在 MES 侧已被删除，回写时找不到对应行，不该抛异常
        assertEquals(0, opTaskService.applySchedule(buildApplyReqVO(randomLongId(), 900L,
                LocalDateTime.of(2026, 9, 10, 8, 0), LocalDateTime.of(2026, 9, 10, 16, 0))));
    }

    private ApplyScheduleReqDTO buildApplyReqVO(Long opTaskId, Long resId, LocalDateTime startTime,
                                                LocalDateTime endTime) {
        ApplyScheduleReqDTO reqDTO = new ApplyScheduleReqDTO();
        reqDTO.setIdempotencyKey("APS:RELEASE_SCHEDULE:1");
        reqDTO.setSourceModule("APS");
        reqDTO.setSourceBizNo("1");
        List<ApplyScheduleReqDTO.Item> items = new java.util.ArrayList<>();
        items.add(buildItem(opTaskId, resId, startTime, endTime));
        reqDTO.setItems(items);
        return reqDTO;
    }

    private ApplyScheduleReqDTO.Item buildItem(Long opTaskId, Long resId, LocalDateTime startTime,
                                               LocalDateTime endTime) {
        ApplyScheduleReqDTO.Item item = new ApplyScheduleReqDTO.Item();
        item.setOpTaskId(opTaskId);
        item.setResId(resId);
        item.setPlannedStartTime(startTime);
        item.setPlannedEndTime(endTime);
        return item;
    }

}
