# 通用规则

1. 始终以简体中文输出计划和回复。
2. 每次回复都叫我老大。
3. 不要假设我清楚自己想要什么，动机或目标不清晰时，停下来讨论。
4. 我发出的指令目标清晰但方案不是最佳，直接告诉我并建议更好的办法。
5. 遇到问题追根因，不打补丁。每个决策都要能回答“为什么”。
6. 不要过度考虑边界，当前项目中不可能出现的边界情况不需要处理。

# 仓库指南

iboot 是基于 Java 17 / Spring Boot 3 的多模块后端项目，前端包含 Vue 3 应用。

## 项目说明与当前开发约定

- 当前 UI 后台二次开发项目：`iboot-ui/iboot-ui-admin-vben` 的 `iboot-ui/iboot-ui-admin-vben/apps/web-ele`。
- 数据库连接一律使用 dbx  MPC操作，连接名称为 `芋道-dev`，数据库为 `ruoyi-vue-pro-20260817`。
- 新增后台 CRUD 功能前，先阅读并遵循 [`dev-docs/通用CRUD开发流程.md`](dev-docs/通用CRUD开发流程.md)，其中定义了 DBX  MCP数据库操作、后端分层、web-ele 页面结构、菜单权限和验证流程。
- 新增后台 CRUD 模块时优先使用 infra 代码生成器生成代码；生成前在“字段管理”里人工维护外键列的“关联表名”和“显示字段”，由生成器产出 XML JOIN 查询和列表序号/关联展示列。
- 查询关联数据时优先在 Mapper 层使用 SQL `JOIN` 一次查询并返回可读字段；不允许默认先查主表再在应用层查询外键数据拼接。只有数据源不同、确实无法 JOIN，或关联数据已有专用聚合接口时才允许应用层组装，并必须在代码评审中说明原因。
- 表单中的外键字段（`xxxId`）一律用选择器让用户从主数据中选取，禁止用 `InputNumber`/`Input` 把主键编号暴露给用户手填：平铺主数据用 `RelationSelect`（关联配置统一沉淀在 `views/mdm/relation-config.ts` 复用），树形主数据用 `ApiTreeSelect`。列表展示关联数据时显示编码/名称等可读字段，不显示原始 ID；代码/枚举值列必须用字典或 formatter 映射成中文，不允许直接展示 `CRITICAL` 这类原始值。

## 项目结构与模块组织

- `iboot-dependencies` 管理所有 Maven 模块的依赖版本。
- `iboot-framework` 包含共享的 Spring Boot Starter 和框架扩展。
- `iboot-server` 是 Spring Boot 启动入口（`IbootServerApplication`），也是可运行的应用。
- `iboot-module-*` 是业务模块，每个模块都包含 `src/main/java` 和 `src/test/java`。
- `iboot-ui/iboot-ui-admin-vben` 是管理后台使用Element-UI-plus。
- `sql/` 存放数据库脚本；`script/` 包含 Docker、Jenkins 和 Shell 辅助脚本。

## 构建、测试与开发命令

- 数据库操作统一使用 dbx 操作，连接名是 芋道-dev，数据库是 ruoyi-vue-pro-20260817。不用记录DDL及DML。

- `mvn clean package -DskipTests`：构建所有后端模块。
- `mvn test`：运行全项目 JUnit 5 测试。
- `mvn -pl <module> -am test`：构建并测试指定模块及其依赖。
- 配置 MySQL 和 Redis 后，从 IDE 运行 `com.iboot.pf.server.IbootServerApplication`。
- 在 `iboot-ui/iboot-ui-admin-vben` 中，使用 `pnpm install`、`pnpm dev`、`pnpm build:prod` 和 `pnpm lint`。

## 编码风格与命名约定

- 后端遵循《阿里巴巴 Java 开发手册》；适用场景使用 Lombok 和 MapStruct。
- 业务代码包名格式为 `com.iboot.pf.module.<module>.<feature>`。
- 类名使用 `XxxController`、`XxxService`、`XxxServiceImpl`，测试类使用 `XxxTest`。
- 前端使用 Vue 3、TypeScript、ESLint、Prettier 和 Stylelint，缩进 2 个空格、使用单引号、加分号。

## 测试规范

- 后端测试使用 JUnit 5 和 Mockito；数据库或服务测试继承 `BaseDbUnitTest`。
- 测试代码放在 `src/test/java` 下，包结构与生产代码保持一致。
- 提交前运行 `mvn test` 或 `mvn -pl <module> -am test`。

## 提交规范

- 使用带模块作用域的 Conventional Commits，例如 `feat(system): add user export`、`fix(bpm): correct process import`。
