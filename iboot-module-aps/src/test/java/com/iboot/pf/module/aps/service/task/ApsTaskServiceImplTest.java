package com.iboot.pf.module.aps.service.task;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskAdjustReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskGanttRespVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.ApsTaskLockDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsOrderProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.dal.mysql.taskdep.ApsTaskDepMapper;
import com.iboot.pf.module.aps.dal.mysql.tasklock.ApsTaskLockMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsTaskDepTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockStatusEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.snapshot.ApsSnapshotService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_CANNOT_UNLOCK;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_RESOURCE_OVERLAP;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_TIME_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 排程任务 Service 测试。
 *
 * <p>覆盖三条不变量：时间区间必须为正、同一资源不允许时间重叠、已开工任务不允许解锁；
 * 另外验证锁定与解锁同时维护任务标记与锁定留痕记录。
 */
@Import({ApsTaskServiceImpl.class, ApsCommandServiceImpl.class})
class ApsTaskServiceImplTest extends BaseDbUnitTest {

    private static final Long VERSION_ID = 100L;
    private static final Long RES_ID = 200L;
    private static final Long SCENARIO_ID = 300L;

    @Resource
    private ApsTaskService taskService;
    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsTaskDepMapper taskDepMapper;
    @Resource
    private ApsTaskLockMapper taskLockMapper;
    @Resource
    private ApsVersionMapper versionMapper;
    @Resource
    private ApsOrderProjectionMapper orderProjectionMapper;

    @MockitoBean
    private ApsVersionService versionService;
    @MockitoBean
    private ApsSnapshotService snapshotService;
    @MockitoBean
    private ApsRelationFiller relationFiller;

    @BeforeEach
    void mockExternalDependencies() {
        ResourceRespDTO resource = new ResourceRespDTO();
        resource.setId(RES_ID);
        resource.setCode("RES-A");
        resource.setName("加工中心A");
        when(snapshotService.requireResource(anyLong())).thenReturn(resource);
    }

    private Long insertTask(String taskNo, Long resId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        return insertTask(taskNo, resId, 1L, "MO001", startTime, endTime, status);
    }

    private Long insertTask(String taskNo, Long resId, Long prodOrderId, String prodOrderNo,
                            LocalDateTime startTime, LocalDateTime endTime, String status) {
        ApsTaskDO task = ApsTaskDO.builder()
                .versionId(VERSION_ID).taskNo(taskNo)
                .prodOrderId(prodOrderId).prodOrderNo(prodOrderNo).workOrderId(0L).opTaskId(1L).routeOpId(1L)
                .opCode("OP010").opName("粗车").plantId(1L).workshopId(1L).projectId(0L).projectTaskId(0L)
                .resId(resId).resCode("RES-A").startTime(startTime).endTime(endTime)
                .durationMinutes(60).sequenceNo(1).lockedFlag(false).status(status)
                .build();
        taskMapper.insert(task);
        return task.getId();
    }

    private TaskAdjustReqVO buildAdjustReqVO(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        TaskAdjustReqVO reqVO = new TaskAdjustReqVO();
        reqVO.setId(id);
        reqVO.setResId(RES_ID);
        reqVO.setStartTime(startTime);
        reqVO.setEndTime(endTime);
        reqVO.setSequenceNo(1);
        reqVO.setRemark("人工微调");
        return reqVO;
    }

    private LockTaskReqDTO buildLockCommand(String idempotencyKey, Long taskId) {
        LockTaskReqDTO reqDTO = new LockTaskReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(taskId));
        reqDTO.setTaskId(taskId);
        reqDTO.setLockType(ApsTaskLockTypeEnum.MANUAL.name());
        reqDTO.setReason("等待外协件");
        reqDTO.setOperatorId(1L);
        return reqDTO;
    }

    @Test
    void testAdjustTask_recomputeDurationFromTimeRange() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());

        taskService.adjustTask(buildAdjustReqVO(id, LocalDateTime.of(2026, 8, 1, 13, 0),
                LocalDateTime.of(2026, 8, 1, 15, 30)));

        ApsTaskDO entity = taskMapper.selectById(id);
        // 占用分钟数由起止时间推导，不接受前端提交
        assertEquals(150, entity.getDurationMinutes());
        assertEquals("RES-A", entity.getResCode());
    }

    @Test
    void testAdjustTask_invalidTimeRangeRejected() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());

        assertServiceException(() -> taskService.adjustTask(buildAdjustReqVO(id,
                LocalDateTime.of(2026, 8, 1, 10, 0), LocalDateTime.of(2026, 8, 1, 10, 0))), TASK_TIME_INVALID);
    }

    @Test
    void testAdjustTask_overlapOnSameResourceRejected() {
        insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0), ApsTaskStatusEnum.PLANNED.name());
        Long secondId = insertTask("T002", RES_ID, LocalDateTime.of(2026, 8, 1, 13, 0),
                LocalDateTime.of(2026, 8, 1, 14, 0), ApsTaskStatusEnum.PLANNED.name());

        // 挪到 11:00-13:00 会和第一条 8:00-12:00 重叠
        assertServiceException(() -> taskService.adjustTask(buildAdjustReqVO(secondId,
                LocalDateTime.of(2026, 8, 1, 11, 0), LocalDateTime.of(2026, 8, 1, 13, 0))),
                TASK_RESOURCE_OVERLAP);
    }

    @Test
    void testAdjustTask_headToTailNotOverlap() {
        insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0), ApsTaskStatusEnum.PLANNED.name());
        Long secondId = insertTask("T002", RES_ID, LocalDateTime.of(2026, 8, 1, 15, 0),
                LocalDateTime.of(2026, 8, 1, 16, 0), ApsTaskStatusEnum.PLANNED.name());

        // 首尾相接不算冲突：前一道 10:00 结束、后一道 10:00 开始
        taskService.adjustTask(buildAdjustReqVO(secondId, LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 11, 0)));

        assertEquals(60, taskMapper.selectById(secondId).getDurationMinutes());
    }

    @Test
    void testAdjustTask_lockedRejected() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.LOCKED.name());
        taskMapper.updateById(ApsTaskDO.builder().id(id).lockedFlag(true).build());

        assertServiceException(() -> taskService.adjustTask(buildAdjustReqVO(id,
                LocalDateTime.of(2026, 8, 1, 13, 0), LocalDateTime.of(2026, 8, 1, 14, 0))), TASK_CANNOT_UNLOCK);
    }

    @Test
    void testLockTask_writesFlagAndLockRecord() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());

        taskService.lockTask(buildLockCommand("UI:LOCK:1", id));

        ApsTaskDO entity = taskMapper.selectById(id);
        assertTrue(entity.getLockedFlag());
        assertEquals(ApsTaskStatusEnum.LOCKED.name(), entity.getStatus());
        ApsTaskLockDO lock = taskLockMapper.selectLockedByTaskId(id);
        assertNotNull(lock);
        assertEquals(ApsTaskLockTypeEnum.MANUAL.name(), lock.getLockType());
    }

    @Test
    void testUnlockTask_keepsLockHistory() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.PLANNED.name());
        taskService.lockTask(buildLockCommand("UI:LOCK:1", id));

        taskService.unlockTask(buildLockCommand("UI:UNLOCK:1", id));

        ApsTaskDO entity = taskMapper.selectById(id);
        assertFalse(entity.getLockedFlag());
        assertEquals(ApsTaskStatusEnum.PLANNED.name(), entity.getStatus());
        // 锁定记录不物理删除，改成已解锁保留历史
        assertEquals(1L, taskLockMapper.selectCountByTaskId(id));
        List<ApsTaskLockDO> locks = taskLockMapper.selectList(ApsTaskLockDO::getTaskId, id);
        assertEquals(ApsTaskLockStatusEnum.UNLOCKED.name(), locks.get(0).getStatus());
        assertNotNull(locks.get(0).getUnlockTime());
        // 已解锁后不该再被当成锁定中的记录
        assertNull(taskLockMapper.selectLockedByTaskId(id));
    }

    @Test
    void testUnlockTask_startedRejected() {
        Long id = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 9, 0), ApsTaskStatusEnum.STARTED.name());

        // 已开工的任务解锁等于允许重排把车间正在做的工序挪走
        assertServiceException(() -> taskService.unlockTask(buildLockCommand("UI:UNLOCK:1", id)),
                TASK_CANNOT_UNLOCK);
    }

    @Test
    void testValidateTaskExists_notExists() {
        assertServiceException(() -> taskService.validateTaskExists(randomLongId()), TASK_NOT_EXISTS);
    }

    @Test
    void testGetTaskGantt_flatTasksWithResourceGroupsAndInVersionLinks() {
        mockVersion();
        Long first = insertTask("T001", RES_ID, LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0), ApsTaskStatusEnum.PLANNED.name());
        Long second = insertTask("T002", RES_ID, LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 11, 0), ApsTaskStatusEnum.PLANNED.name());
        Long other = insertTask("T003", RES_ID + 1, LocalDateTime.of(2026, 8, 1, 9, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0), ApsTaskStatusEnum.PLANNED.name());
        insertDep(first, second);
        // 悬空边：后置任务不属于本版本的任务集合，必须被丢掉，否则前端会画出错位的连线
        insertDep(first, randomLongId());

        TaskGanttRespVO respVO = taskService.getTaskGantt(VERSION_ID);

        assertEquals(3, respVO.getTaskCount());
        assertEquals(3, respVO.getTasks().size());
        assertEquals(2, respVO.getResourceGroups().size());
        assertEquals(LocalDateTime.of(2026, 8, 1, 8, 0), respVO.getMinTime());
        assertEquals(LocalDateTime.of(2026, 8, 1, 12, 0), respVO.getMaxTime());
        assertEquals(1, respVO.getLinks().size());
        assertEquals(first, respVO.getLinks().get(0).getFromTaskId());
        assertEquals(second, respVO.getLinks().get(0).getToTaskId());
        // 分组只带统计，任务在 tasks 里平铺：第一个资源上两条任务，负荷为两段之和，时间跨度取首末
        TaskGanttRespVO.ResourceGroup main = respVO.getResourceGroups().stream()
                .filter(row -> RES_ID.equals(row.getResId())).findFirst().orElseThrow();
        assertEquals(2, main.getTaskCount());
        assertEquals(120, main.getLoadMinutes());
        assertEquals(LocalDateTime.of(2026, 8, 1, 8, 0), main.getPlanStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 1, 11, 0), main.getPlanEndTime());
        // 任务条自带分组键，前端才能在两种维度上归组
        assertEquals(2, respVO.getTasks().stream()
                .filter(bar -> RES_ID.equals(bar.getResId())).count());
        assertEquals(other, respVO.getTasks().stream()
                .filter(bar -> !RES_ID.equals(bar.getResId())).findFirst().orElseThrow().getId());
    }

    @Test
    void testGetTaskGantt_orderGroupsAggregateAndFlagDelay() {
        mockVersion();
        // 同一工单两道工序：分组统计要按工单聚合，不能被工序数放大
        insertTask("T001", RES_ID, 10L, "MO010", LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0), ApsTaskStatusEnum.PLANNED.name());
        insertTask("T002", RES_ID + 1, 10L, "MO010", LocalDateTime.of(2026, 8, 2, 8, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0), ApsTaskStatusEnum.PLANNED.name());
        insertTask("T003", RES_ID, 20L, "MO020", LocalDateTime.of(2026, 8, 1, 13, 0),
                LocalDateTime.of(2026, 8, 1, 15, 0), ApsTaskStatusEnum.PLANNED.name());
        // MO010 交期早于计划完工 -> 超期；MO020 交期还有富余 -> 不超期
        insertOrderProjection(10L, "M001", LocalDateTime.of(2026, 8, 1, 18, 0));
        insertOrderProjection(20L, "M002", LocalDateTime.of(2026, 8, 10, 18, 0));

        TaskGanttRespVO respVO = taskService.getTaskGantt(VERSION_ID);

        assertEquals(2, respVO.getOrderGroups().size());
        TaskGanttRespVO.OrderGroup delayed = respVO.getOrderGroups().stream()
                .filter(row -> "MO010".equals(row.getProdOrderNo())).findFirst().orElseThrow();
        assertEquals(2, delayed.getTaskCount());
        assertEquals(120, delayed.getLoadMinutes());
        assertEquals(LocalDateTime.of(2026, 8, 1, 8, 0), delayed.getPlanStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 2, 12, 0), delayed.getPlanEndTime());
        assertEquals("M001", delayed.getMatCode());
        assertTrue(delayed.getDelayFlag());
        // 2026-08-01 18:00 到 2026-08-02 12:00 共 18 小时
        assertEquals(18 * 60, delayed.getDelayMinutes());

        TaskGanttRespVO.OrderGroup onTime = respVO.getOrderGroups().stream()
                .filter(row -> "MO020".equals(row.getProdOrderNo())).findFirst().orElseThrow();
        assertFalse(onTime.getDelayFlag());
        assertEquals(0, onTime.getDelayMinutes());
    }

    @Test
    void testGetTaskGantt_orderGroupWithoutProjectionNotTreatedAsDelayed() {
        mockVersion();
        insertTask("T001", RES_ID, 30L, "MO030", LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0), ApsTaskStatusEnum.PLANNED.name());

        TaskGanttRespVO respVO = taskService.getTaskGantt(VERSION_ID);

        TaskGanttRespVO.OrderGroup group = respVO.getOrderGroups().get(0);
        // 工单投影里没这张单时交期为空，不能拿"没交期"当逾期报警
        assertNull(group.getDueDate());
        assertFalse(group.getDelayFlag());
        assertEquals(0, group.getDelayMinutes());
    }

    @Test
    void testGetTaskGantt_emptyVersionReturnsNoResource() {
        mockVersion();

        TaskGanttRespVO respVO = taskService.getTaskGantt(VERSION_ID);

        assertEquals(0, respVO.getTaskCount());
        assertTrue(respVO.getTasks().isEmpty());
        assertTrue(respVO.getResourceGroups().isEmpty());
        assertTrue(respVO.getOrderGroups().isEmpty());
        assertTrue(respVO.getLinks().isEmpty());
        assertNull(respVO.getMinTime());
    }

    /**
     * 版本既要 mock 给 Service，也要真实落库。
     *
     * <p>Service 通过 versionService 拿版本头信息（已 mock），但工单分组的 SQL 要 JOIN aps_version
     * 取场景编号去关联工单投影，库里没有这一行交期就查不出来。
     */
    private void mockVersion() {
        VersionRespVO version = new VersionRespVO();
        version.setId(VERSION_ID);
        version.setVersionNo(1);
        version.setScenarioCode("SC001");
        version.setScenarioName("松江工厂 8 月排程");
        version.setStatus(ApsVersionStatusEnum.SOLVED.name());
        when(versionService.getVersion(VERSION_ID)).thenReturn(version);
        versionMapper.insert(ApsVersionDO.builder()
                .id(VERSION_ID)
                .scenarioId(SCENARIO_ID)
                .versionNo(1)
                .status(ApsVersionStatusEnum.SOLVED.name())
                .build());
    }

    /** 工单投影按工序拆行，这里插两行同工单：验证分组统计不会被投影行数放大。 */
    private void insertOrderProjection(Long prodOrderId, String matCode, LocalDateTime dueDate) {
        for (int seq = 1; seq <= 2; seq++) {
            orderProjectionMapper.insert(ApsOrderProjectionDO.builder()
                    .scenarioId(SCENARIO_ID)
                    .sourceType("OP_TASK")
                    .sourceId(prodOrderId * 10 + seq)
                    .sourceNo("SRC" + prodOrderId + seq)
                    .prodOrderId(prodOrderId)
                    .matId(prodOrderId)
                    .matCode(matCode)
                    .seq(seq)
                    .dueDate(dueDate)
                    .priority(1)
                    .build());
        }
    }

    private void insertDep(Long fromTaskId, Long toTaskId) {
        taskDepMapper.insert(ApsTaskDepDO.builder()
                .versionId(VERSION_ID)
                .fromTaskId(fromTaskId)
                .toTaskId(toTaskId)
                .depType(ApsTaskDepTypeEnum.FINISH_START.name())
                .lagMinutes(0)
                .build());
    }

}
