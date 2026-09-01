package com.iboot.pf.module.aps.framework.web.config;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.SolverManagerConfig;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintProvider;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Timefold 求解器配置。
 *
 * <p>手写 {@link SolverConfig} 而不是引 {@code timefold-solver-spring-boot-starter}：starter 会全局扫描
 * {@code @PlanningSolution} 并只支持 yaml 配置，而这里需要按策略在每次求解时动态覆盖终止条件。
 *
 * <p><b>许可红线</b>：以下配置项在 Community Edition（Apache-2.0）里是收费功能的开关，
 * 一旦出现，构建求解器时会抛 {@code EnterpriseLicenseException}，服务直接起不来或求解直接失败：
 * <ul>
 *   <li>{@code moveThreadCount}（多线程增量求解）</li>
 *   <li>{@code PartitionedSearchPhaseConfig}（分区搜索）</li>
 *   <li>{@code NearbySelectionConfig}（近邻选择）</li>
 *   <li>{@code MultistageMoveSelectorConfig}（多阶段移动）</li>
 *   <li>{@code constraintStreamProfilingEnabled}（约束剖析）</li>
 * </ul>
 * 网上大量 OptaPlanner 示例都带 {@code withMoveThreadCount("AUTO")}，抄过来即炸。
 * {@code ApsSolverLicenseGuardTest} 会守住这条线。
 */
@Configuration
public class ApsSolverConfiguration {

    /** 求解线程池 Bean 名，Service 里按名注入，避免和框架共享的 @Async 池混用。 */
    public static final String SOLVER_EXECUTOR = "apsSolverExecutor";

    /** 同时进行的求解个数。求解是 CPU 密集型，放开会把整个应用的响应拖垮。 */
    private static final int PARALLEL_SOLVER_COUNT = 1;

    /** 等待队列容量：排队超过它直接拒绝，让调用方拿到明确错误而不是无限等待。 */
    private static final int QUEUE_CAPACITY = 8;

    @Bean
    public SolverConfig apsSolverConfig() {
        return new SolverConfig()
                .withSolutionClass(ApsSolution.class)
                // 两个都要注册：资源持有列表变量，任务持有影子变量，漏掉任务会导致影子变量不更新
                .withEntityClasses(ApsSolverResource.class, ApsSolverTask.class)
                .withConstraintProviderClass(ApsConstraintProvider.class);
        // 不设置终止条件：每次求解用 SolverConfigOverride 按策略覆盖，避免全局默认值盖住策略配置
    }

    /**
     * 求解器管理器，单例。
     *
     * <p>problemId 用排程版本编号：全局唯一，天然支持按版本查询求解状态与提前终止。
     * 参考项目 jvs-aps 每次求解都新建一个 SolverManager 再 close，反复构建约束会话开销白扛。
     */
    @Bean(destroyMethod = "close")
    public SolverManager<ApsSolution, Long> apsSolverManager(SolverConfig apsSolverConfig) {
        SolverManagerConfig managerConfig = new SolverManagerConfig()
                .withParallelSolverCount(String.valueOf(PARALLEL_SOLVER_COUNT));
        return SolverManager.create(SolverFactory.create(apsSolverConfig), managerConfig);
    }

    /**
     * 求解编排线程池。
     *
     * <p>只负责“组装输入、提交求解、等结果、落库”这条编排链，实际搜索跑在 SolverManager 自己的线程里。
     * 独立成池是为了不占用框架共享的 @Async 池：求解一次可能跑几分钟，挤在共享池里会把日志落库、
     * 消息推送这些短任务全部堵死。
     */
    @Bean(SOLVER_EXECUTOR)
    public TaskExecutor apsSolverExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("aps-solver-");
        // 拒绝时抛异常而不是由调用线程执行：调用线程是 Web 请求线程，让它去跑几分钟的求解等于挂住接口
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }

}
