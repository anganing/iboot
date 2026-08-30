package com.iboot.pf.module.aps.service.task;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.task.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.task.TaskDO;
import com.iboot.pf.module.aps.dal.mysql.task.TaskMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link TaskServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(TaskServiceImpl.class)
public class TaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TaskServiceImpl taskService;

    @Resource
    private TaskMapper taskMapper;

    @Test
    public void testCreateTask_success() {
        // 准备参数
        TaskSaveReqVO createReqVO = randomPojo(TaskSaveReqVO.class).setId(null);

        // 调用
        Long taskId = taskService.createTask(createReqVO);
        // 断言
        assertNotNull(taskId);
        // 校验记录的属性是否正确
        TaskDO task = taskMapper.selectById(taskId);
        assertPojoEquals(createReqVO, task, "id");
    }

    @Test
    public void testUpdateTask_success() {
        // mock 数据
        TaskDO dbTask = randomPojo(TaskDO.class);
        taskMapper.insert(dbTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        TaskSaveReqVO updateReqVO = randomPojo(TaskSaveReqVO.class, o -> {
            o.setId(dbTask.getId()); // 设置更新的 ID
        });

        // 调用
        taskService.updateTask(updateReqVO);
        // 校验是否更新正确
        TaskDO task = taskMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, task);
    }

    @Test
    public void testUpdateTask_notExists() {
        // 准备参数
        TaskSaveReqVO updateReqVO = randomPojo(TaskSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> taskService.updateTask(updateReqVO), TASK_NOT_EXISTS);
    }

    @Test
    public void testDeleteTask_success() {
        // mock 数据
        TaskDO dbTask = randomPojo(TaskDO.class);
        taskMapper.insert(dbTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTask.getId();

        // 调用
        taskService.deleteTask(id);
       // 校验数据不存在了
       assertNull(taskMapper.selectById(id));
    }

    @Test
    public void testDeleteTask_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> taskService.deleteTask(id), TASK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetTaskPage() {
       // mock 数据
       TaskDO dbTask = randomPojo(TaskDO.class, o -> { // 等会查询到
           o.setVersionId(null);
           o.setTaskNo(null);
           o.setProdOrderId(null);
           o.setProdOrderNo(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setRouteOpId(null);
           o.setOpCode(null);
           o.setOpName(null);
           o.setPlantId(null);
           o.setWorkshopId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setResId(null);
           o.setResCode(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setDurationMinutes(null);
           o.setSequenceNo(null);
           o.setLockedFlag(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       taskMapper.insert(dbTask);
       // 测试 versionId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setVersionId(null)));
       // 测试 taskNo 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setTaskNo(null)));
       // 测试 prodOrderId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setProdOrderId(null)));
       // 测试 prodOrderNo 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setProdOrderNo(null)));
       // 测试 workOrderId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setOpTaskId(null)));
       // 测试 routeOpId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setRouteOpId(null)));
       // 测试 opCode 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setOpCode(null)));
       // 测试 opName 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setOpName(null)));
       // 测试 plantId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setPlantId(null)));
       // 测试 workshopId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setWorkshopId(null)));
       // 测试 projectId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setProjectTaskId(null)));
       // 测试 resId 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setResId(null)));
       // 测试 resCode 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setResCode(null)));
       // 测试 startTime 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setEndTime(null)));
       // 测试 durationMinutes 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setDurationMinutes(null)));
       // 测试 sequenceNo 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setSequenceNo(null)));
       // 测试 lockedFlag 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setLockedFlag(null)));
       // 测试 status 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       taskMapper.insert(cloneIgnoreId(dbTask, o -> o.setCreateTime(null)));
       // 准备参数
       TaskPageReqVO reqVO = new TaskPageReqVO();
       reqVO.setVersionId(null);
       reqVO.setTaskNo(null);
       reqVO.setProdOrderId(null);
       reqVO.setProdOrderNo(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setRouteOpId(null);
       reqVO.setOpCode(null);
       reqVO.setOpName(null);
       reqVO.setPlantId(null);
       reqVO.setWorkshopId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setResId(null);
       reqVO.setResCode(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDurationMinutes(null);
       reqVO.setSequenceNo(null);
       reqVO.setLockedFlag(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<TaskRespVO> pageResult = taskService.getTaskPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbTask, pageResult.getList().get(0));
    }

}