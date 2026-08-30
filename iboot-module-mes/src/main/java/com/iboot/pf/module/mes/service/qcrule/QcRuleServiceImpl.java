package com.iboot.pf.module.mes.service.qcrule;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-质检触发规则 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcRuleServiceImpl implements QcRuleService {

    @Resource
    private QcRuleMapper qcRuleMapper;

    @Override
    public Long createQcRule(QcRuleSaveReqVO createReqVO) {
        // 插入
        QcRuleDO qcRule = BeanUtils.toBean(createReqVO, QcRuleDO.class);
        qcRuleMapper.insert(qcRule);

        // 返回
        return qcRule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public QcRuleImportRespVO importQcRuleList(List<QcRuleImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return QcRuleImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        QcRuleImportRespVO respVO = QcRuleImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createQcRule(BeanUtils.toBean(importItem, QcRuleSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateQcRule(QcRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateQcRuleExists(updateReqVO.getId());
        // 更新
        QcRuleDO updateObj = BeanUtils.toBean(updateReqVO, QcRuleDO.class);
        qcRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcRule(Long id) {
        // 校验存在
        validateQcRuleExists(id);
        // 删除
        qcRuleMapper.deleteById(id);
    }

    @Override
        public void deleteQcRuleListByIds(List<Long> ids) {
        // 删除
        qcRuleMapper.deleteByIds(ids);
        }


    private void validateQcRuleExists(Long id) {
        if (qcRuleMapper.selectById(id) == null) {
            throw exception(QC_RULE_NOT_EXISTS);
        }
    }

    @Override
    public QcRuleRespVO getQcRule(Long id) {
        return qcRuleMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcRuleRespVO> getQcRulePage(QcRulePageReqVO pageReqVO) {
        return qcRuleMapper.selectPage(pageReqVO);
    }

}