package com.iboot.pf.module.aps.service.version;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.module.aps.api.version.dto.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.VersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

@Service
public class ApsVersionServiceImpl implements ApsVersionService {
    @Resource private ApsVersionMapper apsVersionMapper;
    @Resource private ApsScenarioMapper apsScenarioMapper;
    @Resource private ApsCommandService commandService;

    @Override @Transactional(rollbackFor = Exception.class)
    public Long generateVersion(GenerateVersionReqDTO reqDTO) {
        return commandService.execute("GENERATE_VERSION", reqDTO, () -> {
            ApsScenarioDO scenario = apsScenarioMapper.selectOneForUpdate(ApsScenarioDO::getId, reqDTO.getScenarioId());
            if (scenario == null) throw exception(SCENARIO_NOT_EXISTS);
            if (ScenarioStatusEnum.CLOSED.name().equals(scenario.getStatus())) throw exception(SCENARIO_CLOSED);
            Long count = apsVersionMapper.selectCount(ApsVersionDO::getScenarioId, reqDTO.getScenarioId());
            ApsVersionDO version = ApsVersionDO.builder().scenarioId(reqDTO.getScenarioId())
                    .versionNo(Math.toIntExact(count + 1)).status(VersionStatusEnum.SOLVING.name())
                    .solveStartTime(LocalDateTime.now()).version(0).remark(reqDTO.getRemark()).build();
            apsVersionMapper.insert(version);
            return version.getId();
        });
    }
    @Override public ApsVersionDO getVersion(Long id) { return apsVersionMapper.selectById(id); }
    @Override public List<ApsVersionDO> getVersionListByScenarioId(Long scenarioId) {
        return apsVersionMapper.selectList(new LambdaQueryWrapper<ApsVersionDO>().eq(ApsVersionDO::getScenarioId, scenarioId)
                .orderByDesc(ApsVersionDO::getVersionNo));
    }
    @Override public void confirmVersion(ChangeVersionStatusReqDTO reqDTO) {
        commandService.executeVoid("CONFIRM_VERSION", reqDTO,
                () -> transit(reqDTO.getVersionId(), VersionStatusEnum.CONFIRMED, null));
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void publishVersion(ChangeVersionStatusReqDTO reqDTO) {
        commandService.executeVoid("PUBLISH_VERSION", reqDTO, () -> {
            ApsVersionDO target = requireForUpdate(reqDTO.getVersionId());
            if (VersionStatusEnum.PUBLISHED.name().equals(target.getStatus())) return;
            validateTransition(target, VersionStatusEnum.PUBLISHED);
            apsVersionMapper.selectList(new LambdaQueryWrapper<ApsVersionDO>()
                    .eq(ApsVersionDO::getScenarioId, target.getScenarioId())
                    .eq(ApsVersionDO::getStatus, VersionStatusEnum.PUBLISHED.name()))
                    .forEach(old -> updateStatus(old, VersionStatusEnum.REPLACED, null));
            updateStatus(target, VersionStatusEnum.PUBLISHED, reqDTO.getOperatorId());
        });
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void replaceVersion(ReplaceVersionReqDTO reqDTO) {
        commandService.executeVoid("REPLACE_VERSION", reqDTO, () -> {
            ApsVersionDO old = requireForUpdate(reqDTO.getOldVersionId());
            ApsVersionDO next = requireForUpdate(reqDTO.getNewVersionId());
            if (!old.getScenarioId().equals(next.getScenarioId())) throw exception(VERSION_STATUS_INVALID);
            if (!VersionStatusEnum.PUBLISHED.name().equals(old.getStatus()) ||
                    !VersionStatusEnum.CONFIRMED.name().equals(next.getStatus())) throw exception(VERSION_STATUS_INVALID);
            updateStatus(old, VersionStatusEnum.REPLACED, null);
            updateStatus(next, VersionStatusEnum.PUBLISHED, reqDTO.getOperatorId());
        });
    }
    @Override public void validateMutable(Long versionId) {
        ApsVersionDO version = require(versionId);
        if (VersionStatusEnum.valueOf(version.getStatus()).isImmutable()) throw exception(VERSION_PUBLISHED_READ_ONLY);
    }
    private void transit(Long id, VersionStatusEnum target, Long operatorId) {
        ApsVersionDO version = requireForUpdate(id);
        if (target.name().equals(version.getStatus())) return;
        validateTransition(version, target);
        updateStatus(version, target, operatorId);
    }
    private void validateTransition(ApsVersionDO version, VersionStatusEnum target) {
        if (!VersionStatusEnum.valueOf(version.getStatus()).canTransitTo(target)) throw exception(VERSION_STATUS_INVALID);
    }
    private void updateStatus(ApsVersionDO version, VersionStatusEnum status, Long operatorId) {
        ApsVersionDO update = ApsVersionDO.builder().id(version.getId()).status(status.name()).build();
        if (status == VersionStatusEnum.PUBLISHED) { update.setPublishedTime(LocalDateTime.now()); update.setPublishedBy(operatorId); }
        apsVersionMapper.updateById(update);
    }
    private ApsVersionDO require(Long id) { ApsVersionDO v = apsVersionMapper.selectById(id); if (v == null) throw exception(VERSION_NOT_EXISTS); return v; }
    private ApsVersionDO requireForUpdate(Long id) { ApsVersionDO v = apsVersionMapper.selectOneForUpdate(ApsVersionDO::getId, id); if (v == null) throw exception(VERSION_NOT_EXISTS); return v; }
}
