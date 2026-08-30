package com.iboot.pf.module.wms.service.batchmgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.batchmgr.BatchDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.batchmgr.BatchMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-批次主档 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class BatchServiceImpl implements BatchService {

    @Resource
    private BatchMapper batchMapper;

    @Override
    public Long createBatch(BatchSaveReqVO createReqVO) {
        // 插入
        BatchDO batch = BeanUtils.toBean(createReqVO, BatchDO.class);
        batchMapper.insert(batch);

        // 返回
        return batch.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public BatchImportRespVO importBatchList(List<BatchImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return BatchImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        BatchImportRespVO respVO = BatchImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createBatch(BeanUtils.toBean(importItem, BatchSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateBatch(BatchSaveReqVO updateReqVO) {
        // 校验存在
        validateBatchExists(updateReqVO.getId());
        // 更新
        BatchDO updateObj = BeanUtils.toBean(updateReqVO, BatchDO.class);
        batchMapper.updateById(updateObj);
    }

    @Override
    public void deleteBatch(Long id) {
        // 校验存在
        validateBatchExists(id);
        // 删除
        batchMapper.deleteById(id);
    }

    @Override
        public void deleteBatchListByIds(List<Long> ids) {
        // 删除
        batchMapper.deleteByIds(ids);
        }


    private void validateBatchExists(Long id) {
        if (batchMapper.selectById(id) == null) {
            throw exception(BATCH_NOT_EXISTS);
        }
    }

    @Override
    public BatchRespVO getBatch(Long id) {
        return batchMapper.selectJoinById(id);
    }

    @Override
    public PageResult<BatchRespVO> getBatchPage(BatchPageReqVO pageReqVO) {
        return batchMapper.selectPage(pageReqVO);
    }

}