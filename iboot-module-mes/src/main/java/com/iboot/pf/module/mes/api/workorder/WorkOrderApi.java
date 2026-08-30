package com.iboot.pf.module.mes.api.workorder;

import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import java.util.Collection;
import java.util.List;

/** 作业工单只读 API 门面。 */
public interface WorkOrderApi {
    MesRespDTO getWorkOrder(Long id);
    List<MesRespDTO> getWorkOrderList(Collection<Long> ids);
    List<MesRespDTO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds);
}
