package com.iboot.pf.module.aps.service.scenario;

import com.iboot.pf.module.aps.api.scenario.dto.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import java.util.Collection;
import java.util.List;

/** 排程场景服务。 */
public interface ApsScenarioService {
    Long createScenario(CreateScenarioReqDTO reqDTO);
    Long copyScenario(CopyScenarioReqDTO reqDTO);
    ApsScenarioDO getScenario(Long id);
    List<ApsScenarioDO> getScenarioList(Collection<Long> ids);
    void closeScenario(CloseScenarioReqDTO reqDTO);
}
