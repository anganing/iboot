package com.iboot.pf.module.aps.service.tasklock;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.TaskLockDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 任务锁定 Service 接口
 *
 * @author iboot
 */
public interface TaskLockService {

    /**
     * 创建任务锁定
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTaskLock(@Valid TaskLockSaveReqVO createReqVO);

    /**
     * 导入任务锁定
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    TaskLockImportRespVO importTaskLockList(List<TaskLockImportExcelVO> importList);

    /**
     * 更新任务锁定
     *
     * @param updateReqVO 更新信息
     */
    void updateTaskLock(@Valid TaskLockSaveReqVO updateReqVO);

    /**
     * 删除任务锁定
     *
     * @param id 编号
     */
    void deleteTaskLock(Long id);

    /**
    * 批量删除任务锁定
    *
    * @param ids 编号
    */
    void deleteTaskLockListByIds(List<Long> ids);

    /**
     * 获得任务锁定
     *
     * @param id 编号
     * @return 任务锁定
     */
    TaskLockRespVO getTaskLock(Long id);

    /**
     * 获得任务锁定分页
     *
     * @param pageReqVO 分页查询
     * @return 任务锁定分页
     */
    PageResult<TaskLockRespVO> getTaskLockPage(TaskLockPageReqVO pageReqVO);

}