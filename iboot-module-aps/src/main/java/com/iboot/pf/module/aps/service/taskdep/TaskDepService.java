package com.iboot.pf.module.aps.service.taskdep;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.TaskDepDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 任务依赖 Service 接口
 *
 * @author iboot
 */
public interface TaskDepService {

    /**
     * 创建任务依赖
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTaskDep(@Valid TaskDepSaveReqVO createReqVO);

    /**
     * 导入任务依赖
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    TaskDepImportRespVO importTaskDepList(List<TaskDepImportExcelVO> importList);

    /**
     * 更新任务依赖
     *
     * @param updateReqVO 更新信息
     */
    void updateTaskDep(@Valid TaskDepSaveReqVO updateReqVO);

    /**
     * 删除任务依赖
     *
     * @param id 编号
     */
    void deleteTaskDep(Long id);

    /**
    * 批量删除任务依赖
    *
    * @param ids 编号
    */
    void deleteTaskDepListByIds(List<Long> ids);

    /**
     * 获得任务依赖
     *
     * @param id 编号
     * @return 任务依赖
     */
    TaskDepRespVO getTaskDep(Long id);

    /**
     * 获得任务依赖分页
     *
     * @param pageReqVO 分页查询
     * @return 任务依赖分页
     */
    PageResult<TaskDepRespVO> getTaskDepPage(TaskDepPageReqVO pageReqVO);

}