package com.iboot.pf.module.wms.service.batchmgr;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_LOT_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_NOT_EXISTS;

/**
 * WMS-批次主档 codegen Service 实现类。
 *
 * <p>只剩 {@link #updateBatch} 一个写入方法（批属性维护，裁定二的例外），
 * 创建与删除已删除，见 {@link BatchService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class BatchServiceImpl implements BatchService {

    @Resource
    private WmsBatchMapper batchMapper;

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
