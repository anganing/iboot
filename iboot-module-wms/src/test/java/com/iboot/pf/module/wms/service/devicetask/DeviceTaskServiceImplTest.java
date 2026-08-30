package com.iboot.pf.module.wms.service.devicetask;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.devicetask.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.devicetask.DeviceTaskDO;
import com.iboot.pf.module.wms.dal.mysql.devicetask.DeviceTaskMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link DeviceTaskServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(DeviceTaskServiceImpl.class)
public class DeviceTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DeviceTaskServiceImpl deviceTaskService;

    @Resource
    private DeviceTaskMapper deviceTaskMapper;

    @Test
    public void testCreateDeviceTask_success() {
        // 准备参数
        DeviceTaskSaveReqVO createReqVO = randomPojo(DeviceTaskSaveReqVO.class).setId(null);

        // 调用
        Long deviceTaskId = deviceTaskService.createDeviceTask(createReqVO);
        // 断言
        assertNotNull(deviceTaskId);
        // 校验记录的属性是否正确
        DeviceTaskDO deviceTask = deviceTaskMapper.selectById(deviceTaskId);
        assertPojoEquals(createReqVO, deviceTask, "id");
    }

    @Test
    public void testUpdateDeviceTask_success() {
        // mock 数据
        DeviceTaskDO dbDeviceTask = randomPojo(DeviceTaskDO.class);
        deviceTaskMapper.insert(dbDeviceTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DeviceTaskSaveReqVO updateReqVO = randomPojo(DeviceTaskSaveReqVO.class, o -> {
            o.setId(dbDeviceTask.getId()); // 设置更新的 ID
        });

        // 调用
        deviceTaskService.updateDeviceTask(updateReqVO);
        // 校验是否更新正确
        DeviceTaskDO deviceTask = deviceTaskMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, deviceTask);
    }

    @Test
    public void testUpdateDeviceTask_notExists() {
        // 准备参数
        DeviceTaskSaveReqVO updateReqVO = randomPojo(DeviceTaskSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> deviceTaskService.updateDeviceTask(updateReqVO), DEVICE_TASK_NOT_EXISTS);
    }

    @Test
    public void testDeleteDeviceTask_success() {
        // mock 数据
        DeviceTaskDO dbDeviceTask = randomPojo(DeviceTaskDO.class);
        deviceTaskMapper.insert(dbDeviceTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDeviceTask.getId();

        // 调用
        deviceTaskService.deleteDeviceTask(id);
       // 校验数据不存在了
       assertNull(deviceTaskMapper.selectById(id));
    }

    @Test
    public void testDeleteDeviceTask_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> deviceTaskService.deleteDeviceTask(id), DEVICE_TASK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDeviceTaskPage() {
       // mock 数据
       DeviceTaskDO dbDeviceTask = randomPojo(DeviceTaskDO.class, o -> { // 等会查询到
           o.setTaskNo(null);
           o.setExternalTaskNo(null);
           o.setTaskType(null);
           o.setDeviceType(null);
           o.setSourceBizType(null);
           o.setSourceBizNo(null);
           o.setSourceLineId(null);
           o.setContainerId(null);
           o.setFromLocId(null);
           o.setToLocId(null);
           o.setActualLocId(null);
           o.setExternalSystemCode(null);
           o.setDispatchStatus(null);
           o.setStatus(null);
           o.setPostStatus(null);
           o.setRequestSnapshotJson(null);
           o.setCallbackSnapshotJson(null);
           o.setIdempotencyKey(null);
           o.setCompletedTime(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       deviceTaskMapper.insert(dbDeviceTask);
       // 测试 taskNo 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setTaskNo(null)));
       // 测试 externalTaskNo 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setExternalTaskNo(null)));
       // 测试 taskType 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setTaskType(null)));
       // 测试 deviceType 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setDeviceType(null)));
       // 测试 sourceBizType 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setSourceBizType(null)));
       // 测试 sourceBizNo 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setSourceBizNo(null)));
       // 测试 sourceLineId 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setSourceLineId(null)));
       // 测试 containerId 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setContainerId(null)));
       // 测试 fromLocId 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setFromLocId(null)));
       // 测试 toLocId 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setToLocId(null)));
       // 测试 actualLocId 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setActualLocId(null)));
       // 测试 externalSystemCode 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setExternalSystemCode(null)));
       // 测试 dispatchStatus 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setDispatchStatus(null)));
       // 测试 status 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setStatus(null)));
       // 测试 postStatus 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setPostStatus(null)));
       // 测试 requestSnapshotJson 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setRequestSnapshotJson(null)));
       // 测试 callbackSnapshotJson 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setCallbackSnapshotJson(null)));
       // 测试 idempotencyKey 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setIdempotencyKey(null)));
       // 测试 completedTime 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setCompletedTime(null)));
       // 测试 version 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       deviceTaskMapper.insert(cloneIgnoreId(dbDeviceTask, o -> o.setCreateTime(null)));
       // 准备参数
       DeviceTaskPageReqVO reqVO = new DeviceTaskPageReqVO();
       reqVO.setTaskNo(null);
       reqVO.setExternalTaskNo(null);
       reqVO.setTaskType(null);
       reqVO.setDeviceType(null);
       reqVO.setSourceBizType(null);
       reqVO.setSourceBizNo(null);
       reqVO.setSourceLineId(null);
       reqVO.setContainerId(null);
       reqVO.setFromLocId(null);
       reqVO.setToLocId(null);
       reqVO.setActualLocId(null);
       reqVO.setExternalSystemCode(null);
       reqVO.setDispatchStatus(null);
       reqVO.setStatus(null);
       reqVO.setPostStatus(null);
       reqVO.setRequestSnapshotJson(null);
       reqVO.setCallbackSnapshotJson(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setCompletedTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<DeviceTaskRespVO> pageResult = deviceTaskService.getDeviceTaskPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDeviceTask, pageResult.getList().get(0));
    }

}