package com.iboot.pf.module.aps.service.version;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.version.dto.ChangeVersionStatusReqDTO;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.api.version.dto.ReplaceVersionReqDTO;
import com.iboot.pf.module.aps.controller.admin.version.vo.EffectiveVersionPageReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionPageReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.release.ApsScheduleReleaseService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_CLOSED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_PUBLISHED_READ_ONLY;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_STATUS_INVALID;

/**
 * APS-排程版本 Service 实现类。
 *
 * <p>版本号在场景内连续分配：生成时用 {@code SELECT ... FOR UPDATE} 锁住场景行，
 * 保证并发生成不会撞到同一个版本号（最终仍由 uk_aps_version_scenario_no_deleted 兜底）。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsVersionServiceImpl implements ApsVersionService {

    @Resource
    private ApsVersionMapper versionMapper;
    @Resource
    private ApsScenarioMapper scenarioMapper;
    @Resource
    private ApsRelationFiller relationFiller;
    @Resource
    private ApsCommandService commandService;
    @Resource
    private ApsScheduleReleaseService releaseService;

    @Override
    public VersionRespVO getVersion(Long id) {
        VersionRespVO respVO = versionMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<VersionRespVO> getVersionPage(VersionPageReqVO pageReqVO) {
        PageResult<VersionRespVO> pageResult = versionMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /**
     * 曾经生效过的排程版本分页。
     *
     * <p>给车间与 PMC 的"排程结果"页用：他们只该看到下过车间的计划，看不到求解过程里的中间版本。
     * 状态白名单在这里收口，传进来的值不在白名单内就回落成只查已发布，而不是照原样丢给 Mapper。
     * 这样即便有人手工构造请求传 {@code SOLVING}，拿到的也只是已发布版本。
     */
    @Override
    public PageResult<VersionRespVO> getEffectiveVersionPage(EffectiveVersionPageReqVO pageReqVO) {
        Collection<String> statuses = ApsVersionStatusEnum.isEffective(pageReqVO.getStatus())
                ? List.of(pageReqVO.getStatus())
                : List.of(ApsVersionStatusEnum.PUBLISHED.name());
        PageResult<VersionRespVO> pageResult = versionMapper.selectEffectivePage(pageReqVO, statuses);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateVersion(GenerateVersionReqDTO reqDTO) {
        return commandService.execute(ApsCommandTypeEnum.GENERATE_VERSION, reqDTO, () -> {
            // 锁场景行：版本号靠“当前数量 + 1”推导，不锁会并发撞号
            ApsScenarioDO scenario = scenarioMapper.selectOneForUpdate(ApsScenarioDO::getId, reqDTO.getScenarioId());
            if (scenario == null) {
                throw exception(SCENARIO_NOT_EXISTS);
            }
            if (ApsScenarioStatusEnum.isClosed(scenario.getStatus())) {
                throw exception(SCENARIO_CLOSED);
            }
            Long count = versionMapper.selectCountByScenarioId(reqDTO.getScenarioId());
            ApsVersionDO version = ApsVersionDO.builder()
                    .scenarioId(reqDTO.getScenarioId())
                    .versionNo(Math.toIntExact(count + ApsConstants.FIRST_VERSION_NO))
                    .status(ApsVersionStatusEnum.SOLVING.name())
                    .solveStartTime(LocalDateTime.now())
                    .version(ApsConstants.INIT_VERSION)
                    .remark(reqDTO.getRemark())
                    .build();
            versionMapper.insert(version);
            return version.getId();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmVersion(ChangeVersionStatusReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.CONFIRM_VERSION, reqDTO,
                () -> transit(reqDTO.getVersionId(), ApsVersionStatusEnum.CONFIRMED, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(ChangeVersionStatusReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.PUBLISH_VERSION, reqDTO, () -> {
            ApsVersionDO target = requireForUpdate(reqDTO.getVersionId());
            // 幂等：已发布直接返回
            if (ApsVersionStatusEnum.isPublished(target.getStatus())) {
                return;
            }
            validateTransition(target, ApsVersionStatusEnum.PUBLISHED);
            // 同场景只允许一个已发布版本，旧的自动置为已被替换
            versionMapper.selectListByScenarioIdAndStatus(target.getScenarioId(),
                            ApsVersionStatusEnum.PUBLISHED.name())
                    .forEach(old -> updateStatus(old, ApsVersionStatusEnum.REPLACED, null));
            updateStatus(target, ApsVersionStatusEnum.PUBLISHED, reqDTO.getOperatorId());
            // 发布即下达：把计划回写到 MES 工序任务，同事务，失败则整个发布回滚
            releaseService.release(target.getId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceVersion(ReplaceVersionReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REPLACE_VERSION, reqDTO, () -> {
            ApsVersionDO old = requireForUpdate(reqDTO.getOldVersionId());
            ApsVersionDO next = requireForUpdate(reqDTO.getNewVersionId());
            // 跨场景替换会把两个场景的计划混在一起
            if (!old.getScenarioId().equals(next.getScenarioId())) {
                throw exception(VERSION_STATUS_INVALID);
            }
            if (!ApsVersionStatusEnum.isPublished(old.getStatus())
                    || !ApsVersionStatusEnum.isConfirmed(next.getStatus())) {
                throw exception(VERSION_STATUS_INVALID);
            }
            updateStatus(old, ApsVersionStatusEnum.REPLACED, null);
            updateStatus(next, ApsVersionStatusEnum.PUBLISHED, reqDTO.getOperatorId());
            // 替换同样是"这份计划从此生效"，必须一起下达，否则 MES 还按旧版本干活
            releaseService.release(next.getId());
        });
    }

    @Override
    public void validateMutable(Long versionId) {
        ApsVersionDO version = require(versionId);
        if (ApsVersionStatusEnum.valueOf(version.getStatus()).isImmutable()) {
            throw exception(VERSION_PUBLISHED_READ_ONLY);
        }
    }

    @Override
    public ApsVersionDO getVersionDO(Long id) {
        return id == null ? null : versionMapper.selectById(id);
    }

    @Override
    public List<ApsVersionDO> getVersionDOListByScenarioId(Long scenarioId) {
        return versionMapper.selectListByScenarioId(scenarioId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSolved(Long versionId, String score, Long hardScore, Long softScore, String solverMessage) {
        ApsVersionDO version = requireForUpdate(versionId);
        validateTransition(version, ApsVersionStatusEnum.SOLVED);
        updateSolveResult(version, ApsVersionStatusEnum.SOLVED, score, hardScore, softScore, solverMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long versionId, String solverMessage) {
        ApsVersionDO version = requireForUpdate(versionId);
        validateTransition(version, ApsVersionStatusEnum.FAILED);
        updateSolveResult(version, ApsVersionStatusEnum.FAILED, null, null, null, solverMessage);
    }

    /**
     * 回填求解结果。
     *
     * <p>和状态流转共用乐观锁：并发下只有一个请求能把版本从 SOLVING 推走，
     * 另一个更新 0 行直接报状态非法，不会出现两次求解互相覆盖得分。
     */
    private void updateSolveResult(ApsVersionDO version, ApsVersionStatusEnum status, String score,
                                   Long hardScore, Long softScore, String solverMessage) {
        int currentVersion = version.getVersion() == null ? ApsConstants.INIT_VERSION : version.getVersion();
        ApsVersionDO update = ApsVersionDO.builder()
                .status(status.name())
                .score(score)
                .hardScore(hardScore)
                .softScore(softScore)
                .solveEndTime(LocalDateTime.now())
                .solverMessage(solverMessage)
                .version(currentVersion + 1)
                .build();
        LambdaUpdateWrapper<ApsVersionDO> wrapper = new LambdaUpdateWrapper<ApsVersionDO>()
                .eq(ApsVersionDO::getId, version.getId())
                .eq(ApsVersionDO::getVersion, currentVersion);
        if (versionMapper.update(update, wrapper) == 0) {
            throw exception(VERSION_STATUS_INVALID);
        }
    }

    private void transit(Long id, ApsVersionStatusEnum target, Long operatorId) {
        ApsVersionDO version = requireForUpdate(id);
        // 幂等：已是目标状态直接返回
        if (target.name().equals(version.getStatus())) {
            return;
        }
        validateTransition(version, target);
        updateStatus(version, target, operatorId);
    }

    private void validateTransition(ApsVersionDO version, ApsVersionStatusEnum target) {
        if (!ApsVersionStatusEnum.valueOf(version.getStatus()).canTransitTo(target)) {
            throw exception(VERSION_STATUS_INVALID);
        }
    }

    /**
     * 带乐观锁条件更新状态。
     *
     * <p>版本号自增 + WHERE version = 原值：并发下只有一个请求能把同一个版本推进到下一状态，
     * 另一个请求更新 0 行，直接报状态非法而不是静默覆盖。
     */
    private void updateStatus(ApsVersionDO version, ApsVersionStatusEnum status, Long operatorId) {
        int currentVersion = version.getVersion() == null ? ApsConstants.INIT_VERSION : version.getVersion();
        ApsVersionDO update = ApsVersionDO.builder()
                .status(status.name())
                .version(currentVersion + 1)
                .build();
        if (status == ApsVersionStatusEnum.PUBLISHED) {
            update.setPublishedTime(LocalDateTime.now());
            update.setPublishedBy(operatorId);
        }
        LambdaUpdateWrapper<ApsVersionDO> wrapper = new LambdaUpdateWrapper<ApsVersionDO>()
                .eq(ApsVersionDO::getId, version.getId())
                .eq(ApsVersionDO::getVersion, currentVersion);
        if (versionMapper.update(update, wrapper) == 0) {
            throw exception(VERSION_STATUS_INVALID);
        }
    }

    private ApsVersionDO require(Long id) {
        ApsVersionDO version = id == null ? null : versionMapper.selectById(id);
        if (version == null) {
            throw exception(VERSION_NOT_EXISTS);
        }
        return version;
    }

    private ApsVersionDO requireForUpdate(Long id) {
        ApsVersionDO version = id == null ? null : versionMapper.selectOneForUpdate(ApsVersionDO::getId, id);
        if (version == null) {
            throw exception(VERSION_NOT_EXISTS);
        }
        return version;
    }

    /**
     * 跨模块可读字段补全。
     *
     * <p>放在 Service 而非 Controller：Excel 导出复用这里的分页结果，写在 Controller 会丢字段。
     */
    private void fillRelationNames(List<VersionRespVO> list) {
        relationFiller.fillPlantName(list, VersionRespVO::getPlantId, VersionRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, VersionRespVO::getWorkshopId, VersionRespVO::setWorkshopName);
        relationFiller.fillUserNickname(list, VersionRespVO::getPublishedBy, VersionRespVO::setPublishedByName);
    }

}
