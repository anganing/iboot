package com.iboot.pf.module.mes.service.qctask.hook;

import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.ResolveOqcTaskReqDTO;
import com.iboot.pf.module.wms.service.outorder.event.OutOrderPickedEvent;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** WMS 销售出库拣选完成后按规则创建 OQC 任务。 */
@Component
public class MesOqcTriggerListener {

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;
    @Resource
    private OutOrderApi outOrderApi;

    @EventListener
    public void onOutOrderPicked(OutOrderPickedEvent event) {
        MesCreateQcTaskBO bo = new MesCreateQcTaskBO();
        bo.setTriggerPoint(MesQcTriggerPointEnum.OQC.name());
        bo.setObjectType(MesQcObjectTypeEnum.OUT_ORDER.name());
        bo.setMatId(event.getMatId());
        bo.setBizType(MesBizTypeEnum.OUT_ORDER.name());
        bo.setBizNo(event.getOutNo());
        bo.setBizLineId(event.getOutOrderId());
        bo.setQty(event.getQty());
        bo.setUomId(event.getUomId());
        ResolveOqcTaskReqDTO result = new ResolveOqcTaskReqDTO();
        result.setOutOrderId(event.getOutOrderId());
        result.setQcTaskId(qcTaskDomainService.createQcTask(bo).orElse(null));
        outOrderApi.resolveOqcTask(result);
    }
}
