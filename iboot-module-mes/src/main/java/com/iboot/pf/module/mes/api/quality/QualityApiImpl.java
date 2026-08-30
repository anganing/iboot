package com.iboot.pf.module.mes.api.quality;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.QualityResultReq;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 质量 API 实现。质量结论由 MES 维护，库存质量维度由 WMS 联动。 */
@Service @Validated
public class QualityApiImpl implements QualityApi {
    @Resource private MesApiService service;
    @Override public MesRespDTO createQualityTask(CreateReq reqDTO) { return service.get("quality-create", service.create("quality", reqDTO)); }
    @Override public void submitQualityResult(QualityResultReq reqDTO) { service.submitQuality(reqDTO); }
    @Override public void createDisposition(MesCommandReqDTO reqDTO) { service.createDisposition(reqDTO); }
    @Override @DataPermission(enable = false) public MesRespDTO getQualityTask(Long id) { return service.get("quality", id); }
}
