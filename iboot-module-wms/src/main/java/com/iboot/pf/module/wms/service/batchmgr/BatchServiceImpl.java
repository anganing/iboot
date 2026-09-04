package com.iboot.pf.module.wms.service.batchmgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;

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
    private WmsCodeService codeService;
    @Resource
    private WmsBatchMapper batchMapper;

    @Override
    public Long createBatch(BatchSaveReqVO createReqVO) {
        // 插入
        WmsBatchDO batch = BeanUtils.toBean(createReqVO, WmsBatchDO.class);
        batch.setId(null);
        batch.setLotNo(codeService.generate(WmsCodeRuleConstants.BATCH_LOT, batch.getLotNo()));
        validateBatchUnique(batch, null);
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
        WmsBatchDO updateObj = BeanUtils.toBean(updateReqVO, WmsBatchDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateBatchUnique(updateObj, updateReqVO.getId());
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


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateBatchUnique(WmsBatchDO batch, Long excludeId) {
        WmsBatchDO existed = batchMapper.selectByMatIdAndLotNo(batch.getMatId(), batch.getLotNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(BATCH_LOT_NO_DUPLICATE);
    }

}
