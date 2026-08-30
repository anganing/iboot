package com.iboot.pf.module.mes.service.optask;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.optask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-工序任务 Service 接口
 *
 * @author iboot
 */
public interface OpTaskService {

    /**
     * 创建MES-工序任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpTask(@Valid OpTaskSaveReqVO createReqVO);

    /**
     * 导入MES-工序任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    OpTaskImportRespVO importOpTaskList(List<OpTaskImportExcelVO> importList);

    /**
     * 更新MES-工序任务
     *
     * @param updateReqVO 更新信息
     */
    void updateOpTask(@Valid OpTaskSaveReqVO updateReqVO);

    /**
     * 删除MES-工序任务
     *
     * @param id 编号
     */
    void deleteOpTask(Long id);

    /**
    * 批量删除MES-工序任务
    *
    * @param ids 编号
    */
    void deleteOpTaskListByIds(List<Long> ids);

    /**
     * 获得MES-工序任务
     *
     * @param id 编号
     * @return MES-工序任务
     */
    OpTaskRespVO getOpTask(Long id);

    /** 按生产工单批量查询工序任务，供模块内 API 门面使用。 */
    List<OpTaskDO> getOpTaskListByProdOrderIds(Collection<Long> prodOrderIds);

    /** 按路线工序批量查询工序任务，供模块内 API 门面使用。 */
    List<OpTaskDO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds);

    /**
     * 获得MES-工序任务分页
     *
     * @param pageReqVO 分页查询
     * @return MES-工序任务分页
     */
    PageResult<OpTaskRespVO> getOpTaskPage(OpTaskPageReqVO pageReqVO);

}
