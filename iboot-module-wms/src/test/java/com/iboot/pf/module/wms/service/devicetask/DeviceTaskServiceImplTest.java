package com.iboot.pf.module.wms.service.devicetask;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.devicetask.vo.DeviceTaskSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.devicetask.DeviceTaskDO;
import com.iboot.pf.module.wms.dal.mysql.devicetask.DeviceTaskMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsDeviceTaskStatusEnum;
import com.iboot.pf.module.wms.enums.WmsDeviceTaskTypeEnum;
import com.iboot.pf.module.wms.enums.WmsDeviceTypeEnum;
import com.iboot.pf.module.wms.enums.WmsDispatchStatusEnum;
import com.iboot.pf.module.wms.enums.WmsPostStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.DEVICE_TASK_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.DEVICE_TASK_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 设备搬运任务 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(DeviceTaskServiceImpl.class)
class DeviceTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DeviceTaskService deviceTaskService;
    @Resource
    private DeviceTaskMapper deviceTaskMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "DT0001" : invocation.getArgument(1));
    }

    private DeviceTaskSaveReqVO buildReqVO(String taskNo) {
        DeviceTaskSaveReqVO reqVO = new DeviceTaskSaveReqVO();
        reqVO.setTaskNo(taskNo);
        reqVO.setTaskType(WmsDeviceTaskTypeEnum.PUTAWAY.name());
        reqVO.setDeviceType(WmsDeviceTypeEnum.WCS.name());
        reqVO.setSourceBizType(WmsSourceBizTypeEnum.IN_ORDER.name());
        reqVO.setSourceBizNo("IN-0001");
        reqVO.setContainerId(1L);
        reqVO.setFromLocId(1L);
        reqVO.setToLocId(2L);
        reqVO.setActualLocId(0L);
        reqVO.setExternalSystemCode("WCS-01");
        reqVO.setDispatchStatus(WmsDispatchStatusEnum.PENDING.name());
        reqVO.setStatus(WmsDeviceTaskStatusEnum.CREATED.name());
        reqVO.setPostStatus(WmsPostStatusEnum.PENDING.name());
        reqVO.setIdempotencyKey("WMS:IN-0001:PUTAWAY");
        reqVO.setVersion(0);
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateDeviceTask_blankCodeFilledByRule() {
        Long id = deviceTaskService.createDeviceTask(buildReqVO(null));

        DeviceTaskDO entity = deviceTaskMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("DT0001", entity.getTaskNo());
    }

    @Test
    void testCreateDeviceTask_duplicateRejected() {
        deviceTaskService.createDeviceTask(buildReqVO("DT0-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> deviceTaskService.createDeviceTask(buildReqVO("DT0-A")), DEVICE_TASK_NO_DUPLICATE);
    }

    @Test
    void testUpdateDeviceTask_keepOwnCode() {
        Long id = deviceTaskService.createDeviceTask(buildReqVO("DT0-A"));

        // 改自己时业务编码没变，不应该撞到自己
        DeviceTaskSaveReqVO updateReqVO = buildReqVO("DT0-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        deviceTaskService.updateDeviceTask(updateReqVO);

        assertEquals("规则调整", deviceTaskMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateDeviceTask_duplicateRejected() {
        deviceTaskService.createDeviceTask(buildReqVO("DT0-A"));
        Long secondId = deviceTaskService.createDeviceTask(buildReqVO("DT0-B"));

        DeviceTaskSaveReqVO updateReqVO = buildReqVO("DT0-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> deviceTaskService.updateDeviceTask(updateReqVO), DEVICE_TASK_NO_DUPLICATE);
    }

    @Test
    void testUpdateDeviceTask_notExists() {
        DeviceTaskSaveReqVO updateReqVO = buildReqVO("DT0-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> deviceTaskService.updateDeviceTask(updateReqVO), DEVICE_TASK_NOT_EXISTS);
    }

    @Test
    void testDeleteDeviceTask_success() {
        Long id = deviceTaskService.createDeviceTask(buildReqVO("DT0-A"));

        deviceTaskService.deleteDeviceTask(id);

        assertNull(deviceTaskMapper.selectById(id));
    }

    @Test
    void testDeleteDeviceTask_notExists() {
        assertServiceException(() -> deviceTaskService.deleteDeviceTask(randomLongId()), DEVICE_TASK_NOT_EXISTS);
    }

}
