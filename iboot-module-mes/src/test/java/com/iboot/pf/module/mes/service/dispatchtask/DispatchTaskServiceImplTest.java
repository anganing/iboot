package com.iboot.pf.module.mes.service.dispatchtask;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.*;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link DispatchTaskServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(DispatchTaskServiceImpl.class)
public class DispatchTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DispatchTaskServiceImpl dispatchTaskService;

    @Resource
    private DispatchTaskMapper dispatchTaskMapper;

    @Test
    public void testCreateDispatchTask_success() {
        // 准备参数
        DispatchTaskSaveReqVO createReqVO = randomPojo(DispatchTaskSaveReqVO.class).setId(null);

        // 调用
        Long dispatchTaskId = dispatchTaskService.createDispatchTask(createReqVO);
        // 断言
        assertNotNull(dispatchTaskId);
        // 校验记录的属性是否正确
        DispatchTaskDO dispatchTask = dispatchTaskMapper.selectById(dispatchTaskId);
        assertPojoEquals(createReqVO, dispatchTask, "id");
    }

    @Test
    public void testUpdateDispatchTask_success() {
        // mock 数据
        DispatchTaskDO dbDispatchTask = randomPojo(DispatchTaskDO.class);
        dispatchTaskMapper.insert(dbDispatchTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DispatchTaskSaveReqVO updateReqVO = randomPojo(DispatchTaskSaveReqVO.class, o -> {
            o.setId(dbDispatchTask.getId()); // 设置更新的 ID
        });

        // 调用
        dispatchTaskService.updateDispatchTask(updateReqVO);
        // 校验是否更新正确
        DispatchTaskDO dispatchTask = dispatchTaskMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, dispatchTask);
    }

    @Test
    public void testUpdateDispatchTask_notExists() {
        // 准备参数
        DispatchTaskSaveReqVO updateReqVO = randomPojo(DispatchTaskSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> dispatchTaskService.updateDispatchTask(updateReqVO), DISPATCH_TASK_NOT_EXISTS);
    }

    @Test
    public void testDeleteDispatchTask_success() {
        // mock 数据
        DispatchTaskDO dbDispatchTask = randomPojo(DispatchTaskDO.class);
        dispatchTaskMapper.insert(dbDispatchTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDispatchTask.getId();

        // 调用
        dispatchTaskService.deleteDispatchTask(id);
       // 校验数据不存在了
       assertNull(dispatchTaskMapper.selectById(id));
    }

    @Test
    public void testDeleteDispatchTask_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> dispatchTaskService.deleteDispatchTask(id), DISPATCH_TASK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDispatchTaskPage() {
       // mock 数据
       DispatchTaskDO dbDispatchTask = randomPojo(DispatchTaskDO.class, o -> { // 等会查询到
           o.setDispatchNo(null);
           o.setOpTaskId(null);
           o.setOperatorId(null);
           o.setTeamId(null);
           o.setResId(null);
           o.setShiftId(null);
           o.setPlannedStartTime(null);
           o.setPlannedEndTime(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       dispatchTaskMapper.insert(dbDispatchTask);
       // 测试 dispatchNo 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setDispatchNo(null)));
       // 测试 opTaskId 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setOpTaskId(null)));
       // 测试 operatorId 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setOperatorId(null)));
       // 测试 teamId 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setTeamId(null)));
       // 测试 resId 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setResId(null)));
       // 测试 shiftId 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setShiftId(null)));
       // 测试 plannedStartTime 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setPlannedStartTime(null)));
       // 测试 plannedEndTime 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setPlannedEndTime(null)));
       // 测试 status 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       dispatchTaskMapper.insert(cloneIgnoreId(dbDispatchTask, o -> o.setCreateTime(null)));
       // 准备参数
       DispatchTaskPageReqVO reqVO = new DispatchTaskPageReqVO();
       reqVO.setDispatchNo(null);
       reqVO.setOpTaskId(null);
       reqVO.setOperatorId(null);
       reqVO.setTeamId(null);
       reqVO.setResId(null);
       reqVO.setShiftId(null);
       reqVO.setPlannedStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPlannedEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<DispatchTaskRespVO> pageResult = dispatchTaskService.getDispatchTaskPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDispatchTask, pageResult.getList().get(0));
    }

}