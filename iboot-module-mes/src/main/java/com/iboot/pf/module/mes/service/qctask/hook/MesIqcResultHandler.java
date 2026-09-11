package com.iboot.pf.module.mes.service.qctask.hook;

import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.wms.api.arrivalorder.ArrivalOrderApi;
import com.iboot.pf.module.wms.api.arrivalorder.dto.CompleteIqcReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** IQC 结论回写 WMS 到货明细并放行入库。 */
@Component
public class MesIqcResultHandler implements MesQcResultHandler {

    @Resource
    private ArrivalOrderApi arrivalOrderApi;

    @Override
    public String triggerPoint() {
        return MesQcTriggerPointEnum.IQC.name();
    }

    @Override
    public void onConclusion(QcTaskDO task) {
        CompleteIqcReqDTO reqDTO = new CompleteIqcReqDTO();
        reqDTO.setArrivalLineId(task.getBizLineId());
        reqDTO.setQcTaskId(task.getId());
        reqDTO.setConclusion(task.getConclusion());
        reqDTO.setQtyQualified(task.getQtyQualified());
        reqDTO.setQtyRejected(task.getQtyRejected());
        arrivalOrderApi.completeIqc(reqDTO);
    }
}
