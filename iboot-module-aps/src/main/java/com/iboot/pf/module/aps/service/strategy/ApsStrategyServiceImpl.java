package com.iboot.pf.module.aps.service.strategy;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import com.iboot.pf.module.aps.dal.mysql.strategy.ApsStrategyMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;

/**
 * APS-排程策略 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ApsStrategyServiceImpl implements ApsStrategyService {

    @Resource
    private ApsStrategyMapper strategyMapper;

    @Override
    public Long createStrategy(StrategySaveReqVO createReqVO) {
        // 插入
        ApsStrategyDO strategy = BeanUtils.toBean(createReqVO, ApsStrategyDO.class);
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
        ApsStrategyDO updateObj = BeanUtils.toBean(updateReqVO, ApsStrategyDO.class);
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

    @Override
    public StrategyRespVO getStrategy(Long id) {
        return strategyMapper.selectJoinById(id);
    }

    @Override
    public PageResult<StrategyRespVO> getStrategyPage(StrategyPageReqVO pageReqVO) {
        return strategyMapper.selectPage(pageReqVO);
    }

}
