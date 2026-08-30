package com.iboot.pf.module.mes.service.workorder;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-作业工单 Service 接口
 *
 * @author iboot
 */
public interface WorkOrderService {

    /**
     * 创建MES-作业工单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkOrder(@Valid WorkOrderSaveReqVO createReqVO);

    /**
     * 导入MES-作业工单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    WorkOrderImportRespVO importWorkOrderList(List<WorkOrderImportExcelVO> importList);

    /**
     * 更新MES-作业工单
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkOrder(@Valid WorkOrderSaveReqVO updateReqVO);

    /**
     * 删除MES-作业工单
     *
     * @param id 编号
     */
    void deleteWorkOrder(Long id);

    /**
    * 批量删除MES-作业工单
    *
    * @param ids 编号
    */
    void deleteWorkOrderListByIds(List<Long> ids);

    /**
     * 获得MES-作业工单
     *
     * @param id 编号
     * @return MES-作业工单
     */
    WorkOrderRespVO getWorkOrder(Long id);

    /** 按生产工单编号批量查询作业工单，供模块内领域服务聚合。 */
    List<WorkOrderDO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds);

    /** 按编号批量查询作业工单，供模块内领域服务聚合。 */
    List<WorkOrderDO> getWorkOrderList(Collection<Long> ids);

    /**
     * 获得MES-作业工单分页
     *
     * @param pageReqVO 分页查询
     * @return MES-作业工单分页
     */
    PageResult<WorkOrderRespVO> getWorkOrderPage(WorkOrderPageReqVO pageReqVO);

}
