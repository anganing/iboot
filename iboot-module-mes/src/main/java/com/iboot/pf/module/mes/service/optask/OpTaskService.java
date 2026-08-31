package com.iboot.pf.module.mes.service.optask;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * MES-工序任务 Service 接口
 *
 * @author iboot
 */
public interface OpTaskService {

    /**
     * 创建工序任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpTask(@Valid OpTaskSaveReqVO createReqVO);

    /**
     * 更新工序任务
     *
     * @param updateReqVO 更新信息
     */
    void updateOpTask(@Valid OpTaskSaveReqVO updateReqVO);

    /**
     * 删除工序任务
     *
     * @param id 编号
     */
    void deleteOpTask(Long id);

    /**
     * 批量删除工序任务
     *
     * @param ids 编号
     */
    void deleteOpTaskListByIds(List<Long> ids);

    /**
     * 获得工序任务
     *
     * @param id 编号
     * @return 工序任务
     */
    OpTaskRespVO getOpTask(Long id);

    /**
     * 获得工序任务分页
     *
     * @param pageReqVO 分页查询
     * @return 工序任务分页
     */
    PageResult<OpTaskRespVO> getOpTaskPage(OpTaskPageReqVO pageReqVO);

    /** 按生产工单编号批量查询工序任务，供 Api 门面一次性拉取。 */
    List<OpTaskDO> getOpTaskListByProdOrderIds(Collection<Long> prodOrderIds);

    /** 按路线工序编号批量查询工序任务，供 Api 门面一次性拉取。 */
    List<OpTaskDO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds);

    /** 按编号批量查询工序任务，供模块内使用。 */
    List<OpTaskDO> getOpTaskList(Collection<Long> ids);

}
