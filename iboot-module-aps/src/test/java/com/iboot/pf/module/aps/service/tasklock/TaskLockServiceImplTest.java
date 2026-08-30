package com.iboot.pf.module.aps.service.tasklock;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.tasklock.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.TaskLockDO;
import com.iboot.pf.module.aps.dal.mysql.tasklock.TaskLockMapper;
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
 * {@link TaskLockServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(TaskLockServiceImpl.class)
public class TaskLockServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TaskLockServiceImpl taskLockService;

    @Resource
    private TaskLockMapper taskLockMapper;

    @Test
    public void testCreateTaskLock_success() {
        // 准备参数
        TaskLockSaveReqVO createReqVO = randomPojo(TaskLockSaveReqVO.class).setId(null);

        // 调用
        Long taskLockId = taskLockService.createTaskLock(createReqVO);
        // 断言
        assertNotNull(taskLockId);
        // 校验记录的属性是否正确
        TaskLockDO taskLock = taskLockMapper.selectById(taskLockId);
        assertPojoEquals(createReqVO, taskLock, "id");
    }

    @Test
    public void testUpdateTaskLock_success() {
        // mock 数据
        TaskLockDO dbTaskLock = randomPojo(TaskLockDO.class);
        taskLockMapper.insert(dbTaskLock);// @Sql: 先插入出一条存在的数据
        // 准备参数
        TaskLockSaveReqVO updateReqVO = randomPojo(TaskLockSaveReqVO.class, o -> {
            o.setId(dbTaskLock.getId()); // 设置更新的 ID
        });

        // 调用
        taskLockService.updateTaskLock(updateReqVO);
        // 校验是否更新正确
        TaskLockDO taskLock = taskLockMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, taskLock);
    }

    @Test
    public void testUpdateTaskLock_notExists() {
        // 准备参数
        TaskLockSaveReqVO updateReqVO = randomPojo(TaskLockSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> taskLockService.updateTaskLock(updateReqVO), TASK_LOCK_NOT_EXISTS);
    }

    @Test
    public void testDeleteTaskLock_success() {
        // mock 数据
        TaskLockDO dbTaskLock = randomPojo(TaskLockDO.class);
        taskLockMapper.insert(dbTaskLock);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTaskLock.getId();

        // 调用
        taskLockService.deleteTaskLock(id);
       // 校验数据不存在了
       assertNull(taskLockMapper.selectById(id));
    }

    @Test
    public void testDeleteTaskLock_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> taskLockService.deleteTaskLock(id), TASK_LOCK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetTaskLockPage() {
       // mock 数据
       TaskLockDO dbTaskLock = randomPojo(TaskLockDO.class, o -> { // 等会查询到
           o.setTaskId(null);
           o.setLockType(null);
           o.setReason(null);
           o.setLockedBy(null);
           o.setLockedTime(null);
           o.setUnlockTime(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       taskLockMapper.insert(dbTaskLock);
       // 测试 taskId 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setTaskId(null)));
       // 测试 lockType 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setLockType(null)));
       // 测试 reason 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setReason(null)));
       // 测试 lockedBy 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setLockedBy(null)));
       // 测试 lockedTime 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setLockedTime(null)));
       // 测试 unlockTime 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setUnlockTime(null)));
       // 测试 status 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       taskLockMapper.insert(cloneIgnoreId(dbTaskLock, o -> o.setCreateTime(null)));
       // 准备参数
       TaskLockPageReqVO reqVO = new TaskLockPageReqVO();
       reqVO.setTaskId(null);
       reqVO.setLockType(null);
       reqVO.setReason(null);
       reqVO.setLockedBy(null);
       reqVO.setLockedTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setUnlockTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<TaskLockRespVO> pageResult = taskLockService.getTaskLockPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbTaskLock, pageResult.getList().get(0));
    }

}