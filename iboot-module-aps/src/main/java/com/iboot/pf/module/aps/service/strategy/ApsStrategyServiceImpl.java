package com.iboot.pf.module.aps.service.strategy;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyPageReqVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategySaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.strategy.ApsStrategyMapper;
import com.iboot.pf.module.aps.enums.ApsCodeRuleConstants;
import com.iboot.pf.module.aps.enums.ApsStrategyStatusEnum;
import com.iboot.pf.module.aps.service.coderule.ApsCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_CODE_DUPLICATE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_DISABLED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_HAS_SCENARIO;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;

/**
 * APS-排程策略 Service 实现类。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsStrategyServiceImpl implements ApsStrategyService {

    @Resource
    private ApsStrategyMapper strategyMapper;
    @Resource
    private ApsScenarioMapper scenarioMapper;
    @Resource
    private ApsCodeService codeService;

    @Override
    public Long createStrategy(StrategySaveReqVO createReqVO) {
        ApsStrategyDO strategy = BeanUtils.toBean(createReqVO, ApsStrategyDO.class);
        strategy.setId(null);
        strategy.setCode(codeService.generate(ApsCodeRuleConstants.STRATEGY, strategy.getCode()));
        validateStrategyUnique(strategy, null);
        strategyMapper.insert(strategy);
        return strategy.getId();
    }

    @Override
    public void updateStrategy(StrategySaveReqVO updateReqVO) {
        ApsStrategyDO existed = validateStrategyExists(updateReqVO.getId());
        ApsStrategyDO updateObj = BeanUtils.toBean(updateReqVO, ApsStrategyDO.class);
        // 编码由编码规则分配，更新时不允许改
        updateObj.setCode(existed.getCode());
        validateStrategyUnique(updateObj, updateReqVO.getId());
        strategyMapper.updateById(updateObj);
    }

    @Override
    public void deleteStrategy(Long id) {
        validateStrategyExists(id);
        validateNoScenario(id);
        strategyMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStrategyListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量删也要逐条校验占用，否则会留下引用不到策略的场景
        for (Long id : ids) {
            validateStrategyExists(id);
            validateNoScenario(id);
        }
        strategyMapper.deleteByIds(ids);
    }

    @Override
    public StrategyRespVO getStrategy(Long id) {
        return strategyMapper.selectJoinById(id);
    }

    @Override
    public PageResult<StrategyRespVO> getStrategyPage(StrategyPageReqVO pageReqVO) {
        return strategyMapper.selectPage(pageReqVO);
    }

    @Override
    public ApsStrategyDO validateStrategyEnabled(Long id) {
        ApsStrategyDO strategy = validateStrategyExists(id);
        // 草稿与停用的策略不允许被场景引用，否则排出来的计划没人认
        if (!ApsStrategyStatusEnum.isEnabled(strategy.getStatus())) {
            throw exception(STRATEGY_DISABLED);
        }
        return strategy;
    }

    private ApsStrategyDO validateStrategyExists(Long id) {
        ApsStrategyDO strategy = id == null ? null : strategyMapper.selectById(id);
        if (strategy == null) {
            throw exception(STRATEGY_NOT_EXISTS);
        }
        return strategy;
    }

    /** 已被场景引用的策略不允许删除。 */
    private void validateNoScenario(Long strategyId) {
        if (scenarioMapper.selectCountByStrategyId(strategyId) > 0) {
            throw exception(STRATEGY_HAS_SCENARIO);
        }
    }

    /**
     * 编码与版本号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateStrategyUnique(ApsStrategyDO strategy, Long excludeId) {
        ApsStrategyDO existed = strategyMapper.selectByCodeAndVersionNo(strategy.getCode(), strategy.getVersionNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(STRATEGY_CODE_DUPLICATE);
    }

}
