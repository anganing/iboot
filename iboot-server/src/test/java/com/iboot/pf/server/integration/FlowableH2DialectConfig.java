package com.iboot.pf.server.integration;

import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 集成测试专用：强制 Flowable 用 MySQL 方言建 ACT_* 表（T6-01）。
 *
 * <h3>为什么必须加这个配置</h3>
 * Flowable 8.0.0 自带的 H2 建表脚本<b>自身有 bug</b>，在 H2 2.x 上根本跑不通：
 * <ul>
 *     <li>{@code flowable.h2.create.common.sql} 第 525 行：
 *         {@code create table ACT_HI_TSK_LOG ( ID_ identity, ... )}</li>
 *     <li>{@code flowable.h2.create.engine.sql} 第 98 行：
 *         {@code create table ACT_EVT_LOG ( LOG_NR_ identity, ... )}</li>
 * </ul>
 * 这两处把 {@code identity} 当数据类型用（H2 1.x 的写法）。H2 2.x 已废弃该类型，
 * 解析器直接报 {@code Unknown data type: "IDENTITY"}（SQLSTATE 50004），
 * 建表中断 → {@code processEngine} FactoryBean 创建失败 → 整个上下文起不来。
 * 对照之下 MySQL 脚本同一张表写的是合法的 {@code ID_ bigint auto_increment}。
 *
 * <p>注意 {@code MODE=MYSQL} 只改变 H2 的 SQL 语法解析行为，
 * <b>不会</b>改变 JDBC 元数据里自报的产品名（仍是 "H2"），
 * 所以 Flowable 的自动探测（{@code DbUtil.determineDatabaseType} 读 JDBC metadata）
 * 依然会选中那份有 bug 的 H2 脚本——光靠 URL 参数解决不了。
 *
 * <h3>为什么这样修是成立的</h3>
 * {@code AbstractEngineConfiguration} 既有的 MySQL 脚本在
 * {@code MODE=MYSQL} 的 H2 上可完整执行（实测：common 100/100、engine 56/56、
 * history 23/23 全部成功，表后缀 {@code ENGINE=InnoDB ... COLLATE utf8_bin}、
 * {@code datetime}、{@code TINYINT}、{@code longtext} 等 MySQL 方言 H2 均接受）。
 *
 * <p>探测逻辑只在 {@code databaseType} 为 null 时才跑
 * （{@code AbstractEngineConfiguration}：
 * {@code if (databaseType == null) { initDatabaseType(); }}），
 * 所以这里显式设定后不会被覆盖回去。
 *
 * <h3>为什么不用别的办法</h3>
 * <ul>
 *     <li><b>改 H2 脚本 / 塞进 schema.sql</b>：AI 手搓 {@code ACT_*} 建表语句违反
 *         CLAUDE.md 红线（"ACT_* 表结构由引擎自建，任何情况下不许手工 DDL"），
 *         且 Flowable 升版后必然与引擎自带脚本漂移。</li>
 *     <li><b>换 H2 版本 / 降级</b>：波及全仓（各模块 BaseDbUnitTest 共用 H2），
 *         且 H2 1.x 早已停止维护，属于为绕一个三方 bug 引入更大的技术债。</li>
 *     <li><b>{@code flowable.database-type} 属性</b>：Flowable 的 Spring Boot starter
 *         没有暴露这个配置项（{@code FlowableProperties} 里只有
 *         {@code databaseSchemaUpdate}/{@code databaseCatalog}/{@code databaseSchema}），
 *         配了也不生效。要用只能走本类的编程式钩子。</li>
 * </ul>
 *
 * <p>作用域：仅集成测试（放在 {@code src/test}，由 {@code @Import} 显式引入），
 * 不影响生产与各模块单测。
 *
 * @author iboot
 */
@TestConfiguration
public class FlowableH2DialectConfig {

    @Bean
    public ProcessEngineConfigurationConfigurer h2UseMysqlDialectConfigurer() {
        return (SpringProcessEngineConfiguration configuration) ->
                configuration.setDatabaseType(AbstractEngineConfiguration.DATABASE_TYPE_MYSQL);
    }

}
