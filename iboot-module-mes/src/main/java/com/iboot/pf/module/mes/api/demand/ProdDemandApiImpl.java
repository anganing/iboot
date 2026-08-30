package com.iboot.pf.module.mes.api.demand;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.Collection;
import java.util.List;

/** 生产需求 API 实现。 */
@Service @Validated
public class ProdDemandApiImpl implements ProdDemandApi {
    @Resource private MesApiService service;
    @Override public Long createProdDemand(CreateReq reqDTO) { return service.create("demand", reqDTO); }
    @Override public void cancelProdDemand(MesCommandReqDTO reqDTO) { service.command("demand-cancel", reqDTO); }
    @Override @DataPermission(enable = false) public MesRespDTO getProdDemand(Long id) { return service.get("demand", id); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getProdDemandList(Collection<Long> ids) { return service.list("demand", ids); }
}
