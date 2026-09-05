package com.iboot.pf.module.mes.api.workorder;

import com.iboot.pf.module.mes.api.workorder.dto.WorkOrderRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 作业工单只读 API 门面。
 *
 * <p>作业工单是生产工单按工作中心切出来的执行段，由下达动作展开、由工序任务完工推进，
 * 因此门面只给读：手工建一条会造出不挂在任何路线上的孤儿段，推进链找不到它的前后序。
 */
public interface WorkOrderApi {

    /**
     * 查询单张作业工单。
     *
     * @param id 作业工单编号
     * @return 作业工单事实，不存在时返回 null
     */
    WorkOrderRespDTO getWorkOrder(Long id);

    /**
     * 批量查询作业工单。
     *
     * @param ids 作业工单编号集合
     * @return 作业工单事实列表
     */
    List<WorkOrderRespDTO> getWorkOrderList(Collection<Long> ids);

    /**
     * 按生产工单批量查询其下的作业工单。
     *
     * @param prodOrderIds 生产工单编号集合
     * @return 作业工单事实列表
     */
    List<WorkOrderRespDTO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds);

}
