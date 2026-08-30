package com.iboot.pf.module.aps.service.tasklock;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.TaskLockDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * APS-排程任务锁定记录 Service 接口
 *
 * @author iboot
 */
public interface TaskLockService {

    /**
     * 创建APS-排程任务锁定记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTaskLock(@Valid TaskLockSaveReqVO createReqVO);

    /**
     * 导入APS-排程任务锁定记录
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    TaskLockImportRespVO importTaskLockList(List<TaskLockImportExcelVO> importList);

    /**
     * 更新APS-排程任务锁定记录
     *
     * @param updateReqVO 更新信息
     */
    void updateTaskLock(@Valid TaskLockSaveReqVO updateReqVO);

    /**
     * 删除APS-排程任务锁定记录
     *
     * @param id 编号
     */
    void deleteTaskLock(Long id);

    /**
    * 批量删除APS-排程任务锁定记录
    *
    * @param ids 编号
    */
    void deleteTaskLockListByIds(List<Long> ids);

    /**
     * 获得APS-排程任务锁定记录
     *
     * @param id 编号
     * @return APS-排程任务锁定记录
     */
    TaskLockRespVO getTaskLock(Long id);

    /**
     * 获得APS-排程任务锁定记录分页
     *
     * @param pageReqVO 分页查询
     * @return APS-排程任务锁定记录分页
     */
    PageResult<TaskLockRespVO> getTaskLockPage(TaskLockPageReqVO pageReqVO);

}