package com.iboot.pf.module.aps.service.scenario;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.aps.api.scenario.dto.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.enums.ScenarioStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

@Service
public class ApsScenarioServiceImpl implements ApsScenarioService {
    @Resource private ApsScenarioMapper apsScenarioMapper;
    @Resource private ApsCommandService commandService;

    @Override @Transactional(rollbackFor = Exception.class)
    public Long createScenario(CreateScenarioReqDTO reqDTO) {
        return commandService.execute("CREATE_SCENARIO", reqDTO, () -> {
            validateWindow(reqDTO.getPlanStartTime(), reqDTO.getPlanEndTime());
            ApsScenarioDO scenario = BeanUtils.toBean(reqDTO, ApsScenarioDO.class);
            scenario.setWorkshopId(reqDTO.getWorkshopId() == null ? 0L : reqDTO.getWorkshopId());
            scenario.setSnapshotTime(LocalDateTime.now());
            scenario.setStatus(ScenarioStatusEnum.READY.name());
            apsScenarioMapper.insert(scenario);
            return scenario.getId();
        });
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public Long copyScenario(CopyScenarioReqDTO reqDTO) {
        return commandService.execute("COPY_SCENARIO", reqDTO, () -> {
            ApsScenarioDO source = requireScenario(reqDTO.getSourceScenarioId());
            ApsScenarioDO target = BeanUtils.toBean(source, ApsScenarioDO.class);
            target.setId(null); target.setCode(reqDTO.getCode()); target.setName(reqDTO.getName());
            target.setSnapshotTime(LocalDateTime.now()); target.setStatus(ScenarioStatusEnum.READY.name());
            apsScenarioMapper.insert(target);
            return target.getId();
        });
    }

    @Override public ApsScenarioDO getScenario(Long id) { return apsScenarioMapper.selectById(id); }
    @Override public List<ApsScenarioDO> getScenarioList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? List.of() : apsScenarioMapper.selectByIds(ids);
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void closeScenario(CloseScenarioReqDTO reqDTO) {
        commandService.executeVoid("CLOSE_SCENARIO", reqDTO, () -> {
            ApsScenarioDO scenario = requireScenario(reqDTO.getScenarioId());
            if (!ScenarioStatusEnum.CLOSED.name().equals(scenario.getStatus())) {
                apsScenarioMapper.updateById(ApsScenarioDO.builder().id(scenario.getId()).status(ScenarioStatusEnum.CLOSED.name()).build());
            }
        });
    }
    private ApsScenarioDO requireScenario(Long id) {
        ApsScenarioDO scenario = apsScenarioMapper.selectById(id);
        if (scenario == null) throw exception(SCENARIO_NOT_EXISTS);
        return scenario;
    }
    private void validateWindow(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) throw exception(SCENARIO_TIME_INVALID);
    }
}
