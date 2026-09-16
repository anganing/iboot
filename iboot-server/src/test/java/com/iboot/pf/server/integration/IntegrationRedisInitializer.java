package com.iboot.pf.server.integration;

import com.github.fppt.jedismock.RedisServer;

/**
 * 内嵌 Redis 启动器（T6-01）。
 *
 * <p>由 {@link BaseIntegrationTest} 的静态块调用，在 <b>测试类加载时</b>
 * 就把 mock Redis 起在配置的端口上，早于 Spring 上下文刷新，
 * 这样 Redisson / Spring Data Redis 无论何时创建连接，服务都已经在监听。
 *
 * @author iboot
 */
public final class IntegrationRedisInitializer {

    /** 端口与 application-integration.yaml 的 spring.data.redis.port 保持一致。 */
    private static final int REDIS_PORT = 16379;

    private static RedisServer server;

    private IntegrationRedisInitializer() {
    }

    /**
     * 幂等启动内嵌 Redis：同一 JVM 内多个测试类复用同一个实例，不重复占用端口。
     *
     * <p>端口被占用（上一次容器没停干净）时直接抛错，不要静默降级成
     * "Redisson 连不上 Redis" 这种指向不明的故障。
     */
    public static synchronized void startEmbeddedRedis() {
        if (server != null) {
            return;
        }
        try {
            RedisServer candidate = new RedisServer(REDIS_PORT);
            candidate.start();
            server = candidate;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "内嵌 Redis 启动失败，端口 " + REDIS_PORT + " 可能被占用：" + e.getMessage(), e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(IntegrationRedisInitializer::stop));
    }

    private static synchronized void stop() {
        if (server == null) {
            return;
        }
        try {
            server.stop();
        } catch (Exception ignore) {
            // 关闭失败不影响测试结论
        }
        server = null;
    }

    /** 供用例取用内嵌实例（需要直接造 Redis 数据时）。 */
    public static synchronized RedisServer server() {
        return server;
    }

}
