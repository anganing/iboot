package com.iboot.pf.module.mes.api.workorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.workorder.dto.WorkOrderRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 作业工单 API 实现。
 *
 * <p>只读门面，三个方法都是转调 codegen {@link WorkOrderService} 加一次 DTO 转换。作业工单由下达动作展开、
 * 由工序任务完工推进，因此没有写入型方法可委托。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：调用方是别的模块（APS 投影、追溯查询），
 * 它的登录用户与作业工单的数据范围无关。
 */
@Service
@DataPermission(enable = false)
public class WorkOrderApiImpl implements WorkOrderApi {

    @Resource
    private WorkOrderService workOrderService;

    @Override
    public WorkOrderRespDTO getWorkOrder(Long id) {
        if (id == null) {
            return null;
        }
        WorkOrderDO workOrder = workOrderService.getWorkOrderList(List.of(id)).stream().findFirst().orElse(null);
        return BeanUtils.toBean(workOrder, WorkOrderRespDTO.class);
    }

    @Override
    public List<WorkOrderRespDTO> getWorkOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return BeanUtils.toBean(workOrderService.getWorkOrderList(ids), WorkOrderRespDTO.class);
    }

    @Override
    public List<WorkOrderRespDTO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds) {
        if (CollUtil.isEmpty(prodOrderIds)) {
            return List.of();
        }
        return BeanUtils.toBean(workOrderService.getWorkOrderListByProdOrderIds(prodOrderIds),
                WorkOrderRespDTO.class);
    }

}
