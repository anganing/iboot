package com.iboot.pf.module.aps.api.scenario;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.scenario.dto.CloseScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CopyScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CreateScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.ScenarioRespDTO;
import com.iboot.pf.module.aps.service.scenario.ApsScenarioService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 排程场景 API 实现。 */
@Service
@Validated
@DataPermission(enable = false)
public class ScenarioApiImpl implements ScenarioApi {

    @Resource
    private ApsScenarioService scenarioService;

    @Override
    public Long createScenario(CreateScenarioReqDTO reqDTO) {
        return scenarioService.createScenario(reqDTO);
    }

    @Override
    public Long copyScenario(CopyScenarioReqDTO reqDTO) {
        return scenarioService.copyScenario(reqDTO);
    }

    @Override
    public ScenarioRespDTO getScenario(Long id) {
        return BeanUtils.toBean(scenarioService.getScenarioDO(id), ScenarioRespDTO.class);
    }

    @Override
    public List<ScenarioRespDTO> getScenarioList(Collection<Long> ids) {
        return BeanUtils.toBean(scenarioService.getScenarioDOList(ids), ScenarioRespDTO.class);
    }

    @Override
    public void closeScenario(CloseScenarioReqDTO reqDTO) {
        scenarioService.closeScenario(reqDTO);
    }

}
