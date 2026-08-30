package com.iboot.pf.module.wms.service.devicetask;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.devicetask.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.devicetask.DeviceTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.devicetask.DeviceTaskMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-设备搬运任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DeviceTaskServiceImpl implements DeviceTaskService {

    @Resource
    private DeviceTaskMapper deviceTaskMapper;

    @Override
    public Long createDeviceTask(DeviceTaskSaveReqVO createReqVO) {
        // 插入
        DeviceTaskDO deviceTask = BeanUtils.toBean(createReqVO, DeviceTaskDO.class);
        deviceTaskMapper.insert(deviceTask);

        // 返回
        return deviceTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public DeviceTaskImportRespVO importDeviceTaskList(List<DeviceTaskImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return DeviceTaskImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        DeviceTaskImportRespVO respVO = DeviceTaskImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createDeviceTask(BeanUtils.toBean(importItem, DeviceTaskSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateDeviceTask(DeviceTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateDeviceTaskExists(updateReqVO.getId());
        // 更新
        DeviceTaskDO updateObj = BeanUtils.toBean(updateReqVO, DeviceTaskDO.class);
        deviceTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceTask(Long id) {
        // 校验存在
        validateDeviceTaskExists(id);
        // 删除
        deviceTaskMapper.deleteById(id);
    }

    @Override
        public void deleteDeviceTaskListByIds(List<Long> ids) {
        // 删除
        deviceTaskMapper.deleteByIds(ids);
        }


    private void validateDeviceTaskExists(Long id) {
        if (deviceTaskMapper.selectById(id) == null) {
            throw exception(DEVICE_TASK_NOT_EXISTS);
        }
    }

    @Override
    public DeviceTaskRespVO getDeviceTask(Long id) {
        return deviceTaskMapper.selectJoinById(id);
    }

    @Override
    public PageResult<DeviceTaskRespVO> getDeviceTaskPage(DeviceTaskPageReqVO pageReqVO) {
        return deviceTaskMapper.selectPage(pageReqVO);
    }

}