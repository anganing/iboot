package com.iboot.pf.module.aps.service.strategy;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyConfigVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategySaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.strategy.ApsStrategyMapper;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.ApsStrategyStatusEnum;
import com.iboot.pf.module.aps.service.coderule.ApsCodeService;
import com.iboot.pf.module.aps.solver.ApsConstraintWeightResolver;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_CODE_DUPLICATE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_DISABLED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_HAS_SCENARIO;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_STRATEGY_CONFIG_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 排程策略 Service 测试：覆盖编码自动分配、编码与版本唯一、启用校验、被场景引用时的删除拦截，
 * 以及结构化配置的落库与回读。
 */
@Import(ApsStrategyServiceImpl.class)
class ApsStrategyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApsStrategyService strategyService;
    @Resource
    private ApsStrategyMapper strategyMapper;
    @Resource
    private ApsScenarioMapper scenarioMapper;

    @MockitoBean
    private ApsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "APS-ST-0001" : invocation.getArgument(1));
    }

    private StrategySaveReqVO buildReqVO(String code, String versionNo) {
        StrategySaveReqVO reqVO = new StrategySaveReqVO();
        reqVO.setCode(code);
        reqVO.setName("交期优先策略");
        reqVO.setVersionNo(versionNo);
        reqVO.setObjective("最小化拖期");
        reqVO.setConfig(ApsStrategyConfigConverter.defaultConfigVO());
        reqVO.setStatus(ApsStrategyStatusEnum.ENABLED.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateStrategy_blankCodeFilledByRule() {
        Long id = strategyService.createStrategy(buildReqVO(null, "V1"));

        ApsStrategyDO entity = strategyMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("APS-ST-0001", entity.getCode());
    }

    @Test
    void testCreateStrategy_sameCodeDifferentVersionAllowed() {
        strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));

        // 编码 + 版本号联合唯一：同一策略保留多个版本做对比是正常用法
        assertNotNull(strategyService.createStrategy(buildReqVO("APS-ST-A", "V2")));
    }

    @Test
    void testCreateStrategy_duplicateRejected() {
        strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));

        assertServiceException(() -> strategyService.createStrategy(buildReqVO("APS-ST-A", "V1")),
                STRATEGY_CODE_DUPLICATE);
    }

    @Test
    void testUpdateStrategy_keepOwnCode() {
        Long id = strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));

        StrategySaveReqVO updateReqVO = buildReqVO("APS-ST-A", "V1");
        updateReqVO.setId(id);
        updateReqVO.setRemark("权重调整");
        strategyService.updateStrategy(updateReqVO);

        assertEquals("权重调整", strategyMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateStrategy_duplicateRejected() {
        strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));
        Long secondId = strategyService.createStrategy(buildReqVO("APS-ST-A", "V2"));

        // 编码不允许改，能撞车的只有版本号：把 V2 改成 V1 会和第一条重复
        StrategySaveReqVO updateReqVO = buildReqVO("APS-ST-A", "V1");
        updateReqVO.setId(secondId);
        assertServiceException(() -> strategyService.updateStrategy(updateReqVO), STRATEGY_CODE_DUPLICATE);
    }

    @Test
    void testUpdateStrategy_codeIsImmutable() {
        Long id = strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));

        StrategySaveReqVO updateReqVO = buildReqVO("APS-ST-CHANGED", "V1");
        updateReqVO.setId(id);
        strategyService.updateStrategy(updateReqVO);

        // 编码由编码规则分配，更新时不允许改
        assertEquals("APS-ST-A", strategyMapper.selectById(id).getCode());
    }

    @Test
    void testUpdateStrategy_notExists() {
        StrategySaveReqVO updateReqVO = buildReqVO("APS-ST-A", "V1");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> strategyService.updateStrategy(updateReqVO), STRATEGY_NOT_EXISTS);
    }

    @Test
    void testDeleteStrategy_success() {
        Long id = strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));

        strategyService.deleteStrategy(id);

        assertNull(strategyMapper.selectById(id));
    }

    @Test
    void testDeleteStrategy_referencedByScenarioRejected() {
        Long id = strategyService.createStrategy(buildReqVO("APS-ST-A", "V1"));
        scenarioMapper.insert(ApsScenarioDO.builder()
                .code("APS-SC-A").name("八月排程").plantId(1L).workshopId(0L).strategyId(id)
                .planStartTime(LocalDateTime.now()).planEndTime(LocalDateTime.now().plusDays(7))
                .status(ApsScenarioStatusEnum.READY.name()).build());

        // 被引用的策略删掉后，场景会指向不存在的策略
        assertServiceException(() -> strategyService.deleteStrategy(id), STRATEGY_HAS_SCENARIO);
    }

    @Test
    void testValidateStrategyEnabled_draftRejected() {
        StrategySaveReqVO reqVO = buildReqVO("APS-ST-A", "V1");
        reqVO.setStatus(ApsStrategyStatusEnum.DRAFT.name());
        Long id = strategyService.createStrategy(reqVO);

        assertServiceException(() -> strategyService.validateStrategyEnabled(id), STRATEGY_DISABLED);
    }

    @Test
    void testValidateStrategyEnabled_notExists() {
        assertServiceException(() -> strategyService.validateStrategyEnabled(randomLongId()), STRATEGY_NOT_EXISTS);
    }

    @Test
    void testCreateStrategy_configRoundTrip() {
        StrategySaveReqVO reqVO = buildReqVO("APS-ST-A", "V1");
        StrategyConfigVO config = reqVO.getConfig();
        config.setSpentLimitSeconds(120L);
        config.setUnimprovedSpentLimitSeconds(30L);
        config.setMaterialConstrained(Boolean.TRUE);
        config.getWeights().get(ApsConstraintNames.DUE_DATE_DELAY).setSoft(200L);

        Long id = strategyService.createStrategy(reqVO);

        StrategyRespVO respVO = strategyService.getStrategy(id);
        assertFalse(respVO.getConfigInvalid());
        assertEquals(120L, respVO.getConfig().getSpentLimitSeconds());
        assertEquals(30L, respVO.getConfig().getUnimprovedSpentLimitSeconds());
        assertTrue(respVO.getConfig().getMaterialConstrained());
        assertEquals(200L, respVO.getConfig().getWeights().get(ApsConstraintNames.DUE_DATE_DELAY).getSoft());
        // 权重全量落库：每条约束的实际权重都要在 config_json 里显式可见，而不是"没写就是走默认"
        assertEquals(ApsConstraintNames.ALL.size(), respVO.getConfig().getWeights().size());
    }

    @Test
    void testCreateStrategy_unknownConstraintNameRejected() {
        StrategySaveReqVO reqVO = buildReqVO("APS-ST-A", "V1");
        StrategyConfigVO.ConstraintWeight weight = new StrategyConfigVO.ConstraintWeight();
        weight.setHard(0L);
        weight.setSoft(1L);
        reqVO.getConfig().getWeights().put("notExistConstraint", weight);

        // 未知约束名必须在保存时就被拒，等到点"生成排程"才报错的话，计划员已经不记得改了什么
        assertServiceException(() -> strategyService.createStrategy(reqVO),
                SOLVE_STRATEGY_CONFIG_INVALID, "存在未知约束名 [notExistConstraint]");
        assertNull(strategyMapper.selectByCodeAndVersionNo("APS-ST-A", "V1"));
    }

    @Test
    void testGetStrategy_legacyConfigFallsBackToDefaults() {
        // 库里存在一批早于结构化配置的历史数据：权重键不是约束名、值也不是 {hard, soft} 结构
        ApsStrategyDO legacy = ApsStrategyDO.builder()
                .code("APS-ST-LEGACY").name("旧版策略").versionNo("V0.9").objective("DUE_DATE_FIRST")
                .configJson("{\"weights\": {\"dueDateViolation\": 100}, \"horizonDays\": 30}")
                .status(ApsStrategyStatusEnum.ENABLED.name()).build();
        strategyMapper.insert(legacy);

        StrategyRespVO respVO = strategyService.getStrategy(legacy.getId());

        // 读路径宽松：能打开、能看到原因、保存一次即修正，不需要 DML 迁移
        assertTrue(respVO.getConfigInvalid());
        assertNotNull(respVO.getConfigInvalidReason());
        assertEquals(ApsStrategyConfig.DEFAULT_SPENT_LIMIT_SECONDS, respVO.getConfig().getSpentLimitSeconds());
        assertEquals(ApsConstraintWeightResolver.defaultWeights().get(ApsConstraintNames.DUE_DATE_DELAY).softOrZero(),
                respVO.getConfig().getWeights().get(ApsConstraintNames.DUE_DATE_DELAY).getSoft());
    }

    @Test
    void testGetConstraintMetadata_coversAllConstraints() {
        var metadata = strategyService.getConstraintMetadata();

        assertEquals(ApsConstraintNames.ALL.size(), metadata.getConstraints().size());
        assertEquals(ApsConstraintNames.ALL, metadata.getConstraints().stream()
                .map(constraint -> constraint.getConstraintName()).toList());
        assertNotNull(metadata.getDefaultConfig());
        assertEquals(ApsConstraintNames.ALL.size(), metadata.getDefaultConfig().getWeights().size());
    }

}
