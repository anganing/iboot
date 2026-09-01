package com.iboot.pf.module.aps.service.task;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskAdjustReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskGanttRespVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskPageReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskRespVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * APS-工序排程任务 Service 接口。
 *
 * <p>任务由求解器产出：管理后台只提供查询、导出、人工调整（资源与时间）和锁定/解锁，
 * 不提供新增与批量导入。凭空造一条任务既没有工单来源，也没有工序快照。
 *
 * @author iboot
 */
public interface ApsTaskService {

    /**
     * 获得排程任务
     *
     * @param id 编号
     * @return 排程任务
     */
    TaskRespVO getTask(Long id);

    /**
     * 获得排程任务分页
     *
     * @param pageReqVO 分页查询
     * @return 排程任务分页
     */
    PageResult<TaskRespVO> getTaskPage(TaskPageReqVO pageReqVO);

    /**
     * 获得某个排程版本的甘特图视图：资源分组 + 任务条 + 依赖连线 + 时间窗
     *
     * @param versionId 排程版本编号
     * @return 甘特图数据
     */
    TaskGanttRespVO getTaskGantt(Long versionId);

    /**
     * 人工调整排程任务的资源与计划时间
     *
     * @param adjustReqVO 调整信息
     */
    void adjustTask(@Valid TaskAdjustReqVO adjustReqVO);

    /**
     * 锁定排程任务，重排时不再移动它
     *
     * @param reqDTO 命令
     */
    void lockTask(@Valid LockTaskReqDTO reqDTO);

    /**
     * 解锁排程任务
     *
     * @param reqDTO 命令
     */
    void unlockTask(@Valid LockTaskReqDTO reqDTO);

    /**
     * 按版本获得排程任务列表
     *
     * @param versionId 版本编号
     * @return 任务列表
     */
    List<ApsTaskDO> getTaskDOListByVersionId(Long versionId);

    /**
     * 按生产工单获得排程任务列表
     *
     * @param prodOrderIds 生产工单编号集合
     * @return 任务列表
     */
    List<ApsTaskDO> getTaskDOListByProdOrderIds(Collection<Long> prodOrderIds);

    /**
     * 校验排程任务存在
     *
     * @param id 编号
     * @return 排程任务
     */
    ApsTaskDO validateTaskExists(Long id);

}
