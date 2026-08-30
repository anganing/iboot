package com.iboot.pf.module.mes.service.rgvtask;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-RGV工位流转任务 Service 接口
 *
 * @author iboot
 */
public interface RgvTaskService {

    /**
     * 创建MES-RGV工位流转任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRgvTask(@Valid RgvTaskSaveReqVO createReqVO);

    /**
     * 导入MES-RGV工位流转任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    RgvTaskImportRespVO importRgvTaskList(List<RgvTaskImportExcelVO> importList);

    /**
     * 更新MES-RGV工位流转任务
     *
     * @param updateReqVO 更新信息
     */
    void updateRgvTask(@Valid RgvTaskSaveReqVO updateReqVO);

    /**
     * 删除MES-RGV工位流转任务
     *
     * @param id 编号
     */
    void deleteRgvTask(Long id);

    /**
    * 批量删除MES-RGV工位流转任务
    *
    * @param ids 编号
    */
    void deleteRgvTaskListByIds(List<Long> ids);

    /**
     * 获得MES-RGV工位流转任务
     *
     * @param id 编号
     * @return MES-RGV工位流转任务
     */
    RgvTaskRespVO getRgvTask(Long id);

    /**
     * 获得MES-RGV工位流转任务分页
     *
     * @param pageReqVO 分页查询
     * @return MES-RGV工位流转任务分页
     */
    PageResult<RgvTaskRespVO> getRgvTaskPage(RgvTaskPageReqVO pageReqVO);

}