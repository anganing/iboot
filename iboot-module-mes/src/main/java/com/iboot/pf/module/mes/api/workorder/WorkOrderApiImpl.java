package com.iboot.pf.module.mes.api.workorder;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.workorder.dto.WorkOrderRespDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/** 作业工单 API 实现。 */
@Service
public class WorkOrderApiImpl implements WorkOrderApi {

    @Override
    @DataPermission(enable = false)
    public WorkOrderRespDTO getWorkOrder(Long id) {
        throw new UnsupportedOperationException("作业工单查询尚未接入领域 Service");
    }

    @Override
    @DataPermission(enable = false)
    public List<WorkOrderRespDTO> getWorkOrderList(Collection<Long> ids) {
        throw new UnsupportedOperationException("作业工单查询尚未接入领域 Service");
    }

    @Override
    @DataPermission(enable = false)
    public List<WorkOrderRespDTO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds) {
        throw new UnsupportedOperationException("作业工单查询尚未接入领域 Service");
    }

}
