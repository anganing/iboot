package com.iboot.pf.module.mes.service.qctask;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-质检任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcTaskServiceImpl implements QcTaskService {

    @Resource
    private QcTaskMapper qcTaskMapper;

    @Override
    public Long createQcTask(QcTaskSaveReqVO createReqVO) {
        // 插入
        QcTaskDO qcTask = BeanUtils.toBean(createReqVO, QcTaskDO.class);
        qcTaskMapper.insert(qcTask);

        // 返回
        return qcTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public QcTaskImportRespVO importQcTaskList(List<QcTaskImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return QcTaskImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        QcTaskImportRespVO respVO = QcTaskImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createQcTask(BeanUtils.toBean(importItem, QcTaskSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateQcTask(QcTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateQcTaskExists(updateReqVO.getId());
        // 更新
        QcTaskDO updateObj = BeanUtils.toBean(updateReqVO, QcTaskDO.class);
        qcTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcTask(Long id) {
        // 校验存在
        validateQcTaskExists(id);
        // 删除
        qcTaskMapper.deleteById(id);
    }

    @Override
        public void deleteQcTaskListByIds(List<Long> ids) {
        // 删除
        qcTaskMapper.deleteByIds(ids);
        }


    private void validateQcTaskExists(Long id) {
        if (qcTaskMapper.selectById(id) == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
    }

    @Override
    public QcTaskRespVO getQcTask(Long id) {
        return qcTaskMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcTaskRespVO> getQcTaskPage(QcTaskPageReqVO pageReqVO) {
        return qcTaskMapper.selectPage(pageReqVO);
    }

}