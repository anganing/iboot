package com.iboot.pf.module.aps.service.solve;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.iboot.pf.framework.test.core.ut.BaseDbAndRedisUnitTest;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsStrategyStatusEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.framework.web.config.ApsSolverConfiguration;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.release.ApsScheduleReleaseService;
import com.iboot.pf.module.aps.service.scenario.ApsScenarioService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.strategy.ApsStrategyService;
import com.iboot.pf.module.aps.service.version.ApsVersionServiceImpl;
import com.iboot.pf.module.aps.solver.ApsSolverProblemBuilder;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_ALREADY_RUNNING;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_NOT_RUNNING;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 求解编排的幂等与并发测试。
 *
 * <p>只覆盖编排层的三条规则：命令幂等、同场景并发拦截、取消求解的前置校验。
 * 真实求解由 {@code ApsSolverEndToEndTest} 覆盖，这里把输入构建与求解器都换成假的，
 * 否则每个用例都要等十几秒的搜索。
 *
 * <p>继承 {@link BaseDbAndRedisUnitTest} 而不是 {@code BaseDbUnitTest}：场景锁与进度缓存都在 Redis 上。
 */
@Import({ApsSolveServiceImpl.class, ApsVersionServiceImpl.class, ApsCommandServiceImpl.class,
        ApsSolveProgressService.class, ApsSolveServiceImplTest.SolverExecutorTestConfiguration.class})
class ApsSolveServiceImplTest extends BaseDbAndRedisUnitTest {

    /**
     * 用“只收不跑”的执行器替掉真实线程池。
     *
     * <p>本类验证的是提交那一刻的状态：版本处于 SOLVING、场景锁已占住、进度已初始化。
     * 真跑求解会让锁在几秒后被释放，并发拦截的断言就成了和后台线程赛跑；同步执行也不行，
     * 那会让求解在返回前就结束，同样观察不到“正在求解”这个中间态。
     */
    static class SolverExecutorTestConfiguration {

        @Bean(ApsSolverConfiguration.SOLVER_EXECUTOR)
        TaskExecutor apsSolverExecutor() {
            return task -> {
                // 故意不执行：保留“求解已提交但未结束”的状态
            };
        }

    }

    @Resource
    private ApsSolveService solveService;
    @Resource
    private ApsVersionMapper versionMapper;
    @Resource
    private ApsScenarioMapper scenarioMapper;

    @MockitoBean
    private ApsScenarioService scenarioService;
    @MockitoBean
    private ApsStrategyService strategyService;
    @MockitoBean
    private ApsSolverProblemBuilder problemBuilder;
    @MockitoBean
    private ApsSolveResultWriter resultWriter;
    @MockitoBean
    private ApsSolverSnapshotWriter snapshotWriter;
    @MockitoBean
    private ApsRelationFiller relationFiller;
    @MockitoBean
    private ApsScheduleReleaseService releaseService;
    @MockitoBean
    private SolverManager<ApsSolution, Long> solverManager;

    private Long scenarioId;

    @BeforeEach
    void prepareScenarioAndStrategy() {
        ApsScenarioDO scenario = ApsScenarioDO.builder()
                .code("APS-SC-SOLVE").name("求解场景").plantId(1L).workshopId(0L).strategyId(2L)
                .planStartTime(LocalDateTime.of(2026, 9, 1, 8, 0))
                .planEndTime(LocalDateTime.of(2026, 9, 30, 20, 0))
                .status(ApsScenarioStatusEnum.READY.name())
                .build();
        scenarioMapper.insert(scenario);
        scenarioId = scenario.getId();

        when(scenarioService.getScenarioDO(anyLong())).thenReturn(scenario);
        ApsStrategyDO strategy = ApsStrategyDO.builder()
                .id(2L).code("AS0001").name("默认策略").versionNo("V1.0")
                .objective("保交期").configJson("{}")
                .status(ApsStrategyStatusEnum.ENABLED.name())
                .build();
        when(strategyService.validateStrategyEnabled(any())).thenReturn(strategy);
        // 让求解线程池里的组装步骤立刻失败，避免真跑求解；本类只验证编排规则
        when(problemBuilder.build(any(), anyLong(), any()))
                .thenThrow(new IllegalStateException("单测不执行真实求解"));
    }

    private GenerateVersionReqDTO buildCommand(String idempotencyKey) {
        GenerateVersionReqDTO reqDTO = new GenerateVersionReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(scenarioId));
        reqDTO.setScenarioId(scenarioId);
        reqDTO.setOperatorId(1L);
        return reqDTO;
    }

    @Test
    void generateAndSolveShouldCreateSolvingVersion() {
        Long versionId = solveService.generateAndSolve(buildCommand("UI:GEN:1"));

        ApsVersionDO version = versionMapper.selectById(versionId);
        assertNotNull(version);
        assertEquals(scenarioId, version.getScenarioId());
        assertEquals(1, version.getVersionNo());
        // 接口只负责建版本并提交求解，返回时状态仍是 SOLVING
        assertEquals(ApsVersionStatusEnum.SOLVING.name(), version.getStatus());
        assertNotNull(version.getSolveStartTime());
    }

    @Test
    void concurrentSolveOnSameScenarioShouldBeRejected() {
        solveService.generateAndSolve(buildCommand("UI:GEN:1"));

        // 场景锁还在（求解未结束），第二次必须被拦：两轮求解会先后往同一批 aps_task 里写
        assertServiceException(() -> solveService.generateAndSolve(buildCommand("UI:GEN:2")),
                SOLVE_ALREADY_RUNNING);
        assertEquals(1L, versionMapper.selectCountByScenarioId(scenarioId));
    }

    @Test
    void cancelSolveShouldRejectUnknownVersion() {
        assertServiceException(() -> solveService.cancelSolve(999_999L), VERSION_NOT_EXISTS);
    }

    @Test
    void cancelSolveShouldRejectVersionNotSolving() {
        Long versionId = solveService.generateAndSolve(buildCommand("UI:GEN:1"));
        when(solverManager.getSolverStatus(versionId)).thenReturn(SolverStatus.NOT_SOLVING);

        // 求解器上没有这个 problemId 时给出明确反馈，而不是静默无操作
        assertServiceException(() -> solveService.cancelSolve(versionId), SOLVE_NOT_RUNNING);
    }

    @Test
    void progressShouldBeReadableAfterSubmit() {
        Long versionId = solveService.generateAndSolve(buildCommand("UI:GEN:1"));

        // 进度写在 Redis 上，供刷新页面或断线重连补偿
        assertNotNull(solveService.getProgress(versionId));
        assertEquals(versionId, solveService.getProgress(versionId).getVersionId());
    }

    @Test
    void solverConfigurationShouldExposeExecutorBeanName() {
        // 线程池 Bean 名是 Service 按名注入的锚点，改名会让注入失败
        assertEquals("apsSolverExecutor", ApsSolverConfiguration.SOLVER_EXECUTOR);
    }

}
