package com.iboot.pf.module.aps.service.taskdep;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepPageReqVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepRespVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS-排程任务依赖 Service 接口。
 *
 * @author iboot
 */
public interface ApsTaskDepService {

    /**
     * 创建排程任务依赖
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTaskDep(@Valid TaskDepSaveReqVO createReqVO);

    /**
     * 更新排程任务依赖
     *
     * @param updateReqVO 更新信息
     */
    void updateTaskDep(@Valid TaskDepSaveReqVO updateReqVO);

    /**
     * 删除排程任务依赖
     *
     * @param id 编号
     */
    void deleteTaskDep(Long id);

    /**
     * 批量删除排程任务依赖
     *
     * @param ids 编号
     */
    void deleteTaskDepListByIds(List<Long> ids);

    /**
     * 获得排程任务依赖
     *
     * @param id 编号
     * @return 排程任务依赖
     */
    TaskDepRespVO getTaskDep(Long id);

    /**
     * 获得排程任务依赖分页
     *
     * @param pageReqVO 分页查询
     * @return 排程任务依赖分页
     */
    PageResult<TaskDepRespVO> getTaskDepPage(TaskDepPageReqVO pageReqVO);

}
