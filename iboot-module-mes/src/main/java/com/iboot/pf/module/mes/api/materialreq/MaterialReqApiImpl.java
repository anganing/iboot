package com.iboot.pf.module.mes.api.materialreq;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 领料申请 API 实现。 */
@Service @Validated
public class MaterialReqApiImpl implements MaterialReqApi {
    @Resource private MesApiService service;
    @Override public Long createMaterialReq(CreateReq reqDTO) { return service.create("materialreq", reqDTO); }
    @Override public void cancelMaterialReq(MesCommandReqDTO reqDTO) { service.command("materialreq-cancel", reqDTO); }
    @Override public void confirmMaterialReq(MesCommandReqDTO reqDTO) { service.command("materialreq-confirm", reqDTO); }
    @Override @DataPermission(enable = false) public MesRespDTO getMaterialReq(Long id) { return service.get("materialreq", id); }
}
