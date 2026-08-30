package com.iboot.pf.module.wms.service.strategy;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * WMS-仓储策略 Service 接口
 *
 * @author iboot
 */
public interface WmsStrategyService {

    /**
     * 创建WMS-仓储策略
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStrategy(@Valid StrategySaveReqVO createReqVO);

    /**
     * 导入WMS-仓储策略
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    StrategyImportRespVO importStrategyList(List<StrategyImportExcelVO> importList);

    /**
     * 更新WMS-仓储策略
     *
     * @param updateReqVO 更新信息
     */
    void updateStrategy(@Valid StrategySaveReqVO updateReqVO);

    /**
     * 删除WMS-仓储策略
     *
     * @param id 编号
     */
    void deleteStrategy(Long id);

    /**
    * 批量删除WMS-仓储策略
    *
    * @param ids 编号
    */
    void deleteStrategyListByIds(List<Long> ids);

    /**
     * 获得WMS-仓储策略
     *
     * @param id 编号
     * @return WMS-仓储策略
     */
    StrategyRespVO getStrategy(Long id);

    /**
     * 获得WMS-仓储策略分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-仓储策略分页
     */
    PageResult<StrategyRespVO> getStrategyPage(StrategyPageReqVO pageReqVO);

}
