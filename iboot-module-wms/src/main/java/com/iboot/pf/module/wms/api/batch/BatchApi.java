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
     * 按批次号批量反查批次主档，一次 {@code IN} 查询，禁止逐条查。
     *
     * <p>调用方手上常常只有批次号（追溯链路的领料行快照、外部单据上的批号），
     * {@link #getBatchMap} 只认单据主键，这里补按批次号的入口。
     *
     * <p><b>为什么值是列表而不是单个 DTO</b>：{@code wms_batch} 的唯一约束是
     * {@code (mat_id, lot_no)}，批次号只在物料内唯一——同一个 {@code lot_no} 可以属于多个物料，
     * 而且无批次物料全部挂在默认批次号 {@code DEFAULT} 上。按批次号建单值 Map 会把这些批次
     * 互相覆盖掉，返回值不可信。调用方按批次号取到列表后，用已知的物料编号定位那一条。
     *
     * @param lotNos 批次号集合
     * @return 批次号到该批次号下各物料批次的映射；空集合返回空 Map，不抛异常
     */
    Map<String, List<BatchRespDTO>> getBatchMapByLotNos(Collection<String> lotNos);

    /**
     * 注册批次，幂等。
     *
     * @param reqDTO 注册参数
     * @return 注册结果，含批次编号
     */
    RegisterBatchRespDTO registerBatch(RegisterBatchReqDTO reqDTO);

}
