package com.iboot.pf.module.aps.service.scenario;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ScenarioDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.scenario.ScenarioMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * 排程场景 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ScenarioServiceImpl implements ScenarioService {

    @Resource
    private ScenarioMapper scenarioMapper;

    @Override
    public Long createScenario(ScenarioSaveReqVO createReqVO) {
        // 插入
        ScenarioDO scenario = BeanUtils.toBean(createReqVO, ScenarioDO.class);
        scenarioMapper.insert(scenario);

        // 返回
        return scenario.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ScenarioImportRespVO importScenarioList(List<ScenarioImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ScenarioImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ScenarioImportRespVO respVO = ScenarioImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createScenario(BeanUtils.toBean(importItem, ScenarioSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateScenario(ScenarioSaveReqVO updateReqVO) {
        // 校验存在
        validateScenarioExists(updateReqVO.getId());
        // 更新
        ScenarioDO updateObj = BeanUtils.toBean(updateReqVO, ScenarioDO.class);
        scenarioMapper.updateById(updateObj);
    }

    @Override
    public void deleteScenario(Long id) {
        // 校验存在
        validateScenarioExists(id);
        // 删除
        scenarioMapper.deleteById(id);
    }

    @Override
        public void deleteScenarioListByIds(List<Long> ids) {
        // 删除
        scenarioMapper.deleteByIds(ids);
        }


    private void validateScenarioExists(Long id) {
        if (scenarioMapper.selectById(id) == null) {
            throw exception(SCENARIO_NOT_EXISTS);
        }
    }

    @Override
    public ScenarioRespVO getScenario(Long id) {
        return scenarioMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ScenarioRespVO> getScenarioPage(ScenarioPageReqVO pageReqVO) {
        return scenarioMapper.selectPage(pageReqVO);
    }

}