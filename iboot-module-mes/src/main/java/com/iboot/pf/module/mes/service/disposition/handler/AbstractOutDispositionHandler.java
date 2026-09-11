package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.CreateOutOrderReqDTO;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;

/** 报废与退供共用的出库建单、过账、回写凭据流程。 */
abstract class AbstractOutDispositionHandler implements MesDispositionHandler {

    @Resource
    private DispositionInventoryResolver resolver;
    @Resource
    private OutOrderApi outOrderApi;

    protected DispositionResult executeOut(DispositionDO disposition, String bizType) {
        var resolved = resolver.resolve(disposition);
        var balance = resolved.balance();
        String baseKey = "MES:" + disposition.getDispositionNo() + ":" + bizType;
        CreateOutOrderReqDTO create = new CreateOutOrderReqDTO();
        create.setIdempotencyKey(baseKey + ":CREATE");
        create.setSourceModule("MES");
        create.setSourceBizNo(disposition.getDispositionNo());
        create.setBizType(bizType);
        create.setInvOrgId(balance.getInvOrgId());
        create.setPlantId(balance.getPlantId());
        create.setWhId(balance.getWhId());
        create.setProjectId(balance.getProjectId());
        create.setProjectTaskId(balance.getProjectTaskId());
        create.setRemark(disposition.getRemark());
        CreateOutOrderReqDTO.Line line = new CreateOutOrderReqDTO.Line();
        line.setLineNo(10);
        line.setMatId(balance.getMatId());
        line.setMatCode(balance.getMatCode());
        line.setMatName(balance.getMatName());
        line.setMatSpec(balance.getMatSpec());
        line.setBatchId(balance.getBatchId());
        line.setLotNo(balance.getLotNo());
        line.setUomId(resolved.qcTask().getUomId());
        line.setUomCode(balance.getUomCode());
        line.setFromLocId(balance.getLocId());
        line.setFromLocCode(balance.getLocCode());
        line.setQualityStatus(balance.getQualityStatus());
        line.setPlanQty(disposition.getQty());
        line.setContainerId(balance.getContainerId());
        create.setLines(List.of(line));
        Long orderId = outOrderApi.createOutOrder(create);
        ConfirmOrderReqDTO confirm = new ConfirmOrderReqDTO();
        confirm.setIdempotencyKey(baseKey + ":CONFIRM");
        confirm.setSourceModule("MES");
        confirm.setSourceBizNo(disposition.getDispositionNo());
        confirm.setOrderId(orderId);
        confirm.setRemark(disposition.getRemark());
        outOrderApi.confirmOutOrder(confirm);
        Map<Long, OutOrderRespDTO> orderMap = outOrderApi.getOutOrderMap(List.of(orderId));
        OutOrderRespDTO order = orderMap.get(orderId);
        return new DispositionResult(MesBizTypeEnum.OUT_ORDER.name(),
                order == null ? String.valueOf(orderId) : order.getOutNo());
    }
}
