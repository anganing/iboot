package com.iboot.pf.module.mes.service.rgvtask;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-RGV 工位流转任务 Service 接口
 *
 * @author iboot
 */
public interface RgvTaskService {

    /**
     * 创建 RGV 任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRgvTask(@Valid RgvTaskSaveReqVO createReqVO);

    /**
     * 更新 RGV 任务
     *
     * @param updateReqVO 更新信息
     */
    void updateRgvTask(@Valid RgvTaskSaveReqVO updateReqVO);

    /**
     * 删除 RGV 任务
     *
     * @param id 编号
     */
    void deleteRgvTask(Long id);

    /**
     * 批量删除 RGV 任务
     *
     * @param ids 编号
     */
    void deleteRgvTaskListByIds(List<Long> ids);

    /**
     * 获得 RGV 任务
     *
     * @param id 编号
     * @return RGV 任务
     */
    RgvTaskRespVO getRgvTask(Long id);

    /**
     * 获得 RGV 任务分页
     *
     * @param pageReqVO 分页查询
     * @return RGV 任务分页
     */
    PageResult<RgvTaskRespVO> getRgvTaskPage(RgvTaskPageReqVO pageReqVO);

}
