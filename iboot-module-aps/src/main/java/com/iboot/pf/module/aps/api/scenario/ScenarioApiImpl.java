package com.iboot.pf.module.aps.api.scenario;
import com.iboot.pf.framework.common.util.object.BeanUtils; import com.iboot.pf.framework.datapermission.core.annotation.DataPermission; import com.iboot.pf.module.aps.api.scenario.dto.*; import com.iboot.pf.module.aps.service.scenario.ApsScenarioService; import jakarta.annotation.Resource; import org.springframework.stereotype.Service; import org.springframework.validation.annotation.Validated; import java.util.*;
/** 排程场景 API 实现。 */ @Service @Validated
public class ScenarioApiImpl implements ScenarioApi { @Resource private ApsScenarioService service;
 public Long createScenario(CreateScenarioReqDTO r){return service.createScenario(r);} public Long copyScenario(CopyScenarioReqDTO r){return service.copyScenario(r);}
 @DataPermission(enable=false) public ScenarioRespDTO getScenario(Long id){return BeanUtils.toBean(service.getScenario(id),ScenarioRespDTO.class);}
 @DataPermission(enable=false) public List<ScenarioRespDTO> getScenarioList(Collection<Long> ids){return BeanUtils.toBean(service.getScenarioList(ids),ScenarioRespDTO.class);}
 public void closeScenario(CloseScenarioReqDTO r){service.closeScenario(r);} }
