package com.iboot.pf.module.mes.service.workorder;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * MES-作业工单 Service 接口
 *
 * @author iboot
 */
public interface WorkOrderService {

    /**
     * 创建作业工单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkOrder(@Valid WorkOrderSaveReqVO createReqVO);

    /**
     * 更新作业工单
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkOrder(@Valid WorkOrderSaveReqVO updateReqVO);

    /**
     * 删除作业工单
     *
     * @param id 编号
     */
    void deleteWorkOrder(Long id);

    /**
     * 批量删除作业工单
     *
     * @param ids 编号
     */
    void deleteWorkOrderListByIds(List<Long> ids);

    /**
     * 获得作业工单
     *
     * @param id 编号
     * @return 作业工单
     */
    WorkOrderRespVO getWorkOrder(Long id);

    /**
     * 获得作业工单分页
     *
     * @param pageReqVO 分页查询
     * @return 作业工单分页
     */
    PageResult<WorkOrderRespVO> getWorkOrderPage(WorkOrderPageReqVO pageReqVO);

    /** 按生产工单编号批量查询作业工单，供 Api 门面一次性拉取。 */
    List<WorkOrderDO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds);

    /** 按编号批量查询作业工单，供模块内使用。 */
    List<WorkOrderDO> getWorkOrderList(Collection<Long> ids);

}
