package com.iboot.pf.module.mes.api.demand;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import java.util.Collection;
import java.util.List;

/** 生产需求 API 门面。 */
public interface ProdDemandApi {
    Long createProdDemand(CreateReq reqDTO);
    void cancelProdDemand(MesCommandReqDTO reqDTO);
    MesRespDTO getProdDemand(Long id);
    List<MesRespDTO> getProdDemandList(Collection<Long> ids);
}
