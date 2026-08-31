package com.iboot.pf.module.aps.service.taskdep;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepSaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import com.iboot.pf.module.aps.dal.mysql.taskdep.ApsTaskDepMapper;
import com.iboot.pf.module.aps.enums.ApsTaskDepTypeEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.aps.service.task.ApsTaskService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_DUPLICATE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_SELF_REFERENCE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_VERSION_MISMATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * 排程任务依赖 Service 测试。
 *
 * <p>覆盖版本编号由前置任务反推、自依赖拦截、跨版本依赖拦截与依赖唯一性。
 */
@Import(ApsTaskDepServiceImpl.class)
class ApsTaskDepServiceImplTest extends BaseDbUnitTest {

    private static final Long VERSION_ID = 100L;
    private static final Long OTHER_VERSION_ID = 200L;

    @Resource
    private ApsTaskDepService taskDepService;
    @Resource
    private ApsTaskDepMapper taskDepMapper;

    @MockitoBean
    private ApsTaskService taskService;
    @MockitoBean
    private ApsVersionService versionService;

    private ApsTaskDO buildTask(Long id, Long versionId) {
        return ApsTaskDO.builder()
                .id(id).versionId(versionId).taskNo("T" + id).prodOrderId(1L).prodOrderNo("MO001")
                .opTaskId(id).routeOpId(1L).opCode("OP010").opName("粗车")
                .plantId(1L).workshopId(1L).projectId(0L).projectTaskId(0L)
                .resId(1L).resCode("RES-A").startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusHours(1))
                .durationMinutes(60).sequenceNo(1).lockedFlag(false).status(ApsTaskStatusEnum.PLANNED.name())
                .build();
    }

    @BeforeEach
    void mockTasks() {
        when(taskService.validateTaskExists(1L)).thenReturn(buildTask(1L, VERSION_ID));
        when(taskService.validateTaskExists(2L)).thenReturn(buildTask(2L, VERSION_ID));
        when(taskService.validateTaskExists(3L)).thenReturn(buildTask(3L, OTHER_VERSION_ID));
    }

    private TaskDepSaveReqVO buildReqVO(Long fromTaskId, Long toTaskId, ApsTaskDepTypeEnum depType) {
        TaskDepSaveReqVO reqVO = new TaskDepSaveReqVO();
        reqVO.setFromTaskId(fromTaskId);
        reqVO.setToTaskId(toTaskId);
        reqVO.setDepType(depType.name());
        reqVO.setLagMinutes(0);
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateTaskDep_versionDerivedFromFromTask() {
        Long id = taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START));

        ApsTaskDepDO entity = taskDepMapper.selectById(id);
        assertNotNull(entity);
        // 版本编号不由前端提交，从前置任务反推
        assertEquals(VERSION_ID, entity.getVersionId());
    }

    @Test
    void testCreateTaskDep_selfReferenceRejected() {
        assertServiceException(
                () -> taskDepService.createTaskDep(buildReqVO(1L, 1L, ApsTaskDepTypeEnum.FINISH_START)),
                TASK_DEP_SELF_REFERENCE);
    }

    @Test
    void testCreateTaskDep_crossVersionRejected() {
        // 跨版本连边会把别的排程结果拉进求解输入
        assertServiceException(
                () -> taskDepService.createTaskDep(buildReqVO(1L, 3L, ApsTaskDepTypeEnum.FINISH_START)),
                TASK_DEP_VERSION_MISMATCH);
    }

    @Test
    void testCreateTaskDep_duplicateRejected() {
        taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START));

        assertServiceException(
                () -> taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START)),
                TASK_DEP_DUPLICATE);
    }

    @Test
    void testCreateTaskDep_differentTypeAllowed() {
        taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START));

        // 唯一键含依赖类型：同一对任务可以同时有完工到开工与开工到开工两条约束
        assertNotNull(taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.START_START)));
    }

    @Test
    void testUpdateTaskDep_success() {
        Long id = taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START));

        TaskDepSaveReqVO updateReqVO = buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START);
        updateReqVO.setId(id);
        updateReqVO.setLagMinutes(30);
        taskDepService.updateTaskDep(updateReqVO);

        assertEquals(30, taskDepMapper.selectById(id).getLagMinutes());
    }

    @Test
    void testUpdateTaskDep_notExists() {
        TaskDepSaveReqVO updateReqVO = buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START);
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> taskDepService.updateTaskDep(updateReqVO), TASK_DEP_NOT_EXISTS);
    }

    @Test
    void testDeleteTaskDep_success() {
        Long id = taskDepService.createTaskDep(buildReqVO(1L, 2L, ApsTaskDepTypeEnum.FINISH_START));

        taskDepService.deleteTaskDep(id);

        assertNull(taskDepMapper.selectById(id));
    }

    @Test
    void testDeleteTaskDep_notExists() {
        assertServiceException(() -> taskDepService.deleteTaskDep(randomLongId()), TASK_DEP_NOT_EXISTS);
    }

}
