package com.iboot.pf.module.aps.api.scenario;

import com.iboot.pf.module.aps.api.scenario.dto.*;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/** 排程场景 API 门面。 */
public interface ScenarioApi {
    Long createScenario(@Valid CreateScenarioReqDTO reqDTO);
    Long copyScenario(@Valid CopyScenarioReqDTO reqDTO);
    ScenarioRespDTO getScenario(Long id);
    List<ScenarioRespDTO> getScenarioList(Collection<Long> ids);
    void closeScenario(@Valid CloseScenarioReqDTO reqDTO);
}
