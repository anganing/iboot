package com.iboot.pf.module.mes.service.qctask.hook;

import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.CompleteOqcReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** OQC 合格或让步接收后放行销售出库确认。 */
@Component
public class MesOqcResultHandler implements MesQcResultHandler {

    @Resource
    private OutOrderApi outOrderApi;

    @Override
    public String triggerPoint() {
        return MesQcTriggerPointEnum.OQC.name();
    }

    @Override
    public void onConclusion(QcTaskDO qcTask) {
        if (!MesQcConclusionEnum.QUALIFIED.name().equals(qcTask.getConclusion())
                && !MesQcConclusionEnum.CONCESSION.name().equals(qcTask.getConclusion())) {
            return;
        }
        CompleteOqcReqDTO reqDTO = new CompleteOqcReqDTO();
        reqDTO.setOutOrderId(qcTask.getBizLineId());
        reqDTO.setQcTaskId(qcTask.getId());
        outOrderApi.completeOqc(reqDTO);
    }
}
