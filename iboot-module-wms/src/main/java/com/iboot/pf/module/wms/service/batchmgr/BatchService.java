package com.iboot.pf.module.wms.service.batchmgr;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import jakarta.validation.Valid;

/**
 * WMS-批次主档 codegen Service 接口。
 *
 * <p><b>只保留一个写入方法 {@link #updateBatch}</b>，这是 codegen 侧唯一的写入例外（调度方裁定二）：
 * {@code wms_batch} 是主档表不是账表（账表只有 {@code wms_inv_balance} / {@code wms_inv_txn} /
 * {@code wms_inv_reservation} / {@code wms_inv_lock} 四张），炉批号、生产日期、失效日期这些批属性是
 * "批次注册之后才知道"的主数据补录，不改任何数量，与"账只能由领域动作改"不冲突。
 * 端点是 {@code PUT /wms/batch/update-attr}，只放行六个批属性字段。
 *
 * <p>批次的<b>创建</b>已收归领域侧 {@code service.batch.WmsBatchService#registerBatch}，由到货确认触发，
 * 人工不许建批次——批次是余额与流水的记账维度，凭空造一个批次等于给账开了个无源头的口子。
 *
 * @author iboot
 */
public interface BatchService {

    /**
     * 更新WMS-批次主档的批属性
     *
     * @param updateReqVO 更新信息
     */
    void updateBatch(@Valid BatchSaveReqVO updateReqVO);

    /**
     * 获得WMS-批次主档
     *
     * @param id 编号
     * @return WMS-批次主档
     */
    BatchRespVO getBatch(Long id);

    /**
     * 获得WMS-批次主档分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-批次主档分页
     */
    PageResult<BatchRespVO> getBatchPage(BatchPageReqVO pageReqVO);

}
