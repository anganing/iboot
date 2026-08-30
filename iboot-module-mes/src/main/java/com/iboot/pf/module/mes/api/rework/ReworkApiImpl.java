package com.iboot.pf.module.mes.api.rework;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.Collection;
import java.util.List;

/** 返工任务 API 实现。 */
@Service @Validated
public class ReworkApiImpl implements ReworkApi {
    @Resource private MesApiService service;
    @Override public Long createRework(CreateReq reqDTO) { return service.create("rework", reqDTO); }
    @Override @DataPermission(enable = false) public MesRespDTO getRework(Long id) { return service.get("rework", id); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getReworkList(Collection<Long> ids) { return service.list("rework", ids); }
}
