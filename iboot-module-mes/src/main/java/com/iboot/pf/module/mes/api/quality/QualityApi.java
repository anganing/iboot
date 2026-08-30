package com.iboot.pf.module.mes.api.quality;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.QualityResultReq;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;

/** 质量任务、结果和处置 API 门面。 */
public interface QualityApi {
    MesRespDTO createQualityTask(CreateReq reqDTO);
    void submitQualityResult(QualityResultReq reqDTO);
    void createDisposition(MesCommandReqDTO reqDTO);
    MesRespDTO getQualityTask(Long id);
}
