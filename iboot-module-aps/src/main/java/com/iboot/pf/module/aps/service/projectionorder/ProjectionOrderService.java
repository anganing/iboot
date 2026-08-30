package com.iboot.pf.module.aps.service.projectionorder;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionorder.ProjectionOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 工单投影 Service 接口
 *
 * @author iboot
 */
public interface ProjectionOrderService {

    /**
     * 创建工单投影
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProjectionOrder(@Valid ProjectionOrderSaveReqVO createReqVO);

    /**
     * 导入工单投影
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProjectionOrderImportRespVO importProjectionOrderList(List<ProjectionOrderImportExcelVO> importList);

    /**
     * 更新工单投影
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectionOrder(@Valid ProjectionOrderSaveReqVO updateReqVO);

    /**
     * 删除工单投影
     *
     * @param id 编号
     */
    void deleteProjectionOrder(Long id);

    /**
    * 批量删除工单投影
    *
    * @param ids 编号
    */
    void deleteProjectionOrderListByIds(List<Long> ids);

    /**
     * 获得工单投影
     *
     * @param id 编号
     * @return 工单投影
     */
    ProjectionOrderRespVO getProjectionOrder(Long id);

    /**
     * 获得工单投影分页
     *
     * @param pageReqVO 分页查询
     * @return 工单投影分页
     */
    PageResult<ProjectionOrderRespVO> getProjectionOrderPage(ProjectionOrderPageReqVO pageReqVO);

}