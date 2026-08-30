package com.iboot.pf.module.wms.api.count;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.api.count.dto.ConfirmCountDiffReqDTO;
import com.iboot.pf.module.wms.api.count.dto.CreateCountOrderReqDTO;
import com.iboot.pf.module.wms.api.count.dto.SubmitCountResultReqDTO;
import com.iboot.pf.module.wms.service.count.WmsCountOrderService;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 盘点单 API 实现类。
 *
 * <p>命令方法由 {@link WmsCountOrderService} 承载事务与幂等，本类只做参数转换。
 */
@Service
@Validated
public class CountOrderApiImpl implements CountOrderApi {

    @Resource
    private WmsCountOrderService countOrderService;

    @Override
    public Long createCountOrder(CreateCountOrderReqDTO reqDTO) {
        WmsCreateCountOrderBO bo = BeanUtils.toBean(reqDTO, WmsCreateCountOrderBO.class);
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        return countOrderService.createCountOrder(bo);
    }

    @Override
    public void submitCountResult(SubmitCountResultReqDTO reqDTO) {
        WmsSubmitCountResultBO bo = BeanUtils.toBean(reqDTO, WmsSubmitCountResultBO.class);
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        countOrderService.submitCountResult(bo);
    }

    @Override
    public void confirmCountDiff(ConfirmCountDiffReqDTO reqDTO) {
        WmsConfirmCountDiffBO bo = new WmsConfirmCountDiffBO();
        bo.setIdempotencyKey(reqDTO.getIdempotencyKey());
        bo.setSourceModule(reqDTO.getSourceModule());
        bo.setSourceBizNo(reqDTO.getSourceBizNo());
        bo.setOrderId(reqDTO.getOrderId());
        bo.setCountNo(reqDTO.getCountNo());
        bo.setRemark(reqDTO.getRemark());
        countOrderService.confirmCountDiff(bo);
    }

}
