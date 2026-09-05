package com.iboot.pf.module.mes.api.order;

import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 生产工单 API 门面。
 *
 * <p>工单状态只能由 MES 的动作推进：跨模块调用方能建单、能触发下达与关闭，但不能直接写状态字段。
 */
public interface ProdOrderApi {

    /**
     * 创建生产工单，落 {@code DRAFT}。
     *
     * @param reqDTO 创建命令
     * @return 生产工单编号
     */
    Long createProdOrder(CreateProdOrderReqDTO reqDTO);

    /**
     * 下达生产工单：锁 BOM 与工艺路线版本，展开作业工单与工序任务。
     *
     * @param reqDTO 动作命令
     */
    void releaseProdOrder(ProdOrderActionReqDTO reqDTO);

    /**
     * 关闭生产工单：释放剩余预留、倒冲件反冲扣料、回写需求状态。
     *
     * @param reqDTO 动作命令
     */
    void closeProdOrder(ProdOrderActionReqDTO reqDTO);

    /**
     * 查询单张生产工单。
     *
     * @param id 生产工单编号
     * @return 工单事实，不存在时返回 null
     */
    ProdOrderRespDTO getProdOrder(Long id);

    /**
     * 批量查询生产工单。
     *
     * @param ids 生产工单编号集合
     * @return 工单事实列表
     */
    List<ProdOrderRespDTO> getProdOrderList(Collection<Long> ids);

    /**
     * 按工厂查询生产工单，APS 投影取数用。
     *
     * @param plantId 工厂编号
     * @return 工单事实列表
     */
    List<ProdOrderRespDTO> getProdOrderListByPlantId(Long plantId);

}
