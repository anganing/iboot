package com.iboot.pf.module.mes.service.qctask;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-质检任务 Service 接口
 *
 * @author iboot
 */
public interface QcTaskService {

    /**
     * 创建MES-质检任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcTask(@Valid QcTaskSaveReqVO createReqVO);

    /**
     * 导入MES-质检任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    QcTaskImportRespVO importQcTaskList(List<QcTaskImportExcelVO> importList);

    /**
     * 更新MES-质检任务
     *
     * @param updateReqVO 更新信息
     */
    void updateQcTask(@Valid QcTaskSaveReqVO updateReqVO);

    /**
     * 删除MES-质检任务
     *
     * @param id 编号
     */
    void deleteQcTask(Long id);

    /**
    * 批量删除MES-质检任务
    *
    * @param ids 编号
    */
    void deleteQcTaskListByIds(List<Long> ids);

    /**
     * 获得MES-质检任务
     *
     * @param id 编号
     * @return MES-质检任务
     */
    QcTaskRespVO getQcTask(Long id);

    /**
     * 获得MES-质检任务分页
     *
     * @param pageReqVO 分页查询
     * @return MES-质检任务分页
     */
    PageResult<QcTaskRespVO> getQcTaskPage(QcTaskPageReqVO pageReqVO);

}