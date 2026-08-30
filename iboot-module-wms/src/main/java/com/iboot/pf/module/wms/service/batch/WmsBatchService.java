package com.iboot.pf.module.wms.service.batch;

import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.service.batch.bo.RegisterBatchBO;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 批次 Service。 */
public interface WmsBatchService {

    WmsBatchDO getBatch(Long id);

    List<WmsBatchDO> getBatchList(Collection<Long> ids);

    Map<Long, WmsBatchDO> getBatchMap(Collection<Long> ids);

    /**
     * 注册批次：同一物料 + 批次号 + 序列号已存在时直接返回原批次，不存在则新建。
     *
     * @param bo 注册参数
     * @return 批次
     */
    WmsBatchDO registerBatch(RegisterBatchBO bo);

    /** 由已完成批量校验的上游流程传入物料快照，避免明细循环重复访问 MDM。 */
    WmsBatchDO registerBatch(RegisterBatchBO bo, MaterialRespDTO material);

}
