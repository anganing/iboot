package com.iboot.pf.module.mes.api.defect;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

/** 缺陷 API 实现。 */
@Service
public class DefectApiImpl implements DefectApi {
    @Resource private MesApiService service;
    @Override @DataPermission(enable = false) public MesRespDTO getDefect(Long id) { return service.get("defect", id); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getDefectList(Collection<Long> ids) { return service.list("defect", ids); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getDefectListByQualityTaskIds(Collection<Long> ids) { return service.listBy("defect-quality", "qualityTaskId", ids); }
}
