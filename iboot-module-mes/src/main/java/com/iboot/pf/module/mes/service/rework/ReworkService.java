package com.iboot.pf.module.mes.service.rework;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-返工任务 Service 接口
 *
 * @author iboot
 */
public interface ReworkService {

    /**
     * 创建返工任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRework(@Valid ReworkSaveReqVO createReqVO);

    /**
     * 更新返工任务
     *
     * @param updateReqVO 更新信息
     */
    void updateRework(@Valid ReworkSaveReqVO updateReqVO);

    /**
     * 删除返工任务
     *
     * @param id 编号
     */
    void deleteRework(Long id);

    /**
     * 批量删除返工任务
     *
     * @param ids 编号
     */
    void deleteReworkListByIds(List<Long> ids);

    /**
     * 获得返工任务
     *
     * @param id 编号
     * @return 返工任务
     */
    ReworkRespVO getRework(Long id);

    /**
     * 获得返工任务分页
     *
     * @param pageReqVO 分页查询
     * @return 返工任务分页
     */
    PageResult<ReworkRespVO> getReworkPage(ReworkPageReqVO pageReqVO);

}
