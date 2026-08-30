package com.iboot.pf.module.mes.api.order;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import java.util.Collection;
import java.util.List;

/** 生产工单 API 门面。工单状态只能由 MES 推进。 */
public interface ProdOrderApi {
    Long createProdOrder(CreateReq reqDTO);
    void releaseProdOrder(MesCommandReqDTO reqDTO);
    void closeProdOrder(MesCommandReqDTO reqDTO);
    ProdOrderRespDTO getProdOrder(Long id);
    List<ProdOrderRespDTO> getProdOrderList(Collection<Long> ids);

    List<ProdOrderRespDTO> getProdOrderListByPlantId(Long plantId);
}
