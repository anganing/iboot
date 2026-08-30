package com.iboot.pf.module.aps.service.taskdep;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.taskdep.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.TaskDepDO;
import com.iboot.pf.module.aps.dal.mysql.taskdep.TaskDepMapper;
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
 * {@link TaskDepServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(TaskDepServiceImpl.class)
public class TaskDepServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TaskDepServiceImpl taskDepService;

    @Resource
    private TaskDepMapper taskDepMapper;

    @Test
    public void testCreateTaskDep_success() {
        // 准备参数
        TaskDepSaveReqVO createReqVO = randomPojo(TaskDepSaveReqVO.class).setId(null);

        // 调用
        Long taskDepId = taskDepService.createTaskDep(createReqVO);
        // 断言
        assertNotNull(taskDepId);
        // 校验记录的属性是否正确
        TaskDepDO taskDep = taskDepMapper.selectById(taskDepId);
        assertPojoEquals(createReqVO, taskDep, "id");
    }

    @Test
    public void testUpdateTaskDep_success() {
        // mock 数据
        TaskDepDO dbTaskDep = randomPojo(TaskDepDO.class);
        taskDepMapper.insert(dbTaskDep);// @Sql: 先插入出一条存在的数据
        // 准备参数
        TaskDepSaveReqVO updateReqVO = randomPojo(TaskDepSaveReqVO.class, o -> {
            o.setId(dbTaskDep.getId()); // 设置更新的 ID
        });

        // 调用
        taskDepService.updateTaskDep(updateReqVO);
        // 校验是否更新正确
        TaskDepDO taskDep = taskDepMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, taskDep);
    }

    @Test
    public void testUpdateTaskDep_notExists() {
        // 准备参数
        TaskDepSaveReqVO updateReqVO = randomPojo(TaskDepSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> taskDepService.updateTaskDep(updateReqVO), TASK_DEP_NOT_EXISTS);
    }

    @Test
    public void testDeleteTaskDep_success() {
        // mock 数据
        TaskDepDO dbTaskDep = randomPojo(TaskDepDO.class);
        taskDepMapper.insert(dbTaskDep);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTaskDep.getId();

        // 调用
        taskDepService.deleteTaskDep(id);
       // 校验数据不存在了
       assertNull(taskDepMapper.selectById(id));
    }

    @Test
    public void testDeleteTaskDep_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> taskDepService.deleteTaskDep(id), TASK_DEP_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetTaskDepPage() {
       // mock 数据
       TaskDepDO dbTaskDep = randomPojo(TaskDepDO.class, o -> { // 等会查询到
           o.setVersionId(null);
           o.setFromTaskId(null);
           o.setToTaskId(null);
           o.setDepType(null);
           o.setLagMinutes(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       taskDepMapper.insert(dbTaskDep);
       // 测试 versionId 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setVersionId(null)));
       // 测试 fromTaskId 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setFromTaskId(null)));
       // 测试 toTaskId 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setToTaskId(null)));
       // 测试 depType 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setDepType(null)));
       // 测试 lagMinutes 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setLagMinutes(null)));
       // 测试 remark 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       taskDepMapper.insert(cloneIgnoreId(dbTaskDep, o -> o.setCreateTime(null)));
       // 准备参数
       TaskDepPageReqVO reqVO = new TaskDepPageReqVO();
       reqVO.setVersionId(null);
       reqVO.setFromTaskId(null);
       reqVO.setToTaskId(null);
       reqVO.setDepType(null);
       reqVO.setLagMinutes(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<TaskDepRespVO> pageResult = taskDepService.getTaskDepPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbTaskDep, pageResult.getList().get(0));
    }

}