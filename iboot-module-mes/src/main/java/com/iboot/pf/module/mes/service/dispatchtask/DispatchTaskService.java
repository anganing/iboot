package com.iboot.pf.module.mes.service.dispatchtask;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-派工任务 Service 接口
 *
 * @author iboot
 */
public interface DispatchTaskService {

    /**
     * 创建派工任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDispatchTask(@Valid DispatchTaskSaveReqVO createReqVO);

    /**
     * 更新派工任务
     *
     * @param updateReqVO 更新信息
     */
    void updateDispatchTask(@Valid DispatchTaskSaveReqVO updateReqVO);

    /**
     * 删除派工任务
     *
     * @param id 编号
     */
    void deleteDispatchTask(Long id);

    /**
     * 批量删除派工任务
     *
     * @param ids 编号
     */
    void deleteDispatchTaskListByIds(List<Long> ids);

    /**
     * 获得派工任务
     *
     * @param id 编号
     * @return 派工任务
     */
    DispatchTaskRespVO getDispatchTask(Long id);

    /**
     * 获得派工任务分页
     *
     * @param pageReqVO 分页查询
     * @return 派工任务分页
     */
    PageResult<DispatchTaskRespVO> getDispatchTaskPage(DispatchTaskPageReqVO pageReqVO);

}
