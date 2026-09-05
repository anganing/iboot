package com.iboot.pf.module.mdm.api.matalt;

import com.iboot.pf.module.mdm.api.matalt.dto.MatAltRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 替代料 API 接口
 *
 * <p>对 MES / APS 提供替代料关系的只读门面。齐套检查主料不足时按优先级逐个试替代料，
 * 缺了这个门面，MES 只能跨库 JOIN {@code mdm_mat_alt}，那是红线。
 *
 * @author iboot
 */
public interface MatAltApi {

    /**
     * 按原物料编号查询替代料，按 priority 升序
     *
     * @param matId 原物料编号
     * @return 替代料列表；无替代料返回空列表
     */
    List<MatAltRespDTO> getAltList(Long matId);

    /**
     * 按原物料编号集合批量查询替代料，一次 SQL 不产生 N+1
     *
     * <p>齐套检查一次可能有多行缺料，逐行调 {@link #getAltList(Long)} 就是 N+1，用这个方法。
     *
     * @param matIds 原物料编号集合
     * @return 原物料编号 -&gt; 替代料列表（按 priority 升序）；无替代料的物料不产生键
     */
    Map<Long, List<MatAltRespDTO>> getAltMap(Collection<Long> matIds);

}
