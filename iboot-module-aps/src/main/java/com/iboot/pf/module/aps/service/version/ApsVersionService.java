package com.iboot.pf.module.aps.service.version;

import com.iboot.pf.module.aps.api.version.dto.*;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import java.util.List;

/** 排程版本服务。 */
public interface ApsVersionService {
    Long generateVersion(GenerateVersionReqDTO reqDTO);
    ApsVersionDO getVersion(Long id);
    List<ApsVersionDO> getVersionListByScenarioId(Long scenarioId);
    void confirmVersion(ChangeVersionStatusReqDTO reqDTO);
    void publishVersion(ChangeVersionStatusReqDTO reqDTO);
    void replaceVersion(ReplaceVersionReqDTO reqDTO);
    void validateMutable(Long versionId);
}
