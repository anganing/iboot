package com.iboot.pf.module.wms.api.inorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.inorder.dto.InOrderRespDTO;
import com.iboot.pf.module.wms.api.order.dto.CancelOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 入库单 API 实现类。
 *
 * <p>命令方法由 {@link WmsInOrderService} 承载事务与幂等，本类只做参数转换。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：跨模块调用天生是系统身份
 * 而不是用户身份，与调用方登录用户的数据范围无关。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class InOrderApiImpl implements InOrderApi {

    @Resource
    private WmsInOrderService inOrderService;
    @Resource
    private WmsInOrderMapper inOrderMapper;

    @Override
    public Long createInOrder(CreateInOrderReqDTO reqDTO) {
        WmsCreateInOrderBO bo = BeanUtils.toBean(reqDTO, WmsCreateInOrderBO.class);
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        return inOrderService.createInOrder(bo);
    }

    @Override
    public void cancelInOrder(CancelOrderReqDTO reqDTO) {
        WmsCancelOrderBO bo = new WmsCancelOrderBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setOrderNo(reqDTO.getOrderNo());
        bo.setReason(reqDTO.getReason());
        inOrderService.cancelInOrder(bo);
    }

    @Override
    public void confirmInOrder(ConfirmOrderReqDTO reqDTO) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setOrderNo(reqDTO.getOrderNo());
        bo.setRemark(reqDTO.getRemark());
        inOrderService.confirmInOrder(bo);
    }

    @Override
    public Map<Long, InOrderRespDTO> getInOrderMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        List<WmsInOrderDO> orders = inOrderMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(orders, InOrderRespDTO.class), InOrderRespDTO::getId);
    }

}
