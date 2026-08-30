package com.iboot.pf.module.aps.service.task;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.task.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.task.TaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * APS-工序排程任务 Service 接口
 *
 * @author iboot
 */
public interface TaskService {

    /**
     * 创建APS-工序排程任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTask(@Valid TaskSaveReqVO createReqVO);

    /**
     * 导入APS-工序排程任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    TaskImportRespVO importTaskList(List<TaskImportExcelVO> importList);

    /**
     * 更新APS-工序排程任务
     *
     * @param updateReqVO 更新信息
     */
    void updateTask(@Valid TaskSaveReqVO updateReqVO);

    /**
     * 删除APS-工序排程任务
     *
     * @param id 编号
     */
    void deleteTask(Long id);

    /**
    * 批量删除APS-工序排程任务
    *
    * @param ids 编号
    */
    void deleteTaskListByIds(List<Long> ids);

    /**
     * 获得APS-工序排程任务
     *
     * @param id 编号
     * @return APS-工序排程任务
     */
    TaskRespVO getTask(Long id);

    /**
     * 获得APS-工序排程任务分页
     *
     * @param pageReqVO 分页查询
     * @return APS-工序排程任务分页
     */
    PageResult<TaskRespVO> getTaskPage(TaskPageReqVO pageReqVO);

}