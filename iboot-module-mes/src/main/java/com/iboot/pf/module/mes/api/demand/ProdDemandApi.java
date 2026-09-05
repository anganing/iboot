package com.iboot.pf.module.mes.api.demand;

import com.iboot.pf.module.mes.api.demand.dto.CreateProdDemandReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandActionReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 生产需求 API 门面。
 *
 * <p>需求是生产工单的上游来源，外部系统（销售、计划）通过它把要生产什么下发给 MES。
 */
public interface ProdDemandApi {

    /**
     * 创建生产需求，落 {@code DRAFT}。
     *
     * @param reqDTO 创建命令
     * @return 生产需求编号
     */
    Long createProdDemand(CreateProdDemandReqDTO reqDTO);

    /**
     * 取消生产需求。
     *
     * @param reqDTO 动作命令
     */
    void cancelProdDemand(ProdDemandActionReqDTO reqDTO);

    /**
     * 查询单条生产需求。
     *
     * @param id 生产需求编号
     * @return 需求事实，不存在时返回 null
     */
    ProdDemandRespDTO getProdDemand(Long id);

    /**
     * 批量查询生产需求。
     *
     * @param ids 生产需求编号集合
     * @return 需求事实列表
     */
    List<ProdDemandRespDTO> getProdDemandList(Collection<Long> ids);

}
