package com.iboot.pf.module.wms.api.outorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.order.dto.CancelOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.CreateOutOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.outorder.WmsOutOrderService;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 出库单 API 实现类。
 *
 * <p>命令方法由 {@link WmsOutOrderService} 承载事务与幂等，本类只做参数转换。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：跨模块调用天生是系统身份
 * 而不是用户身份，与调用方登录用户的数据范围无关。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class OutOrderApiImpl implements OutOrderApi {

    @Resource
    private WmsOutOrderService outOrderService;
    @Resource
    private WmsOutOrderMapper outOrderMapper;

    @Override
    public Long createOutOrder(CreateOutOrderReqDTO reqDTO) {
        WmsCreateOutOrderBO bo = BeanUtils.toBean(reqDTO, WmsCreateOutOrderBO.class);
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        return outOrderService.createOutOrder(bo);
    }

    @Override
    public void cancelOutOrder(CancelOrderReqDTO reqDTO) {
        WmsCancelOrderBO bo = new WmsCancelOrderBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setOrderNo(reqDTO.getOrderNo());
        bo.setReason(reqDTO.getReason());
        outOrderService.cancelOutOrder(bo);
    }

    @Override
    public void confirmOutOrder(ConfirmOrderReqDTO reqDTO) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setOrderNo(reqDTO.getOrderNo());
        bo.setRemark(reqDTO.getRemark());
        outOrderService.confirmOutOrder(bo);
    }

    @Override
    public Map<Long, OutOrderRespDTO> getOutOrderMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        List<WmsOutOrderDO> orders = outOrderMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(orders, OutOrderRespDTO.class), OutOrderRespDTO::getId);
    }

}
