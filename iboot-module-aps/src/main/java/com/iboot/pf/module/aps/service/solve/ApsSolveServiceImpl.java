package com.iboot.pf.module.aps.service.solve;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverConfigOverride;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import com.iboot.pf.module.aps.enums.ApsSolveStatusEnum;
import com.iboot.pf.module.aps.framework.web.config.ApsSolverConfiguration;
import com.iboot.pf.module.aps.service.scenario.ApsScenarioService;
import com.iboot.pf.module.aps.service.solve.dto.ApsSolveProgress;
import com.iboot.pf.module.aps.service.strategy.ApsStrategyService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.aps.solver.ApsSolverProblemBuilder;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfigParser;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_ALREADY_RUNNING;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_NOT_RUNNING;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;

/**
 * APS 排程求解服务实现。
 *
 * <p>整条链路：建版本（同步、幂等） -> 占场景锁 -> 异步组装输入 -> 求解 -> 落库 -> 写快照 -> 推终态。
 * 求解本身几十秒到几分钟，绝不能放在 Web 请求线程里；场景锁挡住同一场景并发求解，
 * 否则两轮求解会先后往同一批 {@code aps_task} 里写，最后留下的是两份计划的混合物。
 *
 * <p>关于事务：这里刻意不给 {@code doSolve} 加 {@code @Transactional}。跨越整轮求解的事务会占着
 * 数据库连接几分钟，并且求解中途的进度也无法提交。落库的事务边界在 {@link ApsSolveResultWriter}
 * 与 {@link ApsVersionService} 各自的方法里。
 */
@Slf4j
@Service
@Validated
public class ApsSolveServiceImpl implements ApsSolveService {

    /** 场景级求解锁键前缀。 */
    private static final String SOLVE_LOCK_KEY_PREFIX = "aps:solve:lock:";

    /**
     * 锁的最长持有时间。
     *
     * <p>比默认求解上限（1 小时）留出余量，覆盖组装输入与落库；进程被强杀时靠它自动释放，
     * 不至于让场景永远排不了。
     */
    private static final Duration SOLVE_LOCK_TTL = Duration.ofHours(2);

    /** 进度总步骤：准备输入、求解、保存结果、完成。 */
    private static final int TOTAL_STEP = 4;

    @Resource
    private ApsScenarioService scenarioService;
    @Resource
    private ApsStrategyService strategyService;
    @Resource
    private ApsVersionService versionService;
    @Resource
    private ApsSolverProblemBuilder problemBuilder;
    @Resource
    private ApsSolveResultWriter resultWriter;
    @Resource
    private ApsSolverSnapshotWriter snapshotWriter;
    @Resource
    private ApsSolveProgressService progressService;
    @Resource
    private SolverManager<ApsSolution, Long> solverManager;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    @Qualifier(ApsSolverConfiguration.SOLVER_EXECUTOR)
    private TaskExecutor solverExecutor;

    @Override
    public Long generateAndSolve(GenerateVersionReqDTO reqDTO) {
        ApsScenarioDO scenario = scenarioService.getScenarioDO(reqDTO.getScenarioId());
        if (scenario == null) {
            throw exception(SCENARIO_NOT_EXISTS);
        }
        Long strategyId = reqDTO.getStrategyId() == null ? scenario.getStrategyId() : reqDTO.getStrategyId();
        ApsStrategyDO strategy = strategyService.validateStrategyEnabled(strategyId);
        // 配置校验放在提交求解之前：约束名写错要当场报出来，不能等异步线程里失败
        ApsStrategyConfig config = ApsStrategyConfigParser.parse(strategy.getConfigJson());
        // 先占锁再建版本：反过来会在并发时留下一堆 SOLVING 的空版本
        if (!tryLock(scenario.getId())) {
            throw exception(SOLVE_ALREADY_RUNNING);
        }
        Long versionId;
        try {
            versionId = versionService.generateVersion(reqDTO);
        } catch (RuntimeException ex) {
            unlock(scenario.getId());
            throw ex;
        }
        Long operatorId = reqDTO.getOperatorId();
        ApsSolveProgress progress = progressService.init(versionId, scenario.getId(), operatorId, TOTAL_STEP);
        try {
            solverExecutor.execute(() -> doSolve(scenario, versionId, config, operatorId, progress));
        } catch (RuntimeException ex) {
            // 线程池满时立刻释放锁并把版本置失败，不能让版本永远停在 SOLVING
            unlock(scenario.getId());
            versionService.markFailed(versionId, "求解任务提交失败：" + ex.getMessage());
            progressService.finish(progress, operatorId, ApsSolveStatusEnum.FAILED, "求解任务提交失败，请稍后再试");
            throw ex;
        }
        return versionId;
    }

    @Override
    public void cancelSolve(Long versionId) {
        if (versionService.getVersionDO(versionId) == null) {
            throw exception(VERSION_NOT_EXISTS);
        }
        // terminateEarly 对未在求解的 problemId 是空操作，这里用求解状态判断给出明确反馈
        if (solverManager.getSolverStatus(versionId) == ai.timefold.solver.core.api.solver.SolverStatus.NOT_SOLVING) {
            throw exception(SOLVE_NOT_RUNNING);
        }
        solverManager.terminateEarly(versionId);
    }

    @Override
    public ApsSolveProgress getProgress(Long versionId) {
        return progressService.get(versionId);
    }

    /**
     * 求解主流程，运行在求解线程池里。
     *
     * <p>整体 try-catch：异步线程里的异常没人接，漏掉会让版本永远停在 SOLVING、锁到过期才释放，
     * 场景在这期间完全排不了。
     */
    private void doSolve(ApsScenarioDO scenario, Long versionId, ApsStrategyConfig config,
                         Long operatorId, ApsSolveProgress progress) {
        try {
            progressService.report(progress, operatorId, ApsSolveStatusEnum.PREPARING, 1, "正在组装求解输入");
            ApsSolution problem = problemBuilder.build(scenario, versionId, config);
            snapshotWriter.writeInput(problem, buildProjectionSnapshotTimes(scenario));

            progressService.report(progress, operatorId, ApsSolveStatusEnum.SOLVING, 2,
                    StrUtil.format("待排任务 {} 个，可用资源 {} 台，开始求解",
                            problem.getTasks().size(), problem.getResources().size()));
            ApsSolution solved = solve(versionId, problem, config, operatorId, progress);
            if (solved == null) {
                progressService.finish(progress, operatorId, ApsSolveStatusEnum.CANCELLED, "求解已被取消");
                versionService.markFailed(versionId, "求解被人工取消");
                return;
            }
            HardSoftLongScore score = solved.getScore();
            // 硬分为负说明存在无法回避的冲突：宁可判为失败，也不发布一份违反约束的计划
            if (score == null || score.hardScore() < 0) {
                handleInfeasible(versionId, operatorId, progress, solved, score);
                return;
            }
            progressService.report(progress, operatorId, ApsSolveStatusEnum.SAVING, 3, "正在保存排程结果");
            int taskCount = resultWriter.save(solved);
            snapshotWriter.writeSolution(solved);
            writeConstraintMatch(versionId, solved);
            versionService.markSolved(versionId, score.toString(), score.hardScore(), score.softScore(),
                    StrUtil.format("求解完成，共排定 {} 道工序", taskCount));
            progressService.reportScore(progress, operatorId, score.hardScore(), score.softScore());
            progressService.finish(progress, operatorId, ApsSolveStatusEnum.SUCCESS,
                    StrUtil.format("求解完成，共排定 {} 道工序", taskCount));
        } catch (Exception ex) {
            log.error("[doSolve][排程求解失败 versionId({})]", versionId, ex);
            String message = StrUtil.blankToDefault(ex.getMessage(), ex.getClass().getSimpleName());
            safeMarkFailed(versionId, message);
            snapshotWriter.writeError(versionId, message, StrUtil.maxLength(stackSummary(ex), MAX_DETAIL_LENGTH));
            progressService.finish(progress, operatorId, ApsSolveStatusEnum.FAILED, "求解失败：" + message);
        } finally {
            unlock(scenario.getId());
        }
    }

    /** 提交求解并等待最终解；被取消时返回 {@code null}。 */
    private ApsSolution solve(Long versionId, ApsSolution problem, ApsStrategyConfig config,
                              Long operatorId, ApsSolveProgress progress) throws Exception {
        SolverJob<ApsSolution, Long> job = solverManager.solveBuilder()
                .withProblemId(versionId)
                .withProblem(problem)
                // 每拿到一个更优解就刷新进度，让计划员看到分数在往好的方向走。
                // 用 Event 版而不是已废弃的 withBestSolutionConsumer
                .withBestSolutionEventConsumer(event -> {
                    HardSoftLongScore bestScore = event.solution().getScore();
                    if (bestScore != null) {
                        progressService.reportScore(progress, operatorId,
                                bestScore.hardScore(), bestScore.softScore());
                    }
                })
                // 终止条件按策略覆盖，不写进全局 SolverConfig，避免所有策略共用一套上限
                .withConfigOverride(new SolverConfigOverride<ApsSolution>()
                        .withTerminationConfig(buildTermination(config)))
                .withExceptionHandler((problemId, throwable) ->
                        log.error("[solve][求解异常 versionId({})]", problemId, throwable))
                .run();
        ApsSolution solved = job.getFinalBestSolution();
        return job.isTerminatedEarly() ? null : solved;
    }

    /**
     * 终止条件：耗时上限与无改进上限取其一先到。
     *
     * <p>两者都是 Community Edition 能力。这里绝不设置 {@code moveThreadCount}：那是商业版功能，
     * 一旦设置，构建求解器时直接抛 {@code EnterpriseLicenseException}。
     */
    private TerminationConfig buildTermination(ApsStrategyConfig config) {
        return new TerminationConfig()
                .withSpentLimit(Duration.ofSeconds(config.spentLimitSecondsOrDefault()))
                .withUnimprovedSpentLimit(Duration.ofSeconds(config.unimprovedSpentLimitSecondsOrDefault()));
    }

    /** 无可行解：写约束命中快照后置为失败，让人能从命中详情里看出是哪条约束卡住了。 */
    private void handleInfeasible(Long versionId, Long operatorId, ApsSolveProgress progress,
                                  ApsSolution solved, HardSoftLongScore score) {
        writeConstraintMatch(versionId, solved);
        String message = score == null ? "求解未产出有效得分" : "未找到可行解，硬约束得分 " + score.hardScore();
        versionService.markFailed(versionId, message);
        snapshotWriter.writeError(versionId, message, "查看 CONSTRAINT_MATCH 快照定位违反的约束");
        progressService.finish(progress, operatorId, ApsSolveStatusEnum.FAILED, message);
    }

    /**
     * 写约束命中快照。
     *
     * <p>{@code SolutionManager.analyze} 是 Timefold 1.x Community 版的免费能力（2.x 起收费）。
     * 分析失败不影响主流程：证据链缺一条好过整轮求解白跑。
     */
    private void writeConstraintMatch(Long versionId, ApsSolution solved) {
        try {
            SolutionManager<ApsSolution, HardSoftLongScore> solutionManager =
                    SolutionManager.create(solverManager);
            ScoreAnalysis<HardSoftLongScore> analysis = solutionManager.analyze(solved);
            snapshotWriter.writeConstraintMatch(versionId, analysis);
        } catch (Exception ex) {
            log.warn("[writeConstraintMatch][约束命中分析失败 versionId({})]", versionId, ex);
        }
    }

    /** 置失败本身也可能抛异常（例如版本已被并发改状态），不能盖掉原始错误。 */
    private void safeMarkFailed(Long versionId, String message) {
        try {
            versionService.markFailed(versionId, StrUtil.maxLength(message, MAX_MESSAGE_LENGTH));
        } catch (Exception ex) {
            log.warn("[safeMarkFailed][回写求解失败状态失败 versionId({})]", versionId, ex);
        }
    }

    /** 输入指纹里的投影快照时间，用于判断两次求解是否同一份输入。 */
    private Map<String, Object> buildProjectionSnapshotTimes(ApsScenarioDO scenario) {
        Map<String, Object> times = new LinkedHashMap<>();
        times.put("scenarioSnapshotTime", scenario.getSnapshotTime());
        times.put("solveTriggerTime", LocalDateTime.now());
        return times;
    }

    private String stackSummary(Exception ex) {
        StringBuilder builder = new StringBuilder(ex.toString());
        StackTraceElement[] traces = ex.getStackTrace();
        for (int index = 0; index < Math.min(traces.length, STACK_SUMMARY_LINES); index++) {
            builder.append('\n').append(traces[index]);
        }
        return builder.toString();
    }

    private boolean tryLock(Long scenarioId) {
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(buildLockKey(scenarioId), LocalDateTime.now().toString(), SOLVE_LOCK_TTL);
        return Boolean.TRUE.equals(locked);
    }

    private void unlock(Long scenarioId) {
        redisTemplate.delete(buildLockKey(scenarioId));
    }

    private String buildLockKey(Long scenarioId) {
        return SOLVE_LOCK_KEY_PREFIX + scenarioId;
    }

    /** {@code aps_version.solver_message} 是 varchar(1000)，超长会插入失败。 */
    private static final int MAX_MESSAGE_LENGTH = 900;

    /** 异常详情长度上限，避免把整段堆栈塞进快照 JSON。 */
    private static final int MAX_DETAIL_LENGTH = 2000;

    /** 异常摘要保留的堆栈行数。 */
    private static final int STACK_SUMMARY_LINES = 5;

}
