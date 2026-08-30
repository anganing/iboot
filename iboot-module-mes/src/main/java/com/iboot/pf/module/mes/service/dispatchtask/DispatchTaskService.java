package com.iboot.pf.module.mes.service.dispatchtask;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-派工任务 Service 接口
 *
 * @author iboot
 */
public interface DispatchTaskService {

    /**
     * 创建MES-派工任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDispatchTask(@Valid DispatchTaskSaveReqVO createReqVO);

    /**
     * 导入MES-派工任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    DispatchTaskImportRespVO importDispatchTaskList(List<DispatchTaskImportExcelVO> importList);

    /**
     * 更新MES-派工任务
     *
     * @param updateReqVO 更新信息
     */
    void updateDispatchTask(@Valid DispatchTaskSaveReqVO updateReqVO);

    /**
     * 删除MES-派工任务
     *
     * @param id 编号
     */
    void deleteDispatchTask(Long id);

    /**
    * 批量删除MES-派工任务
    *
    * @param ids 编号
    */
    void deleteDispatchTaskListByIds(List<Long> ids);

    /**
     * 获得MES-派工任务
     *
     * @param id 编号
     * @return MES-派工任务
     */
    DispatchTaskRespVO getDispatchTask(Long id);

    /**
     * 获得MES-派工任务分页
     *
     * @param pageReqVO 分页查询
     * @return MES-派工任务分页
     */
    PageResult<DispatchTaskRespVO> getDispatchTaskPage(DispatchTaskPageReqVO pageReqVO);

}