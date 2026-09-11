package com.iboot.pf.module.wms.api.arrivalorder;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.arrivalorder.dto.CompleteIqcReqDTO;
import com.iboot.pf.module.wms.service.arrivalorder.WmsArrivalOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 到货登记跨模块门面实现。 */
@Service
@Validated
@DataPermission(enable = false)
public class ArrivalOrderApiImpl implements ArrivalOrderApi {

    @Resource
    private WmsArrivalOrderService arrivalOrderService;

    @Override
    public void resolveIqcTask(Long arrivalLineId, Long qcTaskId) {
        arrivalOrderService.resolveIqcTask(arrivalLineId, qcTaskId);
    }

    @Override
    public void completeIqc(CompleteIqcReqDTO reqDTO) {
        arrivalOrderService.completeIqc(reqDTO);
    }
}
