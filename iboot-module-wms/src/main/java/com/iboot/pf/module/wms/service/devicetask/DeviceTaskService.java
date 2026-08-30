package com.iboot.pf.module.wms.service.devicetask;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.devicetask.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.devicetask.DeviceTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-设备搬运任务 Service 接口
 *
 * @author iboot
 */
public interface DeviceTaskService {

    /**
     * 创建WMS-设备搬运任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeviceTask(@Valid DeviceTaskSaveReqVO createReqVO);

    /**
     * 导入WMS-设备搬运任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    DeviceTaskImportRespVO importDeviceTaskList(List<DeviceTaskImportExcelVO> importList);

    /**
     * 更新WMS-设备搬运任务
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceTask(@Valid DeviceTaskSaveReqVO updateReqVO);

    /**
     * 删除WMS-设备搬运任务
     *
     * @param id 编号
     */
    void deleteDeviceTask(Long id);

    /**
    * 批量删除WMS-设备搬运任务
    *
    * @param ids 编号
    */
    void deleteDeviceTaskListByIds(List<Long> ids);

    /**
     * 获得WMS-设备搬运任务
     *
     * @param id 编号
     * @return WMS-设备搬运任务
     */
    DeviceTaskRespVO getDeviceTask(Long id);

    /**
     * 获得WMS-设备搬运任务分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-设备搬运任务分页
     */
    PageResult<DeviceTaskRespVO> getDeviceTaskPage(DeviceTaskPageReqVO pageReqVO);

}