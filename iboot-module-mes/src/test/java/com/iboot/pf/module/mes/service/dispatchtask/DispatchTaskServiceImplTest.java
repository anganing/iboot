package com.iboot.pf.module.mes.service.dispatchtask;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispatchTaskStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_OPERATOR_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_TIME_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 派工任务 Service 测试：人员与班组至少填一个、计划时间校验、只有待接单允许改与删。
 */
@Import(DispatchTaskServiceImpl.class)
class DispatchTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DispatchTaskService dispatchTaskService;
    @Resource
    private DispatchTaskMapper dispatchTaskMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long opTaskId;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "DSPT0001" : invocation.getArgument(1));
        OpTaskDO opTask = new OpTaskDO().setTaskNo("OPT-0001").setWorkOrderId(1L).setRouteOpId(31L);
        opTaskMapper.insert(opTask);
        opTaskId = opTask.getId();
    }

    private DispatchTaskSaveReqVO buildReqVO(String dispatchNo) {
        DispatchTaskSaveReqVO reqVO = new DispatchTaskSaveReqVO();
        reqVO.setDispatchNo(dispatchNo);
        reqVO.setOpTaskId(opTaskId);
        reqVO.setOperatorId(1L);
        reqVO.setTeamId(MesConstants.NONE_ID);
        reqVO.setResId(2L);
        reqVO.setShiftId(MesConstants.NONE_ID);
        reqVO.setPlannedStartTime(LocalDateTime.now());
        reqVO.setPlannedEndTime(LocalDateTime.now().plusHours(4));
        reqVO.setStatus(MesDispatchTaskStatusEnum.PENDING.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateDispatchTask_blankNoFilledByRule() {
        Long id = dispatchTaskService.createDispatchTask(buildReqVO(null));

        DispatchTaskDO dispatchTask = dispatchTaskMapper.selectById(id);
        assertNotNull(dispatchTask);
        assertEquals("DSPT0001", dispatchTask.getDispatchNo());
        assertEquals(MesConstants.NONE_ID, dispatchTask.getTeamId());
    }

    @Test
    void testCreateDispatchTask_opTaskNotExists() {
        DispatchTaskSaveReqVO reqVO = buildReqVO("DSPT-A");
        reqVO.setOpTaskId(randomLongId());

        assertServiceException(() -> dispatchTaskService.createDispatchTask(reqVO), OP_TASK_NOT_EXISTS);
    }

    @Test
    void testCreateDispatchTask_operatorAndTeamBothEmpty() {
        DispatchTaskSaveReqVO reqVO = buildReqVO("DSPT-A");
        reqVO.setOperatorId(MesConstants.NONE_ID);
        reqVO.setTeamId(MesConstants.NONE_ID);

        // 两个都空这条派工没有接收方
        assertServiceException(() -> dispatchTaskService.createDispatchTask(reqVO),
                DISPATCH_TASK_OPERATOR_REQUIRED);
    }

    @Test
    void testCreateDispatchTask_teamOnlyAccepted() {
        DispatchTaskSaveReqVO reqVO = buildReqVO("DSPT-A");
        reqVO.setOperatorId(MesConstants.NONE_ID);
        reqVO.setTeamId(3L);

        assertNotNull(dispatchTaskService.createDispatchTask(reqVO));
    }

    @Test
    void testCreateDispatchTask_endBeforeStartRejected() {
        DispatchTaskSaveReqVO reqVO = buildReqVO("DSPT-A");
        reqVO.setPlannedEndTime(reqVO.getPlannedStartTime().minusMinutes(1));

        assertServiceException(() -> dispatchTaskService.createDispatchTask(reqVO), DISPATCH_TASK_TIME_INVALID);
    }

    @Test
    void testUpdateDispatchTask_keepDispatchNo() {
        Long id = dispatchTaskService.createDispatchTask(buildReqVO("DSPT-A"));

        DispatchTaskSaveReqVO updateReqVO = buildReqVO("DSPT-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("改派资源");
        dispatchTaskService.updateDispatchTask(updateReqVO);

        DispatchTaskDO dispatchTask = dispatchTaskMapper.selectById(id);
        assertEquals("DSPT-A", dispatchTask.getDispatchNo());
        assertEquals("改派资源", dispatchTask.getRemark());
    }

    @Test
    void testUpdateDispatchTask_acceptedRejected() {
        DispatchTaskSaveReqVO createReqVO = buildReqVO("DSPT-A");
        createReqVO.setStatus(MesDispatchTaskStatusEnum.ACCEPTED.name());
        Long id = dispatchTaskService.createDispatchTask(createReqVO);

        DispatchTaskSaveReqVO updateReqVO = buildReqVO("DSPT-A");
        updateReqVO.setId(id);
        // 已接单的派工现场已经按它开工了，不允许改派
        assertServiceException(() -> dispatchTaskService.updateDispatchTask(updateReqVO),
                DISPATCH_TASK_STATUS_INVALID);
    }

    @Test
    void testUpdateDispatchTask_notExists() {
        DispatchTaskSaveReqVO updateReqVO = buildReqVO("DSPT-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> dispatchTaskService.updateDispatchTask(updateReqVO), DISPATCH_TASK_NOT_EXISTS);
    }

    @Test
    void testDeleteDispatchTask_success() {
        Long id = dispatchTaskService.createDispatchTask(buildReqVO("DSPT-A"));

        dispatchTaskService.deleteDispatchTask(id);

        assertNull(dispatchTaskMapper.selectById(id));
    }

    @Test
    void testDeleteDispatchTask_acceptedRejected() {
        DispatchTaskSaveReqVO createReqVO = buildReqVO("DSPT-A");
        createReqVO.setStatus(MesDispatchTaskStatusEnum.RUNNING.name());
        Long id = dispatchTaskService.createDispatchTask(createReqVO);

        assertServiceException(() -> dispatchTaskService.deleteDispatchTask(id), DISPATCH_TASK_STATUS_INVALID);
    }

    @Test
    void testDeleteDispatchTaskList_acceptedRejected() {
        Long first = dispatchTaskService.createDispatchTask(buildReqVO("DSPT-A"));
        DispatchTaskSaveReqVO running = buildReqVO("DSPT-B");
        running.setStatus(MesDispatchTaskStatusEnum.RUNNING.name());
        Long second = dispatchTaskService.createDispatchTask(running);

        assertServiceException(() -> dispatchTaskService.deleteDispatchTaskListByIds(List.of(first, second)),
                DISPATCH_TASK_STATUS_INVALID);
        assertNotNull(dispatchTaskMapper.selectById(first));
    }

    @Test
    void testGetDispatchTaskPage() {
        dispatchTaskService.createDispatchTask(buildReqVO("DSPT-A"));

        DispatchTaskPageReqVO reqVO = new DispatchTaskPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<DispatchTaskRespVO> list = dispatchTaskService.getDispatchTaskPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("DSPT-A", list.get(0).getDispatchNo());
        // 工序任务号由模块内 JOIN 出来
        assertEquals("OPT-0001", list.get(0).getOpTaskNo());
    }

}
