package com.iboot.pf.module.mes.service.qctask.hook;

import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.wms.api.arrivalorder.ArrivalOrderApi;
import com.iboot.pf.module.wms.service.arrivalorder.event.ArrivalConfirmedEvent;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** WMS 到货确认后按规则创建 IQC 任务。 */
@Component
public class MesIqcTriggerListener {

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private ArrivalOrderApi arrivalOrderApi;

    @EventListener
    public void onArrivalConfirmed(ArrivalConfirmedEvent event) {
        MaterialRespDTO material = materialApi.getMaterial(event.getMatId());
        MesCreateQcTaskBO bo = new MesCreateQcTaskBO();
        bo.setTriggerPoint(MesQcTriggerPointEnum.IQC.name());
        bo.setObjectType(MesQcObjectTypeEnum.MATERIAL.name());
        bo.setMatId(event.getMatId());
        bo.setMatCatId(material == null ? null : material.getCatId());
        bo.setSupplierId(event.getSupplierId());
        bo.setBizType(MesBizTypeEnum.ARRIVAL.name());
        bo.setBizNo(event.getArrivalNo());
        bo.setBizLineId(event.getArrivalLineId());
        bo.setBatchId(event.getBatchId());
        bo.setLotNo(event.getLotNo());
        bo.setQty(event.getQty());
        bo.setUomId(event.getUomId());
        Optional<Long> qcTaskId = qcTaskDomainService.createQcTask(bo);
        arrivalOrderApi.resolveIqcTask(event.getArrivalLineId(), qcTaskId.orElse(null));
    }
}
