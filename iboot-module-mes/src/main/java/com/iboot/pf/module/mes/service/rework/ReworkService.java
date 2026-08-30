package com.iboot.pf.module.mes.service.rework;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.rework.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-返工任务 Service 接口
 *
 * @author iboot
 */
public interface ReworkService {

    /**
     * 创建MES-返工任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRework(@Valid ReworkSaveReqVO createReqVO);

    /**
     * 导入MES-返工任务
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ReworkImportRespVO importReworkList(List<ReworkImportExcelVO> importList);

    /**
     * 更新MES-返工任务
     *
     * @param updateReqVO 更新信息
     */
    void updateRework(@Valid ReworkSaveReqVO updateReqVO);

    /**
     * 删除MES-返工任务
     *
     * @param id 编号
     */
    void deleteRework(Long id);

    /**
    * 批量删除MES-返工任务
    *
    * @param ids 编号
    */
    void deleteReworkListByIds(List<Long> ids);

    /**
     * 获得MES-返工任务
     *
     * @param id 编号
     * @return MES-返工任务
     */
    ReworkRespVO getRework(Long id);

    /**
     * 获得MES-返工任务分页
     *
     * @param pageReqVO 分页查询
     * @return MES-返工任务分页
     */
    PageResult<ReworkRespVO> getReworkPage(ReworkPageReqVO pageReqVO);

}