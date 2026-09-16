package com.iboot.pf.server.integration;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 跨模块集成测试基类（T6-01）。
 *
 * 与各模块 {@code BaseDbUnitTest} 的本质区别：
 * <ul>
 *     <li>加载 {@code IbootServerApplication} 完整 Spring 上下文（全部业务模块真实装配）；</li>
 *     <li><b>不 mock 任何业务门面</b>——跨模块调用（MES → WMS 预留/过账等）走真实实现，
 *         验证的是"闭环真的成立"，不是"本模块调用了门面"；</li>
 *     <li>数据落在 H2 内存库（MODE=MySQL），schema 由 {@code integration/schema.sql}
 *         （155 张业务表 + 81 个 @KeySequence 序列，脚本生成）初始化，
 *         基础数据由 {@code integration/base-data.sql}（字典 / 编码规则 / 测试用户角色）初始化；</li>
 *     <li>类级 {@code @Transactional}：每个用例结束后自动回滚，用例之间互不污染，
 *         用例内建的数据无需手工清理。</li>
 * </ul>
 *
 * 注意事项（写用例前必读）：
 * <ol>
 *     <li>上下文按测试类缓存，不要加 {@code @DirtiesContext}（性能约束：全套集成测试 5 分钟内）；</li>
 *     <li>被测代码里若有 {@code REQUIRES_NEW} / 独立线程写库，本基类的回滚兜不住——
 *         当前工程内 {@code REQUIRES_NEW} 零使用，新增用例时如遇到"数据没回滚"，
 *         先查这两类点并在用例注释里说明；</li>
 *     <li>system 模块的 {@code @DSTransactional}（租户、权限四处）走 dynamic-datasource
 *         自己的事务管理器，不参与测试回滚——集成测试不直接调这几个方法；</li>
 *     <li>fixture 数据工厂在 {@code integration/fixture} 包，建数据优先走各模块 Service / api 门面
 *         （与生产写入路径一致），直接 Mapper insert 仅限没有现成 Service 方法的场景。</li>
 * </ol>
 *
 * <p>为什么内嵌 Redis 要在静态块里起，而不是框架现成的 {@code RedisTestConfiguration}：
 * 后者是 {@code iboot-spring-boot-starter-test} 里的 {@code @Configuration}，
 * 只声明一个 {@code redisServer} Bean 且没有任何 Bean 依赖它；
 * {@code IbootServerApplication} 的 {@code scanBasePackages} 又只覆盖
 * {@code com.iboot.pf.server} 与 {@code com.iboot.pf.module}，扫不到
 * {@code com.iboot.pf.framework.test.config}。即便手工 {@code @Import} 进来，
 * 全量上下文里 Redisson 自动配置排在前面，会先连 127.0.0.1:16379 拿到 Connection refused，
 * 而 mock server 那个 Bean 根本没轮到创建（日志里搜 redisServer 一条都没有）。
 * 改用 {@code ApplicationContextInitializer} 也不行——{@code @SpringBootTest} 会覆盖
 * 基类 {@code @ContextConfiguration} 的 initializers。静态块在测试类加载时执行，
 * 早于上下文刷新，是这里唯一不依赖容器顺序的可靠时机。
 *
 * @author iboot
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration")
@Import(FlowableH2DialectConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public abstract class BaseIntegrationTest {

    static {
        IntegrationRedisInitializer.startEmbeddedRedis();
    }

}
