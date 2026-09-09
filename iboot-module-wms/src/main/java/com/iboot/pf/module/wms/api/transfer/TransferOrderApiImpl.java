package com.iboot.pf.module.wms.api.transfer;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.order.dto.CancelOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.transfer.dto.CreateTransferOrderReqDTO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.transfer.WmsTransferOrderService;
import com.iboot.pf.module.wms.service.transfer.bo.WmsCreateTransferOrderBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 转移调拨单 API 实现类。
 *
 * <p>命令方法由 {@link WmsTransferOrderService} 承载事务与幂等，本类只做参数转换。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：跨模块调用天生是系统身份
 * 而不是用户身份，与调用方登录用户的数据范围无关。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class TransferOrderApiImpl implements TransferOrderApi {

    @Resource
    private WmsTransferOrderService transferOrderService;

    @Override
    public Long createTransferOrder(CreateTransferOrderReqDTO reqDTO) {
        WmsCreateTransferOrderBO bo = BeanUtils.toBean(reqDTO, WmsCreateTransferOrderBO.class);
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        return transferOrderService.createTransferOrder(bo);
    }

    @Override
    public void cancelTransferOrder(CancelOrderReqDTO reqDTO) {
        WmsCancelOrderBO bo = new WmsCancelOrderBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setOrderNo(reqDTO.getOrderNo());
        bo.setReason(reqDTO.getReason());
        transferOrderService.cancelTransferOrder(bo);
    }

    @Override
    public void confirmTransferOrder(ConfirmOrderReqDTO reqDTO) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setOrderNo(reqDTO.getOrderNo());
        bo.setRemark(reqDTO.getRemark());
        transferOrderService.confirmTransferOrder(bo);
    }

}
