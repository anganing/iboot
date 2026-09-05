package com.iboot.pf.module.mes.api.order;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 生产工单 API 实现。 */
@Service
@Validated
public class ProdOrderApiImpl implements ProdOrderApi {

    @Resource
    private ProdOrderService prodOrderService;

    @Override
    public Long createProdOrder(CreateProdOrderReqDTO reqDTO) {
        throw new UnsupportedOperationException("生产工单创建命令尚未接入领域规则");
    }

    @Override
    public void releaseProdOrder(ProdOrderActionReqDTO reqDTO) {
        throw new UnsupportedOperationException("生产工单下达命令尚未接入领域规则");
    }

    @Override
    public void closeProdOrder(ProdOrderActionReqDTO reqDTO) {
        throw new UnsupportedOperationException("生产工单关闭命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public ProdOrderRespDTO getProdOrder(Long id) {
        if (id == null) {
            return null;
        }
        ProdOrderDO prodOrder = prodOrderService.getProdOrderList(List.of(id)).stream().findFirst().orElse(null);
        return BeanUtils.toBean(prodOrder, ProdOrderRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ProdOrderRespDTO> getProdOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return BeanUtils.toBean(prodOrderService.getProdOrderList(ids), ProdOrderRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<ProdOrderRespDTO> getProdOrderListByPlantId(Long plantId) {
        return BeanUtils.toBean(prodOrderService.getProdOrderListByPlantId(plantId), ProdOrderRespDTO.class);
    }

}
