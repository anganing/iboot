package com.iboot.pf.module.mes.service.rgvtask;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.*;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.dal.mysql.rgvtask.RgvTaskMapper;
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
 * {@link RgvTaskServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(RgvTaskServiceImpl.class)
public class RgvTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private RgvTaskServiceImpl rgvTaskService;

    @Resource
    private RgvTaskMapper rgvTaskMapper;

    @Test
    public void testCreateRgvTask_success() {
        // 准备参数
        RgvTaskSaveReqVO createReqVO = randomPojo(RgvTaskSaveReqVO.class).setId(null);

        // 调用
        Long rgvTaskId = rgvTaskService.createRgvTask(createReqVO);
        // 断言
        assertNotNull(rgvTaskId);
        // 校验记录的属性是否正确
        RgvTaskDO rgvTask = rgvTaskMapper.selectById(rgvTaskId);
        assertPojoEquals(createReqVO, rgvTask, "id");
    }

    @Test
    public void testUpdateRgvTask_success() {
        // mock 数据
        RgvTaskDO dbRgvTask = randomPojo(RgvTaskDO.class);
        rgvTaskMapper.insert(dbRgvTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        RgvTaskSaveReqVO updateReqVO = randomPojo(RgvTaskSaveReqVO.class, o -> {
            o.setId(dbRgvTask.getId()); // 设置更新的 ID
        });

        // 调用
        rgvTaskService.updateRgvTask(updateReqVO);
        // 校验是否更新正确
        RgvTaskDO rgvTask = rgvTaskMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, rgvTask);
    }

    @Test
    public void testUpdateRgvTask_notExists() {
        // 准备参数
        RgvTaskSaveReqVO updateReqVO = randomPojo(RgvTaskSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> rgvTaskService.updateRgvTask(updateReqVO), RGV_TASK_NOT_EXISTS);
    }

    @Test
    public void testDeleteRgvTask_success() {
        // mock 数据
        RgvTaskDO dbRgvTask = randomPojo(RgvTaskDO.class);
        rgvTaskMapper.insert(dbRgvTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRgvTask.getId();

        // 调用
        rgvTaskService.deleteRgvTask(id);
       // 校验数据不存在了
       assertNull(rgvTaskMapper.selectById(id));
    }

    @Test
    public void testDeleteRgvTask_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> rgvTaskService.deleteRgvTask(id), RGV_TASK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetRgvTaskPage() {
       // mock 数据
       RgvTaskDO dbRgvTask = randomPojo(RgvTaskDO.class, o -> { // 等会查询到
           o.setTaskNo(null);
           o.setExternalTaskNo(null);
           o.setTaskType(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setTargetOpTaskId(null);
           o.setFromStationId(null);
           o.setToStationId(null);
           o.setCarrierNo(null);
           o.setExternalSystemCode(null);
           o.setDispatchStatus(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setCompletedTime(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       rgvTaskMapper.insert(dbRgvTask);
       // 测试 taskNo 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setTaskNo(null)));
       // 测试 externalTaskNo 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setExternalTaskNo(null)));
       // 测试 taskType 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setTaskType(null)));
       // 测试 workOrderId 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setOpTaskId(null)));
       // 测试 targetOpTaskId 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setTargetOpTaskId(null)));
       // 测试 fromStationId 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setFromStationId(null)));
       // 测试 toStationId 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setToStationId(null)));
       // 测试 carrierNo 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setCarrierNo(null)));
       // 测试 externalSystemCode 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setExternalSystemCode(null)));
       // 测试 dispatchStatus 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setDispatchStatus(null)));
       // 测试 status 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setIdempotencyKey(null)));
       // 测试 completedTime 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setCompletedTime(null)));
       // 测试 version 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       rgvTaskMapper.insert(cloneIgnoreId(dbRgvTask, o -> o.setCreateTime(null)));
       // 准备参数
       RgvTaskPageReqVO reqVO = new RgvTaskPageReqVO();
       reqVO.setTaskNo(null);
       reqVO.setExternalTaskNo(null);
       reqVO.setTaskType(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setTargetOpTaskId(null);
       reqVO.setFromStationId(null);
       reqVO.setToStationId(null);
       reqVO.setCarrierNo(null);
       reqVO.setExternalSystemCode(null);
       reqVO.setDispatchStatus(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setCompletedTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<RgvTaskRespVO> pageResult = rgvTaskService.getRgvTaskPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbRgvTask, pageResult.getList().get(0));
    }

}