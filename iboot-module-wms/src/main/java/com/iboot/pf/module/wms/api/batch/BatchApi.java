package com.iboot.pf.module.wms.api.batch;

import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchReqDTO;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 批次 API 门面。
 *
 * <p>向 MES / Integration / UI 提供批次主档查询与注册。批次注册幂等：
 * 同一物料 + 批次号 + 序列号已存在时直接返回原批次。
 */
public interface BatchApi {

    BatchRespDTO getBatch(Long id);

    List<BatchRespDTO> getBatchList(Collection<Long> ids);

    Map<Long, BatchRespDTO> getBatchMap(Collection<Long> ids);

    /**
     * 注册批次，幂等。
     *
     * @param reqDTO 注册参数
     * @return 注册结果，含批次编号
     */
    RegisterBatchRespDTO registerBatch(RegisterBatchReqDTO reqDTO);

}
