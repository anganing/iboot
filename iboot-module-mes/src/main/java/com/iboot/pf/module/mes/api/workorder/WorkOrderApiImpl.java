package com.iboot.pf.module.mes.api.workorder;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

/** 作业工单 API 实现。 */
@Service
public class WorkOrderApiImpl implements WorkOrderApi {
    @Resource private MesApiService service;
    @Override @DataPermission(enable = false) public MesRespDTO getWorkOrder(Long id) { return service.get("workorder", id); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getWorkOrderList(Collection<Long> ids) { return service.list("workorder", ids); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getWorkOrderListByProdOrderIds(Collection<Long> ids) { return service.listBy("workorder-prod-order", "prodOrderId", ids); }
}
