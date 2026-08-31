package com.iboot.pf.module.aps.service.scenario;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.aps.api.scenario.dto.CloseScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CopyScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CreateScenarioReqDTO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioPageReqVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioRespVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioSaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsCodeRuleConstants;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.service.coderule.ApsCodeService;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.snapshot.ApsSnapshotService;
import com.iboot.pf.module.aps.service.strategy.ApsStrategyService;
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
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_CODE_DUPLICATE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_HAS_VERSION;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_TIME_INVALID;

/**
 * APS-排程场景 Service 实现类。
 *
 * <p>管理后台的 CRUD 与 MES 下发的跨模块命令共用同一套校验：编码唯一、计划窗口合法、策略已启用、
 * 工厂车间存在。跨模块命令额外走 {@link ApsCommandService} 做幂等，重复下发直接返回上一次的结果。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsScenarioServiceImpl implements ApsScenarioService {

    @Resource
    private ApsScenarioMapper scenarioMapper;
    @Resource
    private ApsVersionMapper versionMapper;
    @Resource
    private ApsStrategyService strategyService;
    @Resource
    private ApsSnapshotService snapshotService;
    @Resource
    private ApsRelationFiller relationFiller;
    @Resource
    private ApsCodeService codeService;
    @Resource
    private ApsCommandService commandService;

    @Override
    public Long createScenario(ScenarioSaveReqVO createReqVO) {
        ApsScenarioDO scenario = BeanUtils.toBean(createReqVO, ApsScenarioDO.class);
        scenario.setId(null);
        scenario.setCode(codeService.generate(ApsCodeRuleConstants.SCENARIO, scenario.getCode()));
        validateScenario(scenario, null);
        scenarioMapper.insert(scenario);
        return scenario.getId();
    }

    @Override
    public void updateScenario(ScenarioSaveReqVO updateReqVO) {
        ApsScenarioDO existed = validateScenarioExists(updateReqVO.getId());
        // 已关闭的场景不允许再改，否则会影响已发布版本的追溯口径
        if (ApsScenarioStatusEnum.isClosed(existed.getStatus())) {
            throw exception(SCENARIO_CLOSED);
        }
        ApsScenarioDO updateObj = BeanUtils.toBean(updateReqVO, ApsScenarioDO.class);
        // 编码由编码规则分配，快照时间由投影刷新写入，更新时都不允许改
        updateObj.setCode(existed.getCode());
        updateObj.setSnapshotTime(existed.getSnapshotTime());
        validateScenario(updateObj, updateReqVO.getId());
        scenarioMapper.updateById(updateObj);
    }

    @Override
    public void deleteScenario(Long id) {
        validateScenarioExists(id);
        validateNoVersion(id);
        scenarioMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScenarioListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量删也要逐条校验占用，否则会留下引用不到场景的版本
        for (Long id : ids) {
            validateScenarioExists(id);
            validateNoVersion(id);
        }
        scenarioMapper.deleteByIds(ids);
    }

    @Override
    public ScenarioRespVO getScenario(Long id) {
        ScenarioRespVO respVO = scenarioMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<ScenarioRespVO> getScenarioPage(ScenarioPageReqVO pageReqVO) {
        PageResult<ScenarioRespVO> pageResult = scenarioMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createScenario(CreateScenarioReqDTO reqDTO) {
        return commandService.execute(ApsCommandTypeEnum.CREATE_SCENARIO, reqDTO, () -> {
            ApsScenarioDO scenario = BeanUtils.toBean(reqDTO, ApsScenarioDO.class);
            scenario.setId(null);
            scenario.setCode(codeService.generate(ApsCodeRuleConstants.SCENARIO, reqDTO.getCode()));
            // 车间可选，落 0 表示全工厂，不能留 NULL
            scenario.setWorkshopId(reqDTO.getWorkshopId() == null ? ApsConstants.NONE_ID : reqDTO.getWorkshopId());
            scenario.setStatus(ApsScenarioStatusEnum.READY.name());
            validateScenario(scenario, null);
            scenarioMapper.insert(scenario);
            return scenario.getId();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyScenario(CopyScenarioReqDTO reqDTO) {
        return commandService.execute(ApsCommandTypeEnum.COPY_SCENARIO, reqDTO, () -> {
            ApsScenarioDO source = validateScenarioExists(reqDTO.getSourceScenarioId());
            ApsScenarioDO target = BeanUtils.toBean(source, ApsScenarioDO.class);
            target.setId(null);
            target.setCode(codeService.generate(ApsCodeRuleConstants.SCENARIO, reqDTO.getCode()));
            target.setName(reqDTO.getName());
            // 复制出来的场景是新场景：快照要重新刷，状态回到待求解
            target.setSnapshotTime(null);
            target.setStatus(ApsScenarioStatusEnum.READY.name());
            validateScenario(target, null);
            scenarioMapper.insert(target);
            return target.getId();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeScenario(CloseScenarioReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.CLOSE_SCENARIO, reqDTO, () -> {
            ApsScenarioDO scenario = validateScenarioExists(reqDTO.getScenarioId());
            // 幂等：已关闭直接返回，不报错
            if (ApsScenarioStatusEnum.isClosed(scenario.getStatus())) {
                return;
            }
            scenarioMapper.updateById(ApsScenarioDO.builder()
                    .id(scenario.getId())
                    .status(ApsScenarioStatusEnum.CLOSED.name())
                    .build());
        });
    }

    @Override
    public ApsScenarioDO getScenarioDO(Long id) {
        return id == null ? null : scenarioMapper.selectById(id);
    }

    @Override
    public List<ApsScenarioDO> getScenarioDOList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? List.of() : scenarioMapper.selectByIds(ids);
    }

    /** 存在性校验，供本类与投影、版本等相邻服务复用。 */
    private ApsScenarioDO validateScenarioExists(Long id) {
        ApsScenarioDO scenario = id == null ? null : scenarioMapper.selectById(id);
        if (scenario == null) {
            throw exception(SCENARIO_NOT_EXISTS);
        }
        return scenario;
    }

    /** 已生成版本的场景不允许删除，否则版本会指向不存在的场景。 */
    private void validateNoVersion(Long scenarioId) {
        if (versionMapper.selectCountByScenarioId(scenarioId) > 0) {
            throw exception(SCENARIO_HAS_VERSION);
        }
    }

    /** 写入前的完整校验：编码唯一、计划窗口合法、外键真实存在、策略已启用。 */
    private void validateScenario(ApsScenarioDO scenario, Long excludeId) {
        validateCodeUnique(scenario.getCode(), excludeId);
        validatePlanWindow(scenario);
        snapshotService.validatePlant(scenario.getPlantId());
        snapshotService.validateWorkshop(scenario.getWorkshopId());
        strategyService.validateStrategyEnabled(scenario.getStrategyId());
    }

    /**
     * 编码唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateCodeUnique(String code, Long excludeId) {
        ApsScenarioDO existed = scenarioMapper.selectByCode(code);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(SCENARIO_CODE_DUPLICATE);
    }

    /** 计划窗口必须是正区间，否则求解器拿到的是空时间轴。 */
    private void validatePlanWindow(ApsScenarioDO scenario) {
        LocalDateTime start = scenario.getPlanStartTime();
        LocalDateTime end = scenario.getPlanEndTime();
        if (start == null || end == null || !start.isBefore(end)) {
            throw exception(SCENARIO_TIME_INVALID);
        }
    }

    /**
     * 跨模块可读字段补全。
     *
     * <p>放在 Service 而非 Controller：Excel 导出复用这里的分页结果，写在 Controller 会丢字段。
     */
    private void fillRelationNames(List<ScenarioRespVO> list) {
        relationFiller.fillPlantName(list, ScenarioRespVO::getPlantId, ScenarioRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, ScenarioRespVO::getWorkshopId, ScenarioRespVO::setWorkshopName);
    }

}
