package com.iboot.pf.module.aps.api.shortage;

import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.api.shortage.dto.ShortageRespDTO;
import jakarta.validation.Valid;
import java.util.List;

/** 排程缺料 API 门面。 */
public interface ShortageApi {
    List<ShortageRespDTO> getShortageListByVersionId(Long versionId);
    void refreshShortage(@Valid RefreshShortageReqDTO reqDTO);
}
