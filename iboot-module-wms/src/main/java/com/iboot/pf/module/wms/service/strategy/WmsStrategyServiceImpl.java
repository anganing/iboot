package com.iboot.pf.module.wms.service.strategy;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.strategy.WmsStrategyDO;
import com.iboot.pf.module.wms.dal.mysql.strategy.WmsStrategyMapper;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CODE_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;

/**
 * WMS-仓储策略 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WmsStrategyServiceImpl implements WmsStrategyService {

    @Resource
    private WmsStrategyMapper strategyMapper;
    @Resource
    private WmsCodeService codeService;

    @Override
    public Long createStrategy(StrategySaveReqVO createReqVO) {
        // 插入
        WmsStrategyDO strategy = BeanUtils.toBean(createReqVO, WmsStrategyDO.class);
        strategy.setId(null);
        strategy.setCode(codeService.generate(WmsCodeRuleConstants.STRATEGY, strategy.getCode()));
        validateStrategyUnique(strategy, null);
        strategyMapper.insert(strategy);

        // 返回
        return strategy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public StrategyImportRespVO importStrategyList(List<StrategyImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return StrategyImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        StrategyImportRespVO respVO = StrategyImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createStrategy(BeanUtils.toBean(importItem, StrategySaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateStrategy(StrategySaveReqVO updateReqVO) {
        // 校验存在
        validateStrategyExists(updateReqVO.getId());
        // 更新
        WmsStrategyDO updateObj = BeanUtils.toBean(updateReqVO, WmsStrategyDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateStrategyUnique(updateObj, updateReqVO.getId());
        strategyMapper.updateById(updateObj);
    }

    @Override
    public void deleteStrategy(Long id) {
        // 校验存在
        validateStrategyExists(id);
        // 删除
        strategyMapper.deleteById(id);
    }

    @Override
        public void deleteStrategyListByIds(List<Long> ids) {
        // 删除
        strategyMapper.deleteByIds(ids);
        }


    private void validateStrategyExists(Long id) {
        if (strategyMapper.selectById(id) == null) {
            throw exception(STRATEGY_NOT_EXISTS);
        }
    }

    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateStrategyUnique(WmsStrategyDO strategy, Long excludeId) {
        WmsStrategyDO existed = strategyMapper.selectByTypeAndCodeAndVersion(
                strategy.getStrategyType(), strategy.getCode(), strategy.getVersionNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(STRATEGY_CODE_DUPLICATE);
    }

    @Override
    public StrategyRespVO getStrategy(Long id) {
        return strategyMapper.selectJoinById(id);
    }

    @Override
    public PageResult<StrategyRespVO> getStrategyPage(StrategyPageReqVO pageReqVO) {
        return strategyMapper.selectPage(pageReqVO);
    }

}
