package com.iboot.pf.module.mes.service.qctask;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-质检任务 Service 接口
 *
 * @author iboot
 */
public interface QcTaskService {

    /**
     * 创建质检任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcTask(@Valid QcTaskSaveReqVO createReqVO);

    /**
     * 更新质检任务
     *
     * @param updateReqVO 更新信息
     */
    void updateQcTask(@Valid QcTaskSaveReqVO updateReqVO);

    /**
     * 删除质检任务
     *
     * @param id 编号
     */
    void deleteQcTask(Long id);

    /**
     * 批量删除质检任务
     *
     * @param ids 编号
     */
    void deleteQcTaskListByIds(List<Long> ids);

    /**
     * 获得质检任务
     *
     * @param id 编号
     * @return 质检任务
     */
    QcTaskRespVO getQcTask(Long id);

    /**
     * 获得质检任务分页
     *
     * @param pageReqVO 分页查询
     * @return 质检任务分页
     */
    PageResult<QcTaskRespVO> getQcTaskPage(QcTaskPageReqVO pageReqVO);

    /**
     * 校验质检任务存在并返回，供检验结果、缺陷、处置单引用时使用
     *
     * @param id 编号
     * @return 质检任务
     */
    QcTaskDO validateQcTaskExists(Long id);

}
