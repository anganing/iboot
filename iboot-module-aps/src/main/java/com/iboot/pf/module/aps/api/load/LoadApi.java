package com.iboot.pf.module.aps.api.load;

import com.iboot.pf.module.aps.api.load.dto.LoadRespDTO;
import com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO;
import jakarta.validation.Valid;
import java.util.List;

/** 资源负荷 API 门面。 */
public interface LoadApi {
    List<LoadRespDTO> getLoadListByVersionId(Long versionId);
    void refreshLoad(@Valid RefreshLoadReqDTO reqDTO);
}
