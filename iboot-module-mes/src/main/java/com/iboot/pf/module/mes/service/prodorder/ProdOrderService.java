package com.iboot.pf.module.mes.service.prodorder;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * MES-生产工单 Service 接口
 *
 * @author iboot
 */
public interface ProdOrderService {

    /**
     * 创建MES-生产工单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProdOrder(@Valid ProdOrderSaveReqVO createReqVO);

    /**
     * 导入MES-生产工单
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    ProdOrderImportRespVO importProdOrderList(List<ProdOrderImportExcelVO> importList);

    /**
     * 更新MES-生产工单
     *
     * @param updateReqVO 更新信息
     */
    void updateProdOrder(@Valid ProdOrderSaveReqVO updateReqVO);

    /**
     * 删除MES-生产工单
     *
     * @param id 编号
     */
    void deleteProdOrder(Long id);

    /**
    * 批量删除MES-生产工单
    *
    * @param ids 编号
    */
    void deleteProdOrderListByIds(List<Long> ids);

    /**
     * 获得MES-生产工单
     *
     * @param id 编号
     * @return MES-生产工单
     */
    ProdOrderRespVO getProdOrder(Long id);

    /** 按编号批量查询生产工单，供模块内 API 门面使用。 */
    List<ProdOrderDO> getProdOrderList(Collection<Long> ids);

    /** 按工厂查询生产工单，供跨模块投影一次性拉取事实数据。 */
    List<ProdOrderDO> getProdOrderListByPlantId(Long plantId);

    /**
     * 获得MES-生产工单分页
     *
     * @param pageReqVO 分页查询
     * @return MES-生产工单分页
     */
    PageResult<ProdOrderRespVO> getProdOrderPage(ProdOrderPageReqVO pageReqVO);

}
