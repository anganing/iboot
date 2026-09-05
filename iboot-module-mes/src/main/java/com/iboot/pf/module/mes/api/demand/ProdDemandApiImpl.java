package com.iboot.pf.module.mes.api.demand;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.demand.dto.CreateProdDemandReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandActionReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 生产需求 API 实现。 */
@Service
@Validated
public class ProdDemandApiImpl implements ProdDemandApi {

    @Override
    public Long createProdDemand(CreateProdDemandReqDTO reqDTO) {
        throw new UnsupportedOperationException("生产需求创建命令尚未接入领域规则");
    }

    @Override
    public void cancelProdDemand(ProdDemandActionReqDTO reqDTO) {
        throw new UnsupportedOperationException("生产需求取消命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public ProdDemandRespDTO getProdDemand(Long id) {
        throw new UnsupportedOperationException("生产需求查询尚未接入领域 Service");
    }

    @Override
    @DataPermission(enable = false)
    public List<ProdDemandRespDTO> getProdDemandList(Collection<Long> ids) {
        throw new UnsupportedOperationException("生产需求查询尚未接入领域 Service");
    }

}
