package com.iboot.pf.module.mes.service.qcresult;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-检验结果 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcResultServiceImpl implements QcResultService {

    @Resource
    private QcResultMapper qcResultMapper;

    @Override
    public Long createQcResult(QcResultSaveReqVO createReqVO) {
        // 插入
        QcResultDO qcResult = BeanUtils.toBean(createReqVO, QcResultDO.class);
        qcResultMapper.insert(qcResult);

        // 返回
        return qcResult.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public QcResultImportRespVO importQcResultList(List<QcResultImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return QcResultImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        QcResultImportRespVO respVO = QcResultImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createQcResult(BeanUtils.toBean(importItem, QcResultSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateQcResult(QcResultSaveReqVO updateReqVO) {
        // 校验存在
        validateQcResultExists(updateReqVO.getId());
        // 更新
        QcResultDO updateObj = BeanUtils.toBean(updateReqVO, QcResultDO.class);
        qcResultMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcResult(Long id) {
        // 校验存在
        validateQcResultExists(id);
        // 删除
        qcResultMapper.deleteById(id);
    }

    @Override
        public void deleteQcResultListByIds(List<Long> ids) {
        // 删除
        qcResultMapper.deleteByIds(ids);
        }


    private void validateQcResultExists(Long id) {
        if (qcResultMapper.selectById(id) == null) {
            throw exception(QC_RESULT_NOT_EXISTS);
        }
    }

    @Override
    public QcResultRespVO getQcResult(Long id) {
        return qcResultMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcResultRespVO> getQcResultPage(QcResultPageReqVO pageReqVO) {
        return qcResultMapper.selectPage(pageReqVO);
    }

}