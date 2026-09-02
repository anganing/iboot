package com.iboot.pf.module.aps.service.strategy;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyPageReqVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyConstraintMetadataRespVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategySaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS-排程策略 Service 接口。
 *
 * @author iboot
 */
public interface ApsStrategyService {

    /**
     * 创建排程策略
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStrategy(@Valid StrategySaveReqVO createReqVO);

    /**
     * 更新排程策略
     *
     * @param updateReqVO 更新信息
     */
    void updateStrategy(@Valid StrategySaveReqVO updateReqVO);

    /**
     * 删除排程策略
     *
     * @param id 编号
     */
    void deleteStrategy(Long id);

    /**
     * 批量删除排程策略
     *
     * @param ids 编号
     */
    void deleteStrategyListByIds(List<Long> ids);

    /**
     * 获得排程策略
     *
     * @param id 编号
     * @return 排程策略
     */
    StrategyRespVO getStrategy(Long id);

    /**
     * 获得排程策略分页
     *
     * @param pageReqVO 分页查询
     * @return 排程策略分页
     */
    PageResult<StrategyRespVO> getStrategyPage(StrategyPageReqVO pageReqVO);

    /**
     * 获得约束元数据，供策略配置表单渲染可调项与默认值
     *
     * @return 约束元数据
     */
    StrategyConstraintMetadataRespVO getConstraintMetadata();

    /**
     * 校验策略存在且已启用，场景引用策略时调用
     *
     * @param id 编号
     * @return 策略
     */
    ApsStrategyDO validateStrategyEnabled(Long id);

}
