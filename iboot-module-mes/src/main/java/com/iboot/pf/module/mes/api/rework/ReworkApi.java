package com.iboot.pf.module.mes.api.rework;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import java.util.Collection;
import java.util.List;

/** 返工任务 API 门面。 */
public interface ReworkApi {
    Long createRework(CreateReq reqDTO);
    MesRespDTO getRework(Long id);
    List<MesRespDTO> getReworkList(Collection<Long> ids);
}
