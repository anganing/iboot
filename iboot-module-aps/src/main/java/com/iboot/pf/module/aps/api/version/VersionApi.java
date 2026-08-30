package com.iboot.pf.module.aps.api.version;

import com.iboot.pf.module.aps.api.version.dto.*;
import jakarta.validation.Valid;
import java.util.List;

/** 排程版本 API 门面。 */
public interface VersionApi {
    Long generateVersion(@Valid GenerateVersionReqDTO reqDTO);
    VersionRespDTO getVersion(Long id);
    List<VersionRespDTO> getVersionListByScenarioId(Long scenarioId);
    void confirmVersion(@Valid ChangeVersionStatusReqDTO reqDTO);
    void publishVersion(@Valid ChangeVersionStatusReqDTO reqDTO);
    void replaceVersion(@Valid ReplaceVersionReqDTO reqDTO);
}
