package com.iboot.pf.module.mes.service.qcplan;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-检验方案 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcPlanServiceImpl implements QcPlanService {

    @Resource
    private QcPlanMapper qcPlanMapper;

    @Override
    public Long createQcPlan(QcPlanSaveReqVO createReqVO) {
        // 插入
        QcPlanDO qcPlan = BeanUtils.toBean(createReqVO, QcPlanDO.class);
        qcPlanMapper.insert(qcPlan);

        // 返回
        return qcPlan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public QcPlanImportRespVO importQcPlanList(List<QcPlanImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return QcPlanImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        QcPlanImportRespVO respVO = QcPlanImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createQcPlan(BeanUtils.toBean(importItem, QcPlanSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateQcPlan(QcPlanSaveReqVO updateReqVO) {
        // 校验存在
        validateQcPlanExists(updateReqVO.getId());
        // 更新
        QcPlanDO updateObj = BeanUtils.toBean(updateReqVO, QcPlanDO.class);
        qcPlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcPlan(Long id) {
        // 校验存在
        validateQcPlanExists(id);
        // 删除
        qcPlanMapper.deleteById(id);
    }

    @Override
        public void deleteQcPlanListByIds(List<Long> ids) {
        // 删除
        qcPlanMapper.deleteByIds(ids);
        }


    private void validateQcPlanExists(Long id) {
        if (qcPlanMapper.selectById(id) == null) {
            throw exception(QC_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public QcPlanRespVO getQcPlan(Long id) {
        return qcPlanMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcPlanRespVO> getQcPlanPage(QcPlanPageReqVO pageReqVO) {
        return qcPlanMapper.selectPage(pageReqVO);
    }

}