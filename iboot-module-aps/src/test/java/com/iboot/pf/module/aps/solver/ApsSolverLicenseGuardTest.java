package com.iboot.pf.module.aps.solver;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import com.iboot.pf.module.aps.framework.web.config.ApsSolverConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 许可红线守卫。
 *
 * <p>Timefold Solver Community Edition（Apache-2.0）把若干功能放在商业版后面，一旦在
 * {@code SolverConfig} 里出现对应配置项，构建求解器时就抛 {@code EnterpriseLicenseException}。
 * 网上大量 OptaPlanner 示例都带 {@code withMoveThreadCount("AUTO")}，参考项目 jvs-aps 也是这么写的
 * （它用的 OptaPlanner 9.44 没有这道闸门），照抄进来会让排程功能整体起不来。
 *
 * <p>这条测试就是那道护栏：任何人把收费开关加回配置，构建阶段就会红。
 */
class ApsSolverLicenseGuardTest {

    @Test
    void solverConfigMustNotEnableEnterpriseFeatures() {
        SolverConfig config = new ApsSolverConfiguration().apsSolverConfig();
        // 多线程增量求解：Feature.MULTITHREADED_SOLVING
        assertNull(config.getMoveThreadCount(), "moveThreadCount 是商业版功能，Community 版会抛 EnterpriseLicenseException");
        assertNull(config.getMoveThreadBufferSize(), "moveThreadBufferSize 只在多线程求解下有意义");
        // 分区搜索：Feature.PARTITIONED_SEARCH。未配置阶段时 phaseConfigList 为 null，由默认阶段接管
        assertNull(config.getPhaseConfigList(), "自定义阶段容易引入分区搜索等商业版能力，默认阶段已够用");
    }

    @Test
    void solverFactoryMustBuildWithoutLicense() {
        SolverConfig config = new ApsSolverConfiguration().apsSolverConfig()
                .withTerminationConfig(new TerminationConfig()
                        .withSpentLimit(Duration.ofSeconds(1)));
        // 构建成功即证明整套域模型与约束都没触碰商业版能力
        assertDoesNotThrow(() -> {
            assertNotNull(SolverFactory.create(config).buildSolver());
        });
    }

    @Test
    void terminationConfigMustUseCommunityCapabilities() {
        TerminationConfig termination = new TerminationConfig()
                .withSpentLimit(Duration.ofSeconds(30))
                .withUnimprovedSpentLimit(Duration.ofSeconds(10));
        assertTrue(termination.getSpentLimit().getSeconds() > 0);
        assertTrue(termination.getUnimprovedSpentLimit().getSeconds() > 0);
    }

}
